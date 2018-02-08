/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.indexer;

import java.util.List;
import java.util.Set;

import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Elements index.
 * @author Denis Denisenko
 */
public interface IElementsIndex
{
	static final int ANY_CETEGORY = -1;

	/**
	 * Entry path delimiter character.
	 */
	final char DELIMITER = '/';
	
	/**
	 * Gets entries that have entry path starting with the string specified.
	 * Filters result by category specified, or skips filtering if category value
	 * is {@link IElementsIndex#ANY_CETEGORY}
	 * 
	 * @param category - category to select entries from.
	 * @param path - path entries should start with.
	 * 
	 * @return entries found
	 */
	List<IElementEntry> getEntriesStartingWith(int category,
			String path);
	/**
	 * Gets entries that have entry path equal (in lower) case to the lower case of the 
	 * string specified
	 * Filters result by category specified, or skips filtering if category value
	 * is {@link IElementsIndex#ANY_CETEGORY}
	 * 
	 * @param category - category to select entries from.
	 * @param path - path entries should have.
	 * 
	 * @return entries found
	 */
	List<IElementEntry> getEntries(int category, String path);
	
	/**
	 * Gets unmodifiable module entries list. 
	 * @param module - module.
	 * @return module entries.
	 */
	List<IElementEntry> getModuleEntries(IModule module);
	
	/**
	 * Gets unmodifiable set of indexed modules.
	 * @return indexed modules.
	 */
	Set<IModule> getModules();

}