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
package com.aptana.editor.php.internal.refactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.IBuildPath;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.builder.LocalModule;
import com.aptana.editor.php.internal.builder.ProjectBuildPath;
import com.aptana.editor.php.internal.builder.WorkspaceFolderBuildpath;
import com.aptana.editor.php.internal.indexer.IncludePHPEntryValue;

/**
 * Refactoring utils.
 * @author Denis Denisenko
 */
public final class RefactoringUtils
{
	/**
	 * Constructs include path from one module to another.
	 * @param from - module to construct include path from.
	 * @param to - module to construct include path to.
	 * @return constructed include path
	 */
	public static ConstructedIncludePath constructIncludePath(IModule from, IModule to)
	{
		IBuildPath fromBuildPath = from.getBuildPath();
		IBuildPath toBuildPath = to.getBuildPath();
		Set<IBuildPath> fromDependencies = fromBuildPath.getDependencies();
		if (fromDependencies.equals(toBuildPath)){
			String includePath = constructPathFromRoot(to);
			return new ConstructedIncludePath(includePath, null, null);
		}
		//if "from" build-path directly depends from "to" build-path
		if (fromDependencies.contains(toBuildPath))
		{
			String includePath = constructPathFromRoot(to);
			return new ConstructedIncludePath(includePath, null, null);
		}
		else
		{
			//for local modules using its project-based build-path instead of native module build-path
			if (to instanceof LocalModule)
			{
				IFile file = ((LocalModule) to).getFile();
				if (!file.isSynchronized(1))
				{
					try
					{
						file.refreshLocal(1, new NullProgressMonitor());
						if (file.exists())
						{
							IProject project = file.getProject();
							IBuildPath projectBuildPath = 
								BuildPathManager.getInstance().getBuildPathByResource(project);
							if (projectBuildPath != null)
							{
								IModule alternativeToModule = projectBuildPath.getModule(file);
								if (alternativeToModule != null)
								{
									String includePath = constructPathFromRoot(alternativeToModule);
									return new ConstructedIncludePath(includePath, fromBuildPath,
											projectBuildPath);
								}
							}
						}
					} 
					catch (CoreException e)
					{
						//ignore
					}
				}
			}
			
			//in other case, using original build-paths for reporting unsatisfied state
			String includePath = constructPathFromRoot(to);
			return new ConstructedIncludePath(includePath, fromBuildPath, toBuildPath);
		}
	}
	
	/**
	 * Constructs include path from one module to another.
	 * 
	 * @param from - module to construct include path from.
	 * @param toBuildPath - destination build-path.
	 * @param toPath - path inside destination build-path.
	 * 
	 * @return constructed include path
	 */
	public static ConstructedIncludePath constructIncludePath(IModule from, IBuildPath toBuildPath,
			IPath toPath)
	{
		IBuildPath fromBuildPath = from.getBuildPath();
		Set<IBuildPath> fromDependencies = fromBuildPath.getDependencies();
		
		//if "from" build-path directly depends from "to" build-path
		if (fromBuildPath.equals(toBuildPath)||fromDependencies.contains(toBuildPath))
		{
			String includePath = constructPathFromRoot(toPath);
			return new ConstructedIncludePath(includePath, null, null);
		}
		else
		{
			//for local modules using its project-based build-path instead of native module build-path
			if (toBuildPath instanceof ProjectBuildPath || 
					toBuildPath instanceof WorkspaceFolderBuildpath)
			{
				IProject project = null;
				if (toBuildPath instanceof ProjectBuildPath)
				{
					project = ((ProjectBuildPath) toBuildPath).getProject();
				}
				else
				{
					project = 
						((WorkspaceFolderBuildpath) toBuildPath).getFolder().getProject();
				}
				IBuildPath projectBuildPath = 
					BuildPathManager.getInstance().getBuildPathByResource(project);
				if (projectBuildPath != null)
				{
					String includePath = constructPathFromRoot(toPath);
					return new ConstructedIncludePath(includePath, fromBuildPath,
							projectBuildPath);
				}
			}
			
			//in other case, using original build-paths for reporting unsatisfied state
			String includePath = constructPathFromRoot(toPath);
			return new ConstructedIncludePath(includePath, fromBuildPath, toBuildPath);
		}
	}

