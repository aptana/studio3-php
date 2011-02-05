/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.model.impl;

import com.aptana.editor.php.core.model.IField;
import com.aptana.editor.php.core.model.ISourceRange;
import com.aptana.editor.php.core.model.env.ModelElementInfo;
import com.aptana.editor.php.core.model.env.SourceFieldElementInfo;
import com.aptana.editor.php.indexer.EntryUtils;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.internal.indexer.VariablePHPEntryValue;

/**
 * EntryBasedField
 * 
 * @author Denis Denisenko
 */
public class EntryBasedField extends AbstractMember implements IField
{
	/**
	 * Value.
	 */
	private VariablePHPEntryValue entryValue;

	/**
	 * EntryBasedField constructor.
	 * 
	 * @param fieldEntry
	 *            - field entry.
	 */
	public EntryBasedField(IElementEntry fieldEntry)
	{
		super(fieldEntry);

		if (!EntryUtils.isField(fieldEntry))
		{
			throw new IllegalArgumentException("field entry required"); //$NON-NLS-1$
		}

		this.entryValue = (VariablePHPEntryValue) fieldEntry.getValue();
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
		return FIELD;
	}

	/**
	 * {@inheritDoc}
	 */
	public ModelElementInfo getElementInfo()
	{
		SourceFieldElementInfo info = new SourceFieldElementInfo();
		info.setFlags(getFlags());
		return info;
	}
}
