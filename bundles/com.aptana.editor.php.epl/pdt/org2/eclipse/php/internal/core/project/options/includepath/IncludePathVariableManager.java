/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.core.project.options.includepath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

import com.aptana.core.util.CollectionsUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.php.core.IPHPCoreEPLConstants;
import com.aptana.editor.php.epl.PHPEplPlugin;

public class IncludePathVariableManager
{

	private static final String COMMA = ","; //$NON-NLS-1$
	private static IncludePathVariableManager instance;

	public static IncludePathVariableManager instance()
	{
		if (instance == null)
		{
			instance = new IncludePathVariableManager();
		}
		return instance;
	}

	IPreferenceStore preferenceStore = PHPEplPlugin.getDefault().getPreferenceStore();

	private Map<String, IPath> variables;
	private Set<String> reservedVariables;
	private List<IncludePathVariablesListener> listeners;

	private IncludePathVariableManager()
	{
		variables = new HashMap<String, IPath>();
		reservedVariables = new HashSet<String>();
	}

	public IPath getIncludePathVariable(String variableName)
	{
		IPath varPath = null;
		IPath path = new Path(variableName);
		if (path.segmentCount() == 1)
		{
			varPath = (IPath) variables.get(variableName);
		}
		else
		{
			varPath = (IPath) variables.get(path.segment(0));
			if (varPath != null)
			{
				varPath = varPath.append(path.removeFirstSegments(1));
			}
		}
		return varPath;
	}

	public void setIncludePathVariables(String[] names, IPath[] paths, SubProgressMonitor monitor)
	{
		variables.clear();
		StringBuffer namesString = new StringBuffer();
		StringBuffer pathsString = new StringBuffer();
		for (int i = 0; i < names.length; i++)
		{
			if (paths[i] != null)
			{
				variables.put(names[i], paths[i]);
				if (i > 0)
				{
					namesString.append(COMMA);
					pathsString.append(COMMA);
				}
				namesString.append(names[i]);
				pathsString.append(paths[i].toOSString());
			}
		}
		preferenceStore.setValue(IPHPCoreEPLConstants.INCLUDE_PATH_VARIABLE_NAMES, namesString.toString());
		preferenceStore.setValue(IPHPCoreEPLConstants.INCLUDE_PATH_VARIABLE_PATHS, pathsString.toString());
		fireIncludePathVariablesChanged(names, paths);
	}

	private void fireIncludePathVariablesChanged(String[] names, IPath[] paths)
	{
		if (CollectionsUtil.isEmpty(listeners))
		{
			return;
		}
		for (IncludePathVariablesListener listener : listeners)
		{
			listener.includePathVariablesChanged(names, paths);
		}

	}

	public void addListener(IncludePathVariablesListener listener)
	{
		if (listeners == null)
		{
			listeners = new ArrayList<IncludePathVariablesListener>(1);
		}
		if (!listeners.contains(listener))
		{
			listeners.add(listener);
		}
	}

	public void removeListener(IncludePathVariablesListener listener)
	{
		if (CollectionsUtil.isEmpty(listeners))
		{
			return;
		}
		listeners.remove(listener);
	}

	public String[] getIncludePathVariableNames()
	{
		return (String[]) variables.keySet().toArray(new String[variables.size()]);

	}

	public void startUp()
	{
		String namesString = preferenceStore.getString(IPHPCoreEPLConstants.INCLUDE_PATH_VARIABLE_NAMES);
		String pathsString = preferenceStore.getString(IPHPCoreEPLConstants.INCLUDE_PATH_VARIABLE_PATHS);
		String[] names = {};
		if (namesString.length() > 0)
		{
			names = namesString.split(COMMA);
		}
		String[] paths = {};
		if (pathsString.length() > 0)
		{
			paths = pathsString.split(COMMA);
		}
		// Not good since empty paths are allowed!!!
		// assert (names.length == paths.length);
		for (int i = 0; i < names.length; i++)
		{
			String path;
			if (i < paths.length)
			{
				path = paths[i];
			}
			else
			{
				path = StringUtil.EMPTY;
			}
			variables.put(names[i], new Path(path));
		}

		initExtensionPoints();
	}

	private void initExtensionPoints()
	{
		Plugin phpCorePlugin = PHPEplPlugin.getDefault();
		if (phpCorePlugin == null)
		{
			return;
		}

		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				IPHPCoreEPLConstants.PLUGIN_ID, IPHPCoreEPLConstants.IP_VARIABLE_INITIALIZER_EXTPOINT_ID);
		for (IConfigurationElement element : elements)
		{
			if ("variable".equals(element.getName())) { //$NON-NLS-1$
				String name = element.getAttribute("name"); //$NON-NLS-1$
				String value = element.getAttribute("value"); //$NON-NLS-1$
				//				if (element.getAttribute("initializer") != null) { //$NON-NLS-1$
				// try {
				//						IIncludePathVariableInitializer initializer = (IIncludePathVariableInitializer) element.createExecutableExtension("initializer"); //$NON-NLS-1$
				// value = initializer.initialize(name);
				// } catch (CoreException e) {
				// Logger.logException(e);
				// }
				// }
				// FIXME
				if (value != null)
				{
					putVariable(name, new Path(value));
					reservedVariables.add(name);
				}
			}
		}
	}

	public synchronized void putVariable(String name, IPath path)
	{
		this.variables.put(name, path);
	}

	/**
	 * Returns <code>true</code> if the specified variable is reserved
	 * 
	 * @param variableName
	 *            Variable name
	 */
	public boolean isReserved(String variableName)
	{
		return reservedVariables.contains(variableName);
	}

	public String[] getReservedVariables()
	{
		return (String[]) reservedVariables.toArray(new String[reservedVariables.size()]);
	}

	/**
	 * Returns resolved IPath from the given path string that starts from include path variable
	 * 
	 * @param path
	 *            Path string
	 * @return resolved IPath or <code>null</code> if it couldn't be resolved
	 */
	public IPath resolveVariablePath(String path)
	{
		int index = path.indexOf('/');
		if (index != -1)
		{
			String var = path.substring(0, index);
			IPath varPath = getIncludePathVariable(var);
			if (varPath != null && index + 1 < path.length())
			{
				varPath = varPath.append(path.substring(index + 1));
			}
			return varPath;
		}
		return getIncludePathVariable(path);
	}
}
