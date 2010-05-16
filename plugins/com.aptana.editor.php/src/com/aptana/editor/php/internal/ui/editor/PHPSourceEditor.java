package com.aptana.editor.php.internal.ui.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.outline.CommonOutlinePage;
import com.aptana.editor.html.HTMLEditor;
import com.aptana.editor.php.PHPEditorPlugin;
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
public class PHPSourceEditor extends HTMLEditor implements ILanguageNode
{
	private static final char[] PAIR_MATCHING_CHARS = new char[] { '(', ')', '{', '}', '[', ']', '`', '`', '\'', '\'',
			'"', '"', '?', '?' };

	@Override
	protected void initializeEditor()
	{
		super.initializeEditor();

		setPreferenceStore(new ChainedPreferenceStore(new IPreferenceStore[] {
				PHPEditorPlugin.getDefault().getPreferenceStore(),
				CommonEditorPlugin.getDefault().getPreferenceStore(), EditorsPlugin.getDefault().getPreferenceStore() }));

		setSourceViewerConfiguration(new PHPSourceViewerConfiguration(getPreferenceStore(), this));
		setDocumentProvider(new PHPDocumentProvider());

		getFileService().setParser(new PHPParser());
		// getFileService().setParser(new PHPParser());
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
}
