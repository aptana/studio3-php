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
package com.aptana.debug.php.core.includePath;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
public final class PHPIncludePathUtils {
	/**
	 * Gets interpreter include paths list by project.
	 * 
	 * @param project
	 *            - project.
	 * @return interpreter include paths list.
	 */
	public static List<String> getInterpreterIncludePath(IProject project) {
		ProjectDependencies dependencies = DependenciesManager
				.getDependencies(project);
		if (dependencies == null) {
			return Collections.emptyList();
		}

//		if (dependencies.getDirectories().size() == 0
//				&& dependencies.getWorkspaceResources().size() == 0) {
//			return Collections.emptyList();
//		}

		List<String> result = new ArrayList<String>();

		// appending directories
		for (File directory : dependencies.getDirectories()) {
			result.add(directory.getAbsolutePath());
		}
		IPHPLibrary[] allLibraries = LibraryManager.getInstance()
				.getAllLibraries();
		HashSet<IPHPLibrary> usedLibraries = new HashSet<IPHPLibrary>(Arrays
				.asList(allLibraries));
		if (dependencies.isUsesCustomLibs()) {
			ArrayList<String> notUsedLibrariesIds = dependencies
					.getNotUsedLibrariesIds();
			for (String s : notUsedLibrariesIds) {
				IPHPLibrary library = LibraryManager.getInstance()
						.getLibrary(s);
				if (library != null) {
					usedLibraries.remove(library);
				}
			}
		} else {
			for (IPHPLibrary l : allLibraries) {
				if (!l.isTurnedOn()) {
					usedLibraries.remove(l);
				}
			}
		}
		for (IPHPLibrary l : usedLibraries) {
			for (String s : l.getDirectories()) {
				File fl = new File(s);
				result.add(fl.getAbsolutePath());
			}
		}

		// appending projects
		for (IResource workspaceResource : dependencies.getWorkspaceResources()) {
			IPath resourcePath = workspaceResource.getLocation();
			result.add(resourcePath.toOSString());
		}

		return result;
	}

	/**
	 * CGIIncludePathUtils constructor.
	 */
	private PHPIncludePathUtils() {
	}
}
