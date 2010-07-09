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
package com.aptana.editor.php.internal.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPNature;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.builder.preferences.DependenciesManager;
import com.aptana.editor.php.internal.builder.preferences.IProjectDependencyListener;
import com.aptana.editor.php.internal.builder.preferences.ProjectDependencies;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IBuildPathsListener;
import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * BuildPathManager
 * 
 * @author Denis Denisenko
 */
public final class BuildPathManager
{

	private static class Mutex
	{
	};

	private static Mutex mutex = new Mutex();

	/**
	 * Projects delta visitor.
	 * 
	 * @author Denis Denisenko
	 */
	private class ProjectsDeltaVisitor implements IResourceDeltaVisitor
	{
		/**
		 * Added projects to fill.
		 */
		private List<IProject> added;

		/**
		 * Removed projects to fill.
		 */
		private List<IProject> removed;

		/**
		 * ProjectsDeltaVisitor constructor.
		 * 
		 * @param added
		 *            - added projects list to fill.
		 * @param removed
		 *            - removed projects list to fill.
		 */
		public ProjectsDeltaVisitor(List<IProject> added, List<IProject> removed)
		{
			this.added = added;
			this.removed = removed;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean visit(IResourceDelta delta) throws CoreException
		{
			IResource resource = delta.getResource();

			// ignoring inaccessible resources
			if (!resource.isAccessible() && delta.getKind() != IResourceDelta.REMOVED)
			{
				return false;
			}

			if (!(resource instanceof IProject))
			{
				return true;
			}

			IProject project = (IProject) resource;

			switch (delta.getKind())
			{
				case IResourceDelta.ADDED:
					added.add(project);
					break;
				case IResourceDelta.REMOVED:
					removed.add(project);
					break;
				case IResourceDelta.CHANGED:
					if (getBuildPathByResource(project) == null && project.hasNature(PHPNature.NATURE_ID))
					{
						added.add(project);
					}
					else
					{
						if (!project.isOpen() || !project.hasNature(PHPNature.NATURE_ID))
						{
							removed.add(project);
						}
					}
				default:
					break;
			}

			return true;
		}
	}

	/**
	 * Build path manager instance.
	 */
	private static BuildPathManager instance = null;

	/**
	 * Gets build path manager instance.
	 * 
	 * @return build path manager instance.
	 */
	public static BuildPathManager getInstance()
	{
		synchronized (mutex)
		{
			if (instance == null)
			{
				instance = new BuildPathManager();
			}
			return instance;
		}
	}

	/**
	 * Map of all existing build paths. Map from build path resource to build path.
	 */
	private Map<Object, IBuildPath> buildPaths = new HashMap<Object, IBuildPath>();

	/**
	 * Listeners.
	 */
	private Set<IBuildPathsListener> listeners = new HashSet<IBuildPathsListener>();

	/**
	 * Gets all existing build paths.
	 * 
	 * @return existing build paths.
	 */
	public synchronized List<IBuildPath> getBuildPaths()
	{
		List<IBuildPath> result = new ArrayList<IBuildPath>();
		result.addAll(buildPaths.values());
		return result;
	}

	/**
	 * Gets build path by resource, build path was originally created from.
	 * 
	 * @param resource
	 *            - resource.
	 * @return build path or null.
	 */
	public synchronized IBuildPath getBuildPathByResource(Object resource)
	{
		return buildPaths.get(resource);
	}

	/**
	 * Gets module by resource, module was originally created from.
	 * 
	 * @param resource
	 *            - resource.
	 * @return module or null.
	 */
	public synchronized IModule getModuleByResource(Object resource)
	{
		for (IBuildPath path : buildPaths.values())
		{
			IModule module = path.getModule(resource);
			if (module != null)
			{
				return module;
			}
		}
		return null;
	}

	/**
	 * Gets directory by resource, directory was originally created from.
	 * 
	 * @param resource
	 *            - resource.
	 * @return module or null.
	 */
	public synchronized IDirectory getDirectoryByResource(Object resource)
	{
		for (IBuildPath path : buildPaths.values())
		{
			IDirectory directory = path.getDirectory(resource);
			if (directory != null)
			{
				return directory;
			}
		}
		return null;
	}

	public synchronized void addBuildPathChangeListener(IBuildPathsListener listener)
	{
		listeners.add(listener);
	}

