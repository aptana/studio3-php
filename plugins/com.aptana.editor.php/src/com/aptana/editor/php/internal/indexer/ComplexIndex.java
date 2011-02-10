/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Complex index that contains other indexes referenced by resources.
 * 
 * @author Denis Denisenko
 */
public class ComplexIndex implements IElementsIndex
{
	/**
	 * Indexes.
	 */
	private Map<IBuildPath, IModifiableElementsIndex> indexes = new HashMap<IBuildPath, IModifiableElementsIndex>();

	public ComplexIndex()
	{

	}

	/**
	 * Adds entry.
	 * 
	 * @param category
	 *            - category.
	 * @param entryPath
	 *            - entry path.
	 * @param value
	 *            - value.
	 * @param module
	 *            - module.
	 * @param indexResource
	 *            - index resource.
	 */
	public synchronized IElementEntry addEntry(int category, String entryPath, Object value, IModule module,
			Object indexResource)
	{
		IModifiableElementsIndex index = indexes.get(indexResource);
		if (index != null)
		{
			return index.addEntry(category, entryPath, value, module);
		}

		return null;
	}

	/**
	 * Removes module entries.
	 * 
	 * @param module
	 *            - module.
	 * @param indexResource
	 *            - index resource.
	 */
	public synchronized void removeModuleEntries(IModule module, Object indexResource)
	{
		IModifiableElementsIndex index = indexes.get(indexResource);
		if (index != null)
		{
			index.removeModuleEntries(module);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized List<IElementEntry> getEntries(int category, String path)
	{
		List<IElementEntry> result = new ArrayList<IElementEntry>();
		for (IModifiableElementsIndex index : indexes.values())
		{
			result.addAll(index.getEntries(category, path));
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized List<IElementEntry> getEntriesStartingWith(int category, String path)
	{
		List<IElementEntry> result = new ArrayList<IElementEntry>();
		for (IModifiableElementsIndex index : indexes.values())
		{
			result.addAll(index.getEntriesStartingWith(category, path));
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized List<IElementEntry> getModuleEntries(IModule module)
	{
		List<IElementEntry> result = new ArrayList<IElementEntry>();
		for (IModifiableElementsIndex index : indexes.values())
		{
			result.addAll(index.getModuleEntries(module));
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized Set<IModule> getModules()
	{
		Set<IModule> result = new HashSet<IModule>();
		for (IModifiableElementsIndex index : indexes.values())
		{
			result.addAll(index.getModules());
		}

		return result;
	}

	/**
	 * Adds index.
	 * 
	 * @param indexResource
	 *            - index resource.
	 * @param index
	 *            - index to add.
	 */
	public synchronized void addIndex(IBuildPath indexResource, IModifiableElementsIndex index)
	{
		indexes.put(indexResource, index);
	}

	/**
	 * Removes index.
	 * 
	 * @param indexResource
	 *            - index resource.
	 */
	public synchronized void removeIndex(IBuildPath indexResource)
	{
		indexes.remove(indexResource);
	}

	/**
	 * @return all build paths that are stored in this index
	 */
	public Collection<IBuildPath> getPaths()
	{
		return indexes.keySet();
	}

	/**
	 * @return index for build path
	 */
	public IModifiableElementsIndex getElementIndex(IBuildPath p)
	{
		return indexes.get(p);
	}

}
