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
package com.aptana.editor.php.internal.indexer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Path;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.internal.builder.IBuildPath;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants;

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
				// skip
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