	public synchronized void removeBuildPathChangeListener(IBuildPathsListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Adds build path.
	 * 
	 * @param resource
	 *            - build path resource.
	 * @return added build path or null if build path cannot be created
	 */
	public synchronized IBuildPath addBuildPath(Object resource)
	{
		IBuildPath path = createBuildPathByResource(resource);
		internalAddBuldPath(resource, path);

		return path;
	}

	private void internalAddBuldPath(Object resource, IBuildPath path)
	{
		buildPaths.put(resource, path);

		List<IBuildPath> added = new ArrayList<IBuildPath>(1);
		List<IBuildPath> removed = new ArrayList<IBuildPath>(0);
		added.add(path);
		notifyChanged(added, removed);
	}

	/**
	 * Removes build path.
	 * 
	 * @param resource
	 *            - build path resource.
	 */
	public synchronized void removeBuildPath(Object resource)
	{
		IBuildPath path = getBuildPathByResource(resource);
		if (path == null)
		{
			return;
		}

		buildPaths.remove(resource);

		List<IBuildPath> added = new ArrayList<IBuildPath>(0);
		List<IBuildPath> removed = new ArrayList<IBuildPath>(1);
		removed.add(path);
		notifyChanged(added, removed);
	}

	/**
	 * BuildPathManager private constructor.
	 */
	private BuildPathManager()
	{
		long l0 = System.currentTimeMillis();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		indexExternalLibraries();
		indexLocalProjects(workspace);
		bindListeners(workspace);
		if (PHPEditorPlugin.DEBUG)
		{
			long l1 = System.currentTimeMillis();
			System.out.println("Indexer init:" + (l1 - l0)); //$NON-NLS-1$
		}
	}

	public void indexExternalLibraries()
	{
		IPHPLibrary[] all = LibraryManager.getInstance().getAllLibraries();
		for (IPHPLibrary l : all)
		{
			for (String s : l.getDirectories())
			{
				File path = new File(s);
				if (path != null)
				{
					FileSystemBuildPath fileSystemBuildPath = new FileSystemBuildPath(path);
					internalAddBuldPath(path, fileSystemBuildPath);
				}
			}
		}
		LibraryManager.getInstance().addLibraryListener(new ILibraryListener()
		{

			public void librariesChanged(Set<IPHPLibrary> turnedOn, Set<IPHPLibrary> turnedOf)
			{
				try
				{
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					root.accept(new IResourceVisitor()
					{
						public boolean visit(IResource resource) throws CoreException
						{
							// ignoring inaccessible resources
							if (!resource.isAccessible())
							{
								return false;
							}

							if (resource instanceof IProject)
							{
								if (buildPaths.containsKey(resource))
								{
									IProject project = (IProject) resource;
									ProjectDependencies dependencies = DependenciesManager.getDependencies(project);

									handleDependenciesChange(project, dependencies);
								}
								return false;
							}

							return true;
						}
					});
				}
				catch (CoreException e)
				{
					PHPEditorPlugin.logError(e);
				}
			}

			public void userLibrariesChanged(UserLibrary[] newLibraries)
			{
				Set<IPHPLibrary> emptySet = Collections.emptySet();
				HashSet<File> files = new HashSet<File>();
				for (Object o : buildPaths.keySet())
				{
					if (o instanceof File)
					{
						files.add((File) o);
					}
				}
				HashSet<File> currrentFiles = new HashSet<File>(files);
				IPHPLibrary[] current = LibraryManager.getInstance().getAllLibraries();
				for (IPHPLibrary l : current)
				{
					List<String> directories = l.getDirectories();
					for (String s : directories)
					{
						File file = new File(s);
						files.remove(file);
					}
				}
				for (File f : files)
				{
					IBuildPath path = getBuildPathByResource(f);
					removeBuildPath(f);
					path.close();
					currrentFiles.remove(f);
				}
				for (UserLibrary l : newLibraries)
				{
					if (!LibraryManager.getInstance().isTurnedOn(l))
					{
						continue;
					}

					List<String> directories = l.getDirectories();
					for (String s : directories)
					{
						File file = new File(s);

						if (!buildPaths.containsKey(file))
						{
							FileSystemBuildPath fileSystemBuildPath = new FileSystemBuildPath(file);
							internalAddBuldPath(file, fileSystemBuildPath);
						}
					}
				}
				librariesChanged(emptySet, emptySet);
			}

		});
	}

	/**
	 * Binds required listeners.
	 * 
	 * @param workspace
	 *            - workspace.
	 */
	private void bindListeners(IWorkspace workspace)
	{
		// binding listener to workspace
		IResourceChangeListener workspaceListener = new IResourceChangeListener()
		{
			/**
			 * {@inheritDoc}
			 */
			public void resourceChanged(IResourceChangeEvent event)
			{
				final List<IProject> added = new ArrayList<IProject>();
				final List<IProject> removed = new ArrayList<IProject>();

				IResourceDelta delta = event.getDelta();
				if (delta != null)
				{
					try
					{
						delta.accept(new ProjectsDeltaVisitor(added, removed));
					}
					catch (CoreException e)
					{
						PHPEditorPlugin.logError(e);
					}
				}
				else
				{
					if (event.getResource() instanceof IProject && event.getType() == IResourceChangeEvent.PRE_CLOSE)
					{
						removed.add((IProject) event.getResource());
					}
				}
				if (!added.isEmpty() || !removed.isEmpty())
				{
					handleChanged(added, removed);
				}
			}

		};
		workspace.addResourceChangeListener(workspaceListener);

		// binding listener to project dependencies changes
		DependenciesManager.addListener(new IProjectDependencyListener()
		{
			/**
			 * {@inheritDoc}
			 */
			public void dependenciesChanged(IProject project, ProjectDependencies dependencies)
			{
				// ignoring inaccessible projects
				if (!project.isAccessible())
				{
					return;
				}

				handleDependenciesChange(project, dependencies);
			}
		});
	}

	/**
	 * Handles changes in the projects state.
	 * 
	 * @param added
	 *            - added projects.
	 * @param removed
	 *            - remove projects.
	 */
	public void handleChanged(List<IProject> added, List<IProject> removed)
	{
		List<IBuildPath> addedPaths = new ArrayList<IBuildPath>();
		List<IBuildPath> removedPaths = new ArrayList<IBuildPath>();

		if (added.isEmpty() && removed.isEmpty())
		{
			return;
		}

		List<IProject> toRemove = new ArrayList<IProject>();

		for (IProject addedProject : added)
		{
			IBuildPath path = createBuildPathByResource(addedProject);
			if (path != null)
			{
				buildPaths.put(addedProject, path);
				addedPaths.add(path);
			}
		}

		for (IProject removedProject : removed)
		{
			IBuildPath path = getBuildPathByResource(removedProject);
			if (path != null)
			{
				removedPaths.add(path);
				toRemove.add(removedProject);
			}
		}

		for (IProject toRemoveProject : toRemove)
		{
			IBuildPath path = buildPaths.get(toRemoveProject);
			buildPaths.remove(toRemoveProject);
			removeDepencies(path);
		}

		for (IBuildPath path : removedPaths)
		{
			path.close();
		}

		notifyChanged(addedPaths, removedPaths);
		if (!added.isEmpty())
		{
			updateProjectsDependencies(ResourcesPlugin.getWorkspace().getRoot());
		}
		PHPGlobalIndexer.getInstance().indexLocalModules();
	}

	/**
	 * Notifies build paths changed.
	 * 
	 * @param added
	 *            - added build paths.
	 * @param removed
	 *            - removed build paths.
	 */
	private void notifyChanged(List<IBuildPath> added, List<IBuildPath> removed)
	{
		for (IBuildPathsListener listener : listeners)
		{
			try
			{
				listener.changed(added, removed);
			}
			catch (Throwable th)
			{
				PHPEditorPlugin.logError("Unable notifying build path change listener", th); //$NON-NLS-1$
			}
		}
	}

	private void indexLocalProjects(IWorkspace workspace)
	{
		// creating build path for each project
		IWorkspaceRoot root = workspace.getRoot();
		try
		{
			root.accept(new IResourceVisitor()
			{
				public boolean visit(IResource resource) throws CoreException
				{
					// not visiting inaccessible resources
					if (!resource.isAccessible())
					{
						return false;
					}

					if (resource instanceof IProject)
					{
						if (!buildPaths.containsKey(resource))
						{
							IBuildPath path = createBuildPathByResource(resource);
							if (path != null)
							{
								buildPaths.put(resource, path);
							}
						}
						return false;
					}

					return true;
				}

			});
		}
		catch (CoreException e)
		{
			PHPEditorPlugin.logError(e);
		}

		updateProjectsDependencies(root);
	}

	/**
	 * Traverse the accessible projects in the workspace and update their dependencies. This operation is needed on
	 * startup and when a project is opened.
	 * 
	 * @param root
	 *            The workspace root to visit and update
	 */
	private void updateProjectsDependencies(IWorkspaceRoot root)
	{
		try
		{
			root.accept(new IResourceVisitor()
			{
				public boolean visit(IResource resource) throws CoreException
				{
					// ignoring inaccessible resources
					if (!resource.isAccessible())
					{
						return false;
					}

					if (resource instanceof IProject)
					{
						if (buildPaths.containsKey(resource))
						{
							IProject project = (IProject) resource;
							ProjectDependencies dependencies = DependenciesManager.getDependencies(project);

							handleDependenciesChange(project, dependencies);
						}
						return false;
					}

					return true;
				}
			});
		}
		catch (CoreException e)
		{
			PHPEditorPlugin.logError(e);
		}
	}

	/**
	 * Creates new build path instance by resource.
	 * 
	 * @param resource
	 *            - resource to create build path from.
	 * @return new build path or null if resource is not recognized.
	 */
	private IBuildPath createBuildPathByResource(Object resource)
	{
		// ignoring inaccessible resources
		if (resource instanceof IResource)
		{
			if (!((IResource) resource).isAccessible())
			{
				return null;
			}
			if (resource instanceof IProject)
			{
				IProject project = (IProject) resource;
				try
				{

					if (((IProject) resource).hasNature(PHPNature.NATURE_ID))
					{
						return new ProjectBuildPath(project);
					}
				}
				catch (CoreException e)
				{
					PHPEditorPlugin.logError("Error in the PHP build", e); //$NON-NLS-1$
				}
			}
		}
		else if (resource instanceof File && ((File) resource).exists() && ((File) resource).isDirectory())
		{
			return new FileSystemBuildPath((File) resource);
		}

		return null;
	}

	/**
	 * Handles project dependencies change.
	 * 
	 * @param project
	 *            - project.
	 * @param dependencies
	 *            - dependencies.
	 */
	private void handleDependenciesChange(IProject project, ProjectDependencies dependencies)
	{
		IBuildPath path = buildPaths.get(project);
		if (path == null)
		{
			return;
		}

		path.clearDependencies();

		for (IResource projectDependency : dependencies.getWorkspaceResources())
		{
			if (projectDependency instanceof IProject)
			{
				IBuildPath dependencyBuildPath = buildPaths.get(projectDependency);
				if (dependencyBuildPath != null)
				{
					path.addDependency(dependencyBuildPath);
				}
			}
			else if (projectDependency instanceof IFolder)
			{
				IBuildPath dependencyBuildPath = new WorkspaceFolderBuildpath((IFolder) projectDependency);
				path.addDependency(dependencyBuildPath);
			}
		}
		for (File directoryDependency : dependencies.getDirectories())
		{
			// IBuildPath dependencyBuildPath =
			// new FileSystemBuildPath(directoryDependency);
			IBuildPath dependencyBuildPath = addBuildPath(directoryDependency);
			// buildPaths.put(directoryDependency, dependencyBuildPath);
			path.addDependency(dependencyBuildPath);
		}
		IPHPLibrary[] allLibraries = LibraryManager.getInstance().getAllLibraries();
		HashSet<IPHPLibrary> usedLibraries = new HashSet<IPHPLibrary>(Arrays.asList(allLibraries));
		if (dependencies.isUsesCustomLibs())
		{
			ArrayList<String> notUsedLibrariesIds = dependencies.getNotUsedLibrariesIds();
			for (String s : notUsedLibrariesIds)
			{
				IPHPLibrary library = LibraryManager.getInstance().getLibrary(s);
				if (library != null)
				{
					usedLibraries.remove(library);
				}
			}
		}
		else
		{
			for (IPHPLibrary l : allLibraries)
			{
				if (!l.isTurnedOn())
				{
					usedLibraries.remove(l);
				}
			}
		}
		for (IPHPLibrary l : usedLibraries)
		{
			for (String s : l.getDirectories())
			{
				File fl = new File(s);
				IBuildPath dependency = buildPaths.get(fl);
				if (dependency != null)
				{
					path.addDependency(dependency);
				}
			}
		}
	}

	/**
	 * Removes this build path from all who depends on it.
	 * 
	 * @param path
	 *            - path.
	 */
	private void removeDepencies(IBuildPath path)
	{
		for (IBuildPath currentPath : buildPaths.values())
		{
			currentPath.removeDependency(path);
		}
	}
}
