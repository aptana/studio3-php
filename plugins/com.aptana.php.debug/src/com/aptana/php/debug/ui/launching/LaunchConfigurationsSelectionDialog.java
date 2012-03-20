/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.launching;

import java.util.Arrays;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.SelectionDialog;
import org2.eclipse.php.internal.ui.util.SWTFactory;

import com.aptana.core.util.StringUtil;

@SuppressWarnings("restriction")
public class LaunchConfigurationsSelectionDialog extends SelectionDialog
{

	private static final int Y = 300;
	private static final int X = 400;

	/**
	 * Whether to add Select All / De-select All buttons to the custom footer controls.
	 */
	private boolean fShowSelectButtons = false;
	protected StructuredViewer fViewer = null;
	private final List<ILaunchConfiguration> configurations;

	/**
	 * Constructor
	 * 
	 * @param parentShell
	 */
	public LaunchConfigurationsSelectionDialog(Shell parentShell, List<ILaunchConfiguration> configurations)
	{
		super(parentShell);
		this.configurations = configurations;
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setShowSelectAllButtons(true);
		setMessage(Messages.LaunchConfigurationsSelectionDialog_confirmUpdateMessage);
		setTitle(Messages.LaunchConfigurationsSelectionDialog_confirmUpdateTitle);
		setHelpAvailable(false);
	}

	/**
	 * This method allows the newly created controls to be initialized. This method is called only once all controls
	 * have been created from the <code>createContents</code> method. By default this method initializes the OK button
	 * control.
	 */
	protected void initializeControls()
	{
		getCheckBoxTableViewer().setAllChecked(true);
		getButton(IDialogConstants.OK_ID).setEnabled(isValid());
	}

	/**
	 * Returns the viewer used to display information in this dialog. Can be <code>null</code> if the viewer has not
	 * been created.
	 * 
	 * @return viewer used in this dialog
	 */
	protected Viewer getViewer()
	{
		return fViewer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent)
	{
		Composite comp = (Composite) super.createContents(parent);
		initializeControls();
		return comp;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@SuppressWarnings("rawtypes")
	protected Control createDialogArea(Composite parent)
	{
		initializeDialogUnits(parent);
		Composite comp = (Composite) super.createDialogArea(parent);
		String label = getMessage();
		if (!StringUtil.isEmpty(label))
		{
			SWTFactory.createWrapLabel(comp, label, 1);
		}
		label = getViewerLabel();
		if (!StringUtil.isEmpty(label))
		{
			SWTFactory.createLabel(comp, label, 1);
		}
		fViewer = createViewer(comp);
		fViewer.setLabelProvider(getLabelProvider());
		fViewer.setContentProvider(getContentProvider());
		fViewer.setInput(getViewerInput());
		List selectedElements = getInitialElementSelections();
		if (selectedElements != null && !selectedElements.isEmpty())
		{
			fViewer.setSelection(new StructuredSelection(selectedElements));
		}
		addViewerListeners(fViewer);
		addCustomFooterControls(comp);
		Dialog.applyDialogFont(comp);
		return comp;
	}

	protected Object getViewerInput()
	{
		return configurations;
	}

	protected String getViewerLabel()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns the viewer cast to the correct instance. Possibly <code>null</code> if the viewer has not been created
	 * yet.
	 * 
	 * @return the viewer cast to CheckboxTableViewer
	 */
	protected CheckboxTableViewer getCheckBoxTableViewer()
	{
		return (CheckboxTableViewer) fViewer;
	}

	/**
	 * Returns the content provider for the viewer
	 * 
	 * @return the content provider for the viewer
	 */
	protected IContentProvider getContentProvider()
	{
		// by default return a simple array content provider
		return new ArrayContentProvider();
	}

	/**
	 * Returns the label provider used by the viewer
	 * 
	 * @return the label provider used in the viewer
	 */
	protected IBaseLabelProvider getLabelProvider()
	{
		return new DefaultLabelProvider();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#createViewer(org.eclipse.swt.
	 * widgets.Composite)
	 */
	protected StructuredViewer createViewer(Composite parent)
	{
		// by default return a checkbox table viewer
		Table table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.CHECK);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		return new CheckboxTableViewer(table);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#addViewerListeners(org.eclipse
	 * .jface.viewers.StructuredViewer)
	 */
	protected void addViewerListeners(StructuredViewer viewer)
	{
		getCheckBoxTableViewer().addCheckStateListener(new DefaultCheckboxListener());
	}

	/**
	 * A checkbox state listener that ensures that exactly one element is checked and enables the OK button when this is
	 * the case.
	 */
	private class DefaultCheckboxListener implements ICheckStateListener
	{
		public void checkStateChanged(CheckStateChangedEvent event)
		{
			getButton(IDialogConstants.OK_ID).setEnabled(isValid());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.AbstractDebugSelectionDialog#isValid()
	 */
	protected boolean isValid()
	{
		return getCheckBoxTableViewer().getCheckedElements().length > 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed()
	{
		Object[] elements = getCheckBoxTableViewer().getCheckedElements();
		setResult(Arrays.asList(elements));
		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#addCustomFooterControls(org.eclipse
	 * .swt.widgets.Composite)
	 */
	protected void addCustomFooterControls(Composite parent)
	{
		if (fShowSelectButtons)
		{
			Composite comp = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_HORIZONTAL);
			GridData gd = (GridData) comp.getLayoutData();
			gd.horizontalAlignment = SWT.END;
			Button button = SWTFactory.createPushButton(comp, Messages.LaunchConfigurationsSelectionDialog_selectAll,
					null);
			button.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					getCheckBoxTableViewer().setAllChecked(true);
					getButton(IDialogConstants.OK_ID).setEnabled(isValid());
				}
			});
			button = SWTFactory.createPushButton(comp, Messages.LaunchConfigurationsSelectionDialog_deselectAll, null);
			button.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					getCheckBoxTableViewer().setAllChecked(false);
					getButton(IDialogConstants.OK_ID).setEnabled(isValid());
				}
			});
		}
	}

	/**
	 * If this setting is set to true before the dialog is opened, a Select All and a De-select All button will be added
	 * to the custom footer controls. The default setting is false.
	 * 
	 * @param setting
	 *            whether to show the select all and de-select all buttons
	 */
	protected void setShowSelectAllButtons(boolean setting)
	{
		fShowSelectButtons = setting;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	protected Point getInitialSize()
	{
		IDialogSettings settings = getDialogBoundsSettings();
		if (settings != null)
		{
			try
			{
				int width = settings.getInt("DIALOG_WIDTH"); //$NON-NLS-1$
				int height = settings.getInt("DIALOG_HEIGHT"); //$NON-NLS-1$
				if (width > 0 & height > 0)
				{
					return new Point(width, height);
				}
			}
			catch (NumberFormatException nfe)
			{
				return new Point(X, Y);
			}
		}
		return new Point(X, Y);
	}

}