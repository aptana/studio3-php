/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.epl;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org2.eclipse.php.internal.debug.core.debugger.AbstractDebuggerConfiguration;
import org2.eclipse.php.internal.debug.core.launching.XDebugLaunchListener;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebugCorePreferenceNames;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebuggersRegistry;
import org2.eclipse.php.internal.debug.core.xdebug.XDebugPreferenceMgr;
import org2.eclipse.php.internal.debug.core.xdebug.communication.XDebugCommunicationDaemon;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpProxyHandler;
import org2.eclipse.php.internal.debug.ui.util.ImageDescriptorRegistry;

import com.aptana.core.logging.IdeLog;
import com.aptana.php.debug.IDebugScopes;
import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;
import com.aptana.php.debug.core.daemon.DebugDaemon;
import com.aptana.php.debug.core.tunneling.SSHTunnelFactory;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class PHPDebugEPLPlugin extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.php.debug.epl"; //$NON-NLS-1$
	public static final boolean DEBUG = Boolean.valueOf(Platform.getDebugOption(PLUGIN_ID + "/debug")).booleanValue(); //$NON-NLS-1$

	// The shared instance
	private static PHPDebugEPLPlugin plugin;
	private static boolean fLaunchChangedAutoRemoveLaunches;
	private static boolean fIsSupportingMultipleDebugAllPages;
	private boolean fInitialAutoRemoveLaunches;
	private ImageDescriptorRegistry fImageDescriptorRegistry;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
		try
		{
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
		catch (Exception e)
		{
			IdeLog.logError(this, "Error while initiating the PHP debug (EPL) plugin", e); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		XDebugLaunchListener.shutdown();
		DBGpProxyHandler.instance.unregister();
		new InstanceScope().getNode(PLUGIN_ID).flush();
		// DaemonPlugin.getDefault().stopDaemons(null); // TODO: SG - Check if the daemons are shutting down
		super.stop(context);
		plugin = null;
		DebugUIPlugin.getDefault().getPreferenceStore()
				.setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, fInitialAutoRemoveLaunches);

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
		return Platform.getPreferencesService().getBoolean(PLUGIN_ID, PHPDebugCorePreferenceNames.STOP_AT_FIRST_LINE,
				true, getPreferenceContexts());
	}

	/**
	 * @return debug info
	 */
	public static boolean getDebugInfoOption()
	{
		return Platform.getPreferencesService().getBoolean(PLUGIN_ID, PHPDebugCorePreferenceNames.RUN_WITH_DEBUG_INFO,
				true, getPreferenceContexts());
	}

	/**
	 * @return open in browser
	 */
	public static boolean getOpenInBrowserOption()
	{
		return Platform.getPreferencesService().getBoolean(PLUGIN_ID, PHPDebugCorePreferenceNames.OPEN_IN_BROWSER,
				true, getPreferenceContexts());
	}

	/**
	 * Returns the debugger id that is currently in use.
	 * 
	 * @return The debugger id that is in use.
	 * @since PDT 1.0
	 */
	public static String getCurrentDebuggerId()
	{
		return Platform.getPreferencesService().getString(PLUGIN_ID, IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID,
				XDebugCommunicationDaemon.XDEBUG_DEBUGGER_ID, getPreferenceContexts());
	}

	/**
	 * @return open debug views
	 */
	public static boolean getOpenDebugViewsOption()
	{
		return Platform.getPreferencesService().getBoolean(PLUGIN_ID, PHPDebugCorePreferenceNames.OPEN_DEBUG_VIEWS,
				true, getPreferenceContexts());
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
		return ""; //$NON-NLS-1$
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
			DebugUIPlugin.getDefault().getPreferenceStore()
					.setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, !disableAutoRemoveLaunches);
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
		return Platform.getPreferencesService().getString(PLUGIN_ID, PHPDebugCorePreferenceNames.CLIENT_IP,
				"127.0.0.1", getPreferenceContexts()); //$NON-NLS-1$
	}

	/**
	 * Returns the preferences node for this plugin-id instance scope.<br>
	 * This one is a convenient shortcut for: <code>new InstanceScope().getNode(PLUGIN_ID)</code>
	 * 
	 * @return IEclipsePreferences
	 */
	public static IEclipsePreferences getInstancePreferences()
	{
		return new InstanceScope().getNode(PLUGIN_ID);
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
		IdeLog.logError(getDefault(), msg, t, IDebugScopes.DEBUG);
	}

	/**
	 * @param msg
	 */
	public static void logError(String msg)
	{
		IdeLog.logError(getDefault(), msg, IDebugScopes.DEBUG);
	}

	/**
	 * Logs a warning.
	 * 
	 * @param msg
	 */
	public static void logWarning(String msg)
	{
		IdeLog.logWarning(getDefault(), msg, IDebugScopes.DEBUG);
	}

	/**
	 * Log a status.
	 * 
	 * @param status
	 */
	public static void log(IStatus status)
	{
		IdeLog.log(getDefault(), status, IDebugScopes.DEBUG);
	}

	/**
	 * Returns the active workbench window
	 * 
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow()
	{
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static IWorkbenchPage getActivePage()
	{
		IWorkbenchWindow w = getActiveWorkbenchWindow();
		if (w != null)
		{
			return w.getActivePage();
		}
		return null;
	}

	/**
	 * Returns the active workbench shell or <code>null</code> if none
	 * 
	 * @return the active workbench shell or <code>null</code> if none
	 */
	public static Shell getActiveWorkbenchShell()
	{
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null)
		{
			return window.getShell();
		}
		return null;
	}

	/**
	 * @return The standard {@link Display}
	 */
	public static Display getStandardDisplay()
	{
		Display display;
		display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}

	/**
	 * Returns the image descriptor registry used for this plugin.
	 */
	public static ImageDescriptorRegistry getImageDescriptorRegistry()
	{
		if (getDefault().fImageDescriptorRegistry == null)
		{
			getDefault().fImageDescriptorRegistry = new ImageDescriptorRegistry();
		}
		return getDefault().fImageDescriptorRegistry;
	}

	private static IScopeContext[] getPreferenceContexts()
	{
		return new IScopeContext[] { new InstanceScope(), new DefaultScope() };
	}
}
