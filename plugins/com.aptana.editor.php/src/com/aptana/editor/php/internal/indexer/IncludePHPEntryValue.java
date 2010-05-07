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

import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.indexer.IReportable;

/**
 * IncludePHPEntryValue
 * 
 * @author Denis Denisenko
 */
public class IncludePHPEntryValue implements IReportable
{
	/**
	 * "Include" type.
	 */
	public static int INCLUDE_TYPE = 0;

	/**
	 * "Include once" type.
	 */
	public static int INCLUDE_ONCE_TYPE = 1;

	/**
	 * "Require" type.
	 */
	public static int REQUIRE_TYPE = 2;

	/**
	 * "Require once" type.
	 */
	public static int REQUIRE_ONCE_TYPE = 3;

	/**
	 * Include path.
	 */
	private final String includePath;

	/**
	 * Start offset.
	 */
	private final int startOffset;

	/**
	 * End offset.
	 */
	private final int endOffset;

	/**
	 * Path start offset.
	 */
	private final int pathStartOffset;

	/**
	 * Entry type.
	 */
	private final int type;

	/**
	 * IncludePHPEntryValue constructor.
	 * 
	 * @param includePath
	 *            - include path.
	 * @param startOffset
	 *            - include start offset.
	 * @param endOffset
	 *            - include end offset.
	 * @param type
	 *            - type of the entry.
	 */
	public IncludePHPEntryValue(String includePath, int startOffset, int endOffset, int pathStartOffset, int type)
	{
		this.includePath = includePath;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.pathStartOffset = pathStartOffset;
		this.type = type;
	}

	public IncludePHPEntryValue(DataInputStream di) throws IOException
	{
		this.includePath = di.readUTF();
		this.startOffset = di.readInt();
		this.endOffset = di.readInt();
		this.pathStartOffset = di.readInt();
		this.type = di.readInt();
	}

	public void store(DataOutputStream da) throws IOException
	{
		da.writeInt(this.getKind());
		da.writeUTF(includePath);
		da.writeInt(this.startOffset);
		da.writeInt(this.endOffset);
		da.writeInt(this.pathStartOffset);
		da.writeInt(this.type);
	}

	/**
	 * Gets include path.
	 * 
	 * @return include path
	 */
	public String getIncludePath()
	{
		return includePath;
	}

	@Override
	public String toString()
	{
		return "Include: " + includePath; //$NON-NLS-1$
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
	 * Gets entry end offset.
	 * 
	 * @return entry end offset.
	 */
	public int getEndOffset()
	{
		return endOffset;
	}

	/**
	 * Gets path start offset.
	 * 
	 * @return path start offset.
	 */
	public int getPathStartOffset()
	{
		return pathStartOffset;
	}

	/**
	 * Gets entry type.
	 * 
	 * @return entry type.
	 */
	public int getType()
	{
		return type;
	}

	public int getKind()
	{
		return IPHPIndexConstants.IMPORT_CATEGORY;
	}

}
