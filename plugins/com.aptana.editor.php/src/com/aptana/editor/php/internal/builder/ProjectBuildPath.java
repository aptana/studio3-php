/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.core.builder.IBuildPathResource;
import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * ProjectBuildPath
 * 
 * @author Denis Denisenko
 */
public class ProjectBuildPath extends AbstractBuildPath
{

	/**
	 * PHP elements delta visitor.
	 * 
	 * @author Denis Denisenko
	 */
	private final class PHPElementsDeltaVisitor implements IResourceDeltaVisitor
	{
		/**
		 * Added modules to fill.
		 */
		private List<IModule> added;

		/**
		 * Changed modules to fill.
		 */
		private List<IModule> changed;

		/**
		 * Removed modules to fill.
		 */
		private List<IModule> removed;

		/**
		 * Added modules to fill.
		 */
		private List<IDirectory> addedDirectories;

		/**
		 * Removed modules to fill.
		 */
		private List<IDirectory> removedDirectories;

		/**
		 * PHPElementsDeltaVisitor constructor.
		 * 
		 * @param added
		 *            - added modules list to fill.
		 * @param changed
		 *            - changed modules list to fill.
		 * @param removed
		 *            - removed modules list to fill.
		 */
		private PHPElementsDeltaVisitor(List<IModule> added, List<IModule> changed, List<IModule> removed,
				List<IDirectory> addedDirectories, List<IDirectory> removedDirectories)
		{
			this.added = added;
			this.changed = changed;
			this.removed = removed;
			this.addedDirectories = addedDirectories;
			this.removedDirectories = removedDirectories;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean visit(IResourceDelta delta)
		{
			IResource resource = delta.getResource();

			// ignoring inaccessible resources
			if (!resource.isAccessible() && delta.getKind() != IResourceDelta.REMOVED)
			{
				return false;
			}

			if (resource instanceof IProject)
			{
				if (!resource.equals(project))
				{
					return false;
				}
				/*
				 * if (!project.isSynchronized(1)) { return false; }
				 */
			}

			if (!(resource instanceof IFile || resource instanceof IFolder))
			{
				return true;
			}

			if (!resource.getProject().equals(project))
			{
				return false;
			}

			// we are not interested in flag changes.
			if (delta.getFlags() == IResourceDelta.MARKERS)
			{
				return false;
			}

			if (resource instanceof IFile)
			{
				IModule module = null;

				switch (delta.getKind())
				{
					case IResourceDelta.ADDED:
						module = PHPLocalModuleFactory.getModule(resource, ProjectBuildPath.this);
						if (module == null)
						{
							return true;
						}
						added.add(module);
						if ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0)
						{
							IPath fromPath = delta.getMovedFromPath();
							if (fromPath != null)
							{
								IFile fromFile = resource.getWorkspace().getRoot().getFile(fromPath);
								if (fromFile != null)
								{
									IModule oldModule = PHPLocalModuleFactory.getModuleUnsafe(fromFile,
											ProjectBuildPath.this);
									if (oldModule != null)
									{
										removed.add(oldModule);
									}
								}
							}
						}
						break;
					case IResourceDelta.CHANGED:
						module = PHPLocalModuleFactory.getModule(resource, ProjectBuildPath.this);
						if (module == null)
						{
							return true;
						}
						changed.add(module);
						break;
					case IResourceDelta.REMOVED:
						module = PHPLocalModuleFactory.getModuleUnsafe(resource, ProjectBuildPath.this);
						if (module != null)
						{
							removed.add(module);
						}
						break;
					default:
						break;
				}
			}
			else if (resource instanceof IFolder)
			{
				// skipping unsynchronized resources.
				if (!resource.isSynchronized(1))
				{
					return true;
				}

				if (!FolderFilteringManager.acceptFolder((IFolder) resource))
				{
					return false;
				}

				IDirectory dir = new LocalDirectory((IFolder) resource, ProjectBuildPath.this);
				switch (delta.getKind())
				{
					case IResourceDelta.ADDED:
						addedDirectories.add(dir);
						break;
					case IResourceDelta.REMOVED:
						removedDirectories.add(dir);
						break;
					default:
						break;
				}
			}

			return true;
		}
	}

	/**
	 * Eclipse project.
	 */
	private IProject project;

	/**
	 * Modules.
	 */
	private Map<IFile, IModule> modules = new HashMap<IFile, IModule>();

	/**
	 * Directories.
	 */
	private Map<IContainer, IDirectory> directories = new HashMap<IContainer, IDirectory>();

