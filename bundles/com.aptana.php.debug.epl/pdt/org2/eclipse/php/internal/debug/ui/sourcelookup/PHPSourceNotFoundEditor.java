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
package org2.eclipse.php.internal.debug.ui.sourcelookup;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.part.EditorPart;

/**
 * Editor used when no source if found for a stack frame.
 * 
 */
public class PHPSourceNotFoundEditor extends EditorPart implements IReusableEditor {

    /**
     * Text widget used for this editor
     */
    private Text fText;

    /**
     * @see org.eclipse.ui.IEditorPart#doSave(IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
    }

    /**
     * @see org.eclipse.ui.IEditorPart#doSaveAs()
     */
    public void doSaveAs() {
    }

    /**
     * @see org.eclipse.ui.IEditorPart#gotoMarker(IMarker)
     */
    public void gotoMarker(IMarker marker) {
    }

    /**
     * @see org.eclipse.ui.IEditorPart#init(IEditorSite, IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input) {
        setSite(site);
        setInput(input);
    }

    /**
     * @see org.eclipse.ui.IEditorPart#isDirty()
     */
    public boolean isDirty() {
        return false;
    }

    /**
     * @see org.eclipse.ui.IEditorPart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
     */
    public void createPartControl(Composite parent) {
        fText = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
        fText.setForeground(JFaceColors.getErrorText(fText.getDisplay()));
        fText.setBackground(fText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        if (getEditorInput() != null) {
            setInput(getEditorInput());
        }
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        if (fText != null) {
            fText.setFocus();
        }
    }

    /**
     * @see IReusableEditor#setInput(org.eclipse.ui.IEditorInput)
     */
    public void setInput(IEditorInput input) {
        super.setInput(input);
        setPartName(input.getName());
        if (fText != null) {
            fText.setText(input.getToolTipText());
        }
    }

}
