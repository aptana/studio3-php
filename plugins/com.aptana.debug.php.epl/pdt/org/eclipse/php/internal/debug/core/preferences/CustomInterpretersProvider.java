/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
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
package org.eclipse.php.internal.debug.core.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * Provider of the custom interpreters defined by user.
 * @author Denis Denisenko
 */
public class CustomInterpretersProvider implements IInterpreterProvider
{
	/**
	 * Separator in preferences.
	 */
	private static final String SEPARATOR = ";";
	
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
		
		Preferences prefs = PHPProjectPreferences.getModelPreferences();
		
		// Load the item names array
		String namesString = prefs.getString(PHPDebugCorePreferenceNames.INSTALLED_PHP_NAMES);
		if (namesString == null) {
			namesString = "";
		}
		final String[] names = namesString.length() > 0 ? namesString.split(SEPARATOR) : new String[0];

		// Load the item executable locations array
		String locationsString = prefs.getString(PHPDebugCorePreferenceNames.INSTALLED_PHP_LOCATIONS);
		if (locationsString == null) {
			locationsString = "";
		}
		final String[] phpExecutablesLocations = locationsString.length() > 0 ? locationsString.split(SEPARATOR) : new String[0];

		// Load the item executable ini's array
		String inisString = prefs.getString(PHPDebugCorePreferenceNames.INSTALLED_PHP_INIS);
		if (inisString == null) {
			inisString = "";
		}
		// In case there is no preference value for the PHPDebugCorePreferenceNames.INSTALLED_PHP_INIS,
		// the size of the array is set to be the same as the executables array.
		final String[] phpIniLocations = inisString.length() > 0 ? inisString.split(SEPARATOR) : new String[phpExecutablesLocations.length];

		// Load the debuggers array
		String debuggersString = prefs.getString(PHPDebugCorePreferenceNames.INSTALLED_PHP_DEBUGGERS);
		if (debuggersString == null) {
			debuggersString = "";
		}
		final String[] debuggers = debuggersString.length() > 0 ? debuggersString.split(SEPARATOR) : new String[0];

		// Add the executable items
		assert names.length == phpExecutablesLocations.length;
		for (int i = 0; i < phpExecutablesLocations.length; i++) {
			String iniLocation = "null".equals(phpIniLocations[i]) ? null : phpIniLocations[i]; //$NON-NLS-1$
			final PHPexeItem item = new PHPexeItem(names[i], phpExecutablesLocations[i], iniLocation, debuggers[i]);
			if (item.getExecutable() != null) {
				boolean filterItem = WorkbenchActivityHelper.filterItem(new IPluginContribution() {
					public String getLocalId() {
						return item.getDebuggerID();
					}

					public String getPluginId() {
						return Activator.ID;
					}
				});
				if (!filterItem) {
					interpreters.add(item);
				}
			}
		}

		// Load the defaults
		String defaultsString = prefs.getString(PHPDebugCorePreferenceNames.INSTALLED_PHP_DEFAULTS);
		if (defaultsString == null) {
			defaultsString = "";
		}
		// Apply the default items
		final String[] defaults = defaultsString.length() > 0 ? defaultsString.split(SEPARATOR) : new String[0];
		for (String defaultExe : defaults) {
			// Get a pair of a debugger id and its default executable
			String[] debuggerDefault = defaultExe.split("=");
			if (debuggerDefault.length == 2) {
				for (IInterpreter interpreter : interpreters)
				{
					PHPexeItem item = (PHPexeItem) interpreter;
					if (debuggerDefault[0].equals(item.getDebuggerID())
							&& debuggerDefault[1].equals(item.getName()))
					{
						defaultInterpreter = item;
					}
				}
				//setDefaultItem(debuggerDefault[0], debuggerDefault[1]);
			}
		}
	}
}
