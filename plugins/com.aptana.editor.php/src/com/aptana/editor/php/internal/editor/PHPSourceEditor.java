package com.aptana.editor.php.internal.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.outline.CommonOutlineItem;
import com.aptana.editor.common.outline.CommonOutlinePage;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.lexer.IRange;

/**
 * The PHP editor central class.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
@SuppressWarnings("restriction")
public class PHPSourceEditor extends AbstractThemeableEditor
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

		// getFileService().setParser(new PHPParser());
	}

	@Override
	protected CommonOutlinePage createOutlinePage()
	{
		CommonOutlinePage outline = super.createOutlinePage();
		// TODO - Shalom: Provide a PHP outline
		// outline.setContentProvider(new RHTMLOutlineContentProvider());
		// outline.setLabelProvider(new RHTMLOutlineLabelProvider(getFileService().getParseState()));

		return outline;
	}

	@Override
	protected char[] getPairMatchingCharacters()
	{
		return PAIR_MATCHING_CHARS;
	}

	@Override
	protected void setSelectedElement(IRange element)
	{
		if (element instanceof CommonOutlineItem)
		{
			IParseNode node = ((CommonOutlineItem) element).getReferenceNode();
			// TODO - Shalom: Set selected element
			// if (node instanceof IImportContainer)
			// {
			// // just sets the highlight range and moves the cursor
			// setHighlightRange(element.getStartingOffset(), element.getLength(), true);
			// return;
			// }
		}
		super.setSelectedElement(element);
	}
}
