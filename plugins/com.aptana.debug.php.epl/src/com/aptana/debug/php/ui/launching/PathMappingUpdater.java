package com.aptana.debug.php.ui.launching;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.window.Window;
import org.eclipse.php.debug.core.debugger.pathmapper.PathMapper;
import org.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org.eclipse.php.internal.debug.core.pathmapper.PathMapperRegistry;
import org.eclipse.php.util.SWTUtil;

import com.aptana.debug.php.core.IPHPDebugCorePreferenceKeys;
import com.aptana.debug.php.epl.PHPDebugEPLPlugin;
import com.aptana.webserver.core.AbstractWebServerConfiguration;

/**
 * This class is responsible of updating the Server definition for all the PHP Web Page launch configurations. This
 * class should run from a UI thread.
 * 
 * @author Shalom Gibly
 */
public class PathMappingUpdater
{

	/**
	 * Update the paths by providing the user with a list of the configurations that will be updated and giving the
	 * option to select the ones that will not get the update.
	 * 
	 * @param server The server that was updated with a new Path Mapping settings.
	 */
	public void updatePaths(AbstractWebServerConfiguration server)
	{
		updatePaths(server, null);
	}

	/**
	 * Update the paths by providing the user with a list of the configurations that will be updated and giving the
	 * option to select the ones that will not get the update.
	 * 
	 * @param server The server that was updated with a new Path Mapping settings.
	 * @param excludedLaunches A list of launches names to exclude from the list (usually, we'll exclude the current active launch, so the
	 * user will need to apply to change the current setting)
	 */
	public void updatePaths(AbstractWebServerConfiguration server, String[] excludedLaunches)
	{
		try
		{
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType configurationType = launchManager.getLaunchConfigurationType(IPHPDebugConstants.PHP_WEB_LAUNCH_TYPE_ID);
			ILaunchConfiguration[] configs = launchManager.getLaunchConfigurations(configurationType);
			List<ILaunchConfiguration> launchConfigurations = new ArrayList<ILaunchConfiguration>();
			Collections.addAll(launchConfigurations, configs);
			if (excludedLaunches != null)
			{
				// Clean up any excluded launches
				for (String exclude : excludedLaunches)
				{
					for (int i = launchConfigurations.size() - 1; i >= 0; i--)
					{
						if (launchConfigurations.get(i).getName().equals(exclude))
						{
							launchConfigurations.remove(i);
							break;
						}
					}
				}
			}
			// Scan and remove the configurations that should not be affected by the path mapping change (use a
			// different server)
			filterByLaunchAttributes(launchConfigurations, server);
			if (!launchConfigurations.isEmpty())
			{
				// This will pop-up the user selection dialog
				filterBySelection(launchConfigurations);
			}
			if (!launchConfigurations.isEmpty())
			{
				updateConfigurations(launchConfigurations, server);
			}
		}
		catch (CoreException ce)
		{
			PHPDebugEPLPlugin.logError("Error while trying to propogate the PHP Server path mapping to the PHP web launch configurations", ce); //$NON-NLS-1$
		}
	}

	/*
	 * Filter out any launch configuration that does not use this the affected server, does not use a specific file
	 * or does not auto-generate the script when using a specific file.
	 */
	private void filterByLaunchAttributes(List<ILaunchConfiguration> launchConfigurations, AbstractWebServerConfiguration server) throws CoreException
	{
		for (int i = launchConfigurations.size() - 1; i >= 0; i--)
		{
			ILaunchConfiguration launchConfiguration = launchConfigurations.get(i);
			boolean usesSpecifiedScript = launchConfiguration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_USE_SPECIFIC_FILE, false);
			if (!server.getName().equals(launchConfiguration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_NAME, "")) ||
					!usesSpecifiedScript ||
					usesSpecifiedScript && !launchConfiguration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_AUTO_GENERATED_URL, false))
			{
				launchConfigurations.remove(i);
			}
		}
	}

	/*
	 * Display a selection dialog for the user to select the launch configurations that should be affected.
	 */
	private void filterBySelection(List<ILaunchConfiguration> launchConfigurations)
	{
		LaunchConfigurationsSelectionDialog dialog = new LaunchConfigurationsSelectionDialog(SWTUtil.getStandardDisplay().getActiveShell(), launchConfigurations);
		if (dialog.open() == Window.OK)
		{
			Object[] result = dialog.getResult();
			if (result != null)
			{
				launchConfigurations.clear();
				// Add the confirmed configurations
				for (Object o : result) {
					launchConfigurations.add((ILaunchConfiguration) o);
				}
				return;
			}
		}
		launchConfigurations.clear();
	}

	/**
	 * Update the launch configurations in the list with the new path mapper settings that were assigned to the specified server.
	 * The update process assumes that the given configurations are assigned with the given server, has a selected workspace script settings,
	 * and set the URL generation to 'auto'. 
	 * 
	 * @param configurations
	 * @param server
	 * @throws CoreException 
	 */
	public static void updateConfigurations(List<ILaunchConfiguration> configurations, AbstractWebServerConfiguration server) throws CoreException
	{
		for (ILaunchConfiguration configuration : configurations)
		{
			String fileName = configuration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_FILE_NAME, "");
			if (fileName == null || fileName.trim().length() == 0)
			{
				continue;
			}
			fileName = fileName.replace('\\', '/');
			URL newURL = server.getBaseURL();
			PathMapper pathMapper = PathMapperRegistry.getByServer(server);
			String remoteFile = pathMapper.getRemoteFile(fileName); 
			ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
			if (remoteFile != null && !"".equals(remoteFile) && !remoteFile.equals(fileName))
			{
				workingCopy.setAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_BASE_URL, newURL + remoteFile);
			}
			else
			{
				// recalculate the auto generated, just in case that the mapping was just removed for this file
				workingCopy.setAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_BASE_URL, newURL + fileName);
			}
			workingCopy.doSave();
		}
	}
}
