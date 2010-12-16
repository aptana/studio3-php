package com.aptana.debug.php.core.daemon;

import org.osgi.framework.BundleContext;

/**
 * Debug Daemon class, which is responsible of starting and stopping the listeners for external PHP debug requests. Note
 * that since PDT 1.0 the debug daemon waits for an outside invocation to start the daemons listening (See
 * startDaemons()).
 * 
 * @author Shalom Gibly
 */
public class DebugDaemon
{

	// The shared instance.
	private static DebugDaemon debugDaemon;
	// Hold an array of possible daemons.
	private ICommunicationDaemon[] daemons;

	/**
	 * Initializes and starts the daemons that has the given daemonID. In case that the give id is null, starts all the
	 * registered daemons.
	 * 
	 * @param debuggerID
	 *            The debugger id, or null.
	 * @since PDT 1.0
	 */
	public void startDaemons(String debuggerID)
	{
		if (daemons == null)
		{
			daemons = CommunicationDaemonRegistry.getBestMatchCommunicationDaemons();
		}
		if (daemons != null)
		{
			for (int i = 0; i < daemons.length; i++)
			{
				if (debuggerID == null
						|| (daemons[i].isDebuggerDaemon() && debuggerID.equals(daemons[i].getDebuggerID())))
				{
					daemons[i].init();
					daemons[i].startListen();
				}
			}
		}
	}

	/**
	 * Stop the daemons that has the given daemonID. In case that the give id is null, stop all the registered daemons.
	 * 
	 * @param debuggerID
	 *            The debugger id, or null.
	 * @since PDT 1.0
	 */
	public void stopDaemons(String debuggerID)
	{
		if (daemons != null)
		{
			for (int i = 0; i < daemons.length; i++)
			{
				if (debuggerID == null
						|| (daemons[i].isDebuggerDaemon() && debuggerID.equals(daemons[i].getDebuggerID())))
				{
					daemons[i].stopListen();
				}
			}
		}
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception
	{

		debugDaemon = null;
		stopDaemons(null);
		daemons = null;
	}

	/**
	 * Make sure that the communication daemons are alive and listening. This method can be called before a
	 * communication session is requested in order to make sure that the requested communication daemon is up and
	 * running. The method goes over the registered daemons and reset the socket for any communication daemon that is
	 * not listening. The validation will be made on the daemons that have the given debuggerID or on all the daemons in
	 * case the id is null.
	 * 
	 * @param debuggerID
	 *            The debugger id, or null.
	 * @return True, if all the communication daemons passed the validation; False, otherwise.
	 * @since PDT 1.0
	 */
	public boolean validateCommunicationDaemons(String debuggerID)
	{
		boolean validated = true;
		if (daemons != null)
		{
			for (int i = 0; i < daemons.length; i++)
			{
				if (debuggerID == null
						|| (daemons[i].isDebuggerDaemon() && debuggerID.equals(daemons[i].getDebuggerID())))
				{
					if (!daemons[i].isListening())
					{
						validated &= daemons[i].resetSocket();
					}
				}
			}
		}
		return validated;
	}

	/**
	 * Returns the shared instance.
	 */
	public static DebugDaemon getDefault()
	{
		if (debugDaemon == null)
		{
			debugDaemon = new DebugDaemon();
		}
		return debugDaemon;
	}

}
