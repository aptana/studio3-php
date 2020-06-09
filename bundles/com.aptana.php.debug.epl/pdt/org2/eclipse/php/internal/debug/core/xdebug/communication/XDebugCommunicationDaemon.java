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
/**
 * 
 */
package org2.eclipse.php.internal.debug.core.xdebug.communication;

import java.net.Socket;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org2.eclipse.php.debug.core.debugger.pathmapper.PathMapper;
import org2.eclipse.php.internal.debug.core.PHPDebugCoreMessages;
import org2.eclipse.php.internal.debug.core.daemon.AbstractDebuggerCommunicationDaemon;
import org2.eclipse.php.internal.debug.core.launching.PHPLaunchUtilities;
import org2.eclipse.php.internal.debug.core.pathmapper.PathMapperRegistry;
import org2.eclipse.php.internal.debug.core.sourcelookup.PHPSourceLookupDirector;
import org2.eclipse.php.internal.debug.core.xdebug.IDELayerFactory;
import org2.eclipse.php.internal.debug.core.xdebug.XDebugPreferenceMgr;
import org2.eclipse.php.internal.debug.core.xdebug.XDebugPreferenceMgr.AcceptRemoteSession;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpBreakpointFacade;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpLogger;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpMultiSessionTarget;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpTarget;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.session.DBGpSession;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.session.DBGpSessionHandler;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.session.IDBGpSessionListener;

import com.aptana.php.debug.core.server.PHPServersManager;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;
import com.aptana.webserver.core.IServer;

/**
 * XDebug communication daemon.
 * 
 * @author Shalom Gibly
 * @since PDT 1.0
 */
public class XDebugCommunicationDaemon extends AbstractDebuggerCommunicationDaemon
{

	public static final String XDEBUG_DEBUGGER_ID = "org2.eclipse.php.debug.core.xdebugDebugger"; //$NON-NLS-1$
	public static final int[] DEBUGGER_DEFAULT_PORTS = new int[] { 9000 };
	private PortChangeListener portChangeListener;

	/**
	 * An XDebug communication daemon.
	 */
	public XDebugCommunicationDaemon()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.daemon.AbstractDebuggerCommunicationDaemon#init()
	 */
	public void init()
	{
		initDeamonChangeListener();
		super.init();
	}

	/**
	 * Initialize a daemon change listener
	 */
	protected void initDeamonChangeListener()
	{
		if (portChangeListener == null)
		{
			Preferences preferences = PHPDebugEPLPlugin.getDefault().getPluginPreferences();
			portChangeListener = new PortChangeListener();
			preferences.addPropertyChangeListener(portChangeListener);
		}
	}

	/**
	 * Returns the server socket port used for the debug requests listening thread.
	 * 
	 * @return The port specified in the preferences.
	 */
	public int getReceiverPort()
	{
		return PHPDebugEPLPlugin.getDebugPort(XDEBUG_DEBUGGER_ID);
	}

	/**
	 * Returns the XDebug debugger ID.
	 * 
	 * @return The debugger ID that is using this daemon (e.g. XDebug).
	 * @since PDT 1.0
	 */
	public String getDebuggerID()
	{
		return XDEBUG_DEBUGGER_ID;
	}

	/**
	 * Returns if this daemon is a debugger daemon. In this case, always return true.
	 */
	public boolean isDebuggerDaemon()
	{
		return true;
	}

	/**
	 * Returns true if the given port is defined as one of the default ports for this debugger daemon.
	 * 
	 * @param port
	 *            A port to check
	 * @return True, iff the port matches one of the default ports.
	 * @since Aptana PHP 1.1
	 */
	public static boolean isDefaultDebugPort(int port)
	{
		for (int i : DEBUGGER_DEFAULT_PORTS)
		{
			if (i == port)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Starts a connection handling thread on the given Socket.
	 * 
	 * @param socket
	 */
	protected void startConnectionThread(Socket socket)
	{
		// a socket has been accepted by the listener. This runs on the listener
		// thread so we should make damn sure we don't throw an exception here
		// otherwise it will abort that thread.
		if (DBGpLogger.debugSession())
		{
			DBGpLogger.debug("Connection established: " + socket.toString());
		}

		try
		{
			DBGpSession session = new DBGpSession(socket);
			if (session.isActive())
			{
				if (!DBGpSessionHandler.getInstance().fireSessionAdded(session))
				{
					// Session not taken, we want to create a launch
					AcceptRemoteSession aSess = XDebugPreferenceMgr.getAcceptRemoteSession();
					if (aSess != AcceptRemoteSession.off)
					{
						// Aptana Mod - SG: Also check that the PHPDebugEPLPlugin.getDebugHosts() does not contain the
						// remote address before ignoring.
						if (aSess == AcceptRemoteSession.localhost
								&& session.getRemoteAddress().isLoopbackAddress() == false
								&& !PHPDebugEPLPlugin.getDebugHosts().contains(session.getRemoteHostname()))
						{
							session.endSession();
						}
						else if (aSess == AcceptRemoteSession.prompt)
						{
							PromptUser prompt = new PromptUser(session);
							Display.getDefault().syncExec(prompt);
							if (prompt.isResult())
							{
								createLaunch(session);
							}
							else
							{
								session.endSession();
							}
						}
						else
						{
							// session was either localhost or from any outside one and
							// preferences allow it.
							createLaunch(session);
						}
					}
					else
					{
						// reject the session
						session.endSession();
					}
				}
			}
		}
		catch (Exception e)
		{
			PHPDebugEPLPlugin.logError("Unexpected Exception: Listener thread still listening", e);
			// DBGpLogger.logException("Unexpected Exception: Listener thread still listening", this, e);
		}
	}

	/**
	 * create a launch and appropriate debug targets to automate launch initiation. If any problems occurred, we can
	 * throw the session away using session.endSession();
	 * 
	 * @param session
	 *            the DBGpSession.
	 */
	private void createLaunch(DBGpSession session)
	{
		boolean stopAtFirstLine = true;
		DBGpTarget target = null;
		PathMapper mapper = null;
		PHPSourceLookupDirector srcLocator = new PHPSourceLookupDirector();
		srcLocator.initializeParticipants();
		// SG: Aptana Mod - Make sure that the launch is created in a way that eclipse 3.2
		// systems will not throw NPE on the LaunchView actions.
		// (This was an issue with 3.2 that was fixed for 3.3)
		ILaunchConfigurationType launchType = DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType("com.aptana.php.debug.epl.XDebugJitLaunchConfigurationType"); //$NON-NLS-1$
		ILaunchConfigurationWorkingCopy wc = null;
		if (launchType != null)
		{
			if (DebugUITools.getLaunchPerspective(launchType, ILaunchManager.DEBUG_MODE) == null)
			{
				// Set up the perspective for this kind of launch
				DebugUITools.setLaunchPerspective(launchType, ILaunchManager.DEBUG_MODE,
						"org.eclipse.debug.ui.DebugPerspective"); //$NON-NLS-1$
			}
			try
			{
				wc = launchType.newInstance(null, "XDebug JIT Session");
				wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
			}
			catch (CoreException e)
			{
				wc = null;
			}
		}

		ILaunch remoteLaunch = new Launch(wc, ILaunchManager.DEBUG_MODE, srcLocator);
		boolean multiSession = XDebugPreferenceMgr.useMultiSession();

		if (session.getSessionId() == null && !multiSession)
		{
			// non multisession web launch
			stopAtFirstLine = PHPLaunchUtilities.shouldBreakOnJitFirstLine(session.getRemoteHostname());
			target = new DBGpTarget(remoteLaunch, null, null, session.getIdeKey(), stopAtFirstLine, null, null);
			mapper = resolveMapper(session);
			// need to add ourselves as a session listener for future sessions
			DBGpSessionHandler.getInstance().addSessionListener(target);
		}
		else
		{
			if (session.getSessionId() != null)
			{
				// The remote session was triggered from a browser page that contains a cookie with a session id.
				// Try to locate another debug target with the same session id and grab the path mapper from it.
				String sessionId = session.getSessionId();
				IDebugTarget[] debugTargets = DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
				boolean foundPreviousMatchingLaunch = false;
				for (IDebugTarget aTarget : debugTargets)
				{
					if (aTarget instanceof DBGpTarget)
					{
						DBGpTarget existingTaget = (DBGpTarget) aTarget;
						if (sessionId.equals(existingTaget.getSessionID()))
						{
							mapper = existingTaget.getPathMapper();
							stopAtFirstLine = existingTaget.isStoppingAtFirstLine();
							foundPreviousMatchingLaunch = true;
							break;
						}
					}
				}
				if (!foundPreviousMatchingLaunch)
				{
					stopAtFirstLine = PHPLaunchUtilities.shouldBreakOnJitFirstLine(session.getRemoteHostname());
				}
				target = new DBGpTarget(remoteLaunch, null, null, session.getIdeKey(), stopAtFirstLine, null, sessionId);
				if (mapper == null)
				{
					mapper = resolveMapper(session);
				}
				// need to add ourselves as a session listener for future sessions
				DBGpSessionHandler.getInstance().addSessionListener(target);
			}
			else
			{
				// cli launch or multisession web launch: create a single shot target
				target = new DBGpTarget(remoteLaunch, null /* no script name */, session.getIdeKey(),
						session.getSessionId(), stopAtFirstLine);
				// PathMapper p = PathMapperRegistry.getByPHPExe(null);
				// create a temporary path mapper
				mapper = resolveMapper(session);
			}
		}

		// set up the target with the relevant connections
		target.setPathMapper(mapper);
		target.setSession(session);
		session.setDebugTarget(target);

		if (multiSession && session.getSessionId() == null)
		{
			// we are a multisession web launch
			DBGpMultiSessionTarget multiSessionTarget = new DBGpMultiSessionTarget(remoteLaunch, null, null,
					session.getIdeKey(), stopAtFirstLine, null, session.getSessionId());
			DBGpSessionHandler.getInstance().addSessionListener((IDBGpSessionListener) multiSessionTarget);
			remoteLaunch.addDebugTarget(multiSessionTarget);
			multiSessionTarget.sessionReceived((DBGpBreakpointFacade) IDELayerFactory.getIDELayer(),
					XDebugPreferenceMgr.createSessionPreferences(), target, mapper);
		}
		else
		{
			// we are not a multisession web launch, so just add to the launch
			remoteLaunch.addDebugTarget(target);
			// tell the target it now has a session.
			target.sessionReceived((DBGpBreakpointFacade) IDELayerFactory.getIDELayer(),
					XDebugPreferenceMgr.createSessionPreferences());
			// probably could do waitForInitialSession as session has already been set.
		}

		// add the remote launch to the launch manager
		DebugPlugin.getDefault().getLaunchManager().addLaunch(remoteLaunch);

		// check to see owning session target is still active, if so do a perspective switch
		// TODO - SG: Check if perspective switch is needed at all
		// if (target.isTerminated() == false && target.isTerminating() == false) {
		// Display.getDefault().asyncExec(new Runnable() {
		//
		// public void run() {
		// IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// //code the debug perspectives.
		// //org2.eclipse.php.debug.ui.PHPDebugPerspective
		// //org.eclipse.debug.ui.DebugPerspective
		// //also look at the PHPLaunchUtilities
		// if (!PerspectiveManager.isCurrentPerspective(window, "org2.eclipse.php.debug.ui.PHPDebugPerspective")) {
		// if(PerspectiveManager.shouldSwitchPerspective(window, "org2.eclipse.php.debug.ui.PHPDebugPerspective")) {
		// PerspectiveManager.switchToPerspective(window, "org2.eclipse.php.debug.ui.PHPDebugPerspective");
		// }
		// }
		// }
		//
		// });
		// }
	}

	/*
	 * Resolve the path mapper or create a new one according to the session.
	 * @param session
	 * @param mapper
	 * @return
	 */
	private PathMapper resolveMapper(DBGpSession session)
	{
		// try to locate a relevant server definition so we can get its path mapper
		IServer server = null;
		if (session.getSessionId() != null)
		{
			// In case that the remote session was triggered with a session id, there is a good
			// chance that we already have this server. So we try to look for it in the PHPServersManager.
			List<IServer> servers = PHPServersManager.getServers();
			for (IServer serverConf : servers)
			{
				URL baseURL = serverConf.getBaseURL();
				if (baseURL.getPort() == session.getRemotePort()
						&& baseURL.getHost().equalsIgnoreCase(session.getRemoteHostname()))
				{
					server = serverConf;
					break;
				}
			}
			if (server == null)
			{
				server = PHPServersManager.getServer(session.getRemoteAddress());
			}
		}
		if (server == null || session.getSessionId() == null)
		{
			// In case that we failed the previous search, or that the session id was null (e.g. Triggered from a
			// another tool like the 'XDebug Helper')
			// We search in the temporary servers, and if needed, we create a new one and attach the mapping for it.
			String remoteHostname = session.getRemoteHostname();
			// always pass 80 as a port
			server = PHPServersManager.getTemporaryServer(remoteHostname, 80);
			if (server == null)
			{
				// create it
				server = PHPServersManager.createTemporaryServer(remoteHostname, 80, session.isSecure());
				PHPServersManager.addTemporaryServer(server);
			}
		}
		PathMapper mapper = null;
		if (server != null)
		{
			mapper = PathMapperRegistry.getByServer(server);
		}

		if (mapper == null)
		{
			// Just as a backup. The mapper at this stage should not be null, unless something bad happened before...
			mapper = new PathMapper();
		}
		return mapper;
	}

	/*
	 * A property change listener which resets the server socket listener on every XDebug port change.
	 */
	private class PortChangeListener implements IPropertyChangeListener
	{
		public void propertyChange(PropertyChangeEvent event)
		{
			if (event.getProperty().equals(XDebugPreferenceMgr.XDEBUG_PREF_PORT))
			{
				resetSocket();
			}
		}
	}

	private class PromptUser implements Runnable
	{
		private DBGpSession session;
		private boolean result;

		public boolean isResult()
		{
			return result;
		}

		public PromptUser(DBGpSession session)
		{
			this.session = session;
		}

		public void run()
		{
			String insert = session.getRemoteAddress().getCanonicalHostName() + "/"
					+ session.getRemoteAddress().getHostAddress();
			String message = MessageFormat.format(PHPDebugCoreMessages.XDebugMessage_remoteSessionPrompt,
					new Object[] { insert });
			result = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
					PHPDebugCoreMessages.XDebugMessage_remoteSessionTitle, message);
		}
	}
}
