/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Modifiable elements index.
 * 
 * @author Denis Denisenko
 */
public interface IModifiableElementsIndex extends IElementsIndex
{

	/**
	 * Adds new entry to the index.
	 * 
	 * @param category
	 *            - entry category.
	 * @param entryPath
	 *            - entry path.
	 * @param value
	 *            - entry value.
	 * @param module
	 *            - entry module.
	 * @return entry
	 */
	IElementEntry addEntry(int category, String entryPath, Object value, IModule module);

	/**
	 * Removes all entries belonging to the module.
	 * 
	 * @param module
	 *            - module, which entries to remove.
	 */
	void removeModuleEntries(IModule module);
}