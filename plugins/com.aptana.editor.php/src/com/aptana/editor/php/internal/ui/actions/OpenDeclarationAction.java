package com.aptana.editor.php.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * An Open-Declaration action for PHP elements.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class OpenDeclarationAction extends TextEditorAction
{

	public OpenDeclarationAction(ResourceBundle bundle, ITextEditor editor)
	{
		super(bundle, "openDeclaration.", editor); //$NON-NLS-1$
	}

	/**
	 * Open the declaration if possible.
	 */
	@Override
	public void run()
	{
		// TODO - Shalom - Implement the Open Declaration
		// ITextEditor textEditor = this.getTextEditor();
		// IEditorPart part = (IEditorPart) getAdapter(IEditorPart.class);
		// if (!(part instanceof PHPSourceEditor))
		// {
		// return null;
		// }
		// new PHPHyperlinkDetector().detectHyperlinks(textViewer, region, canShowMultipleHyperlinks)
	}
}
