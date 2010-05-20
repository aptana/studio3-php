package com.aptana.editor.php.internal.ui.editor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import com.aptana.editor.html.HTMLEditor;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.IPHPVersionListener;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.internal.parser.PHPMimeType;
import com.aptana.editor.php.internal.parser.PHPParser;
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
	private PHPParser parser;
	private IProject project;
	private PHPDocumentProvider documentProvider;

	@Override
	protected void initializeEditor()
	{
		super.initializeEditor();

		setPreferenceStore(new ChainedPreferenceStore(new IPreferenceStore[] {
				PHPEditorPlugin.getDefault().getPreferenceStore(),
				CommonEditorPlugin.getDefault().getPreferenceStore(), EditorsPlugin.getDefault().getPreferenceStore() }));

		setSourceViewerConfiguration(new PHPSourceViewerConfiguration(getPreferenceStore(), this));
		documentProvider = new PHPDocumentProvider();
		setDocumentProvider(documentProvider);

		parser = new PHPParser();
		getFileService().setParser(parser);
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
			project = resource.getProject();
			parser.phpVersionChanged(PHPVersionProvider.getPHPVersion(project));
			documentProvider.phpVersionChanged(PHPVersionProvider.getPHPVersion(project));
			PHPVersionProvider.getInstance().addPHPVersionListener(project, parser);
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
		PHPVersionProvider.getInstance().removePHPVersionListener(parser);
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
	@Override
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
}
