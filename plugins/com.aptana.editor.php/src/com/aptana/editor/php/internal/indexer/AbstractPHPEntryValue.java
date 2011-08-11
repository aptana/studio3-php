/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.aptana.editor.php.indexer.IReportable;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.parsing.ast.ILanguageNode;

/**
 * Abstract PHP entry value
 * 
 * @author Denis Denisenko
 */
public abstract class AbstractPHPEntryValue implements IReportable, ILanguageNode
{
	/**
	 * Entry modifiers.
	 */
	private int modifiers;

	/**
	 * Entry start offset.
	 */
	private int startOffset;

	private String nameSpace;

	/**
	 * AbstractPHPEntryValue constructor.
	 * 
	 * @param modifiers
	 *            - modifiers.
	 */
	protected AbstractPHPEntryValue(int modifiers, String namespace)
	{
		this.modifiers = modifiers;
		this.nameSpace = namespace;
	}

	protected AbstractPHPEntryValue(DataInputStream di) throws IOException
	{
		this.modifiers = di.readInt();
		this.startOffset = di.readInt();
		nameSpace = di.readUTF();
	}

	/**
	 * Gets modifiers.
	 * 
	 * @return
	 */
	public int getModifiers()
	{
		return modifiers;
	}

	/**
	 * Gets entry start offset.
	 * 
	 * @return entry start offset.
	 */
	public int getStartOffset()
	{
		return startOffset;
	}

	/**
	 * Sets entry start offset.
	 * 
	 * @param startOffset
	 *            - offset.
	 */
	public void setStartOffset(int startOffset)
	{
		this.startOffset = startOffset;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + modifiers;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AbstractPHPEntryValue other = (AbstractPHPEntryValue) obj;
		if (modifiers != other.modifiers)
			return false;
		return true;
	}

	public void store(DataOutputStream da) throws IOException
	{
		da.writeInt(this.getKind());
		da.writeInt(this.modifiers);
		da.writeInt(this.getStartOffset());
		da.writeUTF(this.nameSpace);
		internalWrite(da);
	}

	public abstract int getKind();

	protected abstract void internalWrite(DataOutputStream da) throws IOException;

	protected abstract void internalRead(DataInputStream di) throws IOException;

	public String getNameSpace()
	{
		return nameSpace;
	}

	/* (non-Javadoc)
	 * @see com.aptana.parsing.ast.ILanguageNode#getLanguage()
	 */
	public String getLanguage()
	{
		return IPHPConstants.CONTENT_TYPE_PHP;
	}
}
