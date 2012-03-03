/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPDebugPreferencesUtil
{
	private static final IScopeContext[] CONTEXTS = new IScopeContext[] { new InstanceScope(), new DefaultScope() };

	/**
	 * @param key
	 * @param defaultValue
	 * @return The preference value for the given key. If not found, the default value is returned.
	 */
	public static String getString(String key, String defaultValue)
	{
		return Platform.getPreferencesService().getString(PHPDebugEPLPlugin.PLUGIN_ID, key, defaultValue, CONTEXTS);
	}

	/**
	 * @param key
	 * @param defaultValue
	 * @return The preference value for the given key. If not found, the default value is returned.
	 */
	public static int getInt(String key, int defaultValue)
	{
		return Platform.getPreferencesService().getInt(PHPDebugEPLPlugin.PLUGIN_ID, key, defaultValue, CONTEXTS);
	}

	/**
	 * @param key
	 * @param defaultValue
	 * @return The preference value for the given key. If not found, the default value is returned.
	 */
	public static boolean getBoolean(String key, boolean defaultValue)
	{
		return Platform.getPreferencesService().getBoolean(PHPDebugEPLPlugin.PLUGIN_ID, key, defaultValue, CONTEXTS);
	}
}
