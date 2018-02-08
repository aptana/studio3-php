/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.indexer;

import org2.eclipse.php.core.compiler.PHPFlags;

import com.aptana.editor.php.internal.indexer.ClassPHPEntryValue;
import com.aptana.editor.php.internal.indexer.FunctionPHPEntryValue;
import com.aptana.editor.php.internal.indexer.TraitPHPEntryValue;
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
	 * Gets whether the entry is a trait entry.
	 * 
	 * @param entry
	 *            - entry.
	 * @return whether the entry is a trait entry.
	 */
	public static boolean isTrait(IElementEntry entry)
	{
		if (entry.getCategory() == IPHPIndexConstants.CLASS_CATEGORY && entry.getValue() instanceof TraitPHPEntryValue)
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
