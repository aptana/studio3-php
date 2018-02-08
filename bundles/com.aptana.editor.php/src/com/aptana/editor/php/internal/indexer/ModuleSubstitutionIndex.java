/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Index that has the special sub-index to handle the module specified, all other sub-indexes are ignored regarding the
 * module entries..
 * 
 * @author Denis Denisenko
 */
public class ModuleSubstitutionIndex implements IElementsIndex
{
	/**
	 * Module to substitute entries for.
	 */
	private IModule module;

	/**
	 * Special module handler.
	 */
	private IElementsIndex handler;

	/**
	 * Main index to user.
	 */
	private IElementsIndex mainIndex;

	/**
	 * ModuleSubstitutionIndex constructor.
	 * 
	 * @param module
	 *            - module to handle.
	 * @param handler
	 *            - module handler.
	 */
	public ModuleSubstitutionIndex(IModule module, IElementsIndex handler, IElementsIndex mainIndex)
	{
		this.module = module;
		this.handler = handler;
		this.mainIndex = mainIndex;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized List<IElementEntry> getEntries(int category, String path)
	{

		List<IElementEntry> result = new ArrayList<IElementEntry>();
		result.addAll(handler.getEntries(category, path));

		List<IElementEntry> mainEntries = mainIndex.getEntries(category, path);
		Iterator<IElementEntry> it = mainEntries.iterator();

		while (it.hasNext())
		{
			IElementEntry entry = it.next();
			if (module.equals(entry.getModule()))
			{
				it.remove();
			}
		}

		result.addAll(mainEntries);

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized List<IElementEntry> getEntriesStartingWith(int category, String path)
	{
		List<IElementEntry> result = new ArrayList<IElementEntry>();
		result.addAll(handler.getEntriesStartingWith(category, path));

		List<IElementEntry> mainEntries = mainIndex.getEntriesStartingWith(category, path);
		Iterator<IElementEntry> it = mainEntries.iterator();

		while (it.hasNext())
		{
			IElementEntry entry = it.next();
			if (module.equals(entry.getModule()))
			{
				it.remove();
			}
		}

		result.addAll(mainEntries);

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized List<IElementEntry> getModuleEntries(IModule module)
	{
		if (this.module.equals(module))
		{
			return handler.getModuleEntries(module);
		}
		else
		{
			return mainIndex.getModuleEntries(module);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<IModule> getModules()
	{
		return mainIndex.getModules();
	}
}
