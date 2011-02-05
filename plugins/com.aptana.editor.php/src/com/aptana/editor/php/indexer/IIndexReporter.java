/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.indexer;

import com.aptana.editor.php.internal.core.builder.IModule;


/**
 * IIndexReporter
 * @author Denis Denisenko
 */
public interface IIndexReporter
{
	/**
	 * Reports entry.
	 * @param category - entry category.
	 * @param entryPath - entry path.
	 * @param value - entry value.
	 * @param module - entry module.
	 * @return entry.
	 */
	IElementEntry reportEntry(int category, String entryPath, IReportable value,
			IModule module);
}
