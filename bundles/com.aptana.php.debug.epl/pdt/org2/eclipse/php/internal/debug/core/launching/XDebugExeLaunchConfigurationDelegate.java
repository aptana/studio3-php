/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.launching;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org2.eclipse.php.debug.core.debugger.parameters.IDebugParametersKeys;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.Logger;
import org2.eclipse.php.internal.debug.core.PHPDebugCoreMessages;
import org2.eclipse.php.internal.debug.core.interpreter.phpIni.PHPINIDebuggerUtil;
import org2.eclipse.php.internal.debug.core.interpreter.phpIni.PHPINIUtil;
import org2.eclipse.php.internal.debug.core.interpreter.preferences.PHPexeItem;
import org2.eclipse.php.internal.debug.core.pathmapper.PathMapperRegistry;
import org2.eclipse.php.internal.debug.core.preferences.PHPProjectPreferences;
import org2.eclipse.php.internal.debug.core.preferences.PHPexes;
import org2.eclipse.php.internal.debug.core.xdebug.IDELayerFactory;
import org2.eclipse.php.internal.debug.core.xdebug.XDebugPreferenceMgr;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpBreakpointFacade;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpPreferences;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpProxyHandler;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.XDebugDebuggerConfiguration;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpTarget;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.session.DBGpSessionHandler;
import org2.eclipse.php.internal.debug.core.zend.debugger.ProcessCrashDetector;

import com.aptana.php.debug.core.launch.ScriptLocator;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

public class XDebugExeLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	public static final String INTERPRETERS_PREF_ID = "org2.eclipse.php.debug.ui.preferencesphps.PHPsPreferencePage"; //$NON-NLS-1$


	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor.isCanceled()) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			return;
		}

		// PHPLaunchUtilities.showDebugViews();

		String phpExeString = configuration.getAttribute(IPHPDebugConstants.ATTR_EXECUTABLE_LOCATION, (String) null);
		String phpIniString = configuration.getAttribute(IPHPDebugConstants.ATTR_INI_LOCATION, (String) null);
		// get the exe, project name, file name
		// final String phpExeString = ScriptLocator.getScriptFile(configuration);
		// final String phpIniPath = configuration.getAttribute(PHPCoreConstants.ATTR_INI_LOCATION, (String) null);
		if (!isXDebugFunctional(phpIniString, configuration))
		{
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			return;
		}
		final String phpScriptString = ScriptLocator.getScriptFile(configuration);
		// SG: Aptana modification - Check if the script name is null due to external script or non-active script
		if (phpScriptString == null || phpScriptString.trim().length() == 0) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			displayErrorMessage("The script could not be launched. \nPlease make sure that the selected script exists in the workspace. \n" +
					"In case you have selected to debug/run the current script, make sure that a PHP script is opened in the editor area.");
			return;
		}
		if (monitor.isCanceled()) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			return;
		}

		// locate the project from the php script
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		final IPath filePath = new Path(phpScriptString);
		final IResource scriptRes = workspaceRoot.findMember(filePath);
		if (scriptRes == null) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			displayErrorMessage(PHPDebugCoreMessages.XDebug_ExeLaunchConfigurationDelegate_1);
			return;
		}

		// resolve php exe location
		final IPath phpExe = new Path(phpExeString);

		// resolve project directory
		IProject project = scriptRes.getProject();

		// Set Project Name as this is required by the source lookup computer delegate
		final String projectString = project.getFullPath().toString();
		ILaunchConfigurationWorkingCopy wc = null;
		if (configuration.isWorkingCopy()) {
			wc = (ILaunchConfigurationWorkingCopy) configuration;
		} else {
			wc = configuration.getWorkingCopy();
		}
		wc.setAttribute(IPHPDebugConstants.PHP_Project, projectString);
		wc.setAttribute(IDebugParametersKeys.TRANSFER_ENCODING, PHPProjectPreferences.getTransferEncoding(project));
		wc.setAttribute(IDebugParametersKeys.OUTPUT_ENCODING, PHPProjectPreferences.getOutputEncoding(project));
		wc.doSave();

		if (monitor.isCanceled()) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			return;
		}

		IPath projectLocation = project.getRawLocation();
		if (projectLocation == null) {
			projectLocation = project.getLocation();
		}
