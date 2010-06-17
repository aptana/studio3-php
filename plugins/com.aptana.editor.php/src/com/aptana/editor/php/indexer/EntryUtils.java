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
package com.aptana.editor.php.indexer;

import org.eclipse.php.core.compiler.PHPFlags;

import com.aptana.editor.php.internal.indexer.ClassPHPEntryValue;
import com.aptana.editor.php.internal.indexer.FunctionPHPEntryValue;
import com.aptana.editor.php.internal.indexer.VariablePHPEntryValue;

/**
 * Utilities for PHP index entries.
 * 
 * @author Denis Denisenko
 */
public class EntryUtils
{

	/**
	 * Gets whether the entry is a field entry.
	 * 
	 * @param entry
	 *            - entry.
	 * @return whether the entry is a field entry.
	 */
	public static boolean isField(IElementEntry entry)
	{
		if (entry.getCategory() != IPHPIndexConstants.VAR_CATEGORY)
		{
			return false;
		}

		if (!(entry.getValue() instanceof VariablePHPEntryValue)
				|| !((VariablePHPEntryValue) entry.getValue()).isField())
		{
			return false;
		}

		return true;
	}

	/**
	 * Gets whether the entry is a method entry.
	 * 
	 * @param entry
	 *            - entry.
	 * @return whether the entry is a method entry.
	 */
	public static boolean isMethod(IElementEntry entry)
	{
		if (entry.getCategory() != IPHPIndexConstants.FUNCTION_CATEGORY)
		{
			return false;
		}

		if (!(entry.getValue() instanceof FunctionPHPEntryValue)
				|| !((FunctionPHPEntryValue) entry.getValue()).isMethod())
		{
			return false;
		}

		return true;
	}

	/**
	 * Gets whether the entry is a type entry.
	 * 
	 * @param entry
	 *            - entry.
	 * @return whether the entry is a type entry.
	 */
	public static boolean isType(IElementEntry entry)
	{
		if (entry.getCategory() == IPHPIndexConstants.CLASS_CATEGORY && entry.getValue() instanceof ClassPHPEntryValue)
		{
			return true;
		}

		return false;
	}

	/**
	 * Gets whether the entry is a type entry.
	 * 
	 * @param entry
	 *            - entry.
	 * @return whether the entry is a type entry.
	 */
	public static boolean isInterface(IElementEntry entry)
	{
		if (entry.getCategory() == IPHPIndexConstants.CLASS_CATEGORY && entry.getValue() instanceof ClassPHPEntryValue
				&& PHPFlags.isInterface(((ClassPHPEntryValue) entry.getValue()).getModifiers()))
		{
			return true;
		}

		return false;
	}

	/**
	 * EntryUtils private constructor.
	 */
	private EntryUtils()
	{

	}

	/**
	 * Returns true iff the given entry is a function; False, otherwise.
	 * 
	 * @param entry
	 * @return true iff the given entry is a function.
	 */
	public static boolean isFunction(IElementEntry entry)
	{
		return entry.getCategory() == IPHPIndexConstants.FUNCTION_CATEGORY;
	}

	/**
	 * Returns true iff the given entry is a lambda function (PHP closure); False, otherwise.
	 * 
	 * @param entry
	 * @return true iff the given entry is a lambda function.
	 */
	public static boolean isLambdaFunction(IElementEntry entry)
	{
		return entry.getCategory() == IPHPIndexConstants.LAMBDA_FUNCTION_CATEGORY;
	}
}
