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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.internal.builder.IModule;

/**
 * Index that has the special sub-index to handle the module specified,
 * all other sub-indexes are ignored regarding the module entries..
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
	 * @param module - module to handle.
	 * @param handler - module handler.
	 */
	public ModuleSubstitutionIndex(IModule module, IElementsIndex handler,
			IElementsIndex mainIndex)
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
	public synchronized List<IElementEntry> getEntriesStartingWith(
			int category, String path)
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
