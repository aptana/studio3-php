/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aptana.editor.php.core.model.IMethod;
import com.aptana.editor.php.core.model.ISourceRange;
import com.aptana.editor.php.core.model.env.ModelElementInfo;
import com.aptana.editor.php.core.model.env.SourceMethodElementInfo;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.internal.indexer.FunctionPHPEntryValue;

/**
 * EntryBasedMethod
 * 
 * @author Denis Denisenko
 */
public class EntryBasedMethod extends AbstractMember implements IMethod
{
	/**
	 * Value.
	 */
	private FunctionPHPEntryValue entryValue;

	/**
	 * EntryBasedMethod constructor.
	 * 
	 * @param methodEntry
	 *            - method entry.
	 */
	public EntryBasedMethod(IElementEntry methodEntry)
	{
		super(methodEntry);

		// if (!EntryUtils.isMethod(methodEntry))
		// {
		// throw new IllegalArgumentException("method entry required");
		// }

		this.entryValue = (FunctionPHPEntryValue) methodEntry.getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getParameters()
	{
		Set<String> keys = entryValue.getParameters().keySet();
		if (keys == null || keys.size() == 0)
		{
			return Collections.emptyList();
		}

		List<String> result = new ArrayList<String>();
		result.addAll(keys);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getFlags()
	{
		return entryValue.getModifiers();
	}

	/**
	 * {@inheritDoc}
	 */
	public ISourceRange getNameRange()
	{
		// TODO add name length
		return new SourceRange(entryValue.getStartOffset());
	}

	/**
	 * {@inheritDoc}
	 */
	public int getElementType()
	{
		return METHOD;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isConstructor()
	{
		// TODO Implement this
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getDirectParameterTypes()
	{
		Map<String, Set<Object>> paramsMap = entryValue.getParameters();

		List<String> result = new ArrayList<String>(paramsMap.size());
		for (Set<Object> paramTypes : paramsMap.values())
		{
			if (paramTypes.size() == 1)
			{
				Object type = paramTypes.iterator().next();
				if (type != null && type instanceof String)
				{
					result.add((String) type);
				}
				else
				{
					result.add(null);
				}
			}
		}

		return result;
	}

	public int getModifiers()
	{
		return entryValue.getModifiers();
	}

	/**
	 * {@inheritDoc}
	 */
	public ModelElementInfo getElementInfo()
	{
		SourceMethodElementInfo info = new SourceMethodElementInfo();
		info.setFlags(getFlags());
		info.setNameSourceStart(getSourceRange().getOffset());

		List<String> parameters = getParameters();
		if (parameters != null)
		{
			info.setArgumentNames(parameters.toArray(new String[parameters.size()]));
		}

		List<String> directParameterTypes = getDirectParameterTypes();
		if (directParameterTypes != null)
		{
			info.setArgumentInializers(directParameterTypes.toArray(new String[directParameterTypes.size()]));
		}

		return info;
	}
}
