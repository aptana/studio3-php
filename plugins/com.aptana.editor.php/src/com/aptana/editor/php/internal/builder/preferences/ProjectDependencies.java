/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import com.aptana.editor.php.internal.builder.IPHPLibrary;
import com.aptana.editor.php.internal.builder.LibraryManager;

/**
 * @author Pavel Petrochenko
 */
public class ProjectDependencies
{
	/**
	 * Directories.
	 */
	private List<File> directories = new ArrayList<File>();

	/**
	 * Projects.
	 */
	private List<IResource> workspaceResources = new ArrayList<IResource>();

	private List<String> librariesIds = new ArrayList<String>();

	public List<String> getNotUsedLibrariesIds()
	{
		return librariesIds;
	}

	public void setNotUsedLibrariesIds(List<String> librariesIds)
	{
		this.librariesIds = librariesIds;
	}

	private boolean usesCustomLibs;

	public boolean isUsesCustomLibs()
	{
		return usesCustomLibs;
	}

	public void setUsesCustomLibs(boolean usesCustomLibs)
	{
		this.usesCustomLibs = usesCustomLibs;
	}

	/**
	 * Loads the dependency.
	 * 
	 * @param persistentProperty
	 */
	public void load(String persistentProperty)
	{
		if (persistentProperty != null)
		{
			String[] pEntries = persistentProperty.split(File.pathSeparator);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			for (String f : pEntries)
			{
				if (f.length() == 0)
				{
					continue;
				}
				if (f.charAt(0) == 'f')
				{
					directories.add(new File(DependenciesManager.descape(f.substring(1))));
				}
				else if (f.charAt(0) == 'p')
				{
					String path = DependenciesManager.descape(f.substring(1));
					IResource resource = root.findMember(path);
					if (resource != null)
					{
						workspaceResources.add(resource);
					}
					// workspaceResources.add(root.getProject(DependenciesManager.descape(f.substring(1))));
				}
			}
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder bld = new StringBuilder();
		for (File f : directories)
		{
			bld.append('f');
			String absolutePath = f.getAbsolutePath();
			absolutePath = DependenciesManager.escape(absolutePath);
			bld.append(absolutePath);
			bld.append(File.pathSeparatorChar);
		}
		for (IResource p : workspaceResources)
		{
			bld.append('p');
			String absolutePath = p.getFullPath().toString();
			absolutePath = DependenciesManager.escape(absolutePath);
			bld.append(absolutePath);
			bld.append(File.pathSeparatorChar);
			/*
			 * bld.append('p'); String name = p.getName(); bld.append(DependenciesManager.escape(name));
			 * bld.append(File.pathSeparatorChar);
			 */
		}
		if (bld.length() > 0)
		{
			bld.deleteCharAt(bld.length() - 1);
		}
		return bld.toString();
	}

	/**
	 * @return directories
	 */
	public List<File> getDirectories()
	{
		return new ArrayList<File>(directories);
	}

	/**
	 * @return projects
	 */
	public List<IResource> getWorkspaceResources()
	{
		return new ArrayList<IResource>(workspaceResources);
	}

	/**
	 * @param ps
	 * @param fs
	 */
	public void set(List<IResource> ps, List<File> fs)
	{
		directories = new ArrayList<File>(fs);
		workspaceResources = new ArrayList<IResource>(ps);
	}

	public void loadLibs(String persistentProperty)
	{
		if (persistentProperty == null || persistentProperty.length() == 0)
		{
			usesCustomLibs = false;
			librariesIds.clear();
			return;
		}
		char charAt = persistentProperty.charAt(0);
		usesCustomLibs = charAt == 'y';
		String[] split = persistentProperty.substring(1).split(File.pathSeparator);
		for (int a = 0; a < split.length; a++)
		{
			IPHPLibrary library = LibraryManager.getInstance().getLibrary(split[a].trim());
			if (library != null)
			{
				librariesIds.add(library.getId());
			}
		}
	}

	public String getLibString()
	{
		StringBuilder bld = new StringBuilder();
		if (usesCustomLibs)
		{
			bld.append('y');
		}
		else
		{
			bld.append('n');
		}
		for (String s : librariesIds)
		{
			bld.append(s);
			bld.append(File.pathSeparator);
		}
		if (librariesIds.size() > 0)
		{
			bld.deleteCharAt(bld.length() - 1);
		}
		return bld.toString();
	}
}
