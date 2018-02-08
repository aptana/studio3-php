/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.model.ISourceFolder;

/**
 * SourceFolder
 * 
 * @author Denis Denisenko
 */
public class SourceFolder extends AbstractResourceElement implements ISourceFolder
{

	/**
	 * SourceFolder constructor.
	 * 
	 * @param directory
	 *            - directory.
	 */
	public SourceFolder(IDirectory directory)
	{
		super(directory);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getElementType()
	{
		return FOLDER;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IModelElement> getChildren()
	{
		IBuildPath buildPath = getBPResource().getBuildPath();
		if (buildPath == null)
		{
			return Collections.emptyList();
		}

		List<IDirectory> subdirectories = buildPath.getSubdirectoriesByPath(getBPResource().getPath());
		List<IModule> modules = buildPath.getModulesByPath(getBPResource().getPath());
		List<IModelElement> result = new ArrayList<IModelElement>();
		if (subdirectories != null)
		{
			for (IDirectory subdirectory : subdirectories)
			{
				ISourceFolder folder = new SourceFolder(subdirectory);
				result.add(folder);
			}
		}

		if (modules != null)
		{
			for (IModule module : modules)
			{
				ISourceModule mdl = new SourceModule(module);
				result.add(mdl);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasChildren()
	{
		IBuildPath buildPath = getBPResource().getBuildPath();
		if (buildPath == null)
		{
			return false;
		}

		List<IDirectory> subdirectories = buildPath.getSubdirectoriesByPath(getBPResource().getPath());
		List<IModule> modules = buildPath.getModulesByPath(getBPResource().getPath());
		if (subdirectories != null && subdirectories.size() > 0)
		{
			return true;
		}

		if (modules != null && modules.size() > 0)
		{
			return true;
		}

		return false;
	}

	protected IDirectory getDirectory()
	{
		return (IDirectory) getBPResource();
	}
}