//		final String location = projectLocation.toOSString(); // TODO - SG: Test
//		final IPath projectPath = new Path(location);
//		final File projectDir = projectPath.toFile();
//
//		// resolve the php script (remove the project directory)
//		IPath phpFile = new Path(phpScriptString);
//		if (phpScriptString.startsWith("/")) {
//			phpFile = phpFile.removeFirstSegments(1);
//		}

		// resolve the script location, but not relative to anything
		IPath phpFile = scriptRes.getLocation();

		if (monitor.isCanceled()) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			return;
		}

		// Resolve the PHP ini location
		// Locate the php ini by using the attribute. If the attribute was null, try to locate an ini that exists next to the executable.
		File phpIni = (phpIniString != null && new File(phpIniString).exists()) ? new File(phpIniString) : PHPINIUtil.findPHPIni(phpExeString);
		File tempIni = PHPINIDebuggerUtil.prepareBeforeDebug(phpIni, phpExeString, project, PHPexes.XDEBUG_DEBUGGER_ID);
		launch.setAttribute(IDebugParametersKeys.PHP_INI_LOCATION, tempIni.getAbsolutePath());

		wc.doSave();


		// add process type to process attributes, basically the name of the exe that was launched
		final Map<String, String> processAttributes = new HashMap<String, String>();
		String programName = phpExe.lastSegment();
		final String extension = phpExe.getFileExtension();
		if (extension != null) {
			programName = programName.substring(0, programName.length() - (extension.length() + 1));
		}
		programName = programName.toLowerCase();

		// used by the console colorer extension to determine what class to use
		// should allow the console color providers and line trackers to work
		//process.setAttribute(IProcess.ATTR_PROCESS_TYPE, IPHPConstants.PHPProcessType);

		processAttributes.put(IProcess.ATTR_PROCESS_TYPE, programName);
		// used by the Console to give that console a name
		processAttributes.put(IProcess.ATTR_CMDLINE, phpScriptString);

		if (monitor.isCanceled()) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			return;
		}

		// determine the environment variables
		String[] envVarString = null;
		DBGpTarget target = null;
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			// check the launch for stop at first line, if not there go to project specifics
			boolean stopAtFirstLine = PHPProjectPreferences.getStopAtFirstLine(project);
			stopAtFirstLine = configuration.getAttribute(IDebugParametersKeys.FIRST_LINE_BREAKPOINT, stopAtFirstLine);
			String sessionID = DBGpSessionHandler.getInstance().generateSessionId();
			String ideKey = null;
			if (DBGpProxyHandler.instance.useProxy()) {
				ideKey = DBGpProxyHandler.instance.getCurrentIdeKey();
				if (DBGpProxyHandler.instance.registerWithProxy() == false) {
					displayErrorMessage(PHPDebugCoreMessages.XDebug_ExeLaunchConfigurationDelegate_2 + DBGpProxyHandler.instance.getErrorMsg());
					DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
					return;
				}
			}
			else {
				ideKey = DBGpSessionHandler.getInstance().getIDEKey();
			}
			target = new DBGpTarget(launch, phpFile.lastSegment(), ideKey, sessionID, stopAtFirstLine);
			target.setPathMapper(PathMapperRegistry.getByLaunchConfiguration(configuration));
			DBGpSessionHandler.getInstance().addSessionListener(target);
			envVarString = createDebugLaunchEnvironment(configuration, sessionID, ideKey, phpExe);
		}
		else {
			envVarString = PHPLaunchUtilities.getEnvironment(configuration, new String[] {getLibraryPath(phpExe)});
		}

		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 30);
		subMonitor.beginTask(PHPDebugCoreMessages.XDebug_ExeLaunchConfigurationDelegate_3, 10);

		//determine the working directory. default is the location of the script
		IPath workingPath = phpFile.removeLastSegments(1);
		File workingDir = workingPath.makeAbsolute().toFile();

		boolean found = false;
		for (int i = 0; i < envVarString.length && !found; i++) {
			String envEntity = envVarString[i];
			String[] elements = envEntity.split("=");//$NON-NLS-1$
			if (elements.length > 0 && elements[0].equals("XDEBUG_WORKING_DIR")) {//$NON-NLS-1$
				found = true;
				workingPath = new Path(elements[1]);
				File temp = workingPath.makeAbsolute().toFile();
				if (temp.exists()) {
					workingDir = temp;
				}
			}
		}

		// Detect PHP SAPI type and thus where we need arguments
		File phpExeFile = new File(phpExeString);
		String sapiType = null;
		PHPexeItem[] items = PHPexes.getInstance().getAllItems();
		for (PHPexeItem item : items) {
			if (item.getExecutable().equals(phpExeFile)) {
				sapiType = item.getSapiType();
				break;
			}
		}
		// SG: Aptana modification - No matter if we are on CGI or CLI, we should pass any argument.
		// In CGI it will be inserted to the $_GET variable, while in CLI it will be available in the $argv array
		// Resolve PHP-686
		String[] args = PHPLaunchUtilities.getProgramArguments(launch.getLaunchConfiguration());

		// TODO - SG: Check that we handle it well. Might need to use createCommandLine
		//define the command line for launching
		String[] cmdLine = PHPLaunchUtilities.getCommandLine(configuration, phpExe.toOSString(), tempIni.toString(), phpFile.toOSString(), args);

		// Launch the process
		final Process phpExeProcess = DebugPlugin.exec(cmdLine, workingDir, envVarString);
		// Attach a crash detector
		new Thread(new ProcessCrashDetector(phpExeProcess)).start();

		IProcess eclipseProcessWrapper = null;
		if (phpExeProcess != null) {
			subMonitor.worked(10);
			eclipseProcessWrapper = DebugPlugin.newProcess(launch, phpExeProcess, phpExe.toOSString(), processAttributes);
			if (eclipseProcessWrapper == null) {

				// another error so we stop everything somehow
				phpExeProcess.destroy();
				subMonitor.done();
				DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
				throw new CoreException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, 0, null, null));
			}

			//if launching in debug mode, create the debug infrastructure and link it with the launched process
			if (mode.equals(ILaunchManager.DEBUG_MODE) && target != null) {
				target.setProcess(eclipseProcessWrapper);
				launch.addDebugTarget(target);
				subMonitor.subTask(PHPDebugCoreMessages.XDebug_ExeLaunchConfigurationDelegate_4);
				target.waitForInitialSession((DBGpBreakpointFacade) IDELayerFactory.getIDELayer(), XDebugPreferenceMgr.createSessionPreferences(), monitor);
			}

		} else {
			// we did not launch
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				DBGpSessionHandler.getInstance().removeSessionListener(target);
			}
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
		}
		subMonitor.done();
	}

	/**
	 * Make sure that the XDebug port that we listen to is equal to the port set in the XDebug ini configurations. In
	 * case it's not equal, prompt the user to edit the Debug settings or to edit the ini. Another option is just to
	 * continue regardless. Nothing will happen on this machine in that case.
	 * 
	 * @param phpIniPath
	 * @param configuration
	 * @return return
	 */
	protected boolean isXDebugFunctional(String phpIniPath, ILaunchConfiguration configuration)
	{
		Properties ini = new Properties();
		try
		{
			ini.load(new FileInputStream(phpIniPath));
		}
		catch (IOException e)
		{
			PHPDebugEPLPlugin.logError("Error locating the XDebug debugger ini", e);
			ErrorDialog
					.openError(
							null,
							"Debugger Error",
							"Could not locate the XDebug ini in the specified location. \nView error log for more information.",
							new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, 0, "Error locating the ini", e));
			return false;
		}
		String xdebugIniPort = ini.getProperty("xdebug.remote_port", "null");
		XDebugDebuggerConfiguration xdebuggerConfigurations = new XDebugDebuggerConfiguration();
		if (!xdebugIniPort.equals(String.valueOf(xdebuggerConfigurations.getPort())))
		{
			// Display a user dialog
			String message = null;
			boolean hasPortSetting = false;
			if (!xdebugIniPort.equals("null"))
			{
				message = "The current configuration defined in the XDebug php.ini set the 'xdebug.remote_port' to "
						+ xdebugIniPort + ", while the Aptana debugger client is set to listen on port "
						+ xdebuggerConfigurations.getPort() + '.';
				hasPortSetting = true;
			}
			else
			{
				// In case there is no setting in the ini, but we listen on port 9000, there is no need to prompt and
				// the internal debugger
				// will work with the default port (9000).
				if (xdebuggerConfigurations.getPort() == DBGpPreferences.DBGP_PORT_DEFAULT)
				{
					return true;
				}
				message = "The current configuration defined in the XDebug php.ini does not have a setting for 'xdebug.remote_port'.\n"
						+ "You will need to set it to port "
						+ xdebuggerConfigurations.getPort()
						+ ", or change the Aptana client port to 9000 (XDebug default) before continuing.";
			}
			message += "\n\nWhat would you like to do?";
			return showPortWarningDialog(message, configuration);
		}
		return true;
	}

	/*
	 * Display the message dialog.
	 */
	private boolean showPortWarningDialog(final String message, final ILaunchConfiguration configuration)
	{
		final String[] buttonsLabels = new String[] { "Change Port", "Edit ini", "Ignore" };
		final boolean[] result = new boolean[1];
		result[0] = true;
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				MessageDialog dialog = new MessageDialog(null, "XDebug port verification", null, message,
						MessageDialog.WARNING, buttonsLabels, 0)
				{
					protected void buttonPressed(int buttonId)
					{
						try
						{
							if (buttonId == 0)
							{
								// Set the return code to Cancel
								okPressed();
								openDebuggerConfigurationDialog(configuration);
							}
							else if (buttonId == 1)
							{
								// Open the interpreters preferences to edit the ini
								okPressed();
								openInterpretersPage(configuration);
							}
							else
							{
								// Ignore button was selected, so set the return code should be set to OK
								cancelPressed();
							}
						}
						catch (CoreException e)
						{
							PHPDebugEPLPlugin.logError("Error while showing the xdebug warning dialog", e);
						}
					}
				};

				if (dialog.open() == MessageDialog.OK)
				{
					result[0] = false; // Ignore
				}
			}
		});
		return result[0];
	}

	// // Open the interpreters preferences to edit the ini
	private void openInterpretersPage(ILaunchConfiguration configuration) throws CoreException
	{
		PHPexes exes = PHPexes.getInstance();

		String phpExeString = configuration.getAttribute(IPHPDebugConstants.ATTR_EXECUTABLE_LOCATION, (String) null);
		String phpIniPath = configuration.getAttribute(IPHPDebugConstants.ATTR_INI_LOCATION, (String) null);

		final PHPexeItem pexeItem = exes.getItemForFile(phpExeString, phpIniPath);
		if (pexeItem == null)
		{
			PHPDebugEPLPlugin.logError("Error editing the ini for " + phpExeString + " ---> " + phpIniPath
					+ ". \nPHPexeItem is null.");
			return;
		}

		PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(null, INTERPRETERS_PREF_ID, null, pexeItem);
		if (pref != null)
		{
			pref.open();
		}
	}

	private void openDebuggerConfigurationDialog(ILaunchConfiguration configuration)
	{
		XDebugDebuggerConfiguration xdebuggerConfigurations = new XDebugDebuggerConfiguration();
		xdebuggerConfigurations.openConfigurationDialog(null);
	}

	/**
	 * create any environment variables that may be required
	 * 
	 * @param configuration launch configuration
	 * @param sessionID the DBGp Session Id
	 * @param ideKey the DBGp ide key
	 * @return string array containing the environment
	 * @throws CoreException rethrown exception
	 */
	public String[] createDebugLaunchEnvironment(ILaunchConfiguration configuration, String sessionID, String ideKey, IPath phpExe) throws CoreException {
		// create XDebug required environment variables, need the
		// session handler to start listening and generate a session id

		String configEnv = "XDEBUG_CONFIG=remote_enable=1 idekey=" + ideKey;
		String extraDBGpEnv = "DBGP_IDEKEY=" + ideKey;
		String sessEnv = "DBGP_COOKIE=" + sessionID;

		Logger.debugMSG("env=" + configEnv + ", Cookie=" + sessEnv);

		String[] envVarString = PHPLaunchUtilities.getEnvironment(configuration, new String[] { configEnv, extraDBGpEnv, sessEnv, getLibraryPath(phpExe) });
		return envVarString;
	}


	/**
	 * @param configuration the launch configuration
	 * @param phpConfigDir ini directory location (probably never used)
	 * @param exeName the name of the executable
	 * @param scriptName name of script relative to working directory
	 * @return the command line to be invoked.
	 * @throws CoreException rethrown exception
	 */
	public String[] createCommandLine(ILaunchConfiguration configuration, String phpConfigDir, String exeName, String scriptName) throws CoreException {
		String phpIniLocation = configuration.getAttribute(IDebugParametersKeys.PHP_INI_LOCATION, "");
		if (!"".equals(phpIniLocation)) {
			phpConfigDir = new File(phpIniLocation).getParent();
		}
		return PHPLaunchUtilities.getCommandLine(configuration, exeName, phpConfigDir, scriptName, null);
	}

	private String getLibraryPath(IPath exePath) {
		//TODO: Should append if already present
		StringBuffer buf = new StringBuffer();
		if (Platform.OS_MACOSX.equals(Platform.getOS())) { //$NON-NLS-1$
			buf.append("DYLD_LIBRARY_PATH"); //$NON-NLS-1$
		} else {
			buf.append("LD_LIBRARY_PATH"); //$NON-NLS-1$
		}
		buf.append('=');
		exePath = exePath.removeLastSegments(1);
		buf.append(exePath.toOSString());
		return buf.toString();
	}

	/**
	 * Displays a dialog with an error message.
	 *
	 * @param message The error to display.
	 */
	protected void displayErrorMessage(final String message) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Debug Error", message);
			}
		});
	}
}
