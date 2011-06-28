package com.aptana.editor.php.internal.ui.editor;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

import com.aptana.editor.common.CommonTextEditorActionContributor;
import com.aptana.editor.php.Messages;
import com.aptana.editor.php.internal.ui.actions.IPHPActionKeys;

public class PHPActionContributor extends CommonTextEditorActionContributor
{
	private RetargetTextEditorAction fOpenDeclaration;

	public PHPActionContributor()
	{
		// Note that this messages bundle is used when constructing the actions.
		// Make sure no string are removed unintentionally from the properties file...
		ResourceBundle resourceBundle = Messages.getResourceBundle();

		fOpenDeclaration = new RetargetTextEditorAction(resourceBundle, "openDeclaration."); //$NON-NLS-1$
		fOpenDeclaration.setActionDefinitionId(IPHPActionKeys.OPEN_DECLARATION);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.texteditor.BasicTextEditorActionContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager
	 * )
	 */
	@Override
	public void contributeToMenu(IMenuManager menu)
	{
		super.contributeToMenu(menu);
		IMenuManager navigateMenu = menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null)
		{
			navigateMenu.appendToGroup(IWorkbenchActionConstants.OPEN_EXT, fOpenDeclaration);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CommonTextEditorActionContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IEditorPart part)
	{
		super.setActiveEditor(part);
		ITextEditor editor = null;
		if (part instanceof ITextEditor)
		{
			editor = (ITextEditor) part;
			fOpenDeclaration.setAction(getAction(editor, IPHPActionKeys.OPEN_DECLARATION));
		}
	}
}
