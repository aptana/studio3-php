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
 * Element entry.
 * @author Denis Denisenko
 */
public interface IElementEntry
{
	/**
	 * Gets entry category.
	 * @see IPHPIndexConstants
	 * @return entry category
	 */
	int getCategory();

	/**
	 * Gets entry path.
	 * @return entry path.
	 */
	String getEntryPath();
	
	/**
	 * Gets lower-case variant of the entry path.
	 * @return lower-case variant of the entry path.
	 */
	String getLowerCaseEntryPath();

	/**
	 * Gets entry value.
	 * @return entry value.
	 */
	Object getValue();

	/**
	 * Gets a module, the entry belongs to.
	 * @return module
	 */
	IModule getModule();
}