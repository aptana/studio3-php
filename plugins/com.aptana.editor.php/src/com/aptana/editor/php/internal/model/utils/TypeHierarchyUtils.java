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
package com.aptana.editor.php.internal.model.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.aptana.editor.php.indexer.EntryUtils;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.internal.contentAssist.ContentAssistCollectors;
import com.aptana.editor.php.internal.contentAssist.ContentAssistUtils;
import com.aptana.editor.php.internal.contentAssist.PHPContentAssistProcessor;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.indexer.AbstractPHPEntryValue;
import com.aptana.editor.php.internal.indexer.ClassPHPEntryValue;
import com.aptana.editor.php.internal.indexer.ElementsIndexingUtils;
import com.aptana.editor.php.internal.indexer.language.PHPBuiltins;

/**
 * Type hierarchy utilities
 * 
 * @author Denis Denisenko
 */
public final class TypeHierarchyUtils
{

	/**
	 * Finds all class ancestors.
	 * 
	 * @param module
	 *            - module, class is defined in.
	 * @param className
	 *            - class name.
	 * @return list of ancestor entries.
	 */
	public static List<IElementEntry> getClassAncestors(IModule module, String className, IElementsIndex index)
	{
		List<IElementEntry> result = new ArrayList<IElementEntry>();

		List<IElementEntry> classEntries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, className);
		for (IElementEntry classEntry : classEntries)
		{
			if (module == null || classEntry.getModule().equals(module))
			{
				findClassAncestorsRecursivelly(classEntry, result, index);
			}
		}

