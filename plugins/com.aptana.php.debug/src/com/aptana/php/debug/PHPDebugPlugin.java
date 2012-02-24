/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
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

	/**
	 * Logs a {@link Throwable} error.
	 * 
	 * @param t
	 *            - A {@link Throwable}
	 */
	public static void logError(Throwable t)
	{
		logError(t.getLocalizedMessage(), t);
	}

	/**
	 * Logs an error.
	 * 
	 * @param msg
	 *            - A String message to log
	 * @param t
	 *            - The {@link Throwable} to log
	 */
	public static void logError(String msg, Throwable t)
	{
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, msg, t));
	}

	/**
	 * Logs an {@link IStatus}.
	 * 
	 * @param status
	 *            - An {@link IStatus} to log.
	 */
	public static void log(IStatus status)
	{
		getDefault().getLog().log(status);
	}

}
