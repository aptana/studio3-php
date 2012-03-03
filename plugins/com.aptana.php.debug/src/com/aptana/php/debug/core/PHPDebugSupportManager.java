/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.aptana.core.logging.IdeLog;
import com.aptana.php.debug.IDebugScopes;
import com.aptana.php.debug.PHPDebugPlugin;

/**
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPDebugSupportManager
{
	private static final String DEBUGGER_SUPPORT_EXTENSION = "debuggerSupport"; //$NON-NLS-1$
	private static final String LAUNCH_SUPPORT_ITEM = "launchSupport"; //$NON-NLS-1$
	private static final String CLASS_ATTR = "class"; //$NON-NLS-1$

	private static PHPDebugSupportManager instance;
	private IPHPDebugLaunchSupport launchSupport;

	private PHPDebugSupportManager()
	{
		load();
	}

	/**
	 * Returns an instance of the {@link PHPDebugSupportManager}.
	 * 
	 * @return A {@link PHPDebugSupportManager} instance.
	 */
	public static PHPDebugSupportManager getInstance()
	{
		if (instance == null)
		{
			instance = new PHPDebugSupportManager();
		}
		return instance;
	}

	/**
	 * Returns a registered {@link IPHPDebugLaunchSupport}.
	 * 
	 * @return A {@link IPHPDebugLaunchSupport} registered through the 'debuggerSupport' extension point; Null, if none
	 *         was registered.
	 */
	public static IPHPDebugLaunchSupport getLaunchSupport()
	{
		return getInstance().launchSupport;
	}

	/**
	 * Loads from the 'debuggerSupport' extension point.
	 */
	private void load()
	{
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				PHPDebugPlugin.PLUGIN_ID, DEBUGGER_SUPPORT_EXTENSION);
		for (IConfigurationElement element : elements)
		{
			if (LAUNCH_SUPPORT_ITEM.equals(element.getName()))
			{
				if (element.getAttribute(CLASS_ATTR) != null)
				{
					try
					{
						launchSupport = (IPHPDebugLaunchSupport) element.createExecutableExtension(CLASS_ATTR);
					}
					catch (CoreException e)
					{
						IdeLog.logError(PHPDebugPlugin.getDefault(), e, IDebugScopes.DEBUG);

					}
				}
			}
		}
	}
}
