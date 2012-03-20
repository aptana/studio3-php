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
package org2.eclipse.php.internal.debug.ui.launching.exe;

import java.util.ArrayList;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org2.eclipse.php.internal.debug.ui.launching.LaunchConfigurationsTabsRegistry;
import org2.eclipse.php.internal.debug.ui.launching.PHPDebugArgumentsTab;

/**
 * A PHP executable launch configuration tab group that loads all of its tabs
 * from the launch configuration tabs which extends
 * org2.eclipse.php.debug.ui.launchConfigurationTabs.
 * 
 * @author shalom
 * 
 */
public class PHPExeLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	protected final String CONFIGURATION_TAB_GROUP_ID = "org2.eclipse.php.deubg.ui.launching.launchConfigurationTabGroup.phpexe";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog,
	 *      java.lang.String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		AbstractLaunchConfigurationTab[] tabs = LaunchConfigurationsTabsRegistry.getLaunchTabs(CONFIGURATION_TAB_GROUP_ID, mode);
		ArrayList<ILaunchConfigurationTab> list = new ArrayList<ILaunchConfigurationTab>();
		if (tabs != null) {
			for (int i = 0; i < tabs.length; i++) {
				list.add(tabs[i]);
				tabs[i].setLaunchConfigurationDialog(dialog);
			}
		}
		if (list.isEmpty()) {
			PHPExecutableLaunchTab aTab = new PHPExecutableLaunchTab();
			aTab.setLaunchConfigurationDialog(dialog);
			list.add(aTab);
		}

		// Add arguments, environment and common tabs to the tabs group.
		PHPDebugArgumentsTab argumentsTab = new PHPDebugArgumentsTab();
		argumentsTab.setLaunchConfigurationDialog(dialog);
		list.add(argumentsTab);

		EnvironmentTab environmentTab = new EnvironmentTab();
		environmentTab.setLaunchConfigurationDialog(dialog);
		list.add(environmentTab);
		
		CommonTab newTab = new CommonTab();
		newTab.setLaunchConfigurationDialog(dialog);
		list.add(newTab);
		
		ILaunchConfigurationTab[] allTabs = list.toArray(new ILaunchConfigurationTab[list.size()]);
		setTabs(allTabs);
	}

}
