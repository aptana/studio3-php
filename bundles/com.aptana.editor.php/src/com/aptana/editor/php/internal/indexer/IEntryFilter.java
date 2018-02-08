/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import java.util.Collection;
import java.util.Set;

import com.aptana.editor.php.indexer.IElementEntry;

/**
 * Entry filter.
 * 
 * @author Denis Denisenko
 */
public interface IEntryFilter
{
	/**
	 * Filters entries.
	 * 
	 * @param toFilter
	 *            - entries to filter.
	 * @return filtered entries.
	 */
	Set<IElementEntry> filter(Collection<IElementEntry> toFilter);
}
