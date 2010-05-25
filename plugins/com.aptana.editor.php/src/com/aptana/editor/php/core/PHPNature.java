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
	public static final String NATURE_ID = PHPEditorPlugin.PLUGIN_ID + ".phpNature"; //$NON-NLS-1$
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
		 addToBuildSpec(PHPEditorPlugin.BUILDER_ID);
	}

	public void deconfigure() throws CoreException
	{
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
	 * Set the project's build spec.
	 */
	private void setBuildSpec(IProjectDescription description, ICommand newCommand) throws CoreException
	{

		ICommand[] prevBuildSpec = description.getBuildSpec();
		int prevBuildSpecIndex = getBuildSpecIndex(prevBuildSpec);
		ICommand[] newCommands;

		if (prevBuildSpecIndex == -1)
		{
			newCommands = new ICommand[prevBuildSpec.length + 1];
			System.arraycopy(prevBuildSpec, 0, newCommands, 1, prevBuildSpec.length);
			newCommands[0] = newCommand;
		}
		else
		{
			prevBuildSpec[prevBuildSpecIndex] = newCommand;
			newCommands = prevBuildSpec;
		}

		description.setBuildSpec(newCommands);
		project.setDescription(description, null);
	}

	/**
	 * Returns the PHP builder ID index in the commands array.
	 */
	private int getBuildSpecIndex(ICommand[] buildSpec)
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
