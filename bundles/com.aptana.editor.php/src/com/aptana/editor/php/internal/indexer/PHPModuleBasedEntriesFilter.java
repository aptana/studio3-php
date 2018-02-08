/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Filter that filters entries by the list of modules.
 * 
 * @author Denis Denisenko
 */
public class PHPModuleBasedEntriesFilter implements IElementEntriesFilter
{
	/**
	 * Allowed modules.
	 */
	private Set<IModule> allowedModules = new HashSet<IModule>();

	/**
	 * PHPModuleBasedEntriesFilter constructor.
	 */
	public PHPModuleBasedEntriesFilter()
	{
	}

	/**
	 * PHPModuleBasedEntriesFilter constructor.
	 * 
	 * @param toAdd
	 *            - modules to add to filter.
	 */
	public PHPModuleBasedEntriesFilter(Collection<IModule> toAdd)
	{
		allowedModules.addAll(toAdd);
	}

	/**
	 * Adds allowed module.
	 * 
	 * @param module
	 *            - module to add.
	 */
	public void addAllowedModule(IModule module)
	{
		allowedModules.add(module);
	}

	/**
	 * Adds allowed modules.
	 * 
	 * @param modules
	 *            - modules to add.
	 */
	public void addAllowedModules(Collection<IModule> modules)
	{
		allowedModules.addAll(modules);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<IElementEntry> filter(Collection<IElementEntry> toFilter)
	{
		if (toFilter == null)
		{
			return Collections.emptySet();
		}

		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();

		for (IElementEntry entry : toFilter)
		{
			if (entry.getModule() != null && allowedModules.contains(entry.getModule()))
			{
				result.add(entry);
			}
		}
		return result;
	}
}
