/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.launching;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org2.eclipse.php.debug.core.debugger.parameters.IDebugParametersInitializer;
import org2.eclipse.php.debug.core.debugger.parameters.IDebugParametersKeys;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.Logger;
import org2.eclipse.php.internal.debug.core.PHPDebugCoreMessages;
import org2.eclipse.php.internal.debug.core.daemon.DebugDaemon;
import org2.eclipse.php.internal.debug.core.interpreter.phpIni.PHPINIDebuggerUtil;
import org2.eclipse.php.internal.debug.core.interpreter.phpIni.PHPINIUtil;
import org2.eclipse.php.internal.debug.core.interpreter.preferences.PHPexeItem;
import org2.eclipse.php.internal.debug.core.preferences.PHPProjectPreferences;
import org2.eclipse.php.internal.debug.core.preferences.PHPexes;
import org2.eclipse.php.internal.debug.core.zend.communication.DebuggerCommunicationDaemon;
import org2.eclipse.php.internal.debug.core.zend.debugger.DebugParametersInitializersRegistry;
import org2.eclipse.php.internal.debug.core.zend.debugger.PHPExecutableDebuggerInitializer;
import org2.eclipse.php.internal.debug.core.zend.debugger.PHPSessionLaunchMapper;
import org2.eclipse.php.internal.debug.core.zend.debugger.ProcessCrashDetector;

import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;
import com.aptana.php.debug.core.launch.ScriptLocator;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

public class PHPExecutableLaunchDelegate extends LaunchConfigurationDelegate {
	/** Constant value indicating if the current platform is Windows */
	private static final boolean WINDOWS = java.io.File.separatorChar == '\\';
	public static final String SAVE_AUTOMATICALLY = "save_automatically";
	// private final static String UNTITLED_FOLDER_PATH = "Untitled_Documents";

	protected Map<String, String> envVariables = null;

