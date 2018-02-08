/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

/**
 * PHP entry value for traits.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class TraitPHPEntryValue extends ClassPHPEntryValue
{

	/**
	 * TraitPHPEntryValue constructor.
	 * 
	 * @param modifiers
	 *            - modifiers.
	 */
	public TraitPHPEntryValue(int modifiers, String namespace)
	{
		super(modifiers, namespace);
	}

	/**
	 * TraitPHPEntryValue constructor.
	 * 
	 * @param modifiers
	 *            - modifiers.
	 * @param superClassName
	 *            - superclass name.
	 * @param interfaces
	 *            - interface names.
	 */
	public TraitPHPEntryValue(int modifiers, String superClassName, List<String> interfaces, String namespace)
	{
		super(modifiers, superClassName, interfaces, namespace);
	}

	public TraitPHPEntryValue(DataInputStream di) throws IOException
	{
		super(di);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof TraitPHPEntryValue))
		{
			return false;
		}
		return super.equals(obj);
	}
}
