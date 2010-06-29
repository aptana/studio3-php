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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.php.core.compiler.PHPFlags;

import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.core.model.ISourceProject;
import com.aptana.editor.php.core.model.IType;
import com.aptana.editor.php.indexer.EntryUtils;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.ProjectBuildPath;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.indexer.ClassPHPEntryValue;
import com.aptana.editor.php.internal.model.ISourceFolder;
import com.aptana.editor.php.internal.model.ITypeHierarchy;
import com.aptana.editor.php.internal.model.impl.EntryBasedField;
import com.aptana.editor.php.internal.model.impl.EntryBasedMethod;
import com.aptana.editor.php.internal.model.impl.EntryBasedType;
import com.aptana.editor.php.internal.model.impl.SourceFolder;
import com.aptana.editor.php.internal.model.impl.SourceModule;
import com.aptana.editor.php.internal.model.impl.SourceProject;

/**
 * ModelUtils.
 * 
 * @author Denis Denisenko
 */
public class ModelUtils
{

	/**
	 * Converts entry to model element.
	 * 
	 * @param entry
	 *            - entry.
	 * @return entry or null.
	 */
	public static IModelElement convertEntry(IElementEntry entry)
	{
		if (EntryUtils.isType(entry))
		{
			return new EntryBasedType(entry);
		}
		else if (EntryUtils.isFunction(entry))
		{
			return new EntryBasedMethod(entry);
		}
		else if (EntryUtils.isField(entry))
		{
			return new EntryBasedField(entry);
		}
		else
		{
			return null;
		}
		// switch (entry.getCategory())
		// {
		// case IPHPIndexConstants.CLASS_CATEGORY:
		// return new EntryBasedType(entry);
		// case IPHPIndexConstants.FUNCTION_CATEGORY:
		// return new EntryBasedMethod(entry);
		// case IPHPIndexConstants.VAR_CATEGORY:
		// return new EntryBasedField(entry);
		// default: return null;
		// }
	}

	/**
	 * Converts module.
	 * 
	 * @param module
	 *            - module.
	 * @return module.
	 */
	public static ISourceModule convertModule(IModule module)
	{
		if (module == null)
		{
			return null;
		}
		return new SourceModule(module);
	}

	/**
	 * Converts folder.
	 * 
	 * @param dir
	 *            - directory to convert.
	 * @return folder.
	 */
	public static ISourceFolder convertFolder(IDirectory dir)
	{
		if (dir == null)
		{
			return null;
		}
		return new SourceFolder(dir);
	}

	/**
	 * Converts build-path to a project if possible.
	 * 
	 * @param buildPath
	 *            - build-path to convert.
	 * @return project or null.
	 */
	public static ISourceProject convertBuildPath(IBuildPath buildPath)
	{
		if (buildPath instanceof ProjectBuildPath)
		{
			return new SourceProject((ProjectBuildPath) buildPath);
		}

		return null;
	}

	/**
	 * Gets source module by file.
	 * 
	 * @param file
	 *            - file.
	 * @return source module or null
	 */
	public static ISourceModule getModule(IFile file)
	{
		IModule module = BuildPathManager.getInstance().getModuleByResource(file);
		if (module != null)
		{
			return convertModule(module);
		}

		return null;
	}

	/**
	 * Converts entries to model elements. If utils are unable to convert some entry, such an entry is skipped.
	 * 
	 * @param entries
	 *            - entries to convert.
	 * @return model elements
	 */
	public static List<IModelElement> convertEntries(List<IElementEntry> entries)
	{
		if (entries == null || entries.size() == 0)
		{
			return Collections.emptyList();
		}

		List<IModelElement> result = new ArrayList<IModelElement>();

		for (IElementEntry entry : entries)
		{
			IModelElement element = convertEntry(entry);
			if (element != null)
			{
				result.add(element);
			}
		}

		return result;
	}

	/**
	 * Converts types to model elements. If utils are unable to convert some entry, such an entry is skipped.
	 * 
	 * @param entries
	 *            - entries to convert.
	 * @return model elements
	 */
	public static List<IType> convertTypes(List<IElementEntry> entries)
	{
		if (entries == null || entries.size() == 0)
		{
			return Collections.emptyList();
		}

		List<IType> result = new ArrayList<IType>();

		for (IElementEntry entry : entries)
		{
			if (entry.getCategory() == IPHPIndexConstants.CLASS_CATEGORY)
			{
				IModelElement element = convertEntry(entry);
				if (element != null)
				{
					result.add((IType) element);
				}
			}
		}

		return result;
	}

	/**
	 * Converts classes (types that are not interfaces) to model elements. If utils are unable to convert some entry,
	 * such an entry is skipped.
	 * 
	 * @param entries
	 *            - entries to convert.
	 * @return model elements
	 */
	public static List<IType> convertClasses(List<IElementEntry> entries)
	{
		if (entries == null || entries.size() == 0)
		{
			return Collections.emptyList();
		}

		List<IType> result = new ArrayList<IType>();

		for (IElementEntry entry : entries)
		{
			if (entry.getCategory() == IPHPIndexConstants.CLASS_CATEGORY
					&& !PHPFlags.isInterface(((ClassPHPEntryValue) entry.getValue()).getModifiers()))
			{
				IModelElement element = convertEntry(entry);
				if (element != null)
				{
					result.add((IType) element);
				}
			}
		}

		return result;
	}

	/**
	 * Converts interfaces to model elements. If utils are unable to convert some entry, such an entry is skipped.
	 * 
	 * @param entries
	 *            - entries to convert.
	 * @return model elements
	 */
	public static List<IType> convertInterfaces(List<IElementEntry> entries)
	{
		if (entries == null || entries.size() == 0)
		{
			return Collections.emptyList();
		}

		List<IType> result = new ArrayList<IType>();

		for (IElementEntry entry : entries)
		{
			if (entry.getCategory() == IPHPIndexConstants.CLASS_CATEGORY
					&& PHPFlags.isInterface(((ClassPHPEntryValue) entry.getValue()).getModifiers()))
			{
				IModelElement element = convertEntry(entry);
				if (element != null)
				{
					result.add((IType) element);
				}
			}
		}

		return result;
	}

	/**
	 * Checks whether a type is a super type of another type.
	 * 
	 * @param hierarchy
	 *            - type hierarchy.
	 * @param possibleSuperType
	 *            - super type to check.
	 * @param type
	 *            - type.
	 * @return true if super type, false otherwise.
	 */
	public static boolean isSuperType(ITypeHierarchy hierarchy, IType possibleSuperType, IType type)
	{

		List<IType> superClass = hierarchy.getSuperclass(type);
		if (superClass != null)
		{
			for (int q = 0; q < superClass.size(); ++q)
			{
				if ((possibleSuperType.equals(superClass.get(q)) || isSuperType(hierarchy, possibleSuperType,
						superClass.get(q))))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * ModelUtils private constructor.
	 */
	private ModelUtils()
	{
	}
}
