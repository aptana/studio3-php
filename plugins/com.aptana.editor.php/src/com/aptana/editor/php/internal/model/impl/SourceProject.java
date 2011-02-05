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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.ISourceProject;
import com.aptana.editor.php.internal.builder.ProjectBuildPath;
import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.model.ModelManager;

/**
 * Source project.
 * 
 * @author Denis Denisenko
 */
public class SourceProject extends AbstractModelElement implements ISourceProject
{
	/**
	 * Build-path.
	 */
	private ProjectBuildPath buildPath;

	/**
	 * SourceProject constructor.
	 * 
	 * @param buildPath
	 */
	public SourceProject(ProjectBuildPath buildPath)
	{
		this.buildPath = buildPath;
	}

	/**
	 * Returns the project associated to this SourceProject instance.
	 */
	public IProject getProject()
	{
		return buildPath.getProject();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean exists()
	{
		return buildPath.getProject().exists();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getElementName()
	{
		return buildPath.getProject().getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getElementType()
	{
		return IModelElement.PROJECT;
	}

	/**
	 * {@inheritDoc}
	 */
	public IModelElement getParent()
	{
		return ModelManager.getInstance().getModel();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IModelElement> getChildren()
	{
		try
		{
			IResource[] members = buildPath.getProject().members();
			List<IModelElement> result = new ArrayList<IModelElement>();
			for (IResource member : members)
			{
				if (member instanceof IFile)
				{
					IModule module = buildPath.getModule(member);
					if (module != null)
					{
						result.add(new SourceModule(module));
					}
				}
				else if (member instanceof IFolder)
				{
					IDirectory dir = buildPath.getDirectory(member);
					if (dir != null)
					{
						result.add(new SourceFolder(dir));
					}
				}
			}

			return result;
		}
		catch (CoreException e)
		{
			return Collections.emptyList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasChildren()
	{
		try
		{
			return buildPath.getProject().members().length != 0;
		}
		catch (CoreException e)
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
		result = prime * result + ((buildPath == null) ? 0 : buildPath.hashCode());
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
		SourceProject other = (SourceProject) obj;
		if (buildPath == null)
		{
			if (other.buildPath != null)
				return false;
		}
		else if (!buildPath.equals(other.buildPath))
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public ISourceProject getSourceProject()
	{
		return this;
	}
}
