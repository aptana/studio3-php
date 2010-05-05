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
package com.aptana.editor.php.indexer;

import java.util.List;
import java.util.Set;

import com.aptana.editor.php.internal.builder.IModule;

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