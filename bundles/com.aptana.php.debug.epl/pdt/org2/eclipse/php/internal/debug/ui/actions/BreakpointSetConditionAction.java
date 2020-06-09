/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.ui.actions;

import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org2.eclipse.php.internal.debug.core.model.PHPConditionalBreakpoint;
import org2.eclipse.php.internal.debug.ui.PHPDebugUIMessages;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

public class BreakpointSetConditionAction implements IObjectActionDelegate {

    IWorkbenchPart fPart = null;

    /**
     * A dialog that sets the focus to the text area.
     */
    class SetConditionDialog extends InputDialog {

        private boolean fSetConditionEnabled;

        protected SetConditionDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, boolean enableCondition, IInputValidator validator) {
            super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
            fSetConditionEnabled = enableCondition;
        }

        /**
         * @see Dialog#createDialogArea(Composite)
         */
        protected Control createDialogArea(Composite parent) {
            Composite area = (Composite) super.createDialogArea(parent);

            final Button checkbox = new Button(area, SWT.CHECK);
            GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            checkbox.setLayoutData(data);
            checkbox.setFont(parent.getFont());
            checkbox.setText(MessageFormat.format(PHPDebugUIMessages.EnableSetCondition_1, new Object[] {}));
            checkbox.setSelection(fSetConditionEnabled);
            getText().setEnabled(fSetConditionEnabled);

            checkbox.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    fSetConditionEnabled = checkbox.getSelection();
                    getText().setEnabled(fSetConditionEnabled);
                    getOkButton().setEnabled(true);
                }

                public void widgetDefaultSelected(SelectionEvent e) {

                }

            });

            getText().addKeyListener(new org.eclipse.swt.events.KeyListener() {

                public void keyPressed(KeyEvent e) {
                    getOkButton().setEnabled(true);

                }

                public void keyReleased(KeyEvent e) {
                    getOkButton().setEnabled(true);

                }

            });

            return area;
        }

        protected boolean isSetConditionEnabled() {
            return fSetConditionEnabled;
        }

    }

    /**
     * @see IActionDelegate#run(IAction)
     */
    public void run(IAction action) {
        IStructuredSelection selection = getCurrentSelection();
        if (selection == null) {
            return;
        }
        Iterator itr = selection.iterator();
        if (!itr.hasNext()) {
            return;
        }

        while (itr.hasNext()) {
            IBreakpoint breakpoint = (IBreakpoint) itr.next();
            if (breakpoint instanceof PHPConditionalBreakpoint) {
                String condition = "";
                try {
                    ConditionDialog((PHPConditionalBreakpoint) breakpoint, condition);
                } catch (CoreException ce) {
                    // ConditionDialog doesn't throw CoreException. Just Log
                    PHPDebugEPLPlugin.logError("PHP: Exception setting condition in breakpoint" , ce );
                }
            }
        }
    }

    protected boolean ConditionDialog(PHPConditionalBreakpoint breakpoint, String condition) throws CoreException {
        IInputValidator validator = new IInputValidator() {
            public String isValid(String value) {
                return value;
            }
        };

        String currentCondition = breakpoint.getCondition();
        condition = currentCondition;
        boolean enableCondition = breakpoint.isConditionEnabled();
        if (currentCondition.equals(""))
            enableCondition = true;

        Shell activeShell = PHPDebugEPLPlugin.getActiveWorkbenchShell();
        String title = MessageFormat.format(PHPDebugUIMessages.SetCondition_1, new Object[] {});
        String message = MessageFormat.format(PHPDebugUIMessages.EnterCondition_1, new Object[] {});
        SetConditionDialog dialog = new SetConditionDialog(activeShell, title, message, currentCondition, enableCondition, validator);
        if (dialog.open() != Window.OK) {
            return false;
        }
        condition = dialog.getValue();
        enableCondition = dialog.isSetConditionEnabled();
        if (condition.equals(""))
            enableCondition = false;
        breakpoint.setConditionWithEnable(enableCondition, condition);
        return true;
    }

    protected IStructuredSelection getCurrentSelection() {
        IWorkbenchPage page = PHPDebugEPLPlugin.getActivePage();
        if (page != null) {
            ISelection selection = page.getSelection();
            if (selection instanceof IStructuredSelection) {
                return (IStructuredSelection) selection;
            }
        }
        return null;
    }

    /**
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        fPart = targetPart;
    }

    /**
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection sel) {
    }
}