	/**
	 * Gets all the values of the entries that include the module specified.
	 * @param index - index to use.
	 * @param module - module.
	 * @return map from module to the list of entries defined in that module
	 */
	public static Map<IModule, List<IncludePHPEntryValue>> getIncludes(IElementsIndex index, IModule module)
	{
		Map<IModule, List<IncludePHPEntryValue>> candidates = 
			new HashMap<IModule, List<IncludePHPEntryValue>>();
		
		String moduleShortName = module.getShortName();
		
		//collecting initial candidates
		Set<IModule> modules = index.getModules();
		for (IModule currentModule : modules)
		{
			List<IElementEntry> moduleEntries = index.getModuleEntries(currentModule);
			for (IElementEntry entry : moduleEntries)
			{
				Object entryValue = entry.getValue();
				if (entryValue instanceof IncludePHPEntryValue)
				{
					IncludePHPEntryValue includeValue = (IncludePHPEntryValue) entryValue;
					if(includeValue.getIncludePath().endsWith(moduleShortName))
					{
						List<IncludePHPEntryValue> moduleValues = candidates.get(currentModule);
						if (moduleValues == null)
						{
							moduleValues = new ArrayList<IncludePHPEntryValue>(1);
							candidates.put(currentModule, moduleValues);
						}
						
						moduleValues.add(includeValue);
					}
				}
			}
		}
		
		Map<IModule, List<IncludePHPEntryValue>> result = 
			new HashMap<IModule, List<IncludePHPEntryValue>>();
		
		//filtering candidates
		for (Entry<IModule, List<IncludePHPEntryValue>> entry : candidates.entrySet())
		{
			IModule currentModule = entry.getKey();
			List<IncludePHPEntryValue> values = entry.getValue();
			for (IncludePHPEntryValue value : values)
			{
				String includePathString = value.getIncludePath();
				IPath includePath = new Path(includePathString);
				IModule resolvedModule = 
					currentModule.getBuildPath().resolveRelativePath(currentModule, includePath);
				if (resolvedModule != null && resolvedModule.equals(module))
				{
					List<IncludePHPEntryValue> moduleValues = result.get(currentModule);
					if (moduleValues == null)
					{
						moduleValues = new ArrayList<IncludePHPEntryValue>(1);
						result.put(currentModule, moduleValues);
					}
					
					moduleValues.add(value);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Constructs path from the root of a build path to a module.
	 * @param module - module.
	 * @return include path.
	 */
	private static String constructPathFromRoot(IModule module)
	{
		IPath path = module.getPath();
		StringBuilder result = new StringBuilder();
		String[] segments = path.segments();
		if (segments.length == 0)
		{
			return null;
		}
		
		for (int i = 0; i < segments.length - 1; i++)
		{
			result.append(segments[i]);
			result.append("/"); //$NON-NLS-1$
		}
		
		result.append(segments[segments.length - 1]);
		
		return result.toString();
	}
	
	/**
	 * Constructs path from the root of a build path to a module.
	 * @param modulePath - module path from root.
	 * @return include path.
	 */
	public static String constructPathFromRoot(IPath modulePath)
	{
		StringBuilder result = new StringBuilder();
		String[] segments = modulePath.segments();
		if (segments.length == 0)
		{
			return null;
		}
		
		for (int i = 0; i < segments.length - 1; i++)
		{
			result.append(segments[i]);
			result.append("/"); //$NON-NLS-1$
		}
		
		result.append(segments[segments.length - 1]);
		
		return result.toString();
	}
	/**
	 * Gets module includes.
	 * @param module - module
	 * 
	 * @return module includes list
	 */
	public static List<IncludePHPEntryValue> getModuleIncludes(IModule module)
	{
		return getModuleIncludes(module, PHPGlobalIndexer.getInstance().getIndex());
	}
	
	/**
	 * Gets module includes.
	 * @param module - module
	 * @param index - index to use.
	 * 
	 * @return module includes list
	 */
	public static List<IncludePHPEntryValue> getModuleIncludes(IModule module, IElementsIndex index)
	{
		List<IncludePHPEntryValue> result = new ArrayList<IncludePHPEntryValue>();
		
		List<IElementEntry> moduleEntries = index.getModuleEntries(module);
		for (IElementEntry entry : moduleEntries)
		{
			Object entryValue = entry.getValue();
			if (entryValue instanceof IncludePHPEntryValue)
			{
				result.add((IncludePHPEntryValue) entryValue);
			}
		}
		
		return result;
	}
	
	/**
	 * RefactoringUtils constructor.
	 */
	private RefactoringUtils()
	{
	}
}
