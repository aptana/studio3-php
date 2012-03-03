/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.launching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.php.internal.ui.wizard.field.ListDialogField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.internal.ui.wizard.field.IListAdapter;
import com.aptana.php.debug.IDebugScopes;
import com.aptana.php.debug.PHPDebugPlugin;
import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;
import com.aptana.php.debug.core.util.NameValuePair;
import com.aptana.ui.util.SWTUtils;

public class HTTPLaunchConfigurationTab extends AbstractLaunchConfigurationTab
{
	private static final String IMG_LAUNCH_HTTP = "/icons/full/obj16/launch-http.gif"; //$NON-NLS-1$
	private static final int IDX_ADD = 0;
	private static final int IDX_EDIT = 1;
	private static final int IDX_REMOVE = 2;
	private static final int IDX_UP = 4;
	private static final int IDX_DOWN = 5;

	private static final String NAME = Messages.HTTPLaunchConfigurationTab_httpLaunchTabName;
	private ListDialogField<NameValuePair> listGET;
	private ListDialogField<NameValuePair> listPOST;

	public HTTPLaunchConfigurationTab()
	{
		listGET = new EnhancedListDialogField<NameValuePair>(new ListAdapter(), new String[] {
				Messages.HTTPLaunchConfigurationTab_getAddLabel, Messages.HTTPLaunchConfigurationTab_getEditLabel,
				Messages.HTTPLaunchConfigurationTab_getRemoveLabel, null,
				Messages.HTTPLaunchConfigurationTab_getUpLabel, Messages.HTTPLaunchConfigurationTab_getDownLabel },
				new NameValueLabelProvider());
		listPOST = new EnhancedListDialogField<NameValuePair>(new ListAdapter(), new String[] {
				Messages.HTTPLaunchConfigurationTab_postAddLabel, Messages.HTTPLaunchConfigurationTab_postEditLabel,
				Messages.HTTPLaunchConfigurationTab_postRemoveLabel, null,
				Messages.HTTPLaunchConfigurationTab_postUpLabel, Messages.HTTPLaunchConfigurationTab_postDownLabel },
				new NameValueLabelProvider());
		ListDialogField.ColumnsDescription columnsDescription = new ListDialogField.ColumnsDescription(new String[] {
				Messages.HTTPLaunchConfigurationTab_nameColumn, Messages.HTTPLaunchConfigurationTab_valueColumn }, true);
		listGET.setTableColumns(columnsDescription);
		listPOST.setTableColumns(columnsDescription);

		listGET.setRemoveButtonIndex(IDX_REMOVE);
		listGET.setUpButtonIndex(IDX_UP);
		listGET.setDownButtonIndex(IDX_DOWN);
		listPOST.setRemoveButtonIndex(IDX_REMOVE);
		listPOST.setUpButtonIndex(IDX_UP);
		listPOST.setDownButtonIndex(IDX_DOWN);

	}

