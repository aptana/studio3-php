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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.aptana.editor.php.indexer.IReportable;

/**
 * Abstract PHP entry value
 * @author Denis Denisenko
 */
public abstract class AbstractPHPEntryValue implements IReportable
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
	 * @param modifiers - modifiers.
	 */
	public AbstractPHPEntryValue(int modifiers,String namespace)
	{
		this.modifiers = modifiers;
		this.nameSpace=namespace;
	}

	public AbstractPHPEntryValue(DataInputStream di) throws IOException{
		this.modifiers=di.readInt();
		this.startOffset=di.readInt();
		nameSpace=di.readUTF();
	}

	

	/**
	 * Gets modifiers. 
	 * @return
	 */
	public int getModifiers()
	{
		return modifiers;
	}
	
	/**
	 * Gets entry start offset. 
	 * @return entry start offset.
	 */
	public int getStartOffset()
	{
		return startOffset;
	}
	
	/**
	 * Sets entry start offset.
	 * @param startOffset - offset.
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

	public void store(DataOutputStream da) throws IOException {
		da.writeInt(this.getKind());
		da.writeInt(this.modifiers);
		da.writeInt(this.getStartOffset());
		da.writeUTF(this.nameSpace);
		internalWrite(da);
	}

	public abstract int getKind();
	
	protected abstract void internalWrite(DataOutputStream da) throws IOException;
	
	protected abstract void internalRead(DataInputStream di) throws IOException ;
	
	public String getNameSpace(){
		return nameSpace;
	}
}
