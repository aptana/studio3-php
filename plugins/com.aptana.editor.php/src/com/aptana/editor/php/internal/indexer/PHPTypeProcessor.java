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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.php.core.compiler.PHPFlags;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IPHPIndexConstants;

/**
 * PHPTypeProcessor
 * 
 * @author Denis Denisenko
 */
public final class PHPTypeProcessor
{
	/**
	 * Maximum recursion depth.
	 */
	private static final int MAX_REC_DEPTH = 10;

	/**
	 * Processes encoded types and returns the decoded type names list.
	 * 
	 * @param encodedTypes
	 *            - types to process.
	 * @param indexer
	 *            - indexer to use for processing.
	 * @return encoded types and returns the decoded type names list.
	 */
	public static Set<String> processTypes(Set<Object> encodedTypes, IElementsIndex indexer)
	{
		Set<String> result = new HashSet<String>();

		processTypes(encodedTypes, result, indexer, 0);

		return result;
	}

	/**
	 * Processes encoded types and returns the decoded types list.
	 * 
	 * @param encodedTypes
	 *            - types to process.
	 * @param indexer
	 *            - indexer to use for processing.
	 * @param depth
	 *            - current depth.
	 * @return
	 */
	private static Set<String> processTypes(Set<Object> encodedTypes, IElementsIndex indexer, int depth)
	{
		Set<String> result = new HashSet<String>();

		processTypes(encodedTypes, result, indexer, depth);

		return result;
	}

	/**
	 * Filters the set of types and returns the custom types only.
	 * 
	 * @param types
	 *            - set of types to choose custom types from.
	 * @return set of custom types.
	 */
	public static Set<String> getCustomTypes(Set<String> types)
	{
		Set<String> result = new HashSet<String>();

		for (String type : types)
		{
			if (!isBuiltInType(type))
			{
				result.add(type);
			}
		}

		return result;
	}

	/**
	 * Gets whether type is built-in.
	 * 
	 * @param type
	 *            - type name.
	 * @return true if built-in, false otherwise.
	 */
	public static boolean isBuiltInType(String type)
	{
		if (IPHPIndexConstants.BOOLEAN_TYPE.equals(type))
		{
			return true;
		}
		else if (IPHPIndexConstants.STRING_TYPE.equals(type))
		{
			return true;
		}
		else if (IPHPIndexConstants.INTEGER_TYPE.equals(type))
		{
			return true;
		}
		else if (IPHPIndexConstants.REAL_TYPE.equals(type))
		{
			return true;
		}
		else if (IPHPIndexConstants.SYSTEM_TYPE.equals(type))
		{
			return true;
		}

		return false;
	}

	/**
	 * Processes types.
	 * 
	 * @param encodedTypes
	 *            - encoded types.
	 * @param result
	 *            - result to fill.
	 * @param indexer
	 *            - indexer.
	 * @param depth
	 *            - current depth.
	 */
	private static void processTypes(Set<Object> encodedTypes, Set<String> result, IElementsIndex indexer, int depth)
	{
		if (depth >= MAX_REC_DEPTH)
		{
			return;
		}

		for (Object type : encodedTypes)
		{
			if (type != null)
			{
				if (type instanceof String)
				{
					result.add((String) type);
				}
				else if (type instanceof VariablePathReference)
				{
					VariablePathReference ref = (VariablePathReference) type;

					Set<Object> dispatcherTypes = ref.getDispatcherTypes();
					procesPathReference(dispatcherTypes, ref.getPath(), result, indexer, depth + 1, false);
				}
				else if (type instanceof FunctionPathReference)
				{
					FunctionPathReference ref = (FunctionPathReference) type;

					List<IElementEntry> entries = indexer.getEntries(IPHPIndexConstants.FUNCTION_CATEGORY, ref
							.getFunctionEntryPath());
					for (IElementEntry entry : entries)
					{
						Set<Object> dispatcherTypes = getEntryDispatcherTypes(entry);
						if (dispatcherTypes != null && !dispatcherTypes.isEmpty())
						{
							procesPathReference(dispatcherTypes, ref.getPath(), result, indexer, depth + 1, false);
						}
					}
				}
				else if (type instanceof StaticPathReference)
				{
					StaticPathReference ref = (StaticPathReference) type;

					Set<Object> dispatcherTypes = ref.getDispatcherTypes();
					procesPathReference(dispatcherTypes, ref.getPath(), result, indexer, depth + 1, true);
				}
			}
		}
	}

