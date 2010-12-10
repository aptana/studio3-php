/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.php.internal.debug.core.launching;

import java.text.MessageFormat;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.php.debug.core.debugger.parameters.IDebugParametersKeys;
import org.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org.eclipse.php.internal.debug.core.Logger;
import org.eclipse.php.internal.debug.core.PHPDebugCoreMessages;
import org.eclipse.php.internal.debug.core.preferences.PHPProjectPreferences;
import org.eclipse.php.internal.debug.core.zend.communication.DebuggerCommunicationDaemon;
import org.eclipse.php.internal.debug.core.zend.debugger.IDebuggerInitializer;
import org.eclipse.php.internal.debug.core.zend.debugger.PHPSessionLaunchMapper;
import org.eclipse.php.internal.debug.core.zend.debugger.PHPWebServerDebuggerInitializer;
import org.eclipse.swt.widgets.Display;

import com.aptana.debug.php.core.daemon.DebugDaemon;
import com.aptana.debug.php.core.tunneling.SSHTunnel;
import com.aptana.debug.php.epl.PHPDebugEPLPlugin;

/**
 * A launch configuration delegate class for launching a PHP web page script.
 *
 * @author shalom
 *
 */
public class PHPWebPageLaunchDelegate extends LaunchConfigurationDelegate {

	protected Job runDispatch;
	protected ILaunch launch;
	protected IDebuggerInitializer debuggerInitializer;

	public PHPWebPageLaunchDelegate() {
		debuggerInitializer = createDebuggerInitilizer();
	}

