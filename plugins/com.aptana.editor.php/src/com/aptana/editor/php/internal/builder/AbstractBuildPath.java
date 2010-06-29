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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IBuildPathChangeListener;
import com.aptana.editor.php.internal.core.builder.IBuildPathResource;
import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Abstract build path.
 * 
 * @author Denis Denisenko
 */
public abstract class AbstractBuildPath implements IBuildPath
{
	/**
	 * Listeners.
	 */
	private Set<IBuildPathChangeListener> listeners = new HashSet<IBuildPathChangeListener>();

	/**
	 * Build path dependencies.
	 */
	private Set<IBuildPath> dependencies = new LinkedHashSet<IBuildPath>();

	/**
	 * {@inheritDoc}
	 */
	public void addBuildPathChangeListener(IBuildPathChangeListener listener)
	{
		synchronized (listeners)
		{
			listeners.add(listener);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void removeBuildPathChangeListener(IBuildPathChangeListener listener)
	{
		synchronized (listeners)
		{
			listeners.remove(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void addDependency(IBuildPath dependency)
	{
		dependencies.add(dependency);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized Set<IBuildPath> getDependencies()
	{
		return Collections.unmodifiableSet(dependencies);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void removeDependency(IBuildPath dependency)
	{
		dependencies.remove(dependency);
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearDependencies()
	{
		dependencies.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized IModule resolveRelativePath(IModule baseModule, IPath relativePath)
	{
		IPath baseModulePath = getResourcePath(baseModule);
		if (baseModulePath == null)
		{
			return null;
		}

		if (relativePath.segmentCount() == 0)
		{
			return baseModule;
		}

		IPath basePath = null;
		if (baseModulePath.segmentCount() <= 1)
		{
			basePath = (new Path("")).makeAbsolute(); //$NON-NLS-1$
		}
		else
		{
			basePath = baseModulePath.removeLastSegments(1);
		}

		IPath resolvedPath = basePath.append(relativePath);

		if (resolvedPath.segmentCount() == 0)
		{
			return null;
		}

		IModule module = getModuleByPath(resolvedPath);
		if (module == null && relativePath.segments()[0].startsWith(".")) //$NON-NLS-1$
		{
			return null;
		}

		// if no module is found relatively to the current file, trying to search it relatively to the project root
		if (module == null)
		{
			module = getModuleByPath(relativePath);
		}

		// if no module found in this build-path, checking the build paths, current build path depends from.
		if (module == null)
		{
			for (IBuildPath currentBuildPath : getDependencies())
			{
				module = currentBuildPath.getModuleByPath(relativePath);
				if (module != null)
				{
					return module;
				}
			}
		}

		return module;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IBuildPathResource> resolveContainerRelativePath(IModule baseModule, IPath relativePath)
	{
		IPath baseModulePath = getResourcePath(baseModule);
		if (baseModulePath == null)
		{
			return null;
		}
		IPath basePath = null;
		if (baseModulePath.segmentCount() <= 1)
		{
			basePath = (new Path("")).makeAbsolute(); //$NON-NLS-1$
		}
		else
		{
			basePath = baseModulePath.removeLastSegments(1);
		}

		IPath resolvedPath = basePath.append(relativePath);

		List<IBuildPathResource> result = new ArrayList<IBuildPathResource>();
		List<IModule> currentBuildPathModules = getModulesByPath(resolvedPath);
		if (currentBuildPathModules != null)
		{
			result.addAll(currentBuildPathModules);
		}

		List<IDirectory> dirs = getSubdirectoriesByPath(resolvedPath);
		if (dirs != null)
		{
			result.addAll(dirs);
		}

		// if no module found in this class, checking the build paths, current build path depends from.
		// but first getting rid of the "./" and "../" paths
		if (relativePath.segmentCount() >= 1 && relativePath.segment(0).startsWith(".")) //$NON-NLS-1$
		{
			return result;
		}

		for (IBuildPath currentBuildPath : getDependencies())
		{
			currentBuildPathModules = currentBuildPath.getModulesByPath(relativePath);
			if (currentBuildPathModules != null)
			{
				result.addAll(currentBuildPathModules);
			}

			dirs = currentBuildPath.getSubdirectoriesByPath(relativePath);
			if (dirs != null)
			{
				result.addAll(dirs);
			}
		}

		return result;
	}

	/**
	 * Notifies build path modules structure or contents changed before the change.
	 * 
	 * @param added
	 *            - added modules.
	 * @param changed
	 *            - changed modules.
	 * @param removed
	 *            - removed modules.
	 * @param addedDirectories
	 *            - added directories.
	 * @param removedDirectories
	 *            - removed directories.
	 */
	protected void notifyChangedBefore(List<IModule> changed, List<IModule> removed, List<IDirectory> removedDirectories)
	{
		Set<IBuildPathChangeListener> buildPathListeners = null;
		synchronized (listeners)
		{
			buildPathListeners = new LinkedHashSet<IBuildPathChangeListener>(listeners);
		}
		for (IBuildPathChangeListener listener : buildPathListeners)
		{
			try
			{
				listener.changedBefore(changed, removed, removedDirectories);
			}
			catch (Throwable th)
			{
				PHPEditorPlugin.logError("Unable notifying build path change listener", th); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Notifies build path modules structure or contents changed after the change.
	 * 
	 * @param added
	 *            - added modules.
	 * @param changed
	 *            - changed modules.
	 * @param removed
	 *            - removed modules.
	 * @param addedDirectories
	 *            - added directories.
	 * @param removedDirectories
	 *            - removed directories.
	 */
	protected void notifyChangedAfter(List<IModule> added, List<IModule> changed, List<IModule> removed,
			List<IDirectory> addedDirectories, List<IDirectory> removedDirectories)
	{
		Set<IBuildPathChangeListener> buildPathListeners = null;
		synchronized (listeners)
		{
			buildPathListeners = new LinkedHashSet<IBuildPathChangeListener>(listeners);
		}
		for (IBuildPathChangeListener listener : buildPathListeners)
		{
			try
			{
				listener.changedAfter(added, changed, removed, addedDirectories, removedDirectories);
			}
			catch (Throwable th)
			{
				PHPEditorPlugin.logError("Unable notifying build path change listener", th); //$NON-NLS-1$
			}
		}
	}
}
