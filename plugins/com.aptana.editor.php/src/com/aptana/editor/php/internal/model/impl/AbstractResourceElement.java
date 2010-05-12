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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.IParent;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.IBuildPath;
import com.aptana.editor.php.internal.builder.IBuildPathResource;
import com.aptana.editor.php.internal.builder.IDirectory;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.builder.ProjectBuildPath;
import com.aptana.editor.php.internal.builder.WorkspaceFolderBuildpath;

/**
 * Abstract resource element.
 * 
 * @author Denis Denisenko
 */
public abstract class AbstractResourceElement extends AbstractModelElement implements IParent
{
	/**
	 * Build-path resource.
	 */
	private IBuildPathResource resource;

	/**
	 * AbstractResourceElement constructor.
	 * 
	 * @param resource
	 *            - resource.
	 */
	public AbstractResourceElement(IBuildPathResource resource)
	{
		this.resource = resource;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean exists()
	{
		IBuildPath buildPath = resource.getBuildPath();
		if (buildPath == null)
		{
			return false;
		}

		if (!buildPathExists(buildPath))
		{
			return false;
		}

		if (resource instanceof IDirectory)
		{
			return buildPath.getDirectoryByPath(resource.getPath()) != null;
		}
		else if (resource instanceof IModule)
		{
			return buildPath.getModuleByPath(resource.getPath()) != null;
		}
		else
		{
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractResourceElement other = (AbstractResourceElement) obj;
		if (resource == null)
		{
			if (other.resource != null)
				return false;
		}
		else if (!resource.equals(other.resource))
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getElementName()
	{
		return resource.getShortName();
	}

	/**
	 * {@inheritDoc}
	 */
	public IModelElement getParent()
	{
		IBuildPath buildPath = resource.getBuildPath();
		if (buildPath == null)
		{
			return null;
		}

		IPath modulePath = resource.getPath();
		IPath directoryPath = modulePath.removeLastSegments(1);
		if (directoryPath.segmentCount() == 0)
		{
			if (buildPath instanceof ProjectBuildPath)
			{
				return new SourceProject((ProjectBuildPath) buildPath);
			}
			else if (buildPath instanceof WorkspaceFolderBuildpath)
			{
				IFolder folder = ((WorkspaceFolderBuildpath) buildPath).getFolder();
				IProject prj = folder.getProject();
				IBuildPath prjBuildPath = BuildPathManager.getInstance().getBuildPathByResource(prj);
				if (prjBuildPath == null)
				{
					return null;
				}

				IDirectory dir = prjBuildPath.getDirectory(folder);
				if (dir == null)
				{
					return null;
				}

				return new SourceFolder(dir);
			}

			return null;
		}
		else
		{
			IDirectory directory = buildPath.getDirectoryByPath(directoryPath);
			if (directory == null)
			{
				return null;
			}

			return new SourceFolder(directory);
		}

	}

	@Override
	public String toString()
	{
		return toDebugString();
	}

	/**
	 * Gets resource.
	 * 
	 * @return resource.
	 */
	protected IBuildPathResource getBPResource()
	{
		return resource;
	}

	/**
	 * Checks whether build-path exists.
	 * 
	 * @param buildPath
	 *            - build-path.
	 * @return true if build-path exists, false otherwise.
	 */
	private boolean buildPathExists(IBuildPath buildPath)
	{
		if (buildPath instanceof ProjectBuildPath)
		{
			return ((ProjectBuildPath) buildPath).getProject().exists();
		}
		else if (buildPath instanceof WorkspaceFolderBuildpath)
		{
			return ((WorkspaceFolderBuildpath) buildPath).getFolder().exists();
		}

		return true;
	}
}
