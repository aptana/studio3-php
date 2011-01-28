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
import java.util.Set;

import com.aptana.editor.php.indexer.IPHPIndexConstants;

/**
 * Reference for a call path starting with variable.
 * 
 * @author Denis Denisenko
 */
public class VariablePathReference extends AbstractPathReference
{
	/**
	 * Dispatcher types.
	 */
	private Set<Object> dispatcherTypes;

	/**
	 * FieldReference constructor.
	 * 
	 * @param dispatcherTypes
	 *            - dispatcher types set.
	 * @param path
	 *            - call path after the first dispatcher. In example reference for $a->$b->$method() would have all
	 *            possible $a types as dispatcher types and [b, method] as a path.
	 */
	public VariablePathReference(Set<Object> dispatcherTypes, CallPath path)
	{
		super(path);
		this.dispatcherTypes = dispatcherTypes;
	}

	public VariablePathReference(DataInputStream di) throws IOException
	{
		super(readPathOrNull(di));
		this.dispatcherTypes = IndexPersistence.readTypeSet(di);
	}

	/**
	 * Gets dispatcher types set.
	 * 
	 * @return dispatcher types set.
	 */
	public Set<Object> getDispatcherTypes()
	{
		return dispatcherTypes;
	}

	@Override
	protected int getKind()
	{
		return IPHPIndexConstants.VAR_CATEGORY;
	}

	@Override
	protected void internalWrite(DataOutputStream da) throws IOException
	{
		IndexPersistence.writeTypeSet(dispatcherTypes, da);
	}
}
