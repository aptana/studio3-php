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

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;

/**
 * Abstract build path.
 * @author Denis Denisenko
 */
public interface IBuildPath
{
	/**
	 * Gets module belonging to build path. 
	 * @return unmodifiable list of modules
	 */
	List<IModule> getModules();
	
	/**
	 * Gets unmodifiable set of dependencies.
	 * @return unmodifiable set of dependencies.
	 */
	Set<IBuildPath> getDependencies();
	
	/**
	 * Adds dependency.
	 * @param dependency - dependency to add.
	 */
	void addDependency(IBuildPath dependency);
	
	/**
	 * Checks whether the build-path contains the module specified.
	 * Module getBuildPath method might return a build-path value different from
	 * the current build-path.
	 * Example: module is defined by the project build-path, but it can also 
	 * be resolved by the workspace folder build-path.
	 */
	boolean contains(IModule module);
	
	/**
	 * Removes dependency.
	 * @param dependency - dependency to remove.
	 */
	void removeDependency(IBuildPath dependency);
	
	/**
	 * Clears path dependencies.
	 */
	void clearDependencies();
	
	/**
	 * Adds build path change listener.
	 * @param listener - listener to add.
	 */
	void addBuildPathChangeListener(IBuildPathChangeListener listener);
	
	/**
	 * Removes build path change listener.
	 * @param listener - listener to remove.
	 */
	void removeBuildPathChangeListener(IBuildPathChangeListener listener);
	
	/**
	 * Closes build path and releases its contents.
	 */
	void close();
	
	/**
	 * Checks whether current path contains module with the given module resource
	 * and return such a module if true. 
	 * @param moduleResource - module resource.
	 * @return module or null if not found.
	 */
	IModule getModule(Object moduleResource);
	
	/**
	 * Checks whether current path contains directory with the given directory resource
	 * and return such a directory if true. 
	 * @param directoryResource - directory resource.
	 * @return directory or null if not found.
	 */
	IDirectory getDirectory(Object directoryResource);
	
	/**
	 * Resolves relative path
	 * @param baseModule - base module to calculate relative path from.
	 * @param relativePath - relative path to calculate.
	 * @return module or null if not found.
	 */
	IModule resolveRelativePath(IModule baseModule, IPath relativePath);
	
	/**
	 * Gets module by path inside the current build path.
	 * @param path - path.
	 * @return module or null if not found.
	 */
	IModule getModuleByPath(IPath path);
	
	/**
	 * Gets directory by path inside the current build path.
	 * @param path - path.
	 * @return directory or null if not found.
	 */
	IDirectory getDirectoryByPath(IPath path);
	
	/**
	 * Gets modules by path to some container (directory or build path root)
	 * inside the current build path.
	 * @param path - path.
	 * @return modules or null if not found.
	 */
	List<IModule> getModulesByPath(IPath path);
	
	/**
	 * Gets sub-directories by path to some container (directory or build path root)
	 * inside the current build path.
	 * @param path - path.
	 * @return sub-directories or null if not found.
	 */
	List<IDirectory> getSubdirectoriesByPath(IPath path);
	
	/**
	 * Resolves relative container path like "dir1/dir2".
	 * @param baseModule - base module to calculate relative path from.
	 * @param relativePath - relative path to calculate.
	 * @return resources or null if not found.
	 */
	List<IBuildPathResource> resolveContainerRelativePath(IModule baseModule, IPath relativePath);
	
	/**
	 * Gets whether build path is passive and requires external modules initialization.
	 * @return true if passive, false otherwise.
	 */
	boolean isPassive();
	
	/**
	 * Gets resource relative path.
	 * @param resource - resource. 
	 * @return resource relative path.
	 */
	IPath getResourcePath(IBuildPathResource resource);
	
	
	String getHandleIdentifier();
}
