/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Filter that does filter element entries the specified module build-path and it's dependencies.
 * 
 * @author Denis Denisenko
 */
public class BuildPathElementEntriesFilter implements IElementEntriesFilter
{

	/**
	 * Build-paths to accept.
	 */
	private Set<IBuildPath> activeBuildPaths = new HashSet<IBuildPath>();

	/**
	 * Build-paths to accept.
	 */
	private Set<IBuildPath> passiveBuildPaths = new HashSet<IBuildPath>();

	/**
	 * BuildPathElementEntriesFilter constructor.
	 * 
	 * @param module
	 *            - module, which build-path to use.
	 */
	public BuildPathElementEntriesFilter(IModule module)
	{
		if (module != null)
		{

			IBuildPath buildPath = module.getBuildPath();
			if (buildPath == null)
			{
				activeBuildPaths = null;
				return;
			}

			if (buildPath.isPassive())
			{
				passiveBuildPaths.add(buildPath);
			}
			else
			{
				activeBuildPaths.add(buildPath);
			}

			if (buildPath.getDependencies() != null)
			{
				for (IBuildPath dependency : buildPath.getDependencies())
				{
					if (dependency.isPassive())
					{
						passiveBuildPaths.add(dependency);
					}
					else
					{
						activeBuildPaths.add(dependency);
					}
				}
			}
		}
		else
		{
			activeBuildPaths = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<IElementEntry> filter(Collection<IElementEntry> toFilter)
	{

		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();
		if (activeBuildPaths == null)
		{
			result.addAll(toFilter);
			return result;
		}
		// To avoid adding elements for different modules to the same entry prefix, we maintain a map
		// between the entry prefix and the module. This is basically first-come-first serve map, and entries that
		// appear in a later arriving module will be filtered out.
		Set<Integer> visitedEntries = new LinkedHashSet<Integer>();
		if (activeBuildPaths.size() != 0 || passiveBuildPaths.size() != 0)
		{
			for (IElementEntry e : toFilter)
			{
				IModule module = e.getModule();
				boolean added = false;
				String entryPath = e.getEntryPath();
				int pathHash = entryPath != null ? entryPath.hashCode() : 0;
				int entryHash = pathHash + ((module != null) ? module.hashCode() : 0);
				if (!visitedEntries.contains(entryHash))
				{
					if (activeBuildPaths.size() != 0)
					{
						if (module == null || activeBuildPaths.contains(module.getBuildPath()))
						{
							result.add(e);
							added = true;
							visitedEntries.add(entryHash);
						}
					}
				}

				// checking passive build-paths in case we have some and we did not add this entry yet
				if (passiveBuildPaths.size() != 0 && !added)
				{
					for (IBuildPath passiveBuildPath : passiveBuildPaths)
					{
						if (passiveBuildPath.contains(module))
						{
							result.add(e);
							break;
						}
					}
				}
			}
		}

		return result;
	}
}