	/**
	 * Workspace listener.
	 */
	private IResourceChangeListener workspaceListener;

	/**
	 * ProjectBuildPath constructor.
	 * 
	 * @param project
	 *            - eclipse project.
	 */
	public ProjectBuildPath(IProject project)
	{
		this.project = project;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		indexLocalResources();

		bindListeners(workspace);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IModule> getModules()
	{
		List<IModule> result = new ArrayList<IModule>();
		result.addAll(modules.values());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public void close()
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace != null && workspaceListener != null)
		{
			workspace.removeResourceChangeListener(workspaceListener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IModule getModule(Object moduleResource)
	{
		if (!(moduleResource instanceof IFile))
		{
			return null;
		}

		// this is possble in some cases
		IModule module = modules.get(moduleResource);
		if (module == null)
		{
			IFile f = (IFile) moduleResource;
			if (f.getProject().equals(this.project))
			{
				IModule module2 = PHPLocalModuleFactory.getModule(f, this);
				if (module2 != null)
				{
					modules.put(f, module2);
				}
				return module2;
			}
		}
		return module;
	}

	/**
	 * {@inheritDoc}
	 */
	public IDirectory getDirectory(Object directoryResource)
	{
		if (directoryResource instanceof IProject && project.equals(directoryResource))
		{
			if (!((IProject) directoryResource).isAccessible())
			{
				return null;
			}

			return new LocalDirectory(project, this);
		}

		if (!(directoryResource instanceof IFolder))
		{
			return null;
		}

		return directories.get(directoryResource);
	}

	/**
	 * {@inheritDoc}
	 */
	public IPath getResourcePath(IBuildPathResource resource)
	{
		if (resource instanceof LocalModule)
		{
			IFile moduleFile = ((LocalModule) resource).getFile();
			if (!project.equals(moduleFile.getProject()))
			{
				return null;
			}

			return moduleFile.getProjectRelativePath().makeAbsolute();
		}
		else if (resource instanceof LocalDirectory)
		{
			IResource fld = ((LocalDirectory) resource).getContainer();
			if (!project.equals(fld.getProject()))
			{
				return null;
			}

			return fld.getProjectRelativePath().makeAbsolute();
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IModule getModuleByPath(IPath path)
	{
		try
		{
			IResource resource = project.getFile(path);
			if (resource == null)
			{
				return null;
			}

			return getModule(resource);
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IDirectory getDirectoryByPath(IPath path)
	{
		if (path.segmentCount() == 0)
		{
			return new LocalDirectory(project, this);
		}

		// if (path.segmentCount() <= 2) {
		// return null;
		// }

		IResource resource = project.getFolder(path);
		if (resource == null)
		{
			return null;
		}

		return getDirectory(resource);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPassive()
	{
		return false;
	}

	/**
	 * Gets project.
	 * 
	 * @return project.
	 */
	public IProject getProject()
	{
		return project;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IModule> getModulesByPath(IPath path)
	{
		IResource resource = null;
		if (path.segmentCount() == 0)
		{
			resource = project;
		}
		else
		{
			resource = project.getFolder(path);
		}

		if (resource == null || !(resource instanceof IContainer) || !resource.exists())
		{
			return null;
		}

		IResource[] innerResources = null;
		try
		{
			innerResources = ((IContainer) resource).members();
		}
		catch (CoreException e)
		{
			IdeLog.logWarning(PHPEditorPlugin.getDefault(),
					"Error getting modules by path", e, PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
			return null;
		}
		List<IFile> innerFiles = new ArrayList<IFile>();
		for (IResource innerResource : innerResources)
		{
			if (innerResource instanceof IFile && ((IFile) innerResource).exists())
			{
				innerFiles.add((IFile) innerResource);
			}
		}

		List<IModule> result = new ArrayList<IModule>();
		for (IFile innerFile : innerFiles)
		{
			IModule currentModule = getModule(innerFile);
			if (currentModule != null)
			{
				result.add(currentModule);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IDirectory> getSubdirectoriesByPath(IPath path)
	{
		IResource resource = null;
		if (path.segmentCount() == 0)
		{
			resource = project;
		}
		else
		{
			resource = project.getFolder(path);
		}

		if (resource == null || !(resource instanceof IContainer) || !resource.exists())
		{
			return null;
		}

		IResource[] innerResources = null;
		try
		{
			innerResources = ((IContainer) resource).members();
		}
		catch (CoreException e)
		{
			IdeLog.logWarning(PHPEditorPlugin.getDefault(),
					"Error getting sub-directories by path", e, PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$

			return null;
		}
		List<IFolder> innerFolders = new ArrayList<IFolder>();
		for (IResource innerResource : innerResources)
		{
			if (innerResource instanceof IFolder && ((IFolder) innerResource).exists())
			{
				innerFolders.add((IFolder) innerResource);
			}
		}

		List<IDirectory> result = new ArrayList<IDirectory>();
		for (IFolder innerFolder : innerFolders)
		{
			// ignoring inaccessible resources
			if (!innerFolder.isAccessible())
			{
				continue;
			}

			IDirectory currentDir = new LocalDirectory(innerFolder, this);
			result.add(currentDir);
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(IModule module)
	{
		if (!(module instanceof LocalModule))
		{
			return false;
		}

		return getModule(((LocalModule) module).getFile()) != null;
	}

	/**
	 * Binds required listeners.
	 * 
	 * @param workspace
	 *            - workspace.
	 */
	private void bindListeners(IWorkspace workspace)
	{
		workspaceListener = new IResourceChangeListener()
		{

			public void resourceChanged(IResourceChangeEvent event)
			{
				final List<IModule> added = new ArrayList<IModule>();
				final List<IModule> changed = new ArrayList<IModule>();
				final List<IModule> removed = new ArrayList<IModule>();
				final List<IDirectory> addedDirectories = new ArrayList<IDirectory>();
				final List<IDirectory> removedDirectories = new ArrayList<IDirectory>();

				IResourceDelta delta = event.getDelta();
				if (delta != null)
				{
					try
					{
						delta.accept(new PHPElementsDeltaVisitor(added, changed, removed, addedDirectories,
								removedDirectories));
					}
					catch (CoreException e)
					{
						IdeLog.logWarning(PHPEditorPlugin.getDefault(),
								"Error binding listeners", e, PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
					}
				}

				Collection<IModule> modulesCollection = modules.values();
				Collection<IDirectory> directoriesCollection = directories.values();

				if (!added.isEmpty() || !removed.isEmpty() || !changed.isEmpty() || !addedDirectories.isEmpty()
						|| !removedDirectories.isEmpty())
				{
					// List<IModule> emptyModules = Collections.emptyList();
					// List<IDirectory> emptyDirectories = Collections.emptyList();

					// first notifying about removed modules and directories BEFORE we actually remove
					// them from the list
					notifyChangedBefore(changed, removed, removedDirectories);

					// removing modules
					modulesCollection.removeAll(removed);
					directoriesCollection.removeAll(removedDirectories);

					// adding new modules
					for (IModule currentModule : added)
					{
						if (currentModule instanceof LocalModule)
						{
							modules.put(((LocalModule) currentModule).getFile(), currentModule);
						}
					}

					// adding directories
					for (IDirectory dir : addedDirectories)
					{
						if (dir instanceof LocalDirectory)
						{
							directories.put(((LocalDirectory) dir).getContainer(), dir);
						}
					}

					// notifying about added modules, added directories and changed modules after
					// all additions are done
					notifyChangedAfter(added, changed, removed, addedDirectories, removedDirectories);
				}
			}
		};
		workspace.addResourceChangeListener(workspaceListener);
	}

	/**
	 * Lists local modules.
	 * 
	 * @param workspace
	 *            - workspace.
	 */
	private void indexLocalResources()
	{
		try
		{
			project.accept(new IResourceVisitor()
			{
				public boolean visit(IResource resource)
				{
					// ignoring inaccessible resources
					if (!resource.isAccessible())
					{
						return false;
					}

					if (resource instanceof IProject)
					{
						if (!resource.equals(project))
						{
							return false;
						}
					}

					if (!resource.getProject().equals(project))
					{
						return false;
					}

					if (resource instanceof IFile)
					{
						IModule module = PHPLocalModuleFactory.getModule(resource, ProjectBuildPath.this);
						if (module != null)
						{
							modules.put((IFile) resource, module);
						}
					}
					else if (resource instanceof IFolder)
					{
						IDirectory dir = new LocalDirectory((IFolder) resource, ProjectBuildPath.this);
						directories.put((IFolder) resource, dir);
					}

					return true;
				}

			});
		}
		catch (CoreException e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(),
					"Error indexing local resources", e, PHPEditorPlugin.INDEXER_SCOPE); //$NON-NLS-1$

		}
	}

	public String getHandleIdentifier()
	{
		return 'P' + project.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof ProjectBuildPath)
		{
			return ((ProjectBuildPath) obj).project.equals(this.project);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return project.hashCode();
	}

}
