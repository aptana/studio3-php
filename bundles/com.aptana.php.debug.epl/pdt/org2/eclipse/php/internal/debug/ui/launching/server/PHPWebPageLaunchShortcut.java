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
package org2.eclipse.php.internal.debug.ui.launching.server;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org2.eclipse.php.debug.core.debugger.parameters.IDebugParametersKeys;
import org2.eclipse.php.internal.core.util.FileUtils;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.debugger.AbstractDebuggerConfiguration;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebuggersRegistry;
import org2.eclipse.php.internal.debug.core.preferences.PHPProjectPreferences;
import org2.eclipse.php.internal.debug.ui.PHPDebugUIMessages;

import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;
import com.aptana.php.debug.core.server.PHPServersManager;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;
import com.aptana.webserver.core.IServer;

/**
 * A PHP web page launch shortcut.
 *
 * @author Shalom Gibly
 */
public class PHPWebPageLaunchShortcut implements ILaunchShortcut {

	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			searchAndLaunch(((IStructuredSelection) selection).toArray(), mode, getPHPServerLaunchConfigType());
		}
	}

	public void launch(IEditorPart editor, String mode) {
		IEditorInput input = editor.getEditorInput();
		IFile file = (IFile) input.getAdapter(IFile.class);
		if (file != null) {
			searchAndLaunch(new Object[] { file }, mode, getPHPServerLaunchConfigType());
		}
	}

	private ILaunchConfigurationType getPHPServerLaunchConfigType() {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		return lm.getLaunchConfigurationType(IPHPDebugConstants.PHPServerLaunchType);
	}

	public static void searchAndLaunch(Object[] search, String mode, ILaunchConfigurationType configType) {
		int entries = search == null ? 0 : search.length;
		for (int i = 0; i < entries; i++) {
			try {
				String phpPathString = null;
				IProject project = null;
				Object obj = search[i];

//				//TODO: if IProject, offer choices?
//				if (obj instanceof PHPCodeData) {
//					obj = PHPModelUtil.getResource(obj);
//				}
				if (obj instanceof IFile) {
					IFile file = (IFile) obj;
					project = file.getProject();
					IContentType contentType = Platform.getContentTypeManager().getContentType(IPHPConstants.CONTENT_TYPE_HTML_PHP);
					if (contentType.isAssociatedWith(file.getName())) {
						phpPathString = file.getFullPath().toString();
					}
				}

				if (phpPathString == null) {
					// Could not find target to launch
					throw new CoreException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IStatus.OK, PHPDebugUIMessages.launch_failure_no_target, null));
				}

				IServer defaultServer = PHPServersManager.getDefaultServer(project);
				if (defaultServer == null) {
					PHPDebugEPLPlugin.createDefaultPHPServer();
					defaultServer = PHPServersManager.getDefaultServer(project);
					if (defaultServer == null) {
						// Sould not happen
						throw new CoreException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IStatus.OK, "Could not create a defualt server for the launch.", null));
					}
				}

				// Launch the app
				ILaunchConfiguration config = findLaunchConfiguration(project, phpPathString, defaultServer, mode, configType);
				if (config != null) {
					DebugUITools.launch(config, mode);
				} else {
					// Could not find launch configuration or the user cancelled.
					// throw new CoreException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IStatus.OK, PHPDebugUIMessages.launch_failure_no_config, null));
				}
			} catch (CoreException ce) {
				final IStatus stat = ce.getStatus();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						ErrorDialog.openError(PHPDebugEPLPlugin.getActiveWorkbenchShell(), PHPDebugUIMessages.launch_failure_msg_title, PHPDebugUIMessages.launch_failure_server_msg_text, stat);
					}
				});
			}
		}
	}

	/**
	 * Locate a configuration to relaunch for the given type.  If one cannot be found, create one.
	 *
	 * @return a re-useable config or <code>null</code> if none
	 */
	static ILaunchConfiguration findLaunchConfiguration(IProject project, String fileName, IServer server, String mode, ILaunchConfigurationType configType) {
		ILaunchConfiguration config = null;

		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);

			int numConfigs = configs == null ? 0 : configs.length;
			for (int i = 0; i < numConfigs; i++) {
				String configuredServerName = configs[i].getAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_NAME, (String) null);
				String configuredFileName = configs[i].getAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_FILE_NAME, (String) null);

				if (configuredFileName.equals(fileName) && server.getName().equals(configuredServerName)) {
					config = configs[i].getWorkingCopy();
					break;
				}
			}

			if (config == null) {
				config = createConfiguration(project, fileName, server, configType, mode);
			}
		} catch (CoreException ce) {
			ce.printStackTrace();
		}
		return config;
	}

	static String computeContextRoot(String url, String fileName, IServer server) {
		String serverBaseURL = server.getBaseURL().toString();
		if (url.length() > serverBaseURL.length() + 1) {
			url = url.substring(serverBaseURL.length() + 1);
		} else if (url.length() == serverBaseURL.length() || url.length() == serverBaseURL.length() + 1) {
			return ""; //$NON-NLS-1$
		}
		// Remove the project name from the file name
		if (fileName.length() > 0) {
			fileName = fileName.substring(1);
			int pathIndex = fileName.indexOf('/');
			if (pathIndex < 0) {
				fileName = ""; //$NON-NLS-1$
			} else {
				fileName = fileName.substring(pathIndex);
			}
		}
		if (url.length() > fileName.length()) {
			url = url.substring(0, url.length() - fileName.length());
		} else {
			return ""; //$NON-NLS-1$
		}
		return url;
	}

	/**
	 * Create & return a new configuration
	 */
	static ILaunchConfiguration createConfiguration(IProject project, String fileName, IServer server, ILaunchConfigurationType configType, String mode) throws CoreException {
		ILaunchConfiguration config = null;
		if (!FileUtils.resourceExists(fileName)) {
			return null;
		}
		String URL = server.getBaseURL() + new Path(fileName).toString();
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getNewConfigurationName(fileName));

		// Set the debugger ID and the configuration delegate for this launch configuration
		String debuggerID = PHPProjectPreferences.getDefaultDebuggerID(project);
		wc.setAttribute(IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID, debuggerID);
		AbstractDebuggerConfiguration debuggerConfiguration = PHPDebuggersRegistry.getDebuggerConfiguration(debuggerID);
		wc.setAttribute(IPHPDebugCorePreferenceKeys.CONFIGURATION_DELEGATE_CLASS, debuggerConfiguration.getWebLaunchDelegateClass());

		wc.setAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_NAME, server.getName());
		wc.setAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_FILE_NAME, fileName);
		wc.setAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_BASE_URL, URL);
		wc.setAttribute(IPHPDebugConstants.RUN_WITH_DEBUG_INFO, PHPDebugEPLPlugin.getDebugInfoOption());
		wc.setAttribute(IPHPDebugConstants.OPEN_IN_BROWSER, PHPDebugEPLPlugin.getOpenInBrowserOption());
		wc.setAttribute(IDebugParametersKeys.FIRST_LINE_BREAKPOINT, PHPProjectPreferences.getStopAtFirstLine(project));

		// Display a dialog for selecting the URL.
		String title = ILaunchManager.DEBUG_MODE.equals(mode) ? "Debug PHP Web Page" : "Run PHP Web Page";
		PHPWebPageURLLaunchDialog launchDialog = new PHPWebPageURLLaunchDialog(wc, server, title);
		launchDialog.setBlockOnOpen(true);
		if (launchDialog.open() == PHPWebPageURLLaunchDialog.OK) {
			// Save the user-given URL as part of the launch configuration.
			config = wc.doSave();
			return config;
		}
		return null;
	}

	/**
	 * Returns a name for a newly created launch configuration according to the given file name.
	 * In case the name generation fails, return the "New_configuration" string.
	 *
	 * @param fileName	The original file name that this shortcut shoul execute.
	 * @return The new configuration name, or "New_configuration" in case it fails for some reason.
	 */
	protected static String getNewConfigurationName(String fileName) {
		String configurationName = "New_configuration";
		try {
			IPath path = Path.fromOSString(fileName);
			String fileExtention = path.getFileExtension();
			String lastSegment = path.lastSegment();
			if (lastSegment != null) {
				if (fileExtention != null) {
					lastSegment = lastSegment.replaceFirst("." + fileExtention, "");
				}
				configurationName = lastSegment;
			}
		} catch (Exception e) {
			PHPDebugEPLPlugin.logError("Could not generate configuration name for " + fileName + ".\nThe default name will be used.", e);
		}
		return DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom(configurationName);
	}
}
