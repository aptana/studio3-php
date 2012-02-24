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
package org.eclipse.php.internal.debug.ui.preferences;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.php.internal.debug.core.preferences.PHPDebugCorePreferenceNames;
import org.eclipse.php.internal.debug.ui.PHPDebugUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.osgi.service.prefs.BackingStoreException;

import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;
import com.aptana.php.debug.core.preferences.PHPDebugPreferencesUtil;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * A PHP debug preferences page Workspace UI addon.
 * This addon adds 3 check boxes for the debug perspective, debug info and debug views.
 *
 * @author shalom
 */
public class WorkbenchOptionsBlock extends AbstractPHPPreferencePageBlock {

	private Button fOpenInBrowser;
	private Button fOpenDebugViews;
	private PreferencePage propertyPage;
	private RadioGroupFieldEditor fSwitchPerspField;
	private RadioGroupFieldEditor fAllowMultipleLnchField;

	public void setCompositeAddon(Composite parent) {
		Composite composite = addPageContents(parent);
		addWorkspacePreferenceSubsection(composite);
	}

	public void initializeValues(PreferencePage propertyPage) {
		this.propertyPage = propertyPage;

		fOpenDebugViews.setSelection(PHPDebugPreferencesUtil.getBoolean(PHPDebugCorePreferenceNames.OPEN_DEBUG_VIEWS, true));
		fOpenInBrowser.setSelection(PHPDebugPreferencesUtil.getBoolean(PHPDebugCorePreferenceNames.OPEN_IN_BROWSER, true));

		fAllowMultipleLnchField.setPreferenceStore(PHPDebugEPLPlugin.getDefault().getPreferenceStore());
		fAllowMultipleLnchField.load();

		fSwitchPerspField.setPreferenceStore(PHPDebugEPLPlugin.getDefault().getPreferenceStore());
		fSwitchPerspField.load();
	}

	public boolean performOK(boolean isProjectSpecific) {
		savePreferences();
		return true;
	}

	public void performApply(boolean isProjectSpecific) {
		performOK(isProjectSpecific);
	}

	public boolean performCancel() {
		return true;
	}

	public void performDefaults() {
		IEclipsePreferences prefs = new DefaultScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID);
		fOpenInBrowser.setSelection(prefs.getBoolean(PHPDebugCorePreferenceNames.OPEN_IN_BROWSER, true));
		fOpenDebugViews.setSelection(prefs.getBoolean(PHPDebugCorePreferenceNames.OPEN_DEBUG_VIEWS, true));

		fAllowMultipleLnchField.setPreferenceStore(PHPDebugEPLPlugin.getDefault().getPreferenceStore());
		fAllowMultipleLnchField.load();

		fSwitchPerspField.setPreferenceStore(PHPDebugEPLPlugin.getDefault().getPreferenceStore());
		fSwitchPerspField.load();
	}

	private void addWorkspacePreferenceSubsection(Composite composite) {
		fAllowMultipleLnchField = new RadioGroupFieldEditor(IPHPDebugCorePreferenceKeys.ALLOW_MULTIPLE_LAUNCHES, PHPDebugUIMessages.PHPLaunchingPreferencePage_multipleMessage, 3, new String[][] { { PHPDebugUIMessages.PHPLaunchingPreferencePage_Always, MessageDialogWithToggle.ALWAYS }, { PHPDebugUIMessages.PHPLaunchingPreferencePage_Never, MessageDialogWithToggle.NEVER }, { PHPDebugUIMessages.PHPLaunchingPreferencePage_Prompt, MessageDialogWithToggle.PROMPT } }, composite, true);

		fSwitchPerspField = new RadioGroupFieldEditor(IPHPDebugCorePreferenceKeys.SWITCH_BACK_TO_PREVIOUS_PERSPECTIVE, PHPDebugUIMessages.PHPLaunchingPreferencePage_switchToPHPMessage, 3, new String[][] { { PHPDebugUIMessages.PHPLaunchingPreferencePage_Always, MessageDialogWithToggle.ALWAYS }, { PHPDebugUIMessages.PHPLaunchingPreferencePage_Never, MessageDialogWithToggle.NEVER }, { PHPDebugUIMessages.PHPLaunchingPreferencePage_Prompt, MessageDialogWithToggle.PROMPT } }, composite, true);

		Group group = new Group(composite, SWT.NONE);
		group.setText(PHPDebugUIMessages.WorkbenchOptionsBlock_workbench_options);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		fOpenInBrowser = addCheckBox(group, PHPDebugUIMessages.PhpDebugPreferencePage_11, PHPDebugCorePreferenceNames.OPEN_IN_BROWSER, 0);
		fOpenDebugViews = addCheckBox(group, PHPDebugUIMessages.PhpDebugPreferencePage_7, PHPDebugCorePreferenceNames.OPEN_DEBUG_VIEWS, 0);
	}

	private void savePreferences() {
		IEclipsePreferences prefs = new InstanceScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID);
		prefs.putBoolean(PHPDebugCorePreferenceNames.OPEN_IN_BROWSER, fOpenInBrowser.getSelection());
		prefs.putBoolean(PHPDebugCorePreferenceNames.OPEN_DEBUG_VIEWS, fOpenDebugViews.getSelection());
		try
		{
			prefs.flush();
		}
		catch (BackingStoreException e)
		{
			PHPDebugEPLPlugin.logError(e);
		}

		fAllowMultipleLnchField.store();
		fSwitchPerspField.store();
	}
}
