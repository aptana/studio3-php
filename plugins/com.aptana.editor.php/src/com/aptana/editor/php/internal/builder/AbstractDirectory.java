/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import org.eclipse.core.runtime.IPath;

import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IDirectory;

/**
 * Abstract directory.
 * 
 * @author Denis Denisenko
 */
public abstract class AbstractDirectory extends AbstractBuildPathResource implements IDirectory
{

	/**
	 * AbstractDirectory constructor.
	 * 
	 * @param buildPath
	 *            - build path.
	 * @param fullPath
	 *            - full path.
	 */
	protected AbstractDirectory(IBuildPath buildPath, String fullPath)
	{
		super(buildPath, fullPath);
	}

	/**
	 * {@inheritDoc}
	 */
	public IPath getModulePath(String moduleName)
	{
		IPath directoryPath = getPath();
		return directoryPath.append(moduleName);
	}
}