	/**
	 * Override the extended getLaunch to create a PHPLaunch.
	 */
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return new PHPLaunch(configuration, mode, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.php.internal.server.core.launch.IHTTPServerLaunch#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// Check that the debug daemon is functional
		// DEBUGGER - Make sure that the active debugger id is indeed Zend's debugger
		if (!DebugDaemon.getDefault().validateCommunicationDaemons(DebuggerCommunicationDaemon.ZEND_DEBUGGER_ID)) {
			monitor.setCanceled(true);
			monitor.done();
			return;
		}
		// Check for previous launches
		if (!PHPLaunchUtilities.notifyPreviousLaunches(launch)) {
			monitor.setCanceled(true);
			monitor.done();
			return;
		}
		if (!PHPLaunchUtilities.checkDebugAllPages(configuration, launch)) {
			monitor.setCanceled(true);
			monitor.done();
			return;
		}
		// PHPLaunchUtilities.showDebugViews();
		this.launch = launch;
		PHPServerProxy server = PHPServersManager.getServer(configuration.getAttribute(PHPServerProxy.NAME, ""));
		if (server == null) {
			Logger.log(Logger.ERROR, "Launch configuration could not find server (server name = " + configuration.getAttribute(PHPServerProxy.NAME, "") + ')');
			displayErrorMessage("Could not launch the session.\nThe application could not find the server that was defined for this launch.\nPlease modify your launch settings through the 'Debug Configurations' dialog.");
			terminated();
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			// throw CoreException();
			return;
		}
		// String fileName = configuration.getAttribute(Server.FILE_NAME, (String) null);
		String fileName = ScriptLocator.getScriptFile(configuration, PHPServerProxy.FILE_NAME);
		if (fileName == null)
		{
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			boolean specificFileLaunch = configuration.getAttribute(IPHPDebugConstants.ATTR_USE_SPECIFIC_FILE, false);
			if (specificFileLaunch) {
				displayErrorMessage("Could not launch the session. \nMake sure that the selected script exists in your project");
			} else {
				displayErrorMessage("Could not launch the session. \nMake sure that a script is visible in your PHP editor");
			}
			return;
		}
		// Get the project from the file name
		IPath filePath = new Path(fileName);
		IProject proj = null;
		try {
			proj = ResourcesPlugin.getWorkspace().getRoot().getProject(filePath.segment(0));
		} catch (Throwable t) {
			if (proj == null) {
				Logger.logException("Could not execute the debug (Project is null).", t); //$NON-NLS-1$
				DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
				return;
			}
		}

		// SG: Aptana Mod - Check the standard debug port for this launch
		if (ILaunchManager.DEBUG_MODE.equals(mode)) 
		{
			if (!PHPLaunchUtilities.checkIsStandardPort(DebuggerCommunicationDaemon.ZEND_DEBUGGER_ID))
			{
				DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
				return;
			}
		}
		
		ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
		String project = proj.getFullPath().toString();
		wc.setAttribute(IPHPDebugConstants.PHP_Project, project);

		// Set transfer encoding:
		wc.setAttribute(IDebugParametersKeys.TRANSFER_ENCODING, PHPProjectPreferences.getTransferEncoding(proj));
		wc.setAttribute(IDebugParametersKeys.OUTPUT_ENCODING, PHPProjectPreferences.getOutputEncoding(proj));
		wc.setAttribute(IDebugParametersKeys.PHP_DEBUG_TYPE, IDebugParametersKeys.PHP_WEB_PAGE_DEBUG);
		wc.doSave();

		String URL = new String(configuration.getAttribute(PHPServerProxy.BASE_URL, "").getBytes());
		if (URL.endsWith("/")) //$NON-NLS-1$
	    {
			URL = URL.substring(0, URL.length() - 1);
	    }
		//If we have a "selected script" option, URL is not ended by file and project name. 
		// SG: Aptana modification
		//		if (URL.endsWith("/"))
		//		{
		//			// Check the Path Mapper to see if any conversions are needed for the URL.
		//			PathMapper pathMapper = PathMapperRegistry.getByServer(server);
		//			String name = filePath.toString();
		//			String remoteFile = pathMapper.getRemoteFile(name);
		//			if (remoteFile != null && !"".equals(name) && !remoteFile.equals(name))
		//			{
		//				// In case that remote file is  pointing to a local resource (can happen if the mapper 
		//				// set the path during the session, and did not replace any existing path setting), we notify 
		//				// that the mapped path will not be used until the path mapper is set to the right location.
		//				if (new File(remoteFile).exists())
		//				{
		//					final boolean[] result = new boolean[1];
		//					Display.getDefault().syncExec(new Runnable() {
		//						public void run() {
		//							String message = "The active path mapping for the selected server contains settings that \nprevents an accurate launch." +
		//							"\n\nPlease modify the path mapping settings for the selected server." +
		//							"\n\nWould you like to launch anyway?";
		//							result[0] = MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "PHP Launch", message);
		//						}
		//					});
		//					if (result[0]) {
		//						remoteFile = filePath.toString(); // In this case, act as if there is no mapping.
		//					} else {
		//						// terminate
		//						DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
		//						return;
		//					}
		//				}
		//				if (remoteFile.charAt(0) != '/')
		//				{
		//					remoteFile = '/' + remoteFile;
		//				}
		//				URL = URL.substring(0, URL.length() - 1) + remoteFile;
		//			}
		//			else
		//			{
		//				// now we need adding project name and file path to the end of the URL
		//				URL = URL.substring(0, URL.length() - 1) + filePath.toString();
		//			}
		//		}
		boolean isDebugLaunch = mode.equals(ILaunchManager.DEBUG_MODE);
		if (isDebugLaunch) {
			boolean stopAtFirstLine = wc.getAttribute(IDebugParametersKeys.FIRST_LINE_BREAKPOINT, PHPProjectPreferences.getStopAtFirstLine(proj));
			launch.setAttribute(IDebugParametersKeys.FIRST_LINE_BREAKPOINT, Boolean.toString(stopAtFirstLine));
		}
		int requestPort = PHPDebugEPLPlugin.getDebugPort(DebuggerCommunicationDaemon.ZEND_DEBUGGER_ID);

		// Generate a session id for this launch and put it in the map
		int sessionID = DebugSessionIdGenerator.generateSessionID();
		PHPSessionLaunchMapper.put(sessionID, launch);

		// Fill all rest of the attributes:
		launch.setAttribute(IDebugParametersKeys.PORT, Integer.toString(requestPort));
		launch.setAttribute(IDebugParametersKeys.WEB_SERVER_DEBUGGER, Boolean.toString(true));
		launch.setAttribute(IDebugParametersKeys.ORIGINAL_URL, URL);
		launch.setAttribute(IDebugParametersKeys.SESSION_ID, Integer.toString(sessionID));

		// Trigger the session by initiating a debug request to the debug server
		runDispatch = new RunDispatchJobWebServer(launch);
		runDispatch.schedule();
	}

	/*
	 * Override the super preLaunchCheck to make sure that the server we are using is still valid.
	 * If not, notify the user that a change should be made and open the launch configuration page to do so.
	 *
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#preLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean preLaunchCheck(final ILaunchConfiguration configuration, final String mode, IProgressMonitor monitor) throws CoreException {
		// Check if the server exists
		final String serverName = configuration.getAttribute(PHPServerProxy.NAME, "");
		PHPServerProxy server = PHPServersManager.getServer(serverName);
		if (server == null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openWarning(Display.getDefault().getActiveShell(), PHPDebugCoreMessages.PHPLaunchUtilities_phpLaunchTitle, MessageFormat.format(PHPDebugCoreMessages.PHPWebPageLaunchDelegate_serverNotFound, new String[] { serverName }));
					PHPLaunchUtilities.openLaunchConfigurationDialog(configuration, mode);
				}
			});
			return false;
		}

		return super.preLaunchCheck(configuration, mode, monitor);
	}

	/**
	 * Initiate a debug session.
	 *
	 * @param launch
	 */
	protected void initiateDebug(ILaunch launch) {
		try {
			// Initiate a debug tunnel in case needed.
			if (!ILaunchManager.RUN_MODE.equals(launch.getLaunchMode()))
			{
				SSHTunnel tunnel = PHPLaunchUtilities.getSSHTunnel(launch.getLaunchConfiguration());
				if (tunnel != null)
				{
					tunnel.connect();
				}
			}
			debuggerInitializer.debug(launch);
		} catch (DebugException e) {
			IStatus status = e.getStatus();
			String errorMessage = null;
			if (status == null) {
				Logger.traceException("Unexpected Error return from debuggerInitializer ", e);
				fireError(PHPDebugCoreMessages.Debugger_Unexpected_Error_1, e);
				errorMessage = PHPDebugCoreMessages.Debugger_Unexpected_Error_1;
			} else {
				fireError(status);
				errorMessage = status.getMessage();
			}
			displayErrorMessage(errorMessage);
		}
	}

