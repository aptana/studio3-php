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
package com.aptana.editor.php.internal.indexer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.php.core.compiler.PHPFlags;

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
	public LinkedHashSet<IElementEntry> filter(Collection<IElementEntry> toFilter)
	{
		LinkedHashSet<IElementEntry> result = new LinkedHashSet<IElementEntry>();

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
