package com.aptana.editor.php.internal.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org2.eclipse.php.internal.core.PHPVersion;

import com.aptana.editor.common.CompositeDocumentProvider;
import com.aptana.editor.html.HTMLSourceConfiguration;
import com.aptana.editor.php.core.IPHPVersionListener;
import com.aptana.editor.php.internal.core.IPHPConstants;

/**
 * PHP document provider.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPDocumentProvider extends CompositeDocumentProvider implements IPHPVersionListener
{

	private PHPVersion phpVersion;
	private IDocument document;

	public PHPDocumentProvider()
	{
		super(IPHPConstants.CONTENT_TYPE_HTML_PHP, HTMLSourceConfiguration.getDefault(),
				PHPSourceConfiguration.getDefault(), PHPPartitionerSwitchStrategy.getDefault());
	}

	/**
	 * Override the {@link #connect(Object)} to map the inner {@link IDocument} to a {@link PHPVersion}.<br>
	 * This is done to provide accurate PHP tokens coloring for each PHP version.
	 */
	@Override
	public void connect(Object element) throws CoreException
	{
		super.connect(element);
		this.document = getDocument(element);
		PHPVersionDocumentManager.increaseDocumentCount(document);
		if (phpVersion != null)
		{
			// This might occur when the file is renamed, for example.
			PHPVersionDocumentManager.updateVersion(document, phpVersion);
		}
	}

	/**
	 * Override the disconnect to unregister the document from the {@link PHPVersionDocumentManager}.
	 */
	@Override
	public void disconnect(Object element)
	{
		PHPVersionDocumentManager.decreaseDocumentCount(document);
		super.disconnect(element);
	}

	public void phpVersionChanged(PHPVersion newVersion)
	{
		this.phpVersion = newVersion;
		if (document != null && newVersion != null)
		{
			PHPVersionDocumentManager.updateVersion(document, phpVersion);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createAnnotationModel(org.eclipse.core.resources.IFile)
	 */
	@Override
	protected IAnnotationModel createAnnotationModel(IFile file)
	{
		return new SourceModuleAnnotationModel(file);
	}
}