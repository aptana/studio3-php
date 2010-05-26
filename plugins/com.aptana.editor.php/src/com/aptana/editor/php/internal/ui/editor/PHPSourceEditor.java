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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.outline.CommonOutlinePage;
import com.aptana.editor.common.parsing.FileService;
import com.aptana.editor.html.HTMLEditor;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.IPHPVersionListener;
import com.aptana.editor.php.core.PHPNature;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.FileSystemModule;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.builder.SingleFileBuildPath;
import com.aptana.editor.php.internal.contentAssist.mapping.PHPOffsetMapper;
import com.aptana.editor.php.internal.parser.PHPMimeType;
import com.aptana.editor.php.internal.parser.PHPParseState;
import com.aptana.editor.php.internal.parser.nodes.PHPExtendsNode;
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
public class PHPSourceEditor extends HTMLEditor implements ILanguageNode, IPHPVersionListener
{
	private static final char[] PAIR_MATCHING_CHARS = new char[] { '(', ')', '{', '}', '[', ']', '`', '`', '\'', '\'',
			'"', '"' };
	private Object mutex = new Object();
	private IProject project;
	private PHPDocumentProvider documentProvider;
	private IModule module;
	private boolean isOutOfWorkspace;
	private String sourceUri;
	private PHPParseState phpParseState;
	private PHPOffsetMapper offsetMapper;

	@Override
	protected void initializeEditor()
	{
		super.initializeEditor();
		module = null;
		isOutOfWorkspace = false;
		setPreferenceStore(new ChainedPreferenceStore(new IPreferenceStore[] {
				PHPEditorPlugin.getDefault().getPreferenceStore(),
				CommonEditorPlugin.getDefault().getPreferenceStore(), EditorsPlugin.getDefault().getPreferenceStore() }));

		setSourceViewerConfiguration(new PHPSourceViewerConfiguration(getPreferenceStore(), this));
		documentProvider = new PHPDocumentProvider();
		setDocumentProvider(documentProvider);
		// TODO: Shalom - Do what updateFileInfo does in the old PHPSourceEditor
	}

	@Override
	protected FileService createFileService()
	{
		phpParseState = new PHPParseState();
		return new FileService(PHPMimeType.MimeType, phpParseState);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		super.init(site, input);
		// Register as a PHP version listener and re-set the document to trigger a refresh and re-parse.
		IResource resource = (IResource) input.getAdapter(IResource.class);
		if (resource != null)
		{
			sourceUri = resource.getLocation().toString();
			project = resource.getProject();
			phpParseState.phpVersionChanged(PHPVersionProvider.getPHPVersion(project));
			documentProvider.phpVersionChanged(PHPVersionProvider.getPHPVersion(project));
			PHPVersionProvider.getInstance().addPHPVersionListener(project, phpParseState);
			PHPVersionProvider.getInstance().addPHPVersionListener(project, documentProvider);
			PHPVersionProvider.getInstance().addPHPVersionListener(project, this);
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
		PHPOpenTagCloser.install(getSourceViewer());
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
			IParseNode node = parseResult.getNodeAt(caret);
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

	@Override
	public String getLanguage()
	{
		return PHPMimeType.MimeType;
	}

	@Override
	public void phpVersionChanged(PHPVersion newVersion)
	{
		Job refreshJob = new UIJob("Refresh document") //$NON-NLS-1$
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				getSourceViewer().setDocument(getSourceViewer().getDocument());
				return Status.OK_STATUS;
			}
		};
		refreshJob.setSystem(true);
		refreshJob.schedule(500L);
	}

	public boolean isOutOfWorkspace()
	{
		return isOutOfWorkspace;
	}

	public PHPOffsetMapper getOffsetMapper() {
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
	 * Gets current module.
	 * 
	 * @return current module.
	 */
	public IModule getModule()
	{
		if (module != null)
		{
			return module;
		}
		if (sourceUri == null)
		{
			PHPEditorPlugin.log(new Status(IStatus.ERROR, PHPEditorPlugin.PLUGIN_ID,
					"Error in getModule(): sourceUri was null. Returning null")); //$NON-NLS-1$
			return null;
		}
		String struri = sourceUri;
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
			return createLocalFileModule(uri);
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
					return createLocalFileModule(uri);
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

	private IModule createLocalFileModule(URI uri)
	{
		File file = new File(uri.getPath());
		FileSystemModule fileSystemModule = new FileSystemModule(file, new SingleFileBuildPath(file));
		this.isOutOfWorkspace = true;
		module = fileSystemModule;
		return fileSystemModule;
	}
}
