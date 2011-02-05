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

/**
 * Reference for a call path starting with variable.
 * 
 * @author Denis Denisenko
 */
public class FunctionPathReference extends AbstractPathReference
{
	/**
	 * Function name.
	 */
	private String functionEntryPath;

	/**
	 * FunctionReference constructor.
	 * 
	 * @param functionEntryPath
	 *            - function entry path like "Class/method".
	 * @param path
	 *            - call path.
	 */
	public FunctionPathReference(String functionEntryPath, CallPath path)
	{
		super(path);
		this.functionEntryPath = functionEntryPath;
	}

	public FunctionPathReference(DataInputStream di) throws IOException
	{
		super(readPathOrNull(di));
		functionEntryPath = di.readUTF();
	}

	/**
	 * Gets function entry.
	 * 
	 * @return function entry.
	 */
	public String getFunctionEntryPath()
	{
		return functionEntryPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((functionEntryPath == null) ? 0 : functionEntryPath.hashCode());
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
		final FunctionPathReference other = (FunctionPathReference) obj;
		if (functionEntryPath == null)
		{
			if (other.functionEntryPath != null)
				return false;
		}
		else if (!functionEntryPath.equals(other.functionEntryPath))
			return false;

		if (getPath() == null)
		{
			if (other.getPath() != null)
				return false;
		}
		else if (!getPath().compare(other.getPath()))
			return false;
		return true;
	}

	@Override
	protected void internalWrite(DataOutputStream da) throws IOException
	{
		da.writeUTF(functionEntryPath);
	}

	@Override
	protected int getKind()
	{
		return IPHPIndexConstants.FUNCTION_CATEGORY;
	}
}
