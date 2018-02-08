/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.aptana.php.debug.ui.actions.breakpoints;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.RulerBreakpointAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;
import org2.eclipse.php.internal.debug.core.model.PHPLineBreakpoint;

/**
 * Presents the standard properties dialog to configure the attibutes of a PHP Breakpoint from the ruler popup menu of a
 * text editor.
 */
public class PHPBreakpointPropertiesRulerAction extends RulerBreakpointAction implements IUpdate
{

	private IBreakpoint fBreakpoint;

	/**
	 * @param editor
	 * @param info
	 */
	public PHPBreakpointPropertiesRulerAction(final ITextEditor editor, final IVerticalRulerInfo info)
	{
		super(editor, info);
		setText(Messages.PHPBreakpointPropertiesRulerAction_actionText);
	}

	/**
	 * @see Action#run()
	 */
	@Override
	public void run()
	{
		if (getBreakpoint() != null)
		{
			final PropertyDialogAction action = new PropertyDialogAction(getEditor().getEditorSite(),
					new ISelectionProvider()
					{
						public void addSelectionChangedListener(final ISelectionChangedListener listener)
						{
						}

						@SuppressWarnings("synthetic-access")
						public ISelection getSelection()
						{
							return new StructuredSelection(getBreakpoint());
						}

						public void removeSelectionChangedListener(final ISelectionChangedListener listener)
						{
						}

						public void setSelection(final ISelection selection)
						{
						}
					});
			action.run();
		}
	}

	/**
	 * @see IUpdate#update()
	 */
	public void update()
	{
		fBreakpoint = null;
		final IBreakpoint breakpoint = getBreakpoint();
		if (breakpoint != null && breakpoint instanceof PHPLineBreakpoint)
		{
			fBreakpoint = breakpoint;
		}
		setEnabled(fBreakpoint != null);
	}
}