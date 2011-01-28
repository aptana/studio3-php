/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.model.utils;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.aptana.editor.php.indexer.EntryUtils;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.indexer.FunctionPHPEntryValue;
import com.aptana.editor.php.internal.indexer.VariablePHPEntryValue;

/**
 * Utilities for analyzing type contents.
 * 
 * @author Denis Denisenko
 */
public class TypeUtils
{

	/**
	 * Gets type fields using the global index.
	 * 
	 * @param typeEntry
	 *            - type entry.
	 * @return list of type fields or empty list otherwise.
	 */
	public static List<IElementEntry> getFields(IElementEntry typeEntry)
	{
		return getFields(typeEntry, PHPGlobalIndexer.getInstance().getIndex());
	}

	/**
	 * Gets type fields using the index specified.
	 * 
	 * @param typeEntry
	 *            - type entry.
	 * @param index
	 *            - index to use.
	 * @return list of type fields or empty list otherwise.
	 */
	public static List<IElementEntry> getFields(IElementEntry typeEntry, IElementsIndex index)
	{
		if (typeEntry.getCategory() != IPHPIndexConstants.CLASS_CATEGORY)
		{
			throw new IllegalArgumentException("Only class entries are accepted"); //$NON-NLS-1$
		}

		IModule module = typeEntry.getModule();

		List<IElementEntry> entries = index.getEntriesStartingWith(IPHPIndexConstants.VAR_CATEGORY, typeEntry
				.getEntryPath());

		// filtering entries
		Iterator<IElementEntry> it = entries.iterator();
		while (it.hasNext())
		{
			IElementEntry currentEntry = it.next();
			if (!currentEntry.getModule().equals(module))
			{
				it.remove();
				continue;
			}

			if (!(currentEntry.getValue() instanceof VariablePHPEntryValue)
					|| !((VariablePHPEntryValue) currentEntry.getValue()).isField())
			{
				it.remove();
				continue;
			}
		}

		return entries;
	}

	/**
	 * Gets type methods using the global index.
	 * 
	 * @param typeEntry
	 *            - type entry.
	 * @return list of type methods or empty list otherwise.
	 */
	public static List<IElementEntry> getMethods(IElementEntry typeEntry)
	{
		return getMethods(typeEntry, PHPGlobalIndexer.getInstance().getIndex());
	}

	/**
	 * Gets type methods using the index specified.
	 * 
	 * @param typeEntry
	 *            - type entry.
	 * @param index
	 *            - index to use.
	 * @return list of type methods or empty list otherwise.
	 */
	public static List<IElementEntry> getMethods(IElementEntry typeEntry, IElementsIndex index)
	{
		if (typeEntry.getCategory() != IPHPIndexConstants.CLASS_CATEGORY)
		{
			throw new IllegalArgumentException("Only class entries are accepted"); //$NON-NLS-1$
		}

		IModule module = typeEntry.getModule();

		List<IElementEntry> entries = index.getEntriesStartingWith(IPHPIndexConstants.FUNCTION_CATEGORY, typeEntry
				.getEntryPath());

		// filtering entries
		Iterator<IElementEntry> it = entries.iterator();
		while (it.hasNext())
		{
			IElementEntry currentEntry = it.next();
			if (!currentEntry.getModule().equals(module))
			{
				it.remove();
				continue;
			}

			if (!(currentEntry.getValue() instanceof FunctionPHPEntryValue)
					|| !((FunctionPHPEntryValue) currentEntry.getValue()).isMethod())
			{
				it.remove();
				continue;
			}
		}

		return entries;
	}

	/**
	 * Gets declaring type entry by a field entry or a method entry using the global index.
	 * 
	 * @param member
	 *            - member entry.
	 * @return declaring type entry or null.
	 */
	public static IElementEntry getDeclaringType(IElementEntry member)
	{
		return getDeclaringType(member, PHPGlobalIndexer.getInstance().getIndex());
	}

	/**
	 * Gets declaring type entry by a field entry or a method entry using the index specified.
	 * 
	 * @param member
	 *            - member entry.
	 * @param index
	 *            - index to use.
	 * @return declaring type entry or null.
	 */
	public static IElementEntry getDeclaringType(IElementEntry member, IElementsIndex index)
	{
		if (!(EntryUtils.isField(member) || EntryUtils.isMethod(member)))
		{
			throw new IllegalArgumentException("Only fields and methods are accepted"); //$NON-NLS-1$
		}

		IModule module = member.getModule();

		String memberPathStr = member.getEntryPath();
		IPath memberPath = new Path(memberPathStr);
		IPath classPath = memberPath.removeLastSegments(1);
		String classPathStr = classPath.toPortableString();
		List<IElementEntry> classEntries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, classPathStr);
		if (classEntries == null || classEntries.isEmpty())
		{
			return null;
		}

		for (IElementEntry classEntry : classEntries)
		{
			if (classEntry.getModule().equals(module))
			{
				return classEntry;
			}
		}

		return null;
	}

	/**
	 * TypeUtils private constructor.
	 */
	private TypeUtils()
	{
	}
}
