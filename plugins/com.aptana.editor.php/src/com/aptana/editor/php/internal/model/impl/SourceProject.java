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
