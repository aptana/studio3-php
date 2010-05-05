/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
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
 */
public final class PHPNature implements IProjectNature
{

	/**
	 * NATURE_ID
	 */
	public static final String NATURE_ID = "com.aptana.ide.editor.php.phpnature"; //$NON-NLS-1$
	private IProject project;

	/**
	 * Constructor.
	 */
	public PHPNature()
	{
	}

	/**
	 * Constructor.
	 */
	public PHPNature(IProject project)
	{
		this.project = project;
	}

	public void configure() throws CoreException
	{
		// register the PHP builder
		addToBuildSpec(PHPEditorPlugin.BUILDER_ID);
	}

	public void deconfigure() throws CoreException
	{
		// unregister the PHP builder
		removeFromBuildSpec(PHPEditorPlugin.BUILDER_ID);
	}

	public IProject getProject()
	{
		return project;
	}

	public void setProject(IProject project)
	{
		this.project = project;

	}

	/**
	 * Adds a builder to the build spec for the given project.
	 */
	protected void addToBuildSpec(String builderID) throws CoreException
	{

		IProjectDescription description = this.project.getDescription();
		int scriptCommandIndex = getScriptCommandIndex(description.getBuildSpec());

		if (scriptCommandIndex == -1)
		{

			// Add a PHP command to the build spec
			ICommand command = description.newCommand();
			command.setBuilderName(builderID);
			setScriptCommand(description, command);
		}
	}

	/**
	 * Removes the given builder from the build spec for the given project.
	 */
	protected void removeFromBuildSpec(String builderID) throws CoreException
	{

		IProjectDescription description = this.project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i)
		{
			if (commands[i].getBuilderName().equals(builderID))
			{
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				this.project.setDescription(description, null);
				return;
			}
		}
	}

	/**
	 * Update the Script command in the build spec (replace existing one if present, add one first if none).
	 */
	private void setScriptCommand(IProjectDescription description, ICommand newCommand) throws CoreException
	{

		ICommand[] oldBuildSpec = description.getBuildSpec();
		int oldScriptCommandIndex = getScriptCommandIndex(oldBuildSpec);
		ICommand[] newCommands;

		if (oldScriptCommandIndex == -1)
		{
			// Add a Java build spec before other builders (1FWJK7I)
			newCommands = new ICommand[oldBuildSpec.length + 1];
			System.arraycopy(oldBuildSpec, 0, newCommands, 1, oldBuildSpec.length);
			newCommands[0] = newCommand;
		}
		else
		{
			oldBuildSpec[oldScriptCommandIndex] = newCommand;
			newCommands = oldBuildSpec;
		}

		// Commit the spec change into the project
		description.setBuildSpec(newCommands);
		this.project.setDescription(description, null);
	}

	/**
	 * Find the specific Script command amongst the given build spec and return its index or -1 if not found.
	 */
	private int getScriptCommandIndex(ICommand[] buildSpec)
	{

		for (int i = 0; i < buildSpec.length; ++i)
		{
			if (buildSpec[i].getBuilderName().equals(PHPEditorPlugin.BUILDER_ID))
			{
				return i;
			}
		}
		return -1;
	}
}
