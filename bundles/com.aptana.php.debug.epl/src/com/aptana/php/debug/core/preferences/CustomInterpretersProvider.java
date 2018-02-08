/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org2.eclipse.php.internal.debug.core.interpreter.preferences.PHPexeItem;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebugCorePreferenceNames;

import com.aptana.core.util.ArrayUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.php.debug.core.interpreter.IInterpreter;
import com.aptana.php.debug.core.interpreter.IInterpreterProvider;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * Provider of the custom interpreters defined by user.
 * 
 * @author Denis Denisenko
 */
public class CustomInterpretersProvider implements IInterpreterProvider
{
	/**
	 * Separator in preferences.
	 */
	private static final String SEPARATOR = ";"; //$NON-NLS-1$

	/**
	 * Cached interpreters.
	 */
	private List<IInterpreter> interpreters;

	/**
	 * Default interpreter.
	 */
	private IInterpreter defaultInterpreter;

	/**
	 * {@inheritDoc}
	 */
	public synchronized IInterpreter getDefaultInterpreter()
	{
		if (interpreters == null)
		{
			collectInterpretersInfo();
		}

		return defaultInterpreter;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized List<IInterpreter> getInterpreters()
	{
		if (interpreters == null)
		{
			collectInterpretersInfo();
		}

		return interpreters;
	}

	/**
	 * Collects interpreters information.
	 */
	private void collectInterpretersInfo()
	{
		interpreters = new ArrayList<IInterpreter>();

		IEclipsePreferences prefs = new InstanceScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID);

		// Load the item names array
		String namesString = prefs.get(PHPDebugCorePreferenceNames.INSTALLED_PHP_NAMES, null);
		if (namesString == null)
		{
			namesString = StringUtil.EMPTY;
		}
		final String[] names = !StringUtil.isEmpty(namesString) ? namesString.split(SEPARATOR) : ArrayUtil.NO_STRINGS;

		// Load the item executable locations array
		String locationsString = prefs.get(PHPDebugCorePreferenceNames.INSTALLED_PHP_LOCATIONS, null);
		if (locationsString == null)
		{
			locationsString = StringUtil.EMPTY;
		}
		final String[] phpExecutablesLocations = !StringUtil.isEmpty(locationsString) ? locationsString
				.split(SEPARATOR) : ArrayUtil.NO_STRINGS;

		// Load the item executable ini's array
		String inisString = prefs.get(PHPDebugCorePreferenceNames.INSTALLED_PHP_INIS, null);
		if (inisString == null)
		{
			inisString = StringUtil.EMPTY;
		}
		// In case there is no preference value for the PHPDebugCorePreferenceNames.INSTALLED_PHP_INIS,
		// the size of the array is set to be the same as the executables array.
		final String[] phpIniLocations = inisString.length() > 0 ? inisString.split(SEPARATOR)
				: new String[phpExecutablesLocations.length];

		// Load the debuggers array
		String debuggersString = prefs.get(PHPDebugCorePreferenceNames.INSTALLED_PHP_DEBUGGERS, null);
		if (debuggersString == null)
		{
			debuggersString = StringUtil.EMPTY;
		}
		final String[] debuggers = !StringUtil.isEmpty(debuggersString) ? debuggersString.split(SEPARATOR)
				: ArrayUtil.NO_STRINGS;

		// Add the executable items
		assert names.length == phpExecutablesLocations.length;
		for (int i = 0; i < phpExecutablesLocations.length; i++)
		{
			String iniLocation = "null".equals(phpIniLocations[i]) ? null : phpIniLocations[i]; //$NON-NLS-1$
			final PHPexeItem item = new PHPexeItem(names[i], phpExecutablesLocations[i], iniLocation, debuggers[i]);
			if (item.getExecutable() != null)
			{
				boolean filterItem = WorkbenchActivityHelper.filterItem(new IPluginContribution()
				{
					public String getLocalId()
					{
						return item.getDebuggerID();
					}

					public String getPluginId()
					{
						return PHPDebugEPLPlugin.PLUGIN_ID;
					}
				});
				if (!filterItem)
				{
					interpreters.add(item);
				}
			}
		}

		// Load the defaults
		String defaultsString = prefs.get(PHPDebugCorePreferenceNames.INSTALLED_PHP_DEFAULTS, null);
		if (defaultsString == null)
		{
			defaultsString = StringUtil.EMPTY;
		}
		// Apply the default items
		final String[] defaults = !StringUtil.isEmpty(defaultsString) ? defaultsString.split(SEPARATOR)
				: ArrayUtil.NO_STRINGS;
		for (String defaultExe : defaults)
		{
			// Get a pair of a debugger id and its default executable
			String[] debuggerDefault = defaultExe.split("="); //$NON-NLS-1$
			if (debuggerDefault.length == 2)
			{
				for (IInterpreter interpreter : interpreters)
				{
					PHPexeItem item = (PHPexeItem) interpreter;
					if (debuggerDefault[0].equals(item.getDebuggerID()) && debuggerDefault[1].equals(item.getName()))
					{
						defaultInterpreter = item;
					}
				}
			}
		}
	}
}
