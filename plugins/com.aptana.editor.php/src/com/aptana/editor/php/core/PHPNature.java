/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import com.aptana.core.build.UnifiedBuilder;
import com.aptana.core.util.ResourceUtil;
import com.aptana.editor.php.PHPEditorPlugin;

/**
 * Aptana PHP Nature definition.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public final class PHPNature implements IProjectNature
{

	/**
	 * The PHP Nature ID.
	 */
	public static final String NATURE_ID = PHPEditorPlugin.PLUGIN_ID + ".phpNature"; //$NON-NLS-1$
	
	private IProject fProject;

	/**
	 * Constructs a new PHPNature class.
	 */
	public PHPNature()
	{
	}

	/**
	 * Constructs a new PHPNature class.
	 * 
	 * @param fProject
	 */
	public PHPNature(IProject project)
	{
		this.fProject = project;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException
	{
		ResourceUtil.addBuilder(getProject(), PHPEditorPlugin.BUILDER_ID);
		ResourceUtil.addBuilder(getProject(), UnifiedBuilder.ID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException
	{
		ResourceUtil.removeBuilder(getProject(), PHPEditorPlugin.BUILDER_ID);
		ResourceUtil.removeBuilderIfOrphaned(getProject(), UnifiedBuilder.ID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject()
	{
		return fProject;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project)
	{
		this.fProject = project;

	}
}
