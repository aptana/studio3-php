/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.include_path;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import com.aptana.editor.php.internal.builder.IPHPLibrary;
import com.aptana.editor.php.internal.builder.LibraryManager;
import com.aptana.editor.php.internal.builder.preferences.DependenciesManager;
import com.aptana.editor.php.internal.builder.preferences.ProjectDependencies;

/**
 * PHP include-path utils.
 * 
 * @author Denis Denisenko
 */
public final class PHPIncludePathUtils
{
	/**
	 * Gets interpreter include paths list by project.
	 * 
	 * @param project
	 *            - project.
	 * @return interpreter include paths list.
	 */
	public static List<String> getInterpreterIncludePath(IProject project)
	{
		ProjectDependencies dependencies = DependenciesManager.getDependencies(project);
		if (dependencies == null)
		{
			return Collections.emptyList();
		}

		// if (dependencies.getDirectories().size() == 0
		// && dependencies.getWorkspaceResources().size() == 0) {
		// return Collections.emptyList();
		// }

		List<String> result = new ArrayList<String>();

		// appending directories
		for (File directory : dependencies.getDirectories())
		{
			result.add(directory.getAbsolutePath());
		}
		IPHPLibrary[] allLibraries = LibraryManager.getInstance().getAllLibraries();
		Set<IPHPLibrary> usedLibraries = new HashSet<IPHPLibrary>(Arrays.asList(allLibraries));
		if (dependencies.isUsesCustomLibs())
		{
			List<String> notUsedLibrariesIds = dependencies.getNotUsedLibrariesIds();
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
				result.add(fl.getAbsolutePath());
			}
		}

		// appending projects
		for (IResource workspaceResource : dependencies.getWorkspaceResources())
		{
			IPath resourcePath = workspaceResource.getLocation();
			result.add(resourcePath.toOSString());
		}

		return result;
	}

	/**
	 * CGIIncludePathUtils constructor.
	 */
	private PHPIncludePathUtils()
	{
	}
}