	/**
	 * Processes variable call path reference.
	 * 
	 * @param ref
	 *            - reference.
	 * @param result
	 *            - result to fill.
	 * @param indexer
	 *            - indexer.
	 * @param depth
	 *            - current recursion depth.
	 * @param dispatcherTypes
	 *            - dispatcher types.
	 * @param path
	 *            - call path to process.
	 * @param staticsOnly
	 *            - whether to accept static entries only.
	 */
	private static void procesPathReference(Set<Object> dispatcherTypes, CallPath path, Set<String> result,
			IElementsIndex indexer, int depth, boolean staticsOnly)
	{
		if (depth >= MAX_REC_DEPTH)
		{
			return;
		}

		if (dispatcherTypes == null || dispatcherTypes.isEmpty())
		{
			return;
		}

		Set<Object> encodedRefTypes = dispatcherTypes;
		Set<String> decodedRefTypes = processTypes(encodedRefTypes, indexer, depth + 1);
		Set<String> customRefTypes = getCustomTypes(decodedRefTypes);
		if (customRefTypes.isEmpty())
		{
			return;
		}

		if (path == null || path.getSize() == 0)
		{
			result.addAll(customRefTypes);
			return;
		}
		CallPath.Entry firstPathEntry = path.getEntries().get(0);

		// reference for all possible dispatcher types
		for (String type : customRefTypes)
		{
			String entryName = firstPathEntry.getName();
			String entryPath = type + IElementsIndex.DELIMITER + entryName;

			int category = -1;
			if (firstPathEntry instanceof CallPath.VariableEntry)
			{
				category = IPHPIndexConstants.VAR_CATEGORY;
			}
			else if (firstPathEntry instanceof CallPath.MethodEntry)
			{
				category = IPHPIndexConstants.FUNCTION_CATEGORY;
			}

			List<IElementEntry> entries = indexer.getEntries(category, entryPath);
			// processing all entries got by the type and the first path entry
			for (IElementEntry entry : entries)
			{
				// skipping non-static entries if "statics only" flag is on
				if (staticsOnly && !isStaticEntry(entry))
				{
					continue;
				}

				Set<Object> entryTypes = getEntryDispatcherTypes(entry);
				if (entryTypes == null || entryTypes.isEmpty())
				{
					continue;
				}

				CallPath subPath = path.subPath(1);

				// if we have more of path to process, do it recursively
				if (subPath.getSize() != 0)
				{
					procesPathReference(entryTypes, subPath, result, indexer, depth + 1, false);
				}
				// in other case reporting the field types found
				else
				{
					processTypes(entryTypes, result, indexer, depth + 1);
				}
			}
		}
	}

	/**
	 * Gets entry dispatcher types: for variable that are variable types, for method that are return types.
	 * 
	 * @return types set or null
	 */
	private static Set<Object> getEntryDispatcherTypes(IElementEntry entry)
	{
		Object val = entry.getValue();
		if (val instanceof VariablePHPEntryValue)
		{
			VariablePHPEntryValue entryValue = (VariablePHPEntryValue) val;
			return entryValue.getTypes();
		}
		else if (val instanceof FunctionPHPEntryValue)
		{
			FunctionPHPEntryValue entryValue = (FunctionPHPEntryValue) val;
			return entryValue.getReturnTypes();
		}

		return null;
	}

	/**
	 * Checks whether entry is static.
	 * 
	 * @param entry
	 *            - entry.
	 * @return true if entry is static, false otherwise.
	 */
	private static boolean isStaticEntry(IElementEntry entry)
	{
		Object entryValue = entry.getValue();
		if (entryValue instanceof AbstractPHPEntryValue)
		{
			int modifiers = ((AbstractPHPEntryValue) entryValue).getModifiers();
			return PHPFlags.isStatic(modifiers);
		}

		return false;
	}

	/**
	 * PHPTypeProcessor private constructor.
	 */
	private PHPTypeProcessor()
	{
	}
}
