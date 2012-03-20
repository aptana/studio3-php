/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;
import com.aptana.php.debug.ui.display.PHPInspectExpression;

/**
 * Inspect action for inspecting a selected text in the PHP editor while debugging.
 * 
 * @author Shalom
 */
@SuppressWarnings("restriction")
public class InspectAction implements IWatchExpressionListener, IInformationProvider, IWorkbenchWindowActionDelegate,
		IObjectActionDelegate, IEditorActionDelegate
{
	private static final String INSPECT_COMMAND_DEFINITION_ID = "com.aptana.php.debug.ui.command.Inspect"; //$NON-NLS-1$
	private ITextViewer viewer;

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.debug.core.model.IWatchExpressionListener#watchEvaluationFinished(org.eclipse.debug.core.model.
	 * IWatchExpressionResult)
	 */
	public void watchEvaluationFinished(final IWatchExpressionResult result)
	{
		if (result == null || DebugUIPlugin.getDefault() == null)
		{
			return;
		}
		if (result.getValue() != null || result.hasErrors())
		{
			DebugUIPlugin.getStandardDisplay().syncExec(new Runnable()
			{
				public void run()
				{
					displayResult(result);
				}
			});
		}
	}

	/**
	 * Override the superclass behavior to
	 * 
	 * @see org2.eclipse.php.internal.debug.ui.actions.PHPWatchAction#createExpression(org.eclipse.debug.core.IExpressionManager,
	 *      java.lang.String)
	 */
	protected void createExpression(IExpressionManager expressionManager, String expression)
	{
		IWatchExpression watchExpression = expressionManager.newWatchExpression(expression.trim());
		watchExpression.setExpressionContext(getContext());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.information.IInformationProvider#getInformation(org.eclipse.jface.text.ITextViewer,
	 * org.eclipse.jface.text.IRegion)
	 */
	public String getInformation(ITextViewer textViewer, IRegion subject)
	{
		return StringUtil.EMPTY;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.information.IInformationProvider#getSubject(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IRegion getSubject(ITextViewer textViewer, int offset)
	{
		Point p = viewer.getSelectedRange();
		return new Region(p.x, p.y);
	}

	/**
	 * Show the inspection pop-up.
	 * 
	 * @param result
	 *            An {@link IWatchExpressionResult}
	 */
	private void showPopup(IWatchExpressionResult result)
	{
		IExpression expression = new PHPInspectExpression(result);
		InspectPopupDialog inspectDialog = new InspectPopupDialog(getShell(), getAnchor(),
				INSPECT_COMMAND_DEFINITION_ID, expression);
		inspectDialog.open();
	}

	/**
	 * Returns the {@link IWorkbenchPart}
	 * 
	 * @return {@link IWorkbenchPart}
	 */
	private IWorkbenchPart getWorkbenchPart()
	{
		return DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
	}

	/**
	 * Returns the active {@link Shell}.
	 * 
	 * @return The active {@link Shell}.
	 */
	private Shell getShell()
	{
		IWorkbenchPart workbenchPart = getWorkbenchPart();
		if (workbenchPart != null)
		{
			return workbenchPart.getSite().getShell();
		}
		return DebugUIPlugin.getShell();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action)
	{
		ISelection selection = getCurrentSelection();
		if (selection == null)
		{
			IWorkbenchPage page = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
			if (page != null)
			{
				selection = page.getSelection();
			}
		}
		IDebugElement context = getContext();
		if (context != null && selection instanceof TextSelection)
		{
			try
			{
				final TextSelection textSelection = (TextSelection) selection;
				IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
				IWatchExpressionDelegate delegate = expressionManager.newWatchExpressionDelegate(context
						.getModelIdentifier());
				if (delegate != null)
				{
					delegate.evaluateExpression(textSelection.getText(), context, this);
				}
				else
				{
					// No delegate
					watchEvaluationFinished(null);
				}
			}
			catch (Exception e)
			{
				PHPDebugEPLPlugin.logError(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection)
	{
		if (selection instanceof ITextSelection)
		{
			ITextSelection textSelection = (ITextSelection) selection;
			action.setEnabled(textSelection.getLength() != 0);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window)
	{
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 * org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
	 * org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor)
	{
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose()
	{
		// do nothing
	}

	/**
	 * Display the evaluation result
	 * 
	 * @param result
	 */
	protected void displayResult(IWatchExpressionResult result)
	{
		if (result == null)
		{
			return;
		}
		IWorkbenchPart part = getWorkbenchPart();
		this.viewer = (ITextViewer) part.getAdapter(ITextViewer.class);
		if (this.viewer == null && part instanceof AbstractThemeableEditor)
		{
			this.viewer = ((AbstractThemeableEditor) part).getISourceViewer();
		}
		if (viewer != null)
		{
			showPopup(result);
		}
	}

	protected IStructuredSelection getCurrentSelection()
	{
		IWorkbenchPage page = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if (page != null)
		{
			ISelection selection = page.getSelection();
			if (selection instanceof IStructuredSelection)
			{
				return (IStructuredSelection) selection;
			}
		}
		return null;
	}

	/**
	 * Finds the currently selected context in the UI.
	 */
	protected IDebugElement getContext()
	{
		IAdaptable object = DebugUITools.getDebugContext();
		IDebugElement context = null;
		if (object instanceof IDebugElement)
		{
			context = (IDebugElement) object;
		}
		else if (object instanceof ILaunch)
		{
			context = ((ILaunch) object).getDebugTarget();
		}
		return context;
	}

	/**
	 * Returns a {@link Point} at which to anchor the popup in Display coordinates.
	 * 
	 * @return A {@link Point}
	 */
	private Point getAnchor()
	{
		StyledText styledText = viewer.getTextWidget();
		Point selection = styledText.getSelectionRange();
		int mid = selection.x + (selection.y / 2);
		Point locationAtOffset = styledText.getLocationAtOffset(mid);
		locationAtOffset = styledText.toDisplay(locationAtOffset);
		GC gc = new GC(styledText);
		gc.setFont(styledText.getFont());
		int height = gc.getFontMetrics().getHeight();
		locationAtOffset.y += height;
		gc.dispose();
		return locationAtOffset;
	}
}