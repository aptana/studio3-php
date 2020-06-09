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
package org2.eclipse.php.internal.debug.core.zend.communication;

import java.net.Socket;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org2.eclipse.php.internal.debug.core.daemon.AbstractDebuggerCommunicationDaemon;
import org2.eclipse.php.internal.debug.core.daemon.ICommunicationDaemon;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebugCorePreferenceNames;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * The debugger communication receiver holds a ServerSocket that remains open for the entire
 * Eclipse running session and accepts debug requests from remote or local debuggers.
 * Any changes in the preferences listening port definition is reflected in this listener by
 * re-initializing the ServerSocket to listen on the new port.
 * 
 * @author Shalom Gibly
 */
public class DebuggerCommunicationDaemon extends AbstractDebuggerCommunicationDaemon implements ICommunicationDaemon {

	public static final String ZEND_DEBUGGER_ID = "org2.eclipse.php.debug.core.zendDebugger"; //$NON-NLS-1$
	public static final int[] DEBUGGER_DEFAULT_PORTS = new int[] { 10000, 10137 };
	private IPropertyChangeListener portChangeListener;

	/**
	 * Constructs a new DebuggerCommunicationDaemon
	 */
	public DebuggerCommunicationDaemon() {
	}

	/**
	 * Initializes the ServerSocket and starts a listen thread. Also, initialize a preferences
	 * change listener for the port that is used by this daemon.
	 */
	public void init() {
		initDeamonChangeListener();
		super.init();
	}

	/**
	 * Initialize a daemon change listener 
	 */
	protected void initDeamonChangeListener() {
		if (portChangeListener == null) {
			Preferences preferences = PHPDebugEPLPlugin.getDefault().getPluginPreferences();
			portChangeListener = new PortChangeListener();
			preferences.addPropertyChangeListener(portChangeListener);
		}
	}

	/**
	 * Returns the server socket port used for the debug requests listening thread. 
	 * @return The port specified in the preferences.
	 */
	public int getReceiverPort() {
		return PHPDebugEPLPlugin.getDebugPort(ZEND_DEBUGGER_ID);
	}

	/**
	 * Starts a connection handling thread on the given Socket. 
	 * This method can be overridden by extending classes to create a different debug connection threads.
	 * The connection thread itself should execute itself in a different thread in order to 
	 * release the current thread.
	 * 
	 * @param socket
	 */
	protected void startConnectionThread(Socket socket) {
		// Handles the connection in a new thread
		new DebugConnectionThread(socket);
	}

	// A port change listener
	private class PortChangeListener implements IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(PHPDebugCorePreferenceNames.ZEND_DEBUG_PORT)) {
				resetSocket();
			}
		}
	}

	/**
	 * Returns the Zend's debugger ID.
	 * 
	 * @return The debugger ID that is using this daemon (e.g. Zend debugger ID).
	 * @since PDT 1.0
	 */
	public String getDebuggerID() {
		return ZEND_DEBUGGER_ID;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.debug.daemon.communication.ICommunicationDaemon#isDebuggerDaemon()
	 */
	public boolean isDebuggerDaemon() {
		return true;
	}

	/**
	 * Returns true if the given port is defined as one of the default ports for this debugger daemon.
	 * 
	 * @param port A port to check
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
}
