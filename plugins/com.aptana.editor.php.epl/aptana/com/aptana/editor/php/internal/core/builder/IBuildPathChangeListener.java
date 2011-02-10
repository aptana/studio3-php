/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.core.builder;

import java.util.List;

/**
 * Listener to the changes inside the build path.
 * 
 * @author Denis Denisenko
 */
public interface IBuildPathChangeListener
{
	/**
	 * Notifies build path modules structure or contents changed.
	 * 
	 * @param changed
	 *            - changed modules.
	 * @param removed
	 *            - removed modules.
	 * @param removedDirectories
	 *            - removed directories.
	 */
	void changedBefore(List<IModule> changed, List<IModule> removed, List<IDirectory> removedDirectories);

	/**
	 * Notifies build path modules structure or contents changed.
	 * 
	 * @param added
	 *            - added modules.
	 * @param changed
	 *            - changed modules.
	 * @param addedDirectories
	 *            - added directories.
	 * @param removed
	 *            - removed modules.
	 * @param removedDirectories
	 *            - removed directories.
	 */
	void changedAfter(List<IModule> added, List<IModule> changed, List<IModule> removed,
			List<IDirectory> addedDirectories, List<IDirectory> removedDirectories);
}
