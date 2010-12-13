package com.aptana.editor.php.internal.ui.editor;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.ui.preferences.PreferenceConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.outline.CommonOutlinePage;
import com.aptana.editor.common.parsing.FileService;
import com.aptana.editor.html.HTMLEditor;
import com.aptana.editor.php.Messages;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.IPHPVersionListener;
import com.aptana.editor.php.core.PHPNature;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.FileSystemModule;
import com.aptana.editor.php.internal.builder.SingleFileBuildPath;
import com.aptana.editor.php.internal.contentAssist.mapping.PHPOffsetMapper;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.core.model.ISourceModuleProviderEditor;
import com.aptana.editor.php.internal.model.utils.ModelUtils;
import com.aptana.editor.php.internal.parser.PHPMimeType;
import com.aptana.editor.php.internal.parser.PHPParseState;
import com.aptana.editor.php.internal.parser.nodes.PHPExtendsNode;
import com.aptana.editor.php.internal.ui.actions.IPHPActionKeys;
import com.aptana.editor.php.internal.ui.actions.OpenDeclarationAction;
import com.aptana.editor.php.internal.ui.editor.outline.PHPDecoratingLabelProvider;
import com.aptana.editor.php.internal.ui.editor.outline.PHPOutlineItem;
import com.aptana.editor.php.internal.ui.editor.outline.PHTMLOutlineContentProvider;
import com.aptana.parsing.ast.ILanguageNode;
import com.aptana.parsing.ast.IParseNode;

