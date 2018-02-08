/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.search.ExternalReference;
import com.aptana.editor.php.internal.search.IElementNode;
import com.aptana.editor.php.internal.ui.dialog.ElementSelectionDialog;
import com.aptana.editor.php.internal.ui.dialog.TypeSelectionDialog;

/**
 * An Open Type handler for PHP.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class OpenTypeHandler extends AbstractHandler implements IHandler
{
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		TypeSelectionDialog typeSelectionDialog = new ElementSelectionDialog(Display.getCurrent().getActiveShell(),
				false);
		typeSelectionDialog.setTitle(Messages.OpenTypeHandler_openTypeTitle);

		typeSelectionDialog.setMessage(Messages.OpenTypeHandler_openTypeMessage);
		int open = typeSelectionDialog.open();
		if (open == Dialog.OK)
		{
			Object[] result = typeSelectionDialog.getResult();
			if (result.length > 0)
			{
				IElementNode node = (IElementNode) result[0];
				ExternalReference externalReference = node.toExternalReference();
				String editorId;
				if (externalReference.editorInput != null)
				{
					try
					{
						IEditorDescriptor editorDescriptor = IDE.getEditorDescriptor(externalReference.editorInput
								.getName());
						editorId = editorDescriptor.getId();
						IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage();

						IEditorPart openEditor = IDE.openEditor(activePage, externalReference.editorInput, editorId);
						if (openEditor instanceof ITextEditor)
						{
							ITextEditor ed = (ITextEditor) openEditor;
							int position = externalReference.position.getStartingOffset();
							ed.selectAndReveal(position, 0);

						}
					}
					catch (Exception e)
					{
						IdeLog.logError(PHPEditorPlugin.getDefault(), e.getMessage(), e);
					}
				}
			}
		}
		return null;
	}

}
