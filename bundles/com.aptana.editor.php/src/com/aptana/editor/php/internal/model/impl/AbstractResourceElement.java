/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.model.impl;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.IParent;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.ProjectBuildPath;
import com.aptana.editor.php.internal.builder.WorkspaceFolderBuildpath;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IBuildPathResource;
import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;

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
	protected AbstractResourceElement(IBuildPathResource resource)
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
