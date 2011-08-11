/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.core.builder.IBuildPathResource;
import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * FileSystemBuildPath
 * 
 * @author Denis Denisenko
 */
public class FileSystemBuildPath extends AbstractBuildPath
{
	/**
	 * File.
	 */
	private File file;

	/**
	 * Modules.
	 */
	private Map<File, IModule> modules = new HashMap<File, IModule>();

	/**
	 * Modules.
	 */
	private Map<File, IDirectory> directories = new HashMap<File, IDirectory>();

	/**
	 * FileSystemBuildPath constructor.
	 * 
	 * @param file
	 *            - root file.
	 */
	public FileSystemBuildPath(File file)
	{
		this.file = file;
		collectInitialResources();
	}

	/**
	 * {@inheritDoc}
	 */
	public void close()
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public IModule getModule(Object moduleResource)
	{
		return modules.get(moduleResource);
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
	 * @return directory
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * {@inheritDoc}
	 */
	public IModule getModuleByPath(IPath path)
	{
		IPath basePath = new Path(file.getAbsolutePath());
		IPath modulePath = basePath.append(path);
		File file = new File(modulePath.toOSString());
		return modules.get(file);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPassive()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IModule> getModulesByPath(IPath path)
	{
		IPath basePath = new Path(file.getAbsolutePath());
		IPath containerPath = basePath.append(path);
		File file = new File(containerPath.toOSString());
		if (!file.exists())
		{
			return null;
		}

		if (!file.isDirectory())
		{
			return null;
		}

		File[] childFiles = file.listFiles();
		List<IModule> result = new ArrayList<IModule>();
		for (File childFile : childFiles)
		{
			IModule currentmodule = getModule(childFile);
			if (currentmodule != null)
			{
				result.add(currentmodule);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IDirectory> getSubdirectoriesByPath(IPath path)
	{
		IPath basePath = new Path(file.getAbsolutePath());
		IPath containerPath = basePath.append(path);
		File file = new File(containerPath.toOSString());
		if (!file.exists())
		{
			return null;
		}

		if (!file.isDirectory())
		{
			return null;
		}

		File[] childFiles = file.listFiles();
		List<IDirectory> result = new ArrayList<IDirectory>();
		for (File childFile : childFiles)
		{
			IDirectory currentDirectory = getDirectory(childFile);
			if (currentDirectory != null)
			{
				result.add(currentDirectory);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public IDirectory getDirectory(Object directoryResource)
	{
		if (!(directoryResource instanceof File))
		{
			return null;
		}

		if (!((File) directoryResource).isDirectory())
		{
			return null;
		}

		return directories.get(directoryResource);
	}

	/**
	 * {@inheritDoc}
	 */
	public IDirectory getDirectoryByPath(IPath path)
	{
		IPath basePath = new Path(file.getAbsolutePath());
		IPath modulePath = basePath.append(path);
		File file = new File(modulePath.toOSString());
		return directories.get(file);
	}

	/**
	 * {@inheritDoc}
	 */
	public IPath getResourcePath(IBuildPathResource resource)
	{
		if (resource instanceof FileSystemModule)
		{
			FileSystemModule fsModule = (FileSystemModule) resource;
			if (modules.get(fsModule.getFile()) == null)
			{
				return null;
			}

			IPath basePath = new Path(file.getAbsolutePath());
			IPath moduleAbsolutePath = new Path(resource.getFullPath());
			if (!basePath.isPrefixOf(moduleAbsolutePath))
			{
				return null;
			}

			IPath result = moduleAbsolutePath.removeFirstSegments(basePath.segmentCount());
			result = result.makeAbsolute();

			return result;
		}
		else if (resource instanceof FileSystemDirectory)
		{
			FileSystemDirectory fsDirectory = (FileSystemDirectory) resource;
			if (directories.get(fsDirectory.getDirectory()) == null)
			{
				return null;
			}

			IPath basePath = new Path(file.getAbsolutePath());
			IPath moduleAbsolutePath = new Path(resource.getFullPath());
			if (!basePath.isPrefixOf(moduleAbsolutePath))
			{
				return null;
			}

			IPath result = moduleAbsolutePath.removeFirstSegments(basePath.segmentCount());
			result = result.makeAbsolute();

			return result;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(IModule module)
	{
		if (!(module instanceof FileSystemModule))
		{
			return false;
		}

		return getModule(((FileSystemModule) module).getFile()) != null;
	}

	/**
	 * Collects initial resources info.
	 */
	private void collectInitialResources()
	{
		if (!file.exists())
		{
			return;
		}
		long timeMillis = System.currentTimeMillis();
		addResourcesRecursively(file);
		IdeLog.logInfo(PHPEditorPlugin.getDefault(), "FileSystemBuildPath.collectInitialResources(" + file.getName() //$NON-NLS-1$
				+ ") -> " + (System.currentTimeMillis() - timeMillis) + "ms", null, PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Adds resources recursively.
	 * 
	 * @param inputFile
	 *            - file to handle.
	 */
	private void addResourcesRecursively(File inputFile)
	{
		if (inputFile.isFile())
		{
			IModule module = PHPFileSystemModuleFactory.getModule(inputFile, this);
			if (module != null)
			{
				modules.put(inputFile, module);
			}
		}
		else if (inputFile.isDirectory())
		{
			if (FolderFilteringManager.acceptFolder(inputFile))
			{
				directories.put(inputFile, new FileSystemDirectory(inputFile, this));
				File[] listFiles = inputFile.listFiles();
				if (listFiles != null)
				{
					for (File child : listFiles)
					{
						addResourcesRecursively(child);
					}
				}
			}
		}
	}

	public String getHandleIdentifier()
	{
		return file.getAbsolutePath();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof FileSystemBuildPath)
		{
			return ((FileSystemBuildPath) obj).file.equals(this.file);
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
		return file.hashCode();
	}

}
