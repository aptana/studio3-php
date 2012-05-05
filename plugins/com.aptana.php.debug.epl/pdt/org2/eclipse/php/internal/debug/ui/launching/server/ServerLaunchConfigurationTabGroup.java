package org2.eclipse.php.internal.debug.ui.launching.server;

import java.util.ArrayList;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org2.eclipse.php.internal.debug.ui.launching.LaunchConfigurationsTabsRegistry;

/**
 * A debug tab group for launching debug on server. 
 */
public class ServerLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	protected final String CONFIGURATION_TAB_GROUP_ID = "org2.eclipse.php.server.ui.launchConfigurationTabGroup";

	public ServerLaunchConfigurationTabGroup() {
		super();

	}

	/*
	 * @see ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog, String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		AbstractLaunchConfigurationTab[] tabs = LaunchConfigurationsTabsRegistry.getLaunchTabs(CONFIGURATION_TAB_GROUP_ID, mode);
		ArrayList list = new ArrayList();
		if (tabs != null) {
			for (int i = 0; i < tabs.length; i++) {
				list.add(tabs[i]);
				tabs[i].setLaunchConfigurationDialog(dialog);
			}
		}
		if (list.isEmpty()) {
			ServerLaunchConfigurationTab aTab = new ServerLaunchConfigurationTab();
			aTab.setLaunchConfigurationDialog(dialog);
			list.add(aTab);
		}

		CommonTab newTab = new CommonTab();
		newTab.setLaunchConfigurationDialog(dialog);
		list.add(newTab);

		ILaunchConfigurationTab[] allTabs = (ILaunchConfigurationTab[]) list.toArray(new ILaunchConfigurationTab[list.size()]);
		setTabs(allTabs);
	}
}
