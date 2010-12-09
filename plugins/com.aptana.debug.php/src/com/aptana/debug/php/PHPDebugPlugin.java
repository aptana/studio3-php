package com.aptana.debug.php;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.aptana.debug.php.core.tunneling.SSHTunnelSession;

/**
 * The activator class controls the plug-in life cycle
 */
public class PHPDebugPlugin extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.debug.php"; //$NON-NLS-1$
	public static final boolean DEBUG = Boolean.valueOf(Platform.getDebugOption(PLUGIN_ID + "/debug")).booleanValue(); //$NON-NLS-1$

	// The shared instance
	private static PHPDebugPlugin plugin;
	private ServiceTracker tracker;

	/**
	 * The constructor
	 */
	public PHPDebugPlugin()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
		tracker = new ServiceTracker(getBundle().getBundleContext(), IJSchService.class.getName(), null);
		tracker.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		SSHTunnelSession.shutdown();
		tracker.close();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static PHPDebugPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Returns an {@link IJSchService}.
	 * 
	 * @return {@link IJSchService}
	 */
	public IJSchService getJSchService()
	{
		return (IJSchService) tracker.getService();
	}
}
