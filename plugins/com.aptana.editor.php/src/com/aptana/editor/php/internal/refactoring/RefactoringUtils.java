/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.refactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.LocalModule;
import com.aptana.editor.php.internal.builder.ProjectBuildPath;
import com.aptana.editor.php.internal.builder.WorkspaceFolderBuildpath;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.indexer.IncludePHPEntryValue;

/**
 * Refactoring utils.
 * 
 * @author Denis Denisenko
 */
public final class RefactoringUtils
{
	/**
	 * Constructs include path from one module to another.
	 * 
	 * @param from
	 *            - module to construct include path from.
	 * @param to
	 *            - module to construct include path to.
	 * @return constructed include path
	 */
	public static ConstructedIncludePath constructIncludePath(IModule from, IModule to)
	{
		IBuildPath fromBuildPath = from.getBuildPath();
		IBuildPath toBuildPath = to.getBuildPath();
		Set<IBuildPath> fromDependencies = fromBuildPath.getDependencies();
		if (fromDependencies.equals(toBuildPath))
		{
			String includePath = constructPathFromRoot(to);
			return new ConstructedIncludePath(includePath, null, null);
		}
		// if "from" build-path directly depends from "to" build-path
		if (fromDependencies.contains(toBuildPath))
		{
			String includePath = constructPathFromRoot(to);
			return new ConstructedIncludePath(includePath, null, null);
		}
		else
		{
			// for local modules using its project-based build-path instead of native module build-path
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
							IBuildPath projectBuildPath = BuildPathManager.getInstance()
									.getBuildPathByResource(project);
							if (projectBuildPath != null)
							{
								IModule alternativeToModule = projectBuildPath.getModule(file);
								if (alternativeToModule != null)
								{
									String includePath = constructPathFromRoot(alternativeToModule);
									return new ConstructedIncludePath(includePath, fromBuildPath, projectBuildPath);
								}
							}
						}
					}
					catch (CoreException e)
					{
						IdeLog.logWarning(PHPEditorPlugin.getDefault(),
								"PHP Refactoring - Error while constructing an include-path (constructIncludePath)", //$NON-NLS-1$
								e, PHPEditorPlugin.DEBUG_SCOPE);
					}
				}
			}

			// in other case, using original build-paths for reporting unsatisfied state
			String includePath = constructPathFromRoot(to);
			return new ConstructedIncludePath(includePath, fromBuildPath, toBuildPath);
		}
	}

	/**
	 * Constructs include path from one module to another.
	 * 
	 * @param from
	 *            - module to construct include path from.
	 * @param toBuildPath
	 *            - destination build-path.
	 * @param toPath
	 *            - path inside destination build-path.
	 * @return constructed include path
	 */
	public static ConstructedIncludePath constructIncludePath(IModule from, IBuildPath toBuildPath, IPath toPath)
	{
		IBuildPath fromBuildPath = from.getBuildPath();
		Set<IBuildPath> fromDependencies = fromBuildPath.getDependencies();

		// if "from" build-path directly depends from "to" build-path
		if (fromBuildPath.equals(toBuildPath) || fromDependencies.contains(toBuildPath))
		{
			String includePath = constructPathFromRoot(toPath);
			return new ConstructedIncludePath(includePath, null, null);
		}
		else
		{
			// for local modules using its project-based build-path instead of native module build-path
			if (toBuildPath instanceof ProjectBuildPath || toBuildPath instanceof WorkspaceFolderBuildpath)
			{
				IProject project = null;
				if (toBuildPath instanceof ProjectBuildPath)
				{
					project = ((ProjectBuildPath) toBuildPath).getProject();
				}
				else
				{
					project = ((WorkspaceFolderBuildpath) toBuildPath).getFolder().getProject();
				}
				IBuildPath projectBuildPath = BuildPathManager.getInstance().getBuildPathByResource(project);
				if (projectBuildPath != null)
				{
					String includePath = constructPathFromRoot(toPath);
					return new ConstructedIncludePath(includePath, fromBuildPath, projectBuildPath);
				}
			}

			// in other case, using original build-paths for reporting unsatisfied state
			String includePath = constructPathFromRoot(toPath);
			return new ConstructedIncludePath(includePath, fromBuildPath, toBuildPath);
		}
	}

	/**
	 * Gets all the values of the entries that include the module specified.
	 * 
	 * @param index
	 *            - index to use.
	 * @param module
	 *            - module.
	 * @return map from module to the list of entries defined in that module
	 */
	public static Map<IModule, List<IncludePHPEntryValue>> getIncludes(IElementsIndex index, IModule module)
	{
		Map<IModule, List<IncludePHPEntryValue>> candidates = new HashMap<IModule, List<IncludePHPEntryValue>>();

		String moduleShortName = module.getShortName();

		// collecting initial candidates
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
					if (includeValue.getIncludePath().endsWith(moduleShortName))
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

		Map<IModule, List<IncludePHPEntryValue>> result = new HashMap<IModule, List<IncludePHPEntryValue>>();

		// filtering candidates
		for (Entry<IModule, List<IncludePHPEntryValue>> entry : candidates.entrySet())
		{
			IModule currentModule = entry.getKey();
			List<IncludePHPEntryValue> values = entry.getValue();
			for (IncludePHPEntryValue value : values)
			{
				String includePathString = value.getIncludePath();
				IPath includePath = new Path(includePathString);
				IModule resolvedModule = currentModule.getBuildPath().resolveRelativePath(currentModule, includePath);
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
	 * 
	 * @param module
	 *            - module.
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
			result.append('/');
		}

		result.append(segments[segments.length - 1]);

		return result.toString();
	}

	/**
	 * Constructs path from the root of a build path to a module.
	 * 
	 * @param modulePath
	 *            - module path from root.
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
			result.append('/');
		}

		result.append(segments[segments.length - 1]);

		return result.toString();
	}

	/**
	 * Gets module includes.
	 * 
	 * @param module
	 *            - module
	 * @return module includes list
	 */
	public static List<IncludePHPEntryValue> getModuleIncludes(IModule module)
	{
		return getModuleIncludes(module, PHPGlobalIndexer.getInstance().getIndex());
	}

	/**
	 * Gets module includes.
	 * 
	 * @param module
	 *            - module
	 * @param index
	 *            - index to use.
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
