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
 * Reference for a call path starting with a static reference.
 * 
 * @author Denis Denisenko
 */
public class StaticPathReference extends AbstractPathReference
{
	/**
	 * Dispatcher types.
	 */
	private Set<Object> dispatcherTypes;

	/**
	 * StaticPathReference constructor.
	 * 
	 * @param dispatcherTypes
	 *            - dispatcher types set.
	 * @param path
	 *            - call path after the first dispatcher. In example reference for ClasssName::$b->$method() would have
	 *            all possible types having ClassName name as dispatcher types and [b, method] as a path.
	 */
	public StaticPathReference(Set<Object> dispatcherTypes, CallPath path)
	{
		super(path);
		this.dispatcherTypes = dispatcherTypes;
	}

	public StaticPathReference(DataInputStream di) throws IOException
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
		return IPHPIndexConstants.IMPORT_CATEGORY;
	}

	@Override
	protected void internalWrite(DataOutputStream da) throws IOException
	{
		IndexPersistence.writeTypeSet(dispatcherTypes, da);
	}
}
