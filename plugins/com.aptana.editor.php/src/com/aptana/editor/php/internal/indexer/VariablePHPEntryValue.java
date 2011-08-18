/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import gnu.trove.set.hash.THashSet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import com.aptana.editor.php.indexer.IPHPIndexConstants;

/**
 * VariablePHPEntryValue
 * 
 * @author Denis Denisenko
 */
public class VariablePHPEntryValue extends AbstractPHPEntryValue
{
	/**
	 * Parameter type.
	 */
	private static final byte TYPE_PARAMETER = 1;

	/**
	 * Local type.
	 */
	private static final byte TYPE_LOCAL = 2;

	/**
	 * Field type.
	 */
	private static final byte TYPE_FIELD = 3;

	/**
	 * Variable kind.
	 */
	private byte kind = 0;

	/**
	 * Variable value types. Might be string value in case of direct type value or reference that might be used to count
	 * the type indirectly.
	 */
	private Set<Object> types;

	/**
	 * VariablePHPEntryValue constructor.
	 * 
	 * @param isParameter
	 *            - whether variable is parameter.
	 * @param isLocalVariable
	 *            - whether variable is local.
	 * @param isField
	 *            - whether variable is field.
	 * @param modifiers
	 *            - modifiers.
	 * @param type
	 *            - type to set. Might be string value in case of direct type value or reference that might be used to
	 *            count the type indirectly. null for unknown type.
	 * @param pos
	 *            - start position.
	 */
	public VariablePHPEntryValue(int modifiers, boolean isParameter, boolean isLocalVariable, boolean isField,
			Object type, int pos, String nameSpace)
	{
		super(modifiers, nameSpace);

		if (isParameter)
		{
			kind = TYPE_PARAMETER;
		}
		else if (isLocalVariable)
		{
			kind = TYPE_LOCAL;
		}
		else if (isField)
		{
			kind = TYPE_FIELD;
		}

		this.types = new THashSet<Object>(1);
		if (type != null)
		{
			types.add(type);
		}

		this.setStartOffset(pos);
	}

	/**
	 * VariablePHPEntryValue constructor.
	 * 
	 * @param isParameter
	 *            - whether variable is parameter.
	 * @param isLocalVariable
	 *            - whether variable is local.
	 * @param isField
	 *            - whether variable is field.
	 * @param modifiers
	 *            - modifiers.
	 * @param types
	 *            - types to set. Might be string value in case of direct type value or reference that might be used to
	 *            count the type indirectly. null for unknown type.
	 * @param pos
	 *            - start position.
	 */
	public VariablePHPEntryValue(int modifiers, boolean isParameter, boolean isLocalVariable, boolean isField,
			Set<Object> types, int pos, String nameSpace)
	{
		super(modifiers, nameSpace);

		if (isParameter)
		{
			kind = TYPE_PARAMETER;
		}
		else if (isLocalVariable)
		{
			kind = TYPE_LOCAL;
		}
		else if (isField)
		{
			kind = TYPE_FIELD;
		}

		if (types != null)
		{
			this.types = new THashSet<Object>(types.size());
			this.types.addAll(types);
		}

		this.setStartOffset(pos);
	}

	public VariablePHPEntryValue(DataInputStream di) throws IOException
	{
		super(di);
		internalRead(di);
	}

	/**
	 * Sets variable type.
	 * 
	 * @param type
	 *            - type to set. Might be string value in case of direct type value or reference that might be used to
	 *            count the type indirectly.
	 */
	public void setType(Object type)
	{
		this.types = new THashSet<Object>(1);
		types.add(type);
	}

	/**
	 * Adds a type to possible variable types.
	 * 
	 * @param type
	 *            - type to add. Might be string value in case of direct type value or reference that might be used to
	 *            count the type indirectly.
	 */
	public void addType(Object type)
	{
		if (types == null)
		{
			this.types = new THashSet<Object>(1);
		}
		types.add(type);
	}

	/**
	 * Gets variable types.
	 * 
	 * @return set of types, each might be string value in case of direct type value or reference that might be used to
	 *         count the type indirectly. null means unknown type.
	 */
	public Set<Object> getTypes()
	{
		if (types == null)
		{
			return Collections.emptySet();
		}
		return types;
	}

	/**
	 * Gets whether variable is local.
	 * 
	 * @return whether variable is local.
	 */
	public boolean isLocal()
	{
		return kind == TYPE_LOCAL;
	}

	/**
	 * Gets whether variable is field.
	 * 
	 * @return whether variable is field.
	 */
	public boolean isField()
	{
		return kind == TYPE_FIELD;
	}

	/**
	 * Gets whether variable is parameter.
	 * 
	 * @return whether variable is parameter.
	 */
	public boolean isParameter()
	{
		return kind == TYPE_PARAMETER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		int result = super.hashCode();
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final VariablePHPEntryValue other = (VariablePHPEntryValue) obj;
		if (kind != other.kind)
			return false;
		return true;
	}

	@Override
	public int getKind()
	{
		return IPHPIndexConstants.VAR_CATEGORY;
	}

	@Override
	protected void internalWrite(DataOutputStream da) throws IOException
	{
		da.write(kind);
		IndexPersistence.writeTypeSet(types, da);
	}

	@Override
	protected void internalRead(DataInputStream di) throws IOException
	{
		kind = di.readByte();
		types = IndexPersistence.readTypeSet(di);
	}
}
