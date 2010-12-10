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
package org.eclipse.php.internal.debug.core.launching;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.php.debug.core.debugger.parameters.IDebugParametersKeys;
import org.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org.eclipse.php.internal.debug.core.Logger;
import org.eclipse.php.internal.debug.core.PHPDebugCoreMessages;
import org.eclipse.php.internal.debug.core.pathmapper.PathMapperRegistry;
import org.eclipse.php.internal.debug.core.preferences.PHPProjectPreferences;
import org.eclipse.php.internal.debug.core.xdebug.IDELayerFactory;
import org.eclipse.php.internal.debug.core.xdebug.XDebugPreferenceMgr;
import org.eclipse.php.internal.debug.core.xdebug.communication.XDebugCommunicationDaemon;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpBreakpointFacade;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpProxyHandler;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpMultiSessionTarget;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpTarget;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.model.IDBGpDebugTarget;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.session.DBGpSessionHandler;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.session.IDBGpSessionListener;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.aptana.debug.php.core.daemon.DebugDaemon;
import com.aptana.debug.php.core.launch.remote.RemoteDebugRedirector;
import com.aptana.debug.php.core.tunneling.SSHTunnel;
import com.aptana.debug.php.epl.PHPDebugEPLPlugin;

public class XDebugWebLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	public XDebugWebLaunchConfigurationDelegate() {
	}

	public void launch(final ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (!DebugDaemon.getDefault().validateCommunicationDaemons(XDebugCommunicationDaemon.XDEBUG_DEBUGGER_ID)) {
			monitor.setCanceled(true);
			monitor.done();
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			return;
		}
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			if (XDebugLaunchListener.getInstance().isWebLaunchActive()) {
				displayErrorMessage(PHPDebugCoreMessages.XDebug_WebLaunchConfigurationDelegate_0);
				DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
				return;
			}
		}
		// PHPLaunchUtilities.showDebugViews();
		// Resolve the Server
		PHPServerProxy server = PHPServersManager.getServer(configuration.getAttribute(PHPServerProxy.NAME, ""));//$NON-NLS-1$
		if (server == null) {
			Logger.log(Logger.ERROR, "Launch configuration could not find server (server name = " + configuration.getAttribute(PHPServerProxy.NAME, "") + ')');//$NON-NLS-1$ //$NON-NLS-2$
			displayErrorMessage(PHPDebugCoreMessages.XDebug_WebLaunchConfigurationDelegate_1);
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			return;
		}

		// Get the project from the file name
		// String fileName = configuration.getAttribute(PHPServerProxy.FILE_NAME, (String) null);
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
			if (!PHPLaunchUtilities.checkIsStandardPort(XDebugCommunicationDaemon.XDEBUG_DEBUGGER_ID))
			{
				DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
				return;
			}
		}
		
		// save the project name for source lookup
		ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
		String project = proj.getFullPath().toString();
		wc.setAttribute(IPHPDebugConstants.PHP_Project, project);
		wc.setAttribute(IDebugParametersKeys.TRANSFER_ENCODING, PHPProjectPreferences.getTransferEncoding(proj));
		wc.setAttribute(IDebugParametersKeys.OUTPUT_ENCODING, PHPProjectPreferences.getOutputEncoding(proj));		
		wc.doSave();

		// determine stop at first line (first calc the default and then try to extract the configuration attribute).
		boolean stopAtFirstLine = PHPProjectPreferences.getStopAtFirstLine(proj);
		stopAtFirstLine = wc.getAttribute(IDebugParametersKeys.FIRST_LINE_BREAKPOINT, stopAtFirstLine);

		// determine from eclipse config whether we use an internal browser, external browser or none
		final boolean useInternalBrowser = launch.getLaunchConfiguration().getAttribute(IPHPDebugConstants.USE_INTERNAL_BROWSER, false);
		String baseURL = new String(configuration.getAttribute(PHPServerProxy.BASE_URL, "").getBytes()); //$NON-NLS-1$
		if (baseURL.endsWith("/")) //$NON-NLS-1$
	    {
			baseURL = baseURL.substring(0, baseURL.length() - 1);
	    }
		final Exception[] exception = new Exception[1];
		final IWebBrowser[] browser = new IWebBrowser[1];
		if (useInternalBrowser)
		{
			String title = baseURL;
			try
			{
				URL url = new URL(title);
				title = "Debug - /" + url.getFile();
			}
			catch (MalformedURLException e)
			{// ignore
			}
			final String internalBrowserTitle = title;
			Display.getDefault().syncExec(new Runnable()
			{
				public void run()
				{
					try
					{
						browser[0] = PHPLaunchUtilities.openInternalBrowser(internalBrowserTitle);
						// Clear the all session data from the internal browsers. Note that this one clear everything! But it's needed for an accurate functionality when we execute a run
						// after a debug.
						Browser.clearSessions();
					}
					catch (PartInitException e)
					{
						Logger.logException("Error initializing the web browser.", e);//$NON-NLS-1$
						exception[0] = e;
					}
				}
			});
		}
		else
		{
			browser[0] = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
		}

		// Generate a session id for this launch and start the listener
		// then create the start and stop debug URLs
		String[] startStopURLs;
		
		//If we have a "selected script" option, URL is not ended by file and project name. 
		// SG: Aptana modification
		//		if (baseURL.endsWith("/")) //$NON-NLS-1$
		//		{
		//			// Check the Path Mapper to see if any conversions are needed for the URL.
		//			PathMapper pathMapper = PathMapperRegistry.getByServer(server);
		//			String name = filePath.toString();
		//			String remoteFile = pathMapper.getRemoteFile(name);
		//			if (remoteFile != null && !"".equals(name) && !remoteFile.equals(name)) //$NON-NLS-1$
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
		//				baseURL = baseURL.substring(0, baseURL.length() - 1) + remoteFile;
		//			}
		//			else
		//			{
		//				// now we need adding project name and file path to the end of the URL
		//				baseURL = baseURL.substring(0, baseURL.length() - 1) + filePath.toString();
		//			}
		//		}
		IDBGpDebugTarget target = null;
		SSHTunnel tunnel = null;
		
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			String sessionId = DBGpSessionHandler.getInstance().generateSessionId();			
			String ideKey = null;
			if (DBGpProxyHandler.instance.useProxy()) {
				ideKey = DBGpProxyHandler.instance.getCurrentIdeKey();
				if (DBGpProxyHandler.instance.registerWithProxy() == false) {
					displayErrorMessage(PHPDebugCoreMessages.XDebug_WebLaunchConfigurationDelegate_2 + DBGpProxyHandler.instance.getErrorMsg());
					DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
					return;					
				}
			}
			else {
				ideKey = DBGpSessionHandler.getInstance().getIDEKey();
			}			
			startStopURLs = generateStartStopDebugURLs(baseURL, sessionId, ideKey, configuration);
			// String launchScript = configuration.getAttribute(PHPServerProxy.FILE_NAME, (String) null);

			// Check if a tunneled connection is needed and create request for a tunnel if needed.
			tunnel = PHPLaunchUtilities.getSSHTunnel(configuration);
			
			// determine if we should use the multisession manager or the single session manager
			if (XDebugPreferenceMgr.useMultiSession() == true) {
				target = new DBGpMultiSessionTarget(launch, fileName, startStopURLs[1], ideKey, stopAtFirstLine, browser[0], sessionId);
				target.setPathMapper(PathMapperRegistry.getByServer(server));
				launch.addDebugTarget(target); //has to be added now, not later.
			}
			else {
				target = new DBGpTarget(launch, fileName, startStopURLs[1], ideKey, stopAtFirstLine, browser[0], sessionId);
				target.setPathMapper(PathMapperRegistry.getByServer(server));				
			}
			DBGpSessionHandler.getInstance().addSessionListener((IDBGpSessionListener)target);
		}
		else {
			startStopURLs = new String[] {baseURL, null};
		}
		final String startURL = startStopURLs[0];

		// load the URL into the appropriate web browser
		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 30);
		subMonitor.beginTask(PHPDebugCoreMessages.XDebug_WebLaunchConfigurationDelegate_3, 10);

		final SSHTunnel sshTunnel = tunnel;
		/*
		 * Start the debug session by opening an internal or an external browser.
		 * Unlike Zend Debugger, the xdebug seems to require a browser to initiate a session. So, we don't
		 * even check for the IPHPDebugConstants.OPEN_IN_BROWSER. We just open one.
		 */
		Display.getDefault().syncExec(new Runnable()
		{
			@SuppressWarnings("unchecked")
			public void run()
			{
				try
				{
					final List postData = configuration.getAttribute(IPHPDebugConstants.ATTR_HTTP_POST,
							Collections.EMPTY_LIST);
					// Has to be done from the UI thread since the Browser can be an internal one
					Display.getDefault().asyncExec(new Runnable()
					{
						public void run()
						{
							try
							{
								// establish a secured tunnel connection in case it's defined in the configuration dialog
								if (sshTunnel != null)
								{
									sshTunnel.connect();
								}
								
								// Check if we have POST data. If so, invoke the redirect through the
								// RemoteDebugConnectionProxy.
								if (postData != null && !postData.isEmpty())
								{
									new RemoteDebugRedirector().redirect(browser[0], new URL(startURL), postData);
								}
								else
								{
									// Normal invocation
									browser[0].openURL(new URL(startURL));
								}
							}
							catch (Exception e)
							{
								Logger.logException("Error initializing the web browser.", e);//$NON-NLS-1$
								exception[0] = e;
							}
						}
					});
				}
				catch (Exception t)
				{
					Logger.logException("Error initializing the web browser.", t);//$NON-NLS-1$
					exception[0] = t;
				}
			}
		});

		subMonitor.worked(10);

		// did the external browser start ok ?
		if (exception[0] == null) {
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				launch.addDebugTarget(target);
				subMonitor.subTask(PHPDebugCoreMessages.XDebug_WebLaunchConfigurationDelegate_4);
				target.waitForInitialSession((DBGpBreakpointFacade) IDELayerFactory.getIDELayer(), XDebugPreferenceMgr.createSessionPreferences(), monitor);
			}
			else {
				// launched ok, so remove the launch from the debug view as we are not debugging.
				DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			}
		} else {
			// display an error about not being able to launch a browser
			Logger.logException("we have an exception on the browser", exception[0]);//$NON-NLS-1$
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				DBGpSessionHandler.getInstance().removeSessionListener((IDBGpSessionListener)target);
			}
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
		}
		subMonitor.done();
	}

	/**
	 * Displays a dialog with an error message.
	 *
	 * @param message The error to display.
	 */
	protected void displayErrorMessage(final String message) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(Display.getDefault().getActiveShell(), PHPDebugCoreMessages.XDebugMessage_debugError, message);
			}
		});
	}

	/**
	 * generate the URLS that start the debug environment and stop the debug environment.
	 * @param baseURL the base URL
	 * @param sessionId the DBGp session Id
	 * @param ideKey the DBGp IDE Key
	 * @param configuration 
	 * @return start and stop queries
	 * @throws CoreException 
	 * @throws UnsupportedEncodingException 
	 */
	@SuppressWarnings("unchecked")
	public String[] generateStartStopDebugURLs(String baseURL, String sessionId, String ideKey,
			ILaunchConfiguration configuration) throws CoreException
	{
		String[] startStopURLs = new String[2];
		// SG: Aptana Mod - Append the GET parameters that were set through the launch configuration dialog
		StringBuilder baseUrlBuilder = new StringBuilder(baseURL);
		if (baseUrlBuilder.indexOf("?") > -1) {//$NON-NLS-1$
			baseUrlBuilder.append('&');
		}
		else
		{
			baseUrlBuilder.append('?');
		}
		// Add the launch HTTP GET parameters before the XDebug GET additions
		List<String> requestParameters = configuration.getAttribute(IPHPDebugConstants.ATTR_HTTP_GET,
				Collections.EMPTY_LIST);
		try
		{
			for (String pair : requestParameters)
			{
				NameValuePair nameValuePair = NameValuePair.fromPairString(pair);
				if (nameValuePair != null)
				{
					baseUrlBuilder.append(URLEncoder.encode(nameValuePair.name, IPHPDebugConstants.URL_ENCODING))
							.append('=')
							.append(URLEncoder.encode(nameValuePair.value, IPHPDebugConstants.URL_ENCODING));
					baseUrlBuilder.append('&');
				}
			}
		}
		catch (UnsupportedEncodingException e)
		{
			throw new CoreException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		baseURL = baseUrlBuilder.toString();
		// SG: Aptana Mod - Append the session id to the IDE key, so it will be maintained in the xdebug cookie
		startStopURLs[0] = baseURL + "XDEBUG_SESSION_START=" + ideKey + "_" + sessionId;//$NON-NLS-1$ //$NON-NLS-2$ 
		startStopURLs[1] = baseURL + "XDEBUG_SESSION_STOP_NO_EXEC=" + ideKey + "_" + sessionId;//$NON-NLS-1$ //$NON-NLS-2$
		return startStopURLs;
	}
}