	/**
	 * Create an {@link IDebuggerInitializer}.
	 *
	 * @return An {@link IDebuggerInitializer} instance.
	 */
	protected IDebuggerInitializer createDebuggerInitilizer() {
		return new PHPWebServerDebuggerInitializer();
	}

	/**
	 * Displays a dialod with an error message.
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

	/**
	 * Throws a IStatus in a Debug Event
	 *
	 */
	public void fireError(IStatus status) {
		DebugEvent event = new DebugEvent(this, DebugEvent.MODEL_SPECIFIC);
		event.setData(status);
		fireEvent(event);
	}

	/**
	 * Throws a IStatus in a Debug Event
	 *
	 */
	public void fireError(String errorMessage, Exception e) {
		Status status = new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IPHPDebugConstants.INTERNAL_ERROR, errorMessage, e);
		DebugEvent event = new DebugEvent(this, DebugEvent.MODEL_SPECIFIC);
		event.setData(status);
		fireEvent(event);
	}

	/**
	 * Called when the debug session was terminated.
	 */
	public void terminated() {
		DebugEvent event = null;
		if (launch.getDebugTarget() == null) {
			// We have to force the termination of the ILaunch because at this stage there is no
			// PHPDebugTarget, thus we create a dummy debug target to overcome this issue and terminate the launch.
			IDebugTarget dummyDebugTarget = new DummyDebugTarget(launch);
			event = new DebugEvent(dummyDebugTarget, DebugEvent.TERMINATE);
			if (launch != null) {
				launch.addDebugTarget(dummyDebugTarget);
				IDebugEventSetListener launchListener = (IDebugEventSetListener) launch;
				launchListener.handleDebugEvents(new DebugEvent[] { event });
			}
		}
		event = new DebugEvent(this, DebugEvent.TERMINATE);
		fireEvent(event);
	}

	/**
	 * Fires a debug event
	 *
	 * @param event 	The event to be fired
	 */
	public void fireEvent(DebugEvent event) {
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { event });
	}

	/*
	 * Run is seperate thread so launch doesn't hang.
	 */
	protected class RunDispatchJobWebServer extends Job {
		private ILaunch launch;

		public RunDispatchJobWebServer(ILaunch launch) {
			super("RunDispatchJobWebServer");
			this.launch = launch;
			setSystem(true);
		}

		protected IStatus run(IProgressMonitor monitor) {
			initiateDebug(launch);
			Logger.debugMSG("Terminating debug session: calling PHPDebugTarget.terminate()");
			terminated();
			return Status.OK_STATUS;
		}
	}

	private static IThread[] EMPTY_THREADS = new IThread[0];

	/*
	 * A dummy debug target for the termination of the ILaunch.
	 */
	private class DummyDebugTarget implements IDebugTarget {

		private ILaunch launch;

		public DummyDebugTarget(ILaunch launch) {
			this.launch = launch;
		}

		public String getName() throws DebugException {
			return "Session Terminated";
		}

		public IProcess getProcess() {
			return null;
		}

		public IThread[] getThreads() throws DebugException {
			return EMPTY_THREADS;
		}

		public boolean hasThreads() throws DebugException {
			return false;
		}

		public boolean supportsBreakpoint(IBreakpoint breakpoint) {
			return false;
		}

		public IDebugTarget getDebugTarget() {
			return this;
		}

		public ILaunch getLaunch() {
			return launch;
		}

		public String getModelIdentifier() {
			return "";
		}

		public Object getAdapter(Class adapter) {
			return null;
		}

		public boolean canTerminate() {
			return false;
		}

		public boolean isTerminated() {
			return true;
		}

		public void terminate() throws DebugException {
		}

		public boolean canResume() {
			return false;
		}

		public boolean canSuspend() {
			return false;
		}

		public boolean isSuspended() {
			return false;
		}

		public void resume() throws DebugException {
		}

		public void suspend() throws DebugException {
		}

		public void breakpointAdded(IBreakpoint breakpoint) {
		}

		public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		}

		public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		}

		public boolean canDisconnect() {
			return false;
		}

		public void disconnect() throws DebugException {
		}

		public boolean isDisconnected() {
			return false;
		}

		public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
			return null;
		}

		public boolean supportsStorageRetrieval() {
			return false;
		}
	}
}
