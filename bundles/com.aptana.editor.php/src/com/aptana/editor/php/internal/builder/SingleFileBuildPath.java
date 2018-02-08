package com.aptana.editor.php.internal.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.aptana.editor.php.internal.core.builder.IBuildPathResource;
import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * A build-path for a single external file.
 * 
 * @author Denis Denisenko
 */
public class SingleFileBuildPath extends AbstractBuildPath
{
	/**
	 * Folder.
	 */
	private File folder;

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
	public SingleFileBuildPath(File file)
	{
		this.folder = file.getParentFile();
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
	 * {@inheritDoc}
	 */
	public IModule getModuleByPath(IPath path)
	{
		IPath basePath = new Path(folder.getAbsolutePath());
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
		IPath basePath = new Path(folder.getAbsolutePath());
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
		IPath basePath = new Path(folder.getAbsolutePath());
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
		IPath basePath = new Path(folder.getAbsolutePath());
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

			IPath basePath = new Path(folder.getAbsolutePath());
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

			IPath basePath = new Path(folder.getAbsolutePath());
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
		if (!folder.exists() || !file.exists())
		{
			return;
		}

		addResources();
	}

	/**
	 * Adds resource.
	 * 
	 * @param inputFile
	 *            - file to handle.
	 */
	private void addResources()
	{
		directories.put(folder, new FileSystemDirectory(folder, this));

		if (!file.isFile())
		{
			throw new IllegalArgumentException("Directories are not accepted"); //$NON-NLS-1$
		}
		IModule module = PHPFileSystemModuleFactory.getModule(file, this);
		if (module != null)
		{
			modules.put(file, module);
		}
	}

	public String getHandleIdentifier()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof SingleFileBuildPath)
		{
			return ((SingleFileBuildPath) obj).file.equals(this.file);
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