		return result;
	}

	/**
	 * Finds direct class ancestors.
	 * 
	 * @param module
	 *            - module, class is defined in.
	 * @param className
	 *            - class name.
	 * @return list of ancestor entries.
	 */
	public static List<IElementEntry> getDirectClassAncestors(IModule module, String className, IElementsIndex index)
	{
		List<IElementEntry> result = new ArrayList<IElementEntry>();

		List<IElementEntry> classEntries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, className);
		for (IElementEntry classEntry : classEntries)
		{
			if (module == null || classEntry.getModule().equals(module))
			{
				findClassAncestors(classEntry, result, index);
			}
		}

		return result;
	}

	/**
	 * Gets direct class descendants (both classes and interfaces).
	 * 
	 * @param module
	 *            - module.
	 * @param className
	 *            - class name.
	 * @param index
	 *            - index.
	 * @return direct class descendants
	 */
	public static List<IElementEntry> getDirectClassDescendants(IModule module, String className, IElementsIndex index)
	{
		List<IElementEntry> result = new ArrayList<IElementEntry>();

		List<IElementEntry> classEntries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, className);
		for (IElementEntry classEntry : classEntries)
		{
			if (module == null || classEntry.getModule().equals(module))
			{
				findClassDescendants(classEntry, result, index);
			}
		}

		return result;
	}

	/**
	 * Gets all class descendants (both classes and interfaces).
	 * 
	 * @param module
	 *            - module.
	 * @param className
	 *            - class name.
	 * @param index
	 *            - index.
	 * @return direct class descendants
	 */
	public static List<IElementEntry> getClassDescendants(IModule module, String className, IElementsIndex index)
	{
		List<IElementEntry> result = new ArrayList<IElementEntry>();

		List<IElementEntry> classEntries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, className);
		for (IElementEntry classEntry : classEntries)
		{
			if (module == null || classEntry.getModule().equals(module))
			{
				findClassDescendantsRecursivelly(classEntry, result, index);
			}
		}

		return result;
	}

	/**
	 * Adds all ancestors for the type set.
	 * 
	 * @param types
	 *            - types, which ancestors to add.
	 * @param index
	 *            - index to use.
	 * @param namespaces
	 *            A namespace to accept
	 * @return types with ancestors added.
	 */
	public static Set<String> addAllAncestors(Set<String> types, IElementsIndex index, String namespaces)
	{
		// FIXME: Shalom - This should probably get a set of namespaces to accept. 
		// Or better, the getClassAncestors() should be the one that deals with it. 
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		for (String type : types)
		{
			result.add(type);
			List<IElementEntry> entries = getClassAncestors(null, type, index);
			for (IElementEntry entry : entries)
			{
				AbstractPHPEntryValue value = (AbstractPHPEntryValue) entry.getValue();
				if (ContentAssistUtils.isInNamespace(value, namespaces))
				{
					result.add(ElementsIndexingUtils.getFirstNameInPath(entry.getEntryPath()));
				}
			}
		}

		return result;
	}

	/**
	 * Finds all classes with the name specified in the module specified.
	 * 
	 * @param module
	 *            - module, class is defined in.
	 * @param className
	 *            - class name.
	 * @return list of class entries.
	 * @param index
	 *            - index to use.
	 */
	public static List<IElementEntry> getClasses(IModule module, String className, IElementsIndex index)
	{
		List<IElementEntry> classEntries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, className);

		return classEntries;
	}

	/**
	 * Filters entries by build-path. Those entries that are not reachable by module's build-path are filtered out.
	 * 
	 * @param module
	 *            - module.
	 * @param toFilter
	 *            - entries to filter.
	 * @return filtered entries.
	 */
	public static List<IElementEntry> filterByBuildPath(IModule module, List<IElementEntry> toFilter)
	{
		IBuildPath buildPath = module.getBuildPath();

		return filterByBuildPath(buildPath, toFilter);
	}

	/**
	 * Filters entries by build-path. Those entries that are not reachable by module's build-path are filtered out.
	 * 
	 * @param module
	 *            - module.
	 * @param entry
	 *            - entry to check.
	 * @return true if on build-path, false otherwise.
	 */
	public static boolean isOnBuildPath(IModule module, IElementEntry entry)
	{
		IBuildPath buildPath = module.getBuildPath();

		return isOnBuildPath(buildPath, entry);
	}

	/**
	 * Filters entries by build-path. Those entries that are not reachable by module's build-path are filtered out.
	 * 
	 * @param buildPath
	 *            - build path.
	 * @param entry
	 *            - entry to check.
	 * @return true if on build-path, false otherwise
	 */
	public static boolean isOnBuildPath(IBuildPath buildPath, IElementEntry entry)
	{
		IBuildPath currentEntryBuildPath = entry.getModule().getBuildPath();
		Set<IBuildPath> dependencies = buildPath.getDependencies();

		return buildPath.equals(currentEntryBuildPath) || dependencies.contains(entry.getModule().getBuildPath());
	}

	/**
	 * Filters entries by build-path. Those entries that are not reachable by module's build-path are filtered out.
	 * 
	 * @param buildPath
	 *            - build path.
	 * @param toFilter
	 *            - entries to filter.
	 * @return filtered entries.
	 */
	public static List<IElementEntry> filterByBuildPath(IBuildPath buildPath, List<IElementEntry> toFilter)
	{
		List<IElementEntry> result = new ArrayList<IElementEntry>();

		Set<IBuildPath> dependencies = (buildPath != null) ? buildPath.getDependencies() : new HashSet<IBuildPath>(1);

		for (IElementEntry entry : toFilter)
		{
			IModule module = entry.getModule();
			if (module == null)
			{
				if (PHPBuiltins.getInstance().isBuiltinClassOrConstant(entry.getEntryPath()))
				{
					result.add(entry);
				}
			}
			else
			{
				IBuildPath currentEntryBuildPath = module.getBuildPath();
				if (buildPath.equals(currentEntryBuildPath) || dependencies.contains(currentEntryBuildPath))
				{
					result.add(entry);
				}
			}
		}

		return result;
	}

	/**
	 * Finds class ancestors recursively.
	 * 
	 * @param _module
	 *            - module, class
	 * @param classEntry
	 *            - class entry.
	 * @param toFill
	 *            - list of ancestors to fill.
	 * @param index
	 *            - index to use.
	 */
	private static void findClassAncestorsRecursivelly(IElementEntry classEntry, List<IElementEntry> toFill,
			IElementsIndex index)
	{
		// FIXME - Shalom: This lookup should take into consideration the namespaces that are involved in the hierarchy
		Object value = classEntry.getValue();
		if (!(value instanceof ClassPHPEntryValue))
		{
			return;
		}

		ClassPHPEntryValue entryValue = (ClassPHPEntryValue) value;
		String superClassName = entryValue.getSuperClassname();

		IBuildPath classEntryBuildPath = null;
		if (classEntry.getModule() != null)
		{
			classEntryBuildPath = classEntry.getModule().getBuildPath();
		}

		if (superClassName != null)
		{
			if (superClassName.startsWith(PHPContentAssistProcessor.GLOBAL_NAMESPACE))
			{
				superClassName = superClassName.substring(1);
			}
			List<IElementEntry> classEntries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, superClassName);
			if (classEntries.isEmpty())
			{
				Set<String> superClass = new HashSet<String>(1);
				superClass.add(superClassName);
				Set<IElementEntry> entries = ContentAssistCollectors.collectBuiltinTypeEntries(superClass, true);
				classEntries.addAll(entries);
			}
			List<IElementEntry> classEntriesOnBuildPath = filterByBuildPath(classEntryBuildPath, classEntries);

			toFill.addAll(classEntriesOnBuildPath);

			for (IElementEntry entry : classEntriesOnBuildPath)
			{
				findClassAncestorsRecursivelly(entry, toFill, index);
			}
		}

		List<String> interfaces = entryValue.getInterfaces();
		if (interfaces != null && !interfaces.isEmpty())
		{
			for (String interfaceName : interfaces)
			{
				if (interfaceName.startsWith(PHPContentAssistProcessor.GLOBAL_NAMESPACE))
				{
					interfaceName = interfaceName.substring(1);
				}
				List<IElementEntry> classEntries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, interfaceName);

				List<IElementEntry> classEntriesOnBuildPath = filterByBuildPath(classEntryBuildPath, classEntries);

				toFill.addAll(classEntriesOnBuildPath);
				for (IElementEntry entry : classEntriesOnBuildPath)
				{
					findClassAncestorsRecursivelly(entry, toFill, index);
				}
			}
		}
	}

	/**
	 * Finds class direct ancestors.
	 * 
	 * @param _module
	 *            - module, class
	 * @param classEntry
	 *            - class entry.
	 * @param toFill
	 *            - list of ancestors to fill.
	 * @param index
	 *            - index to use.
	 */
	private static void findClassAncestors(IElementEntry classEntry, List<IElementEntry> toFill, IElementsIndex index)
	{
		Object value = classEntry.getValue();
		if (!(value instanceof ClassPHPEntryValue))
		{
			return;
		}

		ClassPHPEntryValue entryValue = (ClassPHPEntryValue) value;

		IBuildPath classEntryBuildPath = classEntry.getModule().getBuildPath();

		String superClassName = entryValue.getSuperClassname();
		if (superClassName != null)
		{
			if (superClassName.startsWith(PHPContentAssistProcessor.GLOBAL_NAMESPACE))
			{
				superClassName = superClassName.substring(1);
			}
			List<IElementEntry> classEntries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, superClassName);

			List<IElementEntry> classEntriesOnBuildPath = filterByBuildPath(classEntryBuildPath, classEntries);

			toFill.addAll(classEntriesOnBuildPath);
		}

		List<String> interfaces = entryValue.getInterfaces();
		if (interfaces != null && !interfaces.isEmpty())
		{
			for (String className : interfaces)
			{
				if (className.startsWith(PHPContentAssistProcessor.GLOBAL_NAMESPACE))
				{
					className = className.substring(1);
				}
				List<IElementEntry> classEntries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, className);

				List<IElementEntry> classEntriesOnBuildPath = filterByBuildPath(classEntryBuildPath, classEntries);

				toFill.addAll(classEntriesOnBuildPath);
			}
		}
	}

	/**
	 * Finds class descendants.
	 * 
	 * @param classEntry
	 *            - class entry.
	 * @param result
	 *            - result to add to.
	 * @param index
	 *            - index to use.
	 */
	private static void findClassDescendants(IElementEntry classEntry, List<IElementEntry> result, IElementsIndex index)
	{
		List<IElementEntry> typeEntries = index.getEntriesStartingWith(IPHPIndexConstants.CLASS_CATEGORY, ""); //$NON-NLS-1$

		IBuildPath classEntryBuildPath = classEntry.getModule().getBuildPath();

		String typeName = ElementsIndexingUtils.getLastNameInPath(classEntry.getEntryPath());

		if (EntryUtils.isInterface(classEntry))
		{
			// searching interface descendants
			for (IElementEntry typeEntry : typeEntries)
			{
				ClassPHPEntryValue value = (ClassPHPEntryValue) typeEntry.getValue();
				List<String> interfaceNames = value.getInterfaces();
				if (interfaceNames != null && interfaceNames.size() != 0)
				{
					for (String interfaceName : interfaceNames)
					{
						if (typeName.equals(interfaceName) && isOnBuildPath(classEntryBuildPath, typeEntry))
						{
							result.add(typeEntry);
						}
					}
				}
			}
		}
		else
		{
			// searching class descendants
			for (IElementEntry typeEntry : typeEntries)
			{
				ClassPHPEntryValue value = (ClassPHPEntryValue) typeEntry.getValue();
				String currentSuperClassName = value.getSuperClassname();
				if (currentSuperClassName != null)
				{
					if (typeName.equals(currentSuperClassName) && isOnBuildPath(classEntryBuildPath, typeEntry))
					{
						result.add(typeEntry);
					}
				}
			}
		}
	}

	/**
	 * Finds class descendants.
	 * 
	 * @param classEntry
	 *            - class entry.
	 * @param result
	 *            - result to add to.
	 * @param index
	 *            - index to use.
	 */
	private static void findClassDescendantsRecursivelly(IElementEntry classEntry, List<IElementEntry> result,
			IElementsIndex index)
	{
		List<IElementEntry> typeEntries = index.getEntriesStartingWith(IPHPIndexConstants.CLASS_CATEGORY, ""); //$NON-NLS-1$

		IBuildPath classEntryBuildPath = classEntry.getModule().getBuildPath();

		String typeName = ElementsIndexingUtils.getLastNameInPath(classEntry.getEntryPath());

		if (EntryUtils.isInterface(classEntry))
		{
			// searching interface descendants
			for (IElementEntry typeEntry : typeEntries)
			{
				ClassPHPEntryValue value = (ClassPHPEntryValue) typeEntry.getValue();
				List<String> interfaceNames = value.getInterfaces();
				if (interfaceNames != null && interfaceNames.size() != 0)
				{
					for (String interfaceName : interfaceNames)
					{
						if (typeName.equals(interfaceName) && isOnBuildPath(classEntryBuildPath, typeEntry))
						{
							result.add(typeEntry);
							findClassAncestorsRecursivelly(typeEntry, result, index);
						}
					}
				}
			}
		}
		else
		{
			// searching class descendants
			for (IElementEntry typeEntry : typeEntries)
			{
				ClassPHPEntryValue value = (ClassPHPEntryValue) typeEntry.getValue();
				String currentSuperClassName = value.getSuperClassname();
				if (currentSuperClassName != null)
				{
					if (typeName.equals(currentSuperClassName) && isOnBuildPath(classEntryBuildPath, typeEntry))
					{
						result.add(typeEntry);
						findClassAncestorsRecursivelly(typeEntry, result, index);
					}
				}
			}
		}
	}

	/**
	 * TypeHierarchyUtils private constructor.
	 */
	private TypeHierarchyUtils()
	{
	}
}
