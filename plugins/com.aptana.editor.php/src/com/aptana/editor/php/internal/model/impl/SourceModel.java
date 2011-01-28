/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.model.impl;

import java.util.ArrayList;
import java.util.List;

import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.ISourceModel;
import com.aptana.editor.php.core.model.ISourceProject;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.ProjectBuildPath;
import com.aptana.editor.php.internal.core.builder.IBuildPath;

/**
 * SourceModel
 * 
 * @author Denis Denisenko
 */
public class SourceModel extends AbstractModelElement implements ISourceModel
{

	/**
	 * Element name.
	 */
	public static final String ELEMENT_NAME = "SourceModel"; //$NON-NLS-1$

	/**
	 * {@inheritDoc}
	 */
	public ISourceProject getProject(String name)
	{
		for (ISourceProject project : getProjects())
		{
			if (project.getElementName().equals(name))
			{
				return project;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ISourceProject> getProjects()
	{
		List<ISourceProject> result = new ArrayList<ISourceProject>();

		List<IBuildPath> buildPaths = BuildPathManager.getInstance().getBuildPaths();
		for (IBuildPath path : buildPaths)
		{
			if (path instanceof ProjectBuildPath)
			{
				result.add(new SourceProject((ProjectBuildPath) path));
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean exists()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public IModelElement getAncestor(int ancestorType)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getElementName()
	{
		return ELEMENT_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getElementType()
	{
		return IModelElement.MODEL;
	}

	/**
	 * {@inheritDoc}
	 */
	public IModelElement getParent()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ISourceProject getSourceProject()
	{
		return null;
	}
}