	public void createControl(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group groupGET = new Group(composite, SWT.NONE);
		groupGET.setText(Messages.HTTPLaunchConfigurationTab_getGroupText);
		groupGET.setLayout(new GridLayout(2, false));
		groupGET.setLayoutData(new GridData(GridData.FILL_BOTH));
		Control listControlGET = listGET.getListControl(groupGET);
		Control buttonsControlGET = listGET.getButtonBox(groupGET);

		Group groupPOST = new Group(composite, SWT.NONE);
		groupPOST.setText(Messages.HTTPLaunchConfigurationTab_postGroupText);
		groupPOST.setLayout(new GridLayout(2, false));
		groupPOST.setLayoutData(new GridData(GridData.FILL_BOTH));
		Control listControlPOST = listPOST.getListControl(groupPOST);
		Control buttonsControlPOST = listPOST.getButtonBox(groupPOST);

		GridData data = new GridData(GridData.FILL_BOTH);
		listControlGET.setLayoutData(data);
		data = new GridData(GridData.FILL_BOTH);
		listControlPOST.setLayoutData(data);

		buttonsControlGET
				.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
		buttonsControlPOST.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_BEGINNING));

		setControl(composite);
	}

	public Image getImage()
	{
		return SWTUtils.getImage(PHPEplPlugin.getDefault(), IMG_LAUNCH_HTTP);
	}

	public String getName()
	{
		return NAME;
	}

	@SuppressWarnings("unchecked")
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		try
		{
			List<String> GET = configuration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_HTTP_GET,
					Collections.EMPTY_LIST);
			List<String> POST = configuration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_HTTP_POST,
					Collections.EMPTY_LIST);
			// Convert to name-value pairs
			List<NameValuePair> pairsGET = new ArrayList<NameValuePair>();
			for (String pair : GET)
			{
				NameValuePair nameValuePair = NameValuePair.fromPairString(pair);
				if (nameValuePair != null)
				{
					pairsGET.add(nameValuePair);
				}
			}

			List<NameValuePair> pairsPOST = new ArrayList<NameValuePair>();
			for (String pair : POST)
			{
				NameValuePair nameValuePair = NameValuePair.fromPairString(pair);
				if (nameValuePair != null)
				{
					pairsPOST.add(nameValuePair);
				}
			}
			listGET.setElements(pairsGET);
			listPOST.setElements(pairsPOST);
		}
		catch (CoreException e)
		{
			IdeLog.logError(PHPDebugPlugin.getDefault(),
					"Error initializing the HTTP GET/POST configuration dialog", e, IDebugScopes.DEBUG); //$NON-NLS-1$
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		// save the settings
		List<NameValuePair> elementsGET = listGET.getElements();
		List<String> GET = new ArrayList<String>();
		for (NameValuePair nvp : elementsGET)
		{
			String pair = nvp.toString();
			GET.add(pair);
		}

		List<NameValuePair> elementsPOST = listPOST.getElements();
		List<String> POST = new ArrayList<String>();
		for (NameValuePair nvp : elementsPOST)
		{
			String pair = nvp.toString();
			POST.add(pair);
		}

		configuration.setAttribute(IPHPDebugCorePreferenceKeys.ATTR_HTTP_GET, GET);
		configuration.setAttribute(IPHPDebugCorePreferenceKeys.ATTR_HTTP_POST, POST);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		// DO NOTHING
	}

	private static class NameValueLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element)
		{
			return getColumnText(element, 0);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex)
		{
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex)
		{
			NameValuePair pair = (NameValuePair) element;
			if (columnIndex == 0)
			{
				return pair.name;
			}
			return pair.value;
		}
	}

	/*
	 * List adapter for the GET and POST lists
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private class ListAdapter implements IListAdapter
	{

		public void customButtonPressed(ListDialogField field, int index)
		{
			NameValuePair edited = null;
			if (index != IDX_ADD)
			{
				edited = (NameValuePair) field.getSelectedElements().get(0);
			}
			if (index == IDX_ADD || index == IDX_EDIT)
			{
				NameValuePairInputDialog dialog = new NameValuePairInputDialog(getShell(), edited, field.getElements());
				if (dialog.open() == Window.OK)
				{
					NameValuePair result = dialog.getResult();
					if (edited != null)
					{
						if (!edited.equals(result))
						{
							field.replaceElement(edited, dialog.getResult());
						}
					}
					else
					{
						field.addElement(dialog.getResult());
					}
				}
			}
			updateLaunchConfigurationDialog();
		}

		public void doubleClicked(ListDialogField field)
		{
			customButtonPressed(field, IDX_EDIT);
		}

		public void selectionChanged(ListDialogField field)
		{
			// TODO - update the buttons?
		}
	}

	/**
	 * Override the default behavior of the list to update the launch configuration dialog when Up, Down and Remove are
	 * invoked. Also, handles the Edit button enablement state.
	 */
	private class EnhancedListDialogField<E> extends ListDialogField<E>
	{
		EnhancedListDialogField(IListAdapter adapter, String[] buttonLabels, ILabelProvider lprovider)
		{
			super(adapter, buttonLabels, lprovider);
		}

		protected boolean getManagedButtonState(ISelection sel, int index)
		{
			if (index == IDX_EDIT)
			{
				return !sel.isEmpty();
			}
			return super.getManagedButtonState(sel, index);
		}

		protected boolean managedButtonPressed(int index)
		{
			boolean res = super.managedButtonPressed(index);
			if (res)
			{
				updateLaunchConfigurationDialog();
			}
			return res;
		}
	}
}
