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
