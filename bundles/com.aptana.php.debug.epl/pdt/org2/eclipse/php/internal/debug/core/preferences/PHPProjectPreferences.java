/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *   Aptana - Use of the new IEclipsePreferences API
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.xdebug.communication.XDebugCommunicationDaemon;

import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;
import com.aptana.php.debug.core.server.PHPServersManager;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

public class PHPProjectPreferences
{

	public static String getPreferenceNodeQualifier()
	{
		return IPHPDebugConstants.DEBUG_QUALIFIER;
	}

	public static IScopeContext getProjectScope(IProject project)
	{
		return new ProjectScope(project);
	}

	public static boolean getElementSettingsForProject(IProject project)
	{
		IScopeContext pScope = getProjectScope(project);
		return pScope.getNode(getPreferenceNodeQualifier()).getBoolean(getProjectSettingsKey(), false);
	}

	public static String getProjectSettingsKey()
	{
		return IPHPDebugConstants.DEBUG_PER_PROJECT;
	}

	public static boolean getStopAtFirstLine(IProject project)
	{
		IPreferencesService service = Platform.getPreferencesService();
		return service.getBoolean(PHPDebugEPLPlugin.PLUGIN_ID, PHPDebugCorePreferenceNames.STOP_AT_FIRST_LINE, true,
				getPreferenceContexts(project));
	}

	public static String getDefaultServerName(IProject project)
	{
		IPreferencesService service = Platform.getPreferencesService();
		return service.getString(PHPDebugEPLPlugin.PLUGIN_ID, PHPServersManager.DEFAULT_SERVER_PREFERENCES_KEY, "", //$NON-NLS-1$
				getPreferenceContexts(project));
	}

	public static String getDefaultDebuggerID(IProject project)
	{
		IPreferencesService service = Platform.getPreferencesService();
		return service.getString(PHPDebugEPLPlugin.PLUGIN_ID, IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID,
				XDebugCommunicationDaemon.XDEBUG_DEBUGGER_ID, getPreferenceContexts(project));
	}

	public static String getTransferEncoding(IProject project)
	{
		IPreferencesService service = Platform.getPreferencesService();
		return service.getString(PHPDebugEPLPlugin.PLUGIN_ID, PHPDebugCorePreferenceNames.TRANSFER_ENCODING,
				"UTF-8", getPreferenceContexts(project)); //$NON-NLS-1$
	}

	public static String getOutputEncoding(IProject project)
	{
		IPreferencesService service = Platform.getPreferencesService();
		return service.getString(PHPDebugEPLPlugin.PLUGIN_ID, PHPDebugCorePreferenceNames.OUTPUT_ENCODING,
				"UTF-8", getPreferenceContexts(project)); //$NON-NLS-1$
	}

	private static IScopeContext[] getPreferenceContexts(IProject project)
	{
		IScopeContext[] contexts;
		if (project != null)
		{
			contexts = new IScopeContext[] { new ProjectScope(project), new InstanceScope(), new DefaultScope() };
		}
		else
		{
			contexts = new IScopeContext[] { new InstanceScope(), new DefaultScope() };
		}
		return contexts;
	}
}
