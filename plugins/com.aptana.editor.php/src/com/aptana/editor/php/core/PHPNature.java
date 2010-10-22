/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
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
package com.aptana.editor.php.core;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

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
		addToBuildSpec(PHPEditorPlugin.BUILDER_ID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException
	{
		removeFromBuildSpec(PHPEditorPlugin.BUILDER_ID);
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

	/**
	 * Adds a builder to the build spec for the given fProject.
	 */
	protected void addToBuildSpec(String builderID) throws CoreException
	{

		IProjectDescription description = this.fProject.getDescription();
		int scriptCommandIndex = getBuildSpecIndex(description.getBuildSpec());

		if (scriptCommandIndex == -1)
		{
			// Add a PHP command to the build spec
			ICommand command = description.newCommand();
			command.setBuilderName(builderID);
			setBuildSpec(description, command);
		}
	}

	/**
	 * Removes a builder with the given ID from the fProject specifications.
	 * 
	 * @param builderID
	 *            A builder ID.
	 * @throws CoreException
	 */
	protected void removeFromBuildSpec(String builderID) throws CoreException
	{

		IProjectDescription desc = fProject.getDescription();
		ICommand[] buildSpec = desc.getBuildSpec();
		for (int i = 0; i < buildSpec.length; i++)
		{
			if (buildSpec[i].getBuilderName().equals(builderID))
			{
				ICommand[] newBuildSpec = new ICommand[buildSpec.length - 1];
				System.arraycopy(buildSpec, 0, newBuildSpec, 0, i);
				System.arraycopy(buildSpec, i + 1, newBuildSpec, i, buildSpec.length - i - 1);
				desc.setBuildSpec(newBuildSpec);
				fProject.setDescription(desc, null);
				return;
			}
		}
	}

	/**
	 * Sets a build-spec on the current fProject.
	 * 
	 * @param description
	 * @param newCommand
	 * @throws CoreException
	 */
	protected void setBuildSpec(IProjectDescription description, ICommand newCommand) throws CoreException
	{

		ICommand[] prevBuildSpec = description.getBuildSpec();
		int index = getBuildSpecIndex(prevBuildSpec);
		ICommand[] buildSpecs;
		if (index == -1)
		{
			buildSpecs = new ICommand[prevBuildSpec.length + 1];
			System.arraycopy(prevBuildSpec, 0, buildSpecs, 1, prevBuildSpec.length);
			buildSpecs[0] = newCommand;
		}
		else
		{
			prevBuildSpec[index] = newCommand;
			buildSpecs = prevBuildSpec;
		}

		description.setBuildSpec(buildSpecs);
		fProject.setDescription(description, null);
	}

	/**
	 * Returns the index of the PHP builder ID.
	 * 
	 * @param buildSpec
	 * @return The index of the PHP builder in the build-spec array.
	 */
	private int getBuildSpecIndex(ICommand[] buildSpec)
	{

		for (int i = 0; i < buildSpec.length; i++)
		{
			if (buildSpec[i].getBuilderName().equals(PHPEditorPlugin.BUILDER_ID))
			{
				return i;
			}
		}
		return -1;
	}
}
