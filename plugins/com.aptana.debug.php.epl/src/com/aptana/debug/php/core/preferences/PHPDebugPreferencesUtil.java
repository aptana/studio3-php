package com.aptana.debug.php.core.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.aptana.debug.php.epl.PHPDebugEPLPlugin;

/**
 * 
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
