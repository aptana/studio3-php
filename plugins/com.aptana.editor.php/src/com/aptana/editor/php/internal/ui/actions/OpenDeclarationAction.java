package com.aptana.editor.php.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import com.aptana.editor.php.Messages;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;
import com.aptana.editor.php.internal.ui.editor.hyperlink.PHPHyperlinkDetector;
import com.aptana.ui.util.StatusLineMessageTimerManager;

/**
 * An Open-Declaration action for PHP elements.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class OpenDeclarationAction extends TextEditorAction
{

	public OpenDeclarationAction(ResourceBundle bundle, ITextEditor editor)
	{
		super(bundle, "openDeclaration_", editor); //$NON-NLS-1$
	}

	/**
	 * Open the declaration if possible.
	 */
	@Override
	public void run()
	{
		ITextEditor textEditor = getTextEditor();
		if (!(textEditor instanceof PHPSourceEditor))
		{
			return;
		}
		ITextSelection selection = (ITextSelection) textEditor.getSelectionProvider().getSelection();
		IRegion region = new Region(selection.getOffset(), 1);
		PHPHyperlinkDetector detector = new PHPHyperlinkDetector();
		IHyperlink[] hyperlinks = detector.detectHyperlinks((PHPSourceEditor) textEditor, region, false);
		if (hyperlinks != null && hyperlinks.length > 0)
		{
			hyperlinks[0].open();
		}
		else
		{
			StatusLineMessageTimerManager.setErrorMessage(Messages.OpenDeclarationAction_cannotOpenDeclataion, 3000L,
					true);
		}
	}
}
