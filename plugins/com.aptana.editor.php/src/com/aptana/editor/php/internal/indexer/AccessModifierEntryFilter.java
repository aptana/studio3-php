/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org2.eclipse.php.core.compiler.PHPFlags;

import com.aptana.editor.php.indexer.IElementEntry;

/**
 * Filter that is aware of methods and fields access modifiers.<b> This filter will collects any element that is
 * contained in the root classes. It returns an accepted elements according to the package visibility modifier passed to
 * the constructor.
 * 
 * @author Denis Denisenko, Shalom Gibly
 */
public class AccessModifierEntryFilter implements IEntryFilter
{
	/**
	 * Names of root classes.
	 */
	private Set<String> rootClasses;
	private final boolean isPackageVisibility;

	/**
	 * AccessModifierEntryFilter constructor.<br>
	 * The package visibility argument defines whether the filter will return only public or protected fields, or will
	 * return all fields.
	 * 
	 * @param rootClasses
	 *            - names of root classes.
	 * @param isPackageVisibility
	 *            - Defines the access rule for this filter
	 */
	public AccessModifierEntryFilter(Set<String> rootClasses, boolean isPackageVisibility)
	{
		this.rootClasses = rootClasses;
		this.isPackageVisibility = isPackageVisibility;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<IElementEntry> filter(Collection<IElementEntry> toFilter)
	{
		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();

		for (IElementEntry entry : toFilter)
		{
			if (accept(entry))
			{
				result.add(entry);
			}
		}
		return result;
	}

	/**
	 * Gets entry class name.
	 * 
	 * @param entry
	 *            - entry.
	 * @return class name.
	 */
	private static String getClassName(IElementEntry entry)
	{
		return ElementsIndexingUtils.getFirstNameInPath(entry.getEntryPath());
	}

	/**
	 * Returns true if the given entry should be added to the result.
	 * 
	 * @param entry
	 *            An {@link IElementEntry}
	 * @return True, if the entry should be added to the results, false otherwise.
	 */
	protected boolean accept(IElementEntry entry)
	{
		Object value = entry.getValue();
		if (!(value instanceof FunctionPHPEntryValue || value instanceof VariablePHPEntryValue))
		{
			return true;
		}
		int modifiers = ((AbstractPHPEntryValue) value).getModifiers();
		if (isPackageVisibility)
		{
			if (rootClasses.contains(getClassName(entry))
					&& (PHPFlags.isPublic(modifiers) || PHPFlags.isProtected(modifiers)))
			{
				return true;
			}
		}
		else
		{
			if (rootClasses.contains(getClassName(entry)) || PHPFlags.isPublic(modifiers)
					|| PHPFlags.isProtected(modifiers))
			{
				return true;
			}
		}
		return false;
	}
}