/**
 * The PHP editor central class.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
@SuppressWarnings("restriction")
public class PHPSourceEditor extends HTMLEditor implements ILanguageNode, IPHPVersionListener,
		ISourceModuleProviderEditor
{
	/**
	 * The PHP editor context.<b> This context is also defined in the contexts extension point. <b> The returned value
	 * is <code>com.aptana.editor.php.editorContext</code>
	 */
	public static final String PHP_EDITOR_CONTEXT = "com.aptana.editor.php.editorContext"; //$NON-NLS-1$

	/**
	 * The PHP Editor ID
	 */
	public static final String PHP_EDITOR_ID = "com.aptana.editor.php"; //$NON-NLS-1$

	private static final char[] PAIR_MATCHING_CHARS = new char[] { '(', ')', '{', '}', '[', ']', '`', '`', '\'', '\'',
			'"', '"' };
	private Object mutex = new Object();
	private IProject project;
	private PHPDocumentProvider documentProvider;
	private IModule module;
	private ISourceModule sourceModule;
	private boolean isOutOfWorkspace;
	private String sourceUri;
	private PHPParseState phpParseState;
	private PHPOffsetMapper offsetMapper;

	// Mark Occurrences management
	private OccurrencesUpdater occurrencesUpdater;
	
	/**
	 * Constructs a new PHP source editor.
	 */
	public PHPSourceEditor()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.html.HTMLEditor#initializeEditor()
	 */
	@Override
	protected void initializeEditor()
	{
		super.initializeEditor();
		module = null;
		sourceModule = null;
		isOutOfWorkspace = false;
		ChainedPreferenceStore store = new ChainedPreferenceStore(new IPreferenceStore[] {
				PHPEditorPlugin.getDefault().getPreferenceStore(), PHPEplPlugin.getDefault().getPreferenceStore(),
				CommonEditorPlugin.getDefault().getPreferenceStore(), EditorsPlugin.getDefault().getPreferenceStore() });
		setPreferenceStore(store);

		setSourceViewerConfiguration(new PHPSourceViewerConfiguration(getPreferenceStore(), this));
		documentProvider = new PHPDocumentProvider();
		setDocumentProvider(documentProvider);
		// TODO: Shalom - Do what updateFileInfo does in the old PHPSourceEditor?
	}

	@Override
	protected FileService createFileService()
	{
		if (phpParseState == null)
		{
			phpParseState = new PHPParseState();
		}
		return new FileService(PHPMimeType.MIME_TYPE, phpParseState);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.html.HTMLEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent)
	{
		super.createPartControl(parent);
		IContextService contextService = (IContextService) getSite().getService(IContextService.class);
		contextService.activateContext(PHP_EDITOR_CONTEXT);
		// Initialize the occurrences annotations marker
		occurrencesUpdater = new OccurrencesUpdater(this);
		occurrencesUpdater.initialize(getPreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.AbstractThemeableEditor#createActions()
	 */
	@Override
	protected void createActions()
	{
		super.createActions();
		IAction action = new OpenDeclarationAction(Messages.getResourceBundle(), this);
		action.setActionDefinitionId(IPHPActionKeys.OPEN_DECLARATION);
		setAction(IPHPActionKeys.OPEN_DECLARATION, action);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#handleCursorPositionChanged()
	 */
	@Override
	protected void handleCursorPositionChanged()
	{
		super.handleCursorPositionChanged();
		// TODO: Shalom - Handle the position change for the contributed actions?
	}

	/*
	 * Override this to make sure we maintain valid editor instance members when the editor input changes (probably as a
	 * result of move).
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#doSetInput(org.eclipse.ui.IEditorInput)
	 */
	protected void doSetInput(IEditorInput input) throws CoreException
	{
		super.doSetInput(input);
		// Register as a PHP version listener and re-set the document to trigger a refresh and re-parse.
		IResource resource = (IResource) input.getAdapter(IResource.class);
		if (phpParseState == null)
		{
			phpParseState = new PHPParseState();
		}
		if (resource != null)
		{
			// In case this is the second time we hit that doSetInput, we need to re-register the listeners to make sure
			// the editor is still in a valid state.
			boolean shouldRefresh = (sourceUri != null);
			sourceUri = resource.getLocationURI().toString();
			project = resource.getProject();
			phpParseState.phpVersionChanged(PHPVersionProvider.getPHPVersion(project));
			documentProvider.phpVersionChanged(PHPVersionProvider.getPHPVersion(project));
			if (shouldRefresh)
			{
				module = null;
				sourceModule = null;
				PHPVersionProvider.getInstance().removePHPVersionListener(this);
				PHPVersionProvider.getInstance().removePHPVersionListener(phpParseState);
				PHPVersionProvider.getInstance().removePHPVersionListener(documentProvider);
			}
			PHPVersionProvider.getInstance().addPHPVersionListener(project, phpParseState);
			PHPVersionProvider.getInstance().addPHPVersionListener(project, documentProvider);
			PHPVersionProvider.getInstance().addPHPVersionListener(project, this);

			// Set the current module into the parse state
			phpParseState.setModule(getModule());
			phpParseState.setSourceModule(getSourceModule());
		}
		else
		{
			// It's probably a file out of the workspace
			if (input instanceof FileStoreEditorInput)
			{
				FileStoreEditorInput fsInput = (FileStoreEditorInput) input;
				sourceUri = fsInput.getURI().toString();
				phpParseState.setModule(getModule());
				phpParseState.setSourceModule(getSourceModule());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.AbstractThemeableEditor#dispose()
	 */
	@Override
	public void dispose()
	{
		PHPVersionProvider.getInstance().removePHPVersionListener(this);
		PHPVersionProvider.getInstance().removePHPVersionListener(phpParseState);
		PHPVersionProvider.getInstance().removePHPVersionListener(documentProvider);
		occurrencesUpdater.dispose();
		super.dispose();
	}

	@Override
	protected CommonOutlinePage createOutlinePage()
	{
		CommonOutlinePage outline = super.createOutlinePage();
		// Add the PHP-HTML (PHTML) outline provider
		outline.setContentProvider(new PHTMLOutlineContentProvider());
		outline.setLabelProvider(new PHPDecoratingLabelProvider(getFileService().getParseState()));

		return outline;
	}

	@Override
	protected char[] getPairMatchingCharacters()
	{
		return PAIR_MATCHING_CHARS;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.html.HTMLEditor#installOpenTagCloser()
	 */
	protected void installOpenTagCloser()
	{
		new PHPOpenTagCloser(getSourceViewer()).install();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.AbstractThemeableEditor#getOutlineElementAt(int)
	 */
	@Override
	protected Object getOutlineElementAt(int caret)
	{
		IParseNode parseResult = getFileService().getParseResult();
		if (parseResult != null)
		{
			IParseNode node = parseResult.getNodeAtOffset(caret);
			if (node instanceof PHPExtendsNode)
			{
				node = node.getParent();
			}
			if (node != null)
			{
				return new PHPOutlineItem(node.getNameNode().getNameRange(), node);
			}
		}
		return super.getOutlineElementAt(caret);
	}

	public String getLanguage()
	{
		return PHPMimeType.MIME_TYPE;
	}

	public void phpVersionChanged(PHPVersion newVersion)
	{
		Job refreshJob = new UIJob("Refresh document") //$NON-NLS-1$
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				getFileService().parse(true);
				return Status.OK_STATUS;
			}
		};
		refreshJob.setSystem(true);
		refreshJob.schedule(500L);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.ui.texteditor.AbstractDecoratedTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.
	 * IMenuManager)
	 */
	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu)
	{
		super.editorContextMenuAboutToShow(menu);
		final String openGroup = "group.open"; //$NON-NLS-1$
		IAction action = getAction(IPHPActionKeys.OPEN_DECLARATION);
		if (action != null)
			menu.appendToGroup(openGroup, action);
	}

	/**
	 * Returns true if this editor is now displaying content that is out of the workspace.
	 * 
	 * @return True, if the displayed content is not in the workspace; False, if it is.
	 */
	public boolean isOutOfWorkspace()
	{
		return isOutOfWorkspace;
	}

	/**
	 * Returns the offset mapper for this editor.
	 * 
	 * @return A {@link PHPOffsetMapper} for this editor.
	 */
	public PHPOffsetMapper getOffsetMapper()
	{
		synchronized (mutex)
		{
			if (offsetMapper == null)
			{
				offsetMapper = new PHPOffsetMapper(this);
			}
		}
		return offsetMapper;
	}

	/**
	 * Returns an {@link ISourceModule} for this editor.
	 * 
	 * @return An {@link ISourceModule}
	 */
	public ISourceModule getSourceModule()
	{
		synchronized (mutex)
		{
			if (sourceModule == null)
			{
				sourceModule = ModelUtils.convertModule(getModule());
			}
		}
		return sourceModule;
	}

	/**
	 * Returns the PHP editor's ID.
	 * 
	 * @return The editor's ID
	 * @see ISourceModuleProviderEditor#getEditorID()
	 */
	public String getEditorID()
	{
		return PHP_EDITOR_ID;
	}

	/**
	 * Returns true if this editor is active now.
	 * 
	 * @return True, iff this editor is active.
	 */
	public boolean isActiveEditor()
	{
		IWorkbenchPartSite site = getSite();
		if (site == null)
		{
			return false;
		}
		IWorkbenchWindow window = site.getWorkbenchWindow();
		IPartService service = window.getPartService();
		IWorkbenchPart part = service.getActivePart();
		return part != null && part.equals(this);
	}

	/**
	 * Gets current module.<b> This method caches the returned module for any consequent calls.
	 * 
	 * @return current module.
	 * @see #computeModule(String)
	 */
	public IModule getModule()
	{
		synchronized (mutex)
		{
			if (module != null)
			{
				return module;
			}
			return computeModule(this.sourceUri);
		}
	}

	/**
	 * Returns the IModule using a given sourceURI.<br>
	 * You are encouraged to use the {@link #getModule()} method when an {@link IModule} is needed. This method does not
	 * check if the module was computed before, and runs the computation again.
	 * 
	 * @param sourceURI
	 * @return A computed {@link IModule}
	 * @see #getModule()
	 */
	public IModule computeModule(String sourceURI)
	{
		synchronized (mutex)
		{
			if (sourceURI == null)
			{
				if (PHPEditorPlugin.DEBUG)
				{
					PHPEditorPlugin.log(new Status(IStatus.WARNING, PHPEditorPlugin.PLUGIN_ID,
							"sourceUri was null. Returning null")); //$NON-NLS-1$
				}
				return null;
			}
			String struri = sourceURI;
			URI uri = null;
			try
			{
				uri = new URI(struri);
			}
			catch (URISyntaxException e)
			{
				try
				{
					int fileNameStart = struri.lastIndexOf('/');
					if (fileNameStart > -1 && fileNameStart < struri.length() - 1)
					{
						String fileName = struri.substring(fileNameStart + 1);
						String encoded = URLEncoder.encode(fileName, "UTF-8"); //$NON-NLS-1$

						uri = new URI(struri.substring(0, fileNameStart + 1) + encoded);
					}
				}
				catch (UnsupportedEncodingException e1)
				{
				}
				catch (URISyntaxException e2)
				{
				}
				if (uri == null)
				{
					PHPEditorPlugin.logError(e);
					return null;
				}
			}
			if (!uri.isAbsolute())
			{
				return null;
			}
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
			if (files == null || files.length == 0)
			{
				return createSystemFileModule(uri, false);
			}
			this.isOutOfWorkspace = false;

			if (module == null)
			{
				try
				{
					if (files[0].getProject().getNature(PHPNature.NATURE_ID) == null)
					{
						// we are outside of PHP project (probably web project or
						// other)
						return createSystemFileModule(uri, true);
					}
				}
				catch (CoreException e)
				{
					// ignore
				}
			}

			module = BuildPathManager.getInstance().getModuleByResource(files[0]);
			return module;
		}
	}

	/*
	 * Override this one to expose the progress monitor to the current package.
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#getProgressMonitor()
	 */
	@Override
	protected IProgressMonitor getProgressMonitor()
	{
		return super.getProgressMonitor();
	}

	private IModule createSystemFileModule(URI uri, boolean isInWorkspace)
	{
		File file = new File(uri.getPath());
		FileSystemModule fileSystemModule = new FileSystemModule(file, new SingleFileBuildPath(file), isInWorkspace);
		this.isOutOfWorkspace = true;
		module = fileSystemModule;
		sourceModule = null;
		return fileSystemModule;
	}
}
