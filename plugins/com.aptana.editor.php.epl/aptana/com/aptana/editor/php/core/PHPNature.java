/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org2.eclipse.php.internal.core.project.options.PHPProjectOptions;

import com.aptana.core.build.UnifiedBuilder;
import com.aptana.core.util.ResourceUtil;

/**
 * Aptana PHP Nature definition.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public final class PHPNature implements IPHPNature
{

	/**
	 * Builder ID
	 */
	public static final String BUILDER_ID = "com.aptana.editor.php.aptanaPhpBuilder"; //$NON-NLS-1$

	/**
	 * The PHP Nature ID.
	 */
	public static final String NATURE_ID = "com.aptana.editor.php.phpNature"; //$NON-NLS-1$

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
		ResourceUtil.addBuilder(getProject(), BUILDER_ID);
		ResourceUtil.addBuilder(getProject(), UnifiedBuilder.ID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException
	{
		ResourceUtil.removeBuilder(getProject(), BUILDER_ID);
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

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.IPHPNature#getOptions()
	 */
	public PHPProjectOptions getOptions()
	{
		// TODO - Implement that later on.
		return null;
	}
}
