package com.aptana.debug.php.epl;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.php.internal.debug.core.debugger.AbstractDebuggerConfiguration;
import org.eclipse.php.internal.debug.core.launching.XDebugLaunchListener;
import org.eclipse.php.internal.debug.core.preferences.PHPDebugCorePreferenceNames;
import org.eclipse.php.internal.debug.core.preferences.PHPDebuggersRegistry;
import org.eclipse.php.internal.debug.core.preferences.PHPProjectPreferences;
import org.eclipse.php.internal.debug.core.xdebug.XDebugPreferenceMgr;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpProxyHandler;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.aptana.debug.php.core.daemon.DebugDaemon;
import com.aptana.debug.php.core.tunneling.SSHTunnelFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class PHPDebugEPLPlugin extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.debug.php.epl"; //$NON-NLS-1$
	public static final boolean DEBUG = Boolean.valueOf(Platform.getDebugOption(PLUGIN_ID + "/debug")).booleanValue(); //$NON-NLS-1$

	// The shared instance
	private static PHPDebugEPLPlugin plugin;
	private static boolean fLaunchChangedAutoRemoveLaunches;
	private static boolean fIsSupportingMultipleDebugAllPages;
	private boolean fInitialAutoRemoveLaunches;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
		// Set the AutoRemoveOldLaunchesListener
		IPreferenceStore preferenceStore = DebugUIPlugin.getDefault().getPreferenceStore();
		fInitialAutoRemoveLaunches = preferenceStore.getBoolean(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES);
		preferenceStore.addPropertyChangeListener(new AutoRemoveOldLaunchesListener());
		// check for default server
		createDefaultPHPServer();

		// TODO - XDebug - See if this can be removed and use a preferences initializer.
		// It's important the the default setting will occur before loading the daemons.
		XDebugPreferenceMgr.setDefaults();

		// Start all the daemons
		DebugDaemon.getDefault().startDaemons(null);

		// TODO - XDebug - See if this can be removed
		XDebugLaunchListener.getInstance();
		DBGpProxyHandler.instance.configure();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		XDebugLaunchListener.shutdown();
		DBGpProxyHandler.instance.unregister();
		savePluginPreferences();
		// DaemonPlugin.getDefault().stopDaemons(null); // TODO: SG - Check if the daemons are shutting down
		super.stop(context);
		plugin = null;
		DebugUIPlugin.getDefault().getPreferenceStore().setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES,
				fInitialAutoRemoveLaunches);

		// close all the tunnel connections
		SSHTunnelFactory.closeAllConnections();
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static PHPDebugEPLPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * @return stop at first line
	 */
	public static boolean getStopAtFirstLine()
	{
		Preferences prefs = getDefault().getPluginPreferences();
		return prefs.getBoolean(PHPDebugCorePreferenceNames.STOP_AT_FIRST_LINE);

	}

	/**
	 * @return debug info
	 */
	public static boolean getDebugInfoOption()
	{
		Preferences prefs = getDefault().getPluginPreferences();
		return prefs.getBoolean(PHPDebugCorePreferenceNames.RUN_WITH_DEBUG_INFO);

	}

	/**
	 * @return open in browser
	 */
	public static boolean getOpenInBrowserOption()
	{
		Preferences prefs = getDefault().getPluginPreferences();
		return prefs.getBoolean(PHPDebugCorePreferenceNames.OPEN_IN_BROWSER);
	}

	/**
	 * Returns the debugger id that is currently in use.
	 * 
	 * @return The debugger id that is in use.
	 * @since PDT 1.0
	 */
	public static String getCurrentDebuggerId()
	{
		Preferences prefs = getDefault().getPluginPreferences();
		return prefs.getString(PHPDebugCorePreferenceNames.PHP_DEBUGGER_ID);
	}

	/**
	 * Returns true if the auto-save is on for any dirty file that exists when a Run/Debug launch is triggered.
	 * 
	 * @return auto save dirty
	 * @deprecated since PDT 1.0, this method simply extracts the value of
	 *             IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH from the {@link DebugUIPlugin}
	 */
	public static boolean getAutoSaveDirtyOption()
	{
		String saveDirty = DebugUIPlugin.getDefault().getPreferenceStore().getString(
				IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH);
		if (saveDirty == null)
		{
			return true;
		}
		return Boolean.valueOf(saveDirty).booleanValue();
	}

	/**
	 * @return open debug views
	 */
	public static boolean getOpenDebugViewsOption()
	{
		Preferences prefs = getDefault().getPluginPreferences();
		return prefs.getBoolean(PHPDebugCorePreferenceNames.OPEN_DEBUG_VIEWS);

	}

	/**
	 * Returns the debugger port for the given debugger id. Return -1 if the debuggerId does not exist, or the debugger
	 * does not have a debug port.
	 * 
	 * @param debuggerId
	 * @return The debug port, or -1.
	 */
	public static int getDebugPort(String debuggerId)
	{
		AbstractDebuggerConfiguration debuggerConfiguration = PHPDebuggersRegistry.getDebuggerConfiguration(debuggerId);
		if (debuggerConfiguration == null)
		{
			return -1;
		}
		int port = debuggerConfiguration.getPort();

		return port;

	}

	/**
	 * @return worspace default server
	 */
	public static String getWorkspaceDefaultServer()
	{
		// Preferences serverPrefs = Activator.getDefault().getPluginPreferences();
		// return serverPrefs.getString(PHPServersManager.DEFAULT_SERVER_PREFERENCES_KEY);
		// TODO
		return "";
	}

	/**
	 * Creates a default server in case the ServersManager does not hold any defined server.
	 */
	public static void createDefaultPHPServer()
	{
		// TODO
		// if (PHPServersManager.getServers().length == 0)
		// {
		// PHPServerProxy server = PHPServersManager.createServer(IPHPDebugConstants.Default_Server_Name, BASE_URL);
		// PHPServersManager.save();
		// PHPServersManager.setDefaultServer(null, server);
		// }
	}

	/**
	 * Returns if multiple sessions of debug launches are allowed when one of the launches contains a 'debug all pages'
	 * attribute.
	 * 
	 * @return True, the multiple sessions are allowed; False, otherwise.
	 */
	public static boolean supportsMultipleDebugAllPages()
	{
		return fIsSupportingMultipleDebugAllPages;
	}

	/**
	 * Allow or disallow the multiple debug sessions that has a launch attribute of 'debug all pages'.
	 * 
	 * @param supported
	 */
	public static void setMultipleDebugAllPages(boolean supported)
	{
		fIsSupportingMultipleDebugAllPages = supported;
	}

	//
	// /**
	// * Returns true if the auto remove launches was disabled by a PHP launch.
	// * The auto remove flag is usually disabled when a PHP server launch was triggered and a
	// * 'debug all pages' flag was on.
	// * Note that this method will return true only if a php launch set it and the debug preferences has a 'true'
	// * value for IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES.
	// *
	// * @return True iff the auto remove old launches was disabled.
	// */
	// public static boolean isDisablingAutoRemoveLaunches() {
	// return fDisableAutoRemoveLaunches;
	// }

	/**
	 * Enable or disable the auto remove old launches flag. The auto remove flag is usually disabled when a PHP server
	 * launch was triggered and a 'debug all pages' flag was on. Note that this method actually sets the
	 * IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES preferences key for the {@link DebugUIPlugin}.
	 * 
	 * @param disableAutoRemoveLaunches
	 */
	public static void setDisableAutoRemoveLaunches(boolean disableAutoRemoveLaunches)
	{
		if (DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES) == disableAutoRemoveLaunches)
		{
			fLaunchChangedAutoRemoveLaunches = true;
			DebugUIPlugin.getDefault().getPreferenceStore().setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES,
					!disableAutoRemoveLaunches);
		}
	}

	/**
	 * Returns the initial value of the auto-remove-old launches.
	 * 
	 * @return
	 */
	public boolean getInitialAutoRemoveLaunches()
	{
		return fInitialAutoRemoveLaunches;
	}

	/**
	 * Returns debug hosts
	 * 
	 * @return debug hosts suitable for URL parameter
	 */
	public static String getDebugHosts()
	{
		Preferences prefs = PHPProjectPreferences.getModelPreferences();
		return prefs.getString(PHPDebugCorePreferenceNames.CLIENT_IP);
	}

	//
	private class AutoRemoveOldLaunchesListener implements IPropertyChangeListener
	{

		/**
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event)
		{
			if (IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES.equals(event.getProperty()))
			{
				if (fLaunchChangedAutoRemoveLaunches)
				{
					fLaunchChangedAutoRemoveLaunches = false;// We got the event, so reset the flag.
				}
				else
				{
					// The event was triggered from some other source - e.g. The user changed the preferences manually.
					fInitialAutoRemoveLaunches = ((Boolean) event.getNewValue()).booleanValue();
				}
			}
		}
	}

	/**
	 * Logs an error
	 * 
	 * @param t
	 */
	public static void logError(Throwable t)
	{
		logError(t.getLocalizedMessage(), t);
	}

	/**
	 * Logs an error
	 * 
	 * @param msg
	 * @param t
	 */
	public static void logError(String msg, Throwable t)
	{
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, msg, t));
	}

	/**
	 * @param msg
	 */
	public static void logError(String msg)
	{
		logError(msg, null);
	}

	/**
	 * Logs a warning.
	 * 
	 * @param msg
	 */
	public static void logWarning(String msg)
	{
		getDefault().getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, msg));
	}

	/**
	 * Log a status.
	 * 
	 * @param status
	 */
	public static void log(IStatus status)
	{
		getDefault().getLog().log(status);
	}
}
