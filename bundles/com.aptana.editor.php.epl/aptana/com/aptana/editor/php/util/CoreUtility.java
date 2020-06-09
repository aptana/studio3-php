/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package com.aptana.editor.php.util;

import java.text.MessageFormat;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.custom.BusyIndicator;
import org.osgi.framework.Bundle;

import com.aptana.editor.php.epl.PHPEplPlugin;

public class CoreUtility
{

	/**
	 * Creates a folder and all parent folders if not existing. Project must exist.
	 * <code> org.eclipse.ui.dialogs.ContainerGenerator</code> is too heavy (creates a runnable)
	 */
	public static void createFolder(IFolder folder, boolean force, boolean local, IProgressMonitor monitor)
			throws CoreException
	{
		if (!folder.exists())
		{
			IContainer parent = folder.getParent();
			if (parent instanceof IFolder)
			{
				createFolder((IFolder) parent, force, local, null);
			}
			folder.create(force, local, monitor);
		}
	}

	/**
	 * Creates an extension. If the extension plugin has not been loaded a busy cursor will be activated during the
	 * duration of the load.
	 * 
	 * @param element
	 *            the config element defining the extension
	 * @param classAttribute
	 *            the name of the attribute carrying the class
	 * @return the extension object
	 */
	public static Object createExtension(final IConfigurationElement element, final String classAttribute)
			throws CoreException
	{
		// If plugin has been loaded create extension.
		// Otherwise, show busy cursor then create extension.
		String pluginId = element.getNamespaceIdentifier();
		Bundle bundle = Platform.getBundle(pluginId);
		if (bundle != null && bundle.getState() == Bundle.ACTIVE)
		{
			return element.createExecutableExtension(classAttribute);
		}
		else
		{
			final Object[] ret = new Object[1];
			final CoreException[] exc = new CoreException[1];
			BusyIndicator.showWhile(null, new Runnable()
			{
				public void run()
				{
					try
					{
						ret[0] = element.createExecutableExtension(classAttribute);
					}
					catch (CoreException e)
					{
						exc[0] = e;
					}
				}
			});
			if (exc[0] != null)
				throw exc[0];
			else
				return ret[0];
		}
	}

	/**
	 * Starts a build in the background.
	 * 
	 * @param project
	 *            The project to build or <code>null</code> to build the workspace.
	 */
	public static void startBuildInBackground(final IProject project)
	{
		getBuildJob(project).schedule();
	}

	private static final class BuildJob extends Job
	{
		private final IProject fProject;

		private BuildJob(String name, IProject project)
		{
			super(name);
			fProject = project;
		}

		public boolean isCoveredBy(BuildJob other)
		{
			if (other.fProject == null)
			{
				return true;
			}
			return fProject != null && fProject.equals(fProject);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor)
		{
			synchronized (getClass())
			{
				if (monitor.isCanceled())
				{
					return Status.CANCEL_STATUS;
				}
				Job[] buildJobs = Job.getJobManager().find(ResourcesPlugin.FAMILY_MANUAL_BUILD);
				for (Job curr : buildJobs)
				{
					if (curr != this && curr instanceof BuildJob)
					{
						BuildJob job = (BuildJob) curr;
						if (job.isCoveredBy(this))
						{
							curr.cancel(); // cancel all other build jobs of our kind
						}
					}
				}
			}
			try
			{
				// FIXME: Shalom - Integrate the Task Tags project/workspace rebuild with a fully integrated builder.
				// SG: For now, till we'll have the builder fully integrated, we use the PHPGlobalIndexer clean
				// functions
				// to clear the memory and rebuild the workspace or the project.
				if (fProject != null)
				{
					monitor.beginTask(MessageFormat.format(Messages.CoreUtility_buildProject, new Object[] { fProject
							.getName() }), 2);
					fProject.build(IncrementalProjectBuilder.FULL_BUILD, new SubProgressMonitor(monitor, 1));
					PHPEplPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
							new SubProgressMonitor(monitor, 1));
					// PHPGlobalIndexer.getInstance().clean(fProject);
				}
				else
				{
					monitor.beginTask(Messages.CoreUtility_buildAll, 2);
					PHPEplPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD,
							new SubProgressMonitor(monitor, 2));
					// PHPGlobalIndexer.getInstance().cleanAllProjects();
				}
			}
			catch (CoreException e)
			{
				return e.getStatus();
			}
			catch (OperationCanceledException e)
			{
				return Status.CANCEL_STATUS;
			}
			finally
			{
				monitor.done();
			}
			return Status.OK_STATUS;
		}

		public boolean belongsTo(Object family)
		{
			return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
		}
	}

	/**
	 * Returns a build job
	 * 
	 * @param project
	 *            The project to build or <code>null</code> to build the workspace.
	 */
	public static Job getBuildJob(final IProject project)
	{
		Job buildJob = new BuildJob(Messages.CoreUtility_rebuilding, project);
		buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		buildJob.setUser(true);
		return buildJob;
	}

	/**
	 * Set the autobuild to the value of the parameter and return the old one.
	 * 
	 * @param state
	 *            the value to be set for autobuilding.
	 * @return the old value of the autobuild state
	 */
	public static boolean enableAutoBuild(boolean state) throws CoreException
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription desc = workspace.getDescription();
		boolean isAutoBuilding = desc.isAutoBuilding();
		if (isAutoBuilding != state)
		{
			desc.setAutoBuilding(state);
			workspace.setDescription(desc);
		}
		return isAutoBuilding;
	}

	public static boolean isPartialPrefixOf(IPath prefix, IPath path)
	{
		prefix = pathToLower(prefix);
		path = pathToLower(path);
		if (prefix.isPrefixOf(path))
			return true;
		if (prefix.segmentCount() > path.segmentCount())
			return false;
		if (prefix.segmentCount() != 0 && !prefix.hasTrailingSeparator()
				&& prefix.removeLastSegments(1).isPrefixOf(path)
				&& path.segment(prefix.segmentCount() - 1).startsWith(prefix.lastSegment()))
			return true;
		return false;
	}

	private static IPath pathToLower(IPath path)
	{
		String pathString = path.toString();
		int length = pathString.length();
		char[] lowerChars = new char[length];
		for (int i = 0; i < length; ++i)
		{ // don't trust to String.toLowerCase(), because it's locale based, while Path's compare is not
			lowerChars[i] = Character.toLowerCase(pathString.charAt(i));
		}
		return new Path(new String(lowerChars));
	}

}
