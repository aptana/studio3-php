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
