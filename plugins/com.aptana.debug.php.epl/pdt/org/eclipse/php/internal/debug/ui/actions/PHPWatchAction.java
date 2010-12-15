/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org.eclipse.php.internal.debug.ui.actions;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.expressions.WatchExpressionAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.aptana.debug.php.epl.PHPDebugEPLPlugin;

/**
 * An action for adding a new PHP watch expressions.
 * 
 * @author shalom
 */
public class PHPWatchAction extends WatchExpressionAction implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {

	public void init(IWorkbenchWindow window) {
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection) selection;
			action.setEnabled(textSelection.getLength() != 0);
		}
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
	}

	public void run(IAction action) {
		ISelection selection = getCurrentSelection();
		if (selection==null) 
		{
			IWorkbenchPage page = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
			if (page != null) 
			{
				selection = page.getSelection();										
			}
		}
		if (selection instanceof TextSelection) {
			TextSelection textSelection = (TextSelection) selection;
			IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
			String expression;
			try {
				expression = textSelection.getText();
				//	create the new watch expression
				IWatchExpression watchExpression = expressionManager.newWatchExpression(expression.trim());
				expressionManager.addExpression(watchExpression);
				// refresh and re-evaluate
				watchExpression.setExpressionContext(getContext());
			} catch (Exception e) {
				PHPDebugEPLPlugin.logError(e);
			}
		}
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow!=null) {
			IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			if (activePage!=null) {
				try {
					activePage.showView("org.eclipse.debug.ui.ExpressionView");
				} catch (PartInitException e) {
					PHPDebugEPLPlugin.logError(e.getMessage(),e);
				}				
			}
		}
	}

	/**
	 * @since 3.4
	 */
	public void dispose() {
	}
}
