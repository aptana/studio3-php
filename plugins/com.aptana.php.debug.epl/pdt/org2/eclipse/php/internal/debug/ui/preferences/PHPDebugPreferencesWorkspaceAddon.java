/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.ui.preferences;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.prefs.BackingStoreException;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebugCorePreferenceNames;
import org2.eclipse.php.internal.debug.ui.PHPDebugUIMessages;

import com.aptana.php.debug.core.preferences.PHPDebugPreferencesUtil;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * A PHP debug preferences page Workspace UI addon. This addon adds 3 check boxes for the debug perspective, debug info
 * and debug views.
 * 
 * @author shalom
 */
public class PHPDebugPreferencesWorkspaceAddon extends AbstractPHPPreferencePageBlock
{

	private Button fOpenInBrowser;
	private Button fOpenDebugViews;
	private PreferencePage propertyPage;

	public void setCompositeAddon(Composite parent)
	{
		Composite composite = this.addPageContents(parent);
		this.addWorkspacePreferenceSubsection(this.createSubsection(composite,
				PHPDebugUIMessages.PhpDebugPreferencePage_0));
	}

	public void initializeValues(PreferencePage propertyPage)
	{
		this.propertyPage = propertyPage;
		this.fOpenDebugViews.setSelection(PHPDebugPreferencesUtil.getBoolean(PHPDebugCorePreferenceNames.OPEN_DEBUG_VIEWS, true));
		this.fOpenInBrowser.setSelection(PHPDebugPreferencesUtil.getBoolean(PHPDebugCorePreferenceNames.OPEN_IN_BROWSER, true));
	}

	public boolean performOK(boolean isProjectSpecific)
	{
		this.savePreferences();
		return true;
	}

	public void performApply(boolean isProjectSpecific)
	{
		this.performOK(isProjectSpecific);
	}

	public boolean performCancel()
	{
		return true;
	}

	public void performDefaults()
	{
		IEclipsePreferences prefs = new DefaultScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID);
		// fRunWithDebugInfo.setSelection(prefs.getDefaultBoolean(PHPDebugCorePreferenceNames.RUN_WITH_DEBUG_INFO));
		this.fOpenInBrowser.setSelection(prefs.getBoolean(PHPDebugCorePreferenceNames.OPEN_IN_BROWSER, true));
		this.fOpenDebugViews.setSelection(prefs.getBoolean(PHPDebugCorePreferenceNames.OPEN_DEBUG_VIEWS, true));
		// fDebugTextBox.setText(Integer.toString(prefs.getDefaultInt(PHPDebugCorePreferenceNames.DEBUG_PORT)));
	}

	private void addWorkspacePreferenceSubsection(Composite composite)
	{
		this.fOpenInBrowser = this.addCheckBox(composite, PHPDebugUIMessages.PhpDebugPreferencePage_11,
				PHPDebugCorePreferenceNames.OPEN_IN_BROWSER, 0);
		this.fOpenDebugViews = this.addCheckBox(composite, PHPDebugUIMessages.PhpDebugPreferencePage_7,
				PHPDebugCorePreferenceNames.OPEN_DEBUG_VIEWS, 0);
	}

	private void savePreferences()
	{
		IEclipsePreferences prefs = new InstanceScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID);
		prefs.putBoolean(PHPDebugCorePreferenceNames.OPEN_IN_BROWSER, this.fOpenInBrowser.getSelection());
		prefs.putBoolean(PHPDebugCorePreferenceNames.OPEN_DEBUG_VIEWS, this.fOpenDebugViews.getSelection());
		try
		{
			prefs.flush();
		}
		catch (BackingStoreException e)
		{
			PHPDebugEPLPlugin.logError(e);
		}
	}
}
