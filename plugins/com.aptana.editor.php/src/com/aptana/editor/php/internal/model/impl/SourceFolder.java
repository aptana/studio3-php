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
