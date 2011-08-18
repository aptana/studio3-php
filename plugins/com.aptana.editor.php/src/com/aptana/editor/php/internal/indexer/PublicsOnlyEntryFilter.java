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
 * Filter that removes all private and protected fields and methods.
 * 
 * @author Denis Denisenko
 */
public class PublicsOnlyEntryFilter implements IEntryFilter
{

	/**
	 * {@inheritDoc}
	 */
	public Set<IElementEntry> filter(Collection<IElementEntry> toFilter)
	{
		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();

		for (IElementEntry entry : toFilter)
		{
			Object value = entry.getValue();
			if (value instanceof FunctionPHPEntryValue || value instanceof VariablePHPEntryValue)
			{
				int modifiers = ((AbstractPHPEntryValue) value).getModifiers();
				if (modifiers == 0 || PHPFlags.isPublic(modifiers))
				{
					result.add(entry);
				}
			}
			else
			{
				result.add(entry);
			}
		}

		return result;
	}
}
