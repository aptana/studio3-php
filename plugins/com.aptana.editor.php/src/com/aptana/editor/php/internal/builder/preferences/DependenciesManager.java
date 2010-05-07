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
package com.aptana.editor.php.internal.builder.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import com.aptana.editor.php.PHPEditorPlugin;

/**
 * Project dependencies manager.
 * 
 * @author Pavel Petrochenko
 * @author Denis Denisenko
 */
public final class DependenciesManager
{
	/**
	 * Listeners.
	 */
	private static List<IProjectDependencyListener> listeners = new ArrayList<IProjectDependencyListener>();

	/**
	 * Adds listener.
	 * 
	 * @param listener
	 *            - listener to add.
	 */
	public static void addListener(IProjectDependencyListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Removes listener.
	 * 
	 * @param listener
	 *            - listener to remove.
	 */
	public static void removeListener(IProjectDependencyListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Gets project dependencies.
	 * 
	 * @param project
	 *            - project.
	 * @return project dependencies
	 */
	public static ProjectDependencies getDependencies(IProject project)
	{
		ProjectDependencies dependencies = new ProjectDependencies();

		if (!project.isAccessible())
		{
			return dependencies;
		}

		try
		{
			String persistentProperty = project.getPersistentProperty(buildPathPropertyName());
			dependencies.load(persistentProperty);
			dependencies.loadLibs(project.getPersistentProperty(libsPropertyName()));
		}
		catch (CoreException e)
		{
			PHPEditorPlugin.logError(e);
		}
		return dependencies;
	}

	/**
	 * Sets project dependencies.
	 * 
	 * @param project
	 *            - project.
	 * @param dependencies
	 *            - dependencies.
	 */
	public static void setDependencies(IProject project, ProjectDependencies dependencies)
	{
		try
		{
			project.setPersistentProperty(buildPathPropertyName(), dependencies.toString());
			project.setPersistentProperty(libsPropertyName(), dependencies.getLibString());
			notifyChanged(project, dependencies);
		}
		catch (CoreException e)
		{
			PHPEditorPlugin.logError(e);
		}
	}

	/**
	 * Sets project extensions.
	 * 
	 * @param extensions
	 *            - esnetnsions to set.
	 */
	public static void setExtensions(List<PHPExtension> extensions)
	{
		StringBuilder bld = new StringBuilder();
		for (PHPExtension e : extensions)
		{
			String eName = escape(e.getName());
			String ePath = escape(e.getPath());
			bld.append(eName);
			bld.append((char) 3);
			bld.append(ePath);
			bld.append(File.pathSeparatorChar);
		}
		if (bld.length() > 0)
		{
			bld.deleteCharAt(bld.length() - 1);
		}
		PHPEditorPlugin.getDefault().getPreferenceStore().setValue("php-extensions", bld.toString()); //$NON-NLS-1$		
	}

	private static QualifiedName buildPathPropertyName()
	{
		return new QualifiedName(PHPEditorPlugin.PLUGIN_ID, "phpbuildpath"); //$NON-NLS-1$
	}

	private static QualifiedName libsPropertyName()
	{
		return new QualifiedName(PHPEditorPlugin.PLUGIN_ID, "libs"); //$NON-NLS-1$
	}

	/**
	 * Gets project extensions.
	 * 
	 * @return project extensions.
	 */
	public static List<PHPExtension> getExtensions()
	{
		String string = PHPEditorPlugin.getDefault().getPreferenceStore().getString("php-extensions"); //$NON-NLS-1$
		String[] split = string.split(File.pathSeparator);
		ArrayList<PHPExtension> result = new ArrayList<PHPExtension>();
		for (String s : split)
		{
			if (s.length() > 0)
			{
				PHPExtension e = new PHPExtension();
				int indexOf = s.indexOf((char) 3);
				String name = s.substring(0, indexOf);
				String path = s.substring(indexOf + 1);
				e.setName(descape(name));
				e.setPath(descape(path));
				result.add(e);
			}
		}
		return result;
	}

	static String descape(String substring)
	{

		String replace = substring.replace((char) 2, File.pathSeparatorChar);
		return replace;
	}

	static String escape(String absolutePath)
	{
		String replace = absolutePath.replace(File.pathSeparatorChar, (char) 2);
		return replace;
	}

	/**
	 * Notifies project dependencies are changed.
	 * 
	 * @param project
	 *            - project.
	 * @param dependencies
	 *            - dependencies.
	 */
	private static void notifyChanged(IProject project, ProjectDependencies dependencies)
	{
		for (IProjectDependencyListener listener : listeners)
		{
			listener.dependenciesChanged(project, dependencies);
		}
	}

	/**
	 * DependenciesManager private constructor.
	 */
	private DependenciesManager()
	{

	}
}
