/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Path;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Indexing utilities.
 * 
 * @author Denis Denisenko
 */
public final class ElementsIndexingUtils
{

	/**
	 * Gets first name in the path.
	 * 
	 * @param path
	 *            - path.
	 * @return first name
	 */
	public static String getFirstNameInPath(String path)
	{
		if (path == null)
		{
			return null;
		}

		int delimiterIndex = path.indexOf(IElementsIndex.DELIMITER);
		if (delimiterIndex == -1)
		{
			return path;
		}

		return path.substring(0, delimiterIndex);
	}

	/**
	 * Gets last name in the path.
	 * 
	 * @param path
	 *            - path.
	 * @return first name
	 */
	public static String getLastNameInPath(String path)
	{
		if (path == null)
		{
			return null;
		}

		int delimiterIndex = path.lastIndexOf(IElementsIndex.DELIMITER);
		if (delimiterIndex == -1 || delimiterIndex == path.length() - 1)
		{
			return path;
		}

		return path.substring(delimiterIndex + 1, path.length());
	}

	/**
	 * Creates the filter that checks whether entry may be included from the specified module.
	 * 
	 * @param module
	 *            - module.
	 * @param index
	 *            - index to use for calculations.
	 * @return entries filter.
	 */
	public static IElementEntriesFilter createIncludeFilter(IModule module, IElementsIndex index)
	{
		if (!includeFilteringEnabled())
		{
			return new BuildPathElementEntriesFilter(module);
		}

		// getting include entries
		List<IElementEntry> includeEntries = index.getEntries(IPHPIndexConstants.IMPORT_CATEGORY, ""); //$NON-NLS-1$

		// building fast includes index
		Map<IModule, Set<String>> includes = new HashMap<IModule, Set<String>>();
		for (IElementEntry includeEntry : includeEntries)
		{
			Object val = includeEntry.getValue();
			if (val == null || !(val instanceof IncludePHPEntryValue))
			{
				continue;
			}

			IncludePHPEntryValue value = (IncludePHPEntryValue) val;
			String includePath = value.getIncludePath();
			if (includePath == null || includePath.length() == 0)
			{
				continue;
			}

			IModule currentModule = includeEntry.getModule();
			Set<String> moduleIncludes = includes.get(currentModule);
			if (moduleIncludes == null)
			{
				moduleIncludes = new HashSet<String>();
				includes.put(currentModule, moduleIncludes);
			}
			moduleIncludes.add(includePath);
		}

		// index is built and we may find all the modules, current module includes recursively
		Set<IModule> includedModules = new HashSet<IModule>();
		addModulesIncluded(includes, module, includedModules);

		includedModules.add(module);
		return new PHPModuleBasedEntriesFilter(includedModules);
	}

	private static void addModulesIncluded(Map<IModule, Set<String>> index, IModule module, Set<IModule> result)
	{
		Set<String> includes = index.get(module);
		if (includes == null || includes.isEmpty())
		{
			return;
		}

		IBuildPath buildPath = module.getBuildPath();

		for (String include : includes)
		{
			try
			{
				Path path = new Path(include);
				if (path.isAbsolute())
				{
					continue;
				}

				IModule includedModule = buildPath.resolveRelativePath(module, path);
				if (includedModule != null)
				{
					if (!result.contains(includedModule))
					{
						result.add(includedModule);
						addModulesIncluded(index, includedModule, result);
					}
				}
			}
			catch (Throwable th)
			{
				IdeLog.logWarning(PHPEditorPlugin.getDefault(),
						"PHP elements indexing - Error while including modules (addModulesIncluded)", //$NON-NLS-1$
						th, PHPEditorPlugin.INDEXER_SCOPE);
			}
		}
	}

	/**
	 * Checks whether include-based filtering is enabled.
	 * 
	 * @return true if enabled, false otherwise.
	 */
	private static boolean includeFilteringEnabled()
	{
		return IContentAssistPreferencesConstants.CONTENT_ASSIST_EXPLICIT_INCLUDE.equals(PHPEditorPlugin.getDefault()
				.getPreferenceStore().getString(IContentAssistPreferencesConstants.CONTENT_ASSIST_FILTER_TYPE));
	}

	/**
	 * ElementsIndexingUtils private constructor.
	 */
	private ElementsIndexingUtils()
	{
	}
}
