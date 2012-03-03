/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.aptana.php.debug.core.tunneling.SSHTunnelSession;

/**
 * The activator class controls the plug-in life cycle
 */
public class PHPDebugPlugin extends AbstractUIPlugin
{
	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.php.debug"; //$NON-NLS-1$
	public static final boolean DEBUG = Boolean.valueOf(Platform.getDebugOption(PLUGIN_ID + "/debug")).booleanValue(); //$NON-NLS-1$

	// The shared instance
	private static PHPDebugPlugin plugin;
	private ServiceTracker tracker;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	// $codepro.audit.disable declaredExceptions
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
	// $codepro.audit.disable declaredExceptions
	public void stop(BundleContext context) throws Exception
	{
		SSHTunnelSession.shutdown();
		tracker.close(); // $codepro.audit.disable closeInFinally
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
