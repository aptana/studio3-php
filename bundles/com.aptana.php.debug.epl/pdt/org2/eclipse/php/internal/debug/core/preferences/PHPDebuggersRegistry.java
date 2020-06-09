/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.preferences;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org2.eclipse.php.internal.debug.core.Logger;
import org2.eclipse.php.internal.debug.core.daemon.CommunicationDaemonRegistry;
import org2.eclipse.php.internal.debug.core.daemon.ICommunicationDaemon;
import org2.eclipse.php.internal.debug.core.debugger.AbstractDebuggerConfiguration;
import org2.eclipse.php.internal.debug.core.xdebug.communication.XDebugCommunicationDaemon;

import com.aptana.core.logging.IdeLog;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * A registry class for all the PHP debuggers. This registry class supplies the IDs and the names of all the registered
 * PHP debuggers. The basic PDT supports Zend's debugger and XDebug debugger.
 * 
 * @author Shalom Gibly
 * @since PDT 1.0
 */
public class PHPDebuggersRegistry
{

	private static final String EXTENSION_POINT_NAME = "phpDebuggers"; //$NON-NLS-1$
	private static final String DEBUGGER_TAG = "phpDebugger"; //$NON-NLS-1$
	private static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
	private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private static final String CONFIGURATION_CLASS_ATTRIBUTE = "debuggerConfiguration"; //$NON-NLS-1$

	// Zend's debugger is the default for the PDT, however, this can be changed by calling the setDefaultDebuggerId()
	// method.
	private static String DEFAULT_DEBUGGER_ID = XDebugCommunicationDaemon.XDEBUG_DEBUGGER_ID;

	private static PHPDebuggersRegistry instance;

	private HashMap<String, String> debuggers = new HashMap<String, String>();
	private HashMap<String, AbstractDebuggerConfiguration> configurations = new HashMap<String, AbstractDebuggerConfiguration>();

	private PHPDebuggersRegistry()
	{
		loadDebuggers();
	}

	private static PHPDebuggersRegistry getInstance()
	{
		if (instance == null)
		{
			instance = new PHPDebuggersRegistry();
		}
		return instance;
	}

	/**
	 * Returns an unmodifiable Set of the registered debuggers ids.
	 * 
	 * @return An unmodifiable Set of the registered debuggers ids.
	 */
	public static Set<String> getDebuggersIds()
	{
		return Collections.unmodifiableSet(getInstance().debuggers.keySet());
	}

	/**
	 * Returns the debugger configuration for the given debugger id.
	 * 
	 * @param debuggerId
	 * @return An AbstractDebuggerConfiguration, or null if no such debugger id exists.
	 */
	public static AbstractDebuggerConfiguration getDebuggerConfiguration(String debuggerId)
	{
		return getInstance().configurations.get(debuggerId);
	}

	/**
	 * Returns all the debuggers configurations.
	 * 
	 * @return An array of all the loaded AbstractDebuggerConfiguration.
	 */
	public static AbstractDebuggerConfiguration[] getDebuggersConfigurations()
	{
		Collection<AbstractDebuggerConfiguration> values = getInstance().configurations.values();
		AbstractDebuggerConfiguration[] configurations = new AbstractDebuggerConfiguration[values.size()];
		return values.toArray(configurations);
	}

	/**
	 * Returns the default debugger ID.
	 * 
	 * @return The default debugger ID.
	 */
	public static String getDefaultDebuggerId()
	{
		return DEFAULT_DEBUGGER_ID;
	}

	/**
	 * Set the default debugger ID.
	 * 
	 * @param id
	 *            The debugger id (must exist in the registered ids)
	 * @throws IllegalArgumentException
	 *             If the given id is not registered as part of the supported ids.
	 * @see #getDefaultDebuggerId()
	 * @see #getDebuggersIds()
	 */
	public static void setDefaultDebuggerId(String id) throws IllegalArgumentException
	{
		if (getInstance().debuggers.containsKey(id))
		{
			DEFAULT_DEBUGGER_ID = id;
		}
		else
		{
			throw new IllegalArgumentException("No such debugger id was registered: " + id);
		}
	}

	/**
	 * Returns the debugger name according to the given debugger id.
	 * 
	 * @param debuggerID
	 * @return The debugger name
	 */
	public static String getDebuggerName(String debuggerID)
	{
		return getInstance().debuggers.get(debuggerID);
	}

	// Load the debuggers into the map.
	// Do this only once.
	private void loadDebuggers()
	{
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IConfigurationElement[] elements = registry.getConfigurationElementsFor(PHPDebugEPLPlugin.PLUGIN_ID,
				EXTENSION_POINT_NAME);
		// We use the following HashMap in order to accumulate non PDT debugger configurations.
		// that are extension to point: org2.eclipse.php.debug.core.phpDebuggers
		HashMap<String, AbstractDebuggerConfiguration> nonPDTConfigurations = new HashMap<String, AbstractDebuggerConfiguration>();
		for (final IConfigurationElement element : elements)
		{

			if (DEBUGGER_TAG.equals(element.getName()))
			{
				final String name = element.getAttribute(NAME_ATTRIBUTE);
				final String id = element.getAttribute(ID_ATTRIBUTE);
				// boolean isPDT = element.getNamespaceIdentifier().startsWith("org2.eclipse.php");
				boolean isPDT = true;
				boolean filter = WorkbenchActivityHelper.filterItem(new IPluginContribution()
				{
					public String getLocalId()
					{
						return id;
					}

					public String getPluginId()
					{
						return element.getNamespaceIdentifier();
					}
				});
				if (filter)
				{
					continue;
				}
				debuggers.put(id, name);
				try
				{
					AbstractDebuggerConfiguration configuration = (AbstractDebuggerConfiguration) element
							.createExecutableExtension(CONFIGURATION_CLASS_ATTRIBUTE);
					configuration.setDebuggerId(id);
					configuration.setName(name);
					try
					{
						ICommunicationDaemon[] daemons = CommunicationDaemonRegistry.getBestMatchCommunicationDaemons();
						// find the daemon that fits this configuration (match by debugger-id)
						for (ICommunicationDaemon daemon : daemons)
						{
							if (daemon.isDebuggerDaemon() && id.equals(daemon.getDebuggerID()))
							{
								configuration.setCommunicationDaemon(daemon); // Attach the daemon reference to the
																				// configuration.
								break;
							}
						}
					}
					catch (Exception e)
					{
						IdeLog.logError(PHPDebugEPLPlugin.getDefault(), "Error loading a debugger configuration", e); //$NON-NLS-1$
						configuration.setPort(-1);
					}
					configurations.put(id, configuration);
					if (!isPDT)
					{
						nonPDTConfigurations.put(id, configuration);
					}
				}
				catch (CoreException e)
				{
					Logger.logException(e);
				}
			}
		}
		// Override any PDT debugger settings with any extension of debugger configuration.
		Set<String> keySet = nonPDTConfigurations.keySet();
		for (String key : keySet)
		{
			AbstractDebuggerConfiguration configuration = nonPDTConfigurations.get(key);
			configurations.put(key, configuration);
			debuggers.put(configuration.getDebuggerId(), configuration.getName());
		}
	}

}
