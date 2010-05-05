package com.aptana.editor.php.internal.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

import com.aptana.editor.common.CommonDocumentProvider;
import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.php.internal.IPHPConstants;
import com.aptana.editor.php.internal.editor.scanner.PHPSourcePartitionScanner;

/**
 * PHP document provider.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPDocumentProvider extends CommonDocumentProvider
{

	@Override
	public void connect(Object element) throws CoreException
	{
		super.connect(element);

		IDocument document = getDocument(element);
		if (document != null)
		{
			// TODO: Shalom - Eventually, we'll need to have the HTML scanner in too + a switching rule.
			IDocumentPartitioner partitioner = new FastPartitioner(new PHPSourcePartitionScanner(),
					PHPSourceConfiguration.CONTENT_TYPES);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
			CommonEditorPlugin.getDefault().getDocumentScopeManager().registerConfiguration(document,
					PHPSourceConfiguration.getDefault());
		}
	}

	protected String getDefaultContentType()
	{
		return IPHPConstants.CONTENT_TYPE_PHP;
	}
}