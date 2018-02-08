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
 * Abstract call path - based reference
 * 
 * @author Denis Denisenko
 */
public abstract class AbstractPathReference
{
	/**
	 * Call path.
	 */
	private CallPath path;

	/**
	 * AbstractPathReference constructor.
	 * 
	 * @param path
	 *            - call path.
	 */
	protected AbstractPathReference(CallPath path)
	{
		this.path = path;
	}

	/**
	 * Gets path.
	 * 
	 * @return path. null means no path is available (pure function reference).
	 */
	public CallPath getPath()
	{
		return path;
	}

	public void write(DataOutputStream da) throws IOException
	{
		da.writeInt(getKind());
		boolean has = path != null;
		da.writeBoolean(has);
		if (has)
		{
			path.write(da);
		}
		internalWrite(da);
	}

	protected static CallPath readPathOrNull(DataInputStream di) throws IOException
	{
		if (di.readBoolean())
		{
			return new CallPath(di);
		}
		return null;
	}

	protected abstract int getKind();

	protected abstract void internalWrite(DataOutputStream da) throws IOException;

	public static AbstractPathReference read(DataInputStream di) throws IOException
	{
		int readInt = di.readInt();
		switch (readInt)
		{
			case IPHPIndexConstants.FUNCTION_CATEGORY:
				return new FunctionPathReference(di);
			case IPHPIndexConstants.VAR_CATEGORY:
				return new VariablePathReference(di);
			case IPHPIndexConstants.IMPORT_CATEGORY:
				return new StaticPathReference(di);

			default:
				throw new IllegalArgumentException("unknown path reference:" + readInt); //$NON-NLS-1$
		}
	}
}