	/**
	 * Override the extended getLaunch to create a PHPLaunch.
	 */
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return new PHPLaunch(configuration, mode, null);
	}

	public void debugPHPExecutable(ILaunch launch, String phpExe, String fileToDebug) throws DebugException {
		try {
			launch.setAttribute(IDebugParametersKeys.EXECUTABLE_LAUNCH, Boolean.toString(true));

			IDebugParametersInitializer parametersInitializer = DebugParametersInitializersRegistry.getBestMatchDebugParametersInitializer(launch);
			PHPExecutableDebuggerInitializer debuggerInitializer = new PHPExecutableDebuggerInitializer(launch);

			String phpExeString = new File(phpExe).getAbsolutePath();
			String fileName = new File(fileToDebug).getAbsolutePath();
			String query = PHPLaunchUtilities.generateQuery(launch, parametersInitializer);
			String iniFileLocation = launch.getAttribute(IDebugParametersKeys.PHP_INI_LOCATION);
			String workingDir = new File(fileToDebug).getParentFile().getAbsolutePath();

			debuggerInitializer.initializeDebug(phpExeString, fileName, workingDir, query, envVariables, iniFileLocation);

		} catch (java.io.IOException e1) {
			Logger.logException("PHPDebugTarget: Debugger didn't find file to debug.", e1);
			String errorMessage = PHPDebugCoreMessages.DebuggerFileNotFound_1;
			throw new DebugException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IPHPDebugConstants.INTERNAL_ERROR, errorMessage, e1));
		}
	}

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// Check that the debug daemon is functional
		// DEBUGGER - Make sure that the active debugger id is indeed Zend's debugger
		if (!DebugDaemon.getDefault().validateCommunicationDaemons(DebuggerCommunicationDaemon.ZEND_DEBUGGER_ID)) {
			monitor.setCanceled(true);
			monitor.done();
			return;
		}
		// Check for previous launches.
		if (!PHPLaunchUtilities.notifyPreviousLaunches(launch)) {
			monitor.setCanceled(true);
			monitor.done();
			return;
		}
		
		IProgressMonitor subMonitor; // the total of monitor is 100
		if (monitor.isCanceled()) {
			return;
		}
		
		// PHPLaunchUtilities.showDebugViews();
		
		String phpExeString = configuration.getAttribute(IPHPDebugConstants.ATTR_EXECUTABLE_LOCATION, (String) null);
		String phpIniPath = configuration.getAttribute(IPHPDebugConstants.ATTR_INI_LOCATION, (String) null);
		String projectName = configuration.getAttribute(IPHPDebugConstants.ATTR_WORKING_DIRECTORY, (String) null);
		// String fileName = configuration.getAttribute(IPHPDebugConstants.ATTR_FILE_FULL_PATH, (String) null);
		String fileName = ScriptLocator.getScriptFile(configuration);
		// SG: Aptana modification - Check if the script name is null due to external script or non-active script
		if (fileName == null || fileName.length() == 0) {
			displayErrorMessage("The script could not be launched. \nPlease make sure that the selected script exists in the workspace. \n" +
				"In case you have selected to debug/run the current script, make sure that a PHP script is opened in the editor area.");
			return;
		}
		boolean runWithDebugInfo = configuration.getAttribute(IPHPDebugConstants.RUN_WITH_DEBUG_INFO, true);
		
		IProject project = null;
		if (fileName == null){
			fileName = configuration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_FILE, (String) null);
		}
		if (fileName != null) { 
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(fileName);
			if (resource != null) {
				project = resource.getProject();
				fileName = resource.getLocation().toFile().getAbsolutePath(); // (Aptana) - Make sure that the file name is the full path
			}
		}

		if (monitor.isCanceled()) {
			return;
		}

		if (fileName == null || fileName.equals("")) {
			PHPDebugEPLPlugin.logError(
					"Please set a valid PHP executable for this launch.", 
					new Exception("\nCould not launch the debug/run session for the given PHP file. \nPlease set a valid PHP file for this launch. \nNote that files external, non project, files are not supported."));
			displayErrorMessage("Please set a valid PHP file for this launch.\nView the error log for more details.");
			return;
		}

		if (phpExeString == null) {
			displayErrorMessage("Please set a valid PHP executable for this launch.");
			return;
		}

		subMonitor = new SubProgressMonitor(monitor, 10); // 10 of 100

		// Locate the php.ini by using the attribute. If the attribute was null, try to locate an php.ini that exists next to the executable.
		File phpIni = (phpIniPath != null && new File(phpIniPath).exists()) ? new File(phpIniPath) : PHPINIUtil.findPHPIni(phpExeString);
		File tempIni = PHPINIDebuggerUtil.prepareBeforeDebug(phpIni, phpExeString, project, PHPexes.ZEND_DEBUGGER_ID);
		launch.setAttribute(IDebugParametersKeys.PHP_INI_LOCATION, tempIni.getAbsolutePath());

		if (mode.equals(ILaunchManager.DEBUG_MODE) || runWithDebugInfo == true) {
			boolean stopAtFirstLine = configuration.getAttribute(IDebugParametersKeys.FIRST_LINE_BREAKPOINT, PHPProjectPreferences.getStopAtFirstLine(project));
			int requestPort = PHPDebugEPLPlugin.getDebugPort(DebuggerCommunicationDaemon.ZEND_DEBUGGER_ID);

			ILaunchConfigurationWorkingCopy wc;
			if (configuration.isWorkingCopy()) {
				wc = (ILaunchConfigurationWorkingCopy) configuration;
			} else {
				wc = configuration.getWorkingCopy();
			}

			// Set Project Name
			if (project != null) {
				wc.setAttribute(IPHPDebugConstants.PHP_Project, project.getFullPath().toString());
			}

			// Set transfer encoding:
			wc.setAttribute(IDebugParametersKeys.TRANSFER_ENCODING, PHPProjectPreferences.getTransferEncoding(project));
			wc.setAttribute(IDebugParametersKeys.OUTPUT_ENCODING, PHPProjectPreferences.getOutputEncoding(project));
			wc.setAttribute(IDebugParametersKeys.PHP_DEBUG_TYPE, IDebugParametersKeys.PHP_EXE_SCRIPT_DEBUG);
			wc.doSave();

			if (monitor.isCanceled()) {
				return;
			}

			// Generate a session id for this launch and put it in the map
			int sessionID = DebugSessionIdGenerator.generateSessionID();
			PHPSessionLaunchMapper.put(sessionID, launch);

			// Define all needed debug attributes:
			launch.setAttribute(IDebugParametersKeys.PORT, Integer.toString(requestPort));
			launch.setAttribute(IDebugParametersKeys.FIRST_LINE_BREAKPOINT, Boolean.toString(stopAtFirstLine));
			launch.setAttribute(IDebugParametersKeys.SESSION_ID, Integer.toString(sessionID));

			// Trigger the debug session by initiating a debug requset to the php.exe
			debugPHPExecutable(launch, phpExeString, fileName);

		} else {
			// resolve location
			IPath phpExe = new Path(phpExeString);

			String[] envp = DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
			File phpExeFile = new File(phpExeString);
			String phpIniLocation = launch.getAttribute(IDebugParametersKeys.PHP_INI_LOCATION);

			// Determine PHP configuration file location:
			String phpConfigDir = phpExeFile.getParent();
			if (phpIniLocation != null && !phpIniLocation.equals("")) {
				phpConfigDir = new File(phpIniLocation).getParent();
			}

			// Detect PHP SAPI type:
			String sapiType = null;
			PHPexeItem[] items = PHPexes.getInstance().getAllItems();
			for (PHPexeItem item : items) {
				if (item.getExecutable().equals(phpExeFile)) {
					sapiType = item.getSapiType();
					break;
				}
			}

			String[] args = PHPLaunchUtilities.getProgramArguments(launch.getLaunchConfiguration());
			String[] cmdLine = PHPLaunchUtilities.getCommandLine(launch.getLaunchConfiguration(), phpExeString, phpConfigDir, fileName, sapiType == PHPexeItem.SAPI_CLI ? args : null);

			// Set library search path:
			if (!WINDOWS) {
				StringBuffer buf = new StringBuffer();
				if (Platform.OS_MACOSX.equals(Platform.getOS())) { 
					buf.append("DYLD_LIBRARY_PATH"); //$NON-NLS-1$
				} else {
					buf.append("LD_LIBRARY_PATH"); //$NON-NLS-1$
				}
				buf.append('=');
				buf.append(phpExeFile.getParent());
				String[] envpNew = new String[envp == null ? 1 : envp.length + 1];
				if (envp != null) {
					System.arraycopy(envp, 0, envpNew, 0, envp.length);
				}
				envpNew[envpNew.length - 1] = buf.toString();
				envp = envpNew;
			}

			if (monitor.isCanceled()) {
				return;
			}

			File workingDir = new File(fileName).getParentFile();
			Process p = workingDir.exists() ? DebugPlugin.exec(cmdLine, workingDir, envp) : DebugPlugin.exec(cmdLine, null, envp);

			// Attach a crash detector
			new Thread(new ProcessCrashDetector(p)).start();

			IProcess process = null;

			// add process type to process attributes
			Map<String, String> processAttributes = new HashMap<String, String>();
			String programName = phpExe.lastSegment();
			String extension = phpExe.getFileExtension();

			if (extension != null) {
				programName = programName.substring(0, programName.length() - (extension.length() + 1));
			}

			programName = programName.toLowerCase();
			processAttributes.put(IProcess.ATTR_PROCESS_TYPE, programName);

			if (p != null) {
				subMonitor = new SubProgressMonitor(monitor, 80); // 10+80 of 100;
				subMonitor.beginTask(MessageFormat.format("start launch", new Object[] { configuration.getName() }), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
				process = DebugPlugin.newProcess(launch, p, phpExe.toOSString(), processAttributes);
				if (process == null) {
					p.destroy();
					throw new CoreException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, 0, null, null));
				}
				subMonitor.done();
			}
			process.setAttribute(IProcess.ATTR_CMDLINE, fileName);

			if (CommonTab.isLaunchInBackground(configuration)) {
				// refresh resources after process finishes
				/*
				 if (RefreshTab.getRefreshScope(configuration) != null) {
				 BackgroundResourceRefresher refresher = new BackgroundResourceRefresher(configuration, process);
				 refresher.startBackgroundRefresh();
				 }
				 */
			} else {
				// wait for process to exit
				while (!process.isTerminated()) {
					try {
						if (monitor.isCanceled()) {
							process.terminate();
							break;
						}
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}

				// refresh resources
				subMonitor = new SubProgressMonitor(monitor, 10); // 10+80+10 of 100;
				RefreshTab.refreshResources(configuration, subMonitor);
			}
		}
	}

	private void displayErrorMessage(final String message) {
		final Display display = Display.getDefault();
		display.asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(display.getActiveShell(), PHPDebugCoreMessages.Debugger_LaunchError_title, message);
			}
		});
	}

	protected boolean saveBeforeLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		String filePath = configuration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_FILE, "");
		if ("".equals(filePath)) {
			return super.saveBeforeLaunch(configuration, mode, monitor);
		}
//		IPath path = Path.fromOSString(filePath);

		// TODO - check if bug isn't reopened due to the change 
		// find if the file is under UNTITLED_FOLDER_PATH always look like .../Untitled_Documents/filename.php
		// note that if segment count == 1, no need to check since there's no parent folder
		//		if ((path.segmentCount() > 1) && UNTITLED_FOLDER_PATH.equals(path.segment(path.segmentCount() - 2))) {
		//			// means this is untitled file no need for save before launch
		//			return true;
		//		}
		return super.saveBeforeLaunch(configuration, mode, monitor);
	}

}
