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
	private ArrayList<File> directories = new ArrayList<File>();
	
	/**
	 * Projects.
	 */
	private ArrayList<IResource> workspaceResources = new ArrayList<IResource>();
	
	private ArrayList<String>librariesIds=new ArrayList<String>();
	
	public ArrayList<String> getNotUsedLibrariesIds() {
		return librariesIds;
	}


	public void setNotUsedLibrariesIds(ArrayList<String> librariesIds) {
		this.librariesIds = librariesIds;
	}


	private boolean usesCustomLibs;

	public boolean isUsesCustomLibs() {
		return usesCustomLibs;
	}


	public void setUsesCustomLibs(boolean usesCustomLibs) {
		this.usesCustomLibs = usesCustomLibs;
	}


	/**
	 * Loads the dependency.
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
				if (f.length()==0){
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
					//workspaceResources.add(root.getProject(DependenciesManager.descape(f.substring(1))));
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
			absolutePath=DependenciesManager.escape(absolutePath);
			bld.append(absolutePath);
			bld.append(File.pathSeparatorChar);
		}
		for (IResource p : workspaceResources)
		{
			bld.append('p');
			String absolutePath = p.getFullPath().toString();
			absolutePath=DependenciesManager.escape(absolutePath);
			bld.append(absolutePath);
			bld.append(File.pathSeparatorChar);
			/*bld.append('p');
			String name = p.getName();
			bld.append(DependenciesManager.escape(name));
			bld.append(File.pathSeparatorChar);*/
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
	public ArrayList<File> getDirectories()
	{
		return new ArrayList<File>(directories);
	}
	
	

	/**
	 * @return projects
	 */
	public ArrayList<IResource> getWorkspaceResources()
	{
		return new ArrayList<IResource>(workspaceResources);
	}

	/**
	 * @param ps
	 * @param fs
	 */
	public void set(ArrayList<IResource> ps, ArrayList<File> fs)
	{
		directories = new ArrayList<File>(fs);
		workspaceResources = new ArrayList<IResource>(ps);
	}


	public void loadLibs(String persistentProperty) {
		if (persistentProperty==null||persistentProperty.length()==0) {
			usesCustomLibs=false;
			librariesIds.clear();
			return;
		}
		char charAt = persistentProperty.charAt(0);
		usesCustomLibs=charAt=='y';
		String[] split = persistentProperty.substring(1).split(File.pathSeparator);
		for (int a=0;a<split.length;a++) {
			IPHPLibrary library = LibraryManager.getInstance().getLibrary(split[a].trim());
			if (library!=null) {
				librariesIds.add(library.getId());
			}
		}
	}


	public String getLibString() {
		StringBuilder bld=new StringBuilder();
		if (usesCustomLibs) {
			bld.append('y');
		}
		else {
			bld.append('n');
		}
		for (String s:librariesIds) {
			bld.append(s);
			bld.append(File.pathSeparator);
		}
		if (librariesIds.size()>0) {
			bld.deleteCharAt(bld.length()-1);
		}
		return bld.toString();
	}
}
