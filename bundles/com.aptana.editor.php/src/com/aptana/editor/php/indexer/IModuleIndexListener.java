/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.indexer;

import java.util.List;

import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * IModuleIndexListener
 * @author Denis Denisenko
 */
public interface IModuleIndexListener
{
	/**
	 * Is called after source modules are actually changed, but before reindexing the changes.
	 * @param changed - change source modules.
	 * @param removed - removed source modules.
	 * @param removedDirectories - removed directories.
	 */
	public void beforeIndexChange(List<IModule> changed, List<IModule> removed, 
			List<IDirectory> removedDirectories);
	
	/**
	 * Is called after reindexing the changes. 
	 * @param added - added source modules.
	 * @param changed - changed source modules.
	 * @param addedDirectories - added directories.
	 */
	public void afterIndexChange(List<IModule> added, List<IModule> changed, List<IDirectory> addedDirectories);
}
