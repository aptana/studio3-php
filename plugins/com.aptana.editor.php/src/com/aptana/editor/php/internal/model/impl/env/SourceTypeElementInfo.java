/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package com.aptana.editor.php.internal.model.impl.env;

import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.IType;
import com.aptana.editor.php.core.model.env.ISourceField;
import com.aptana.editor.php.core.model.env.ISourceMethod;
import com.aptana.editor.php.core.model.env.ISourceType;
import com.aptana.editor.php.core.model.env.MemberElementInfo;
import com.aptana.editor.php.internal.model.impl.AbstractModelElement;
import com.aptana.editor.php.internal.model.impl.EntryBasedField;
import com.aptana.editor.php.internal.model.impl.EntryBasedMethod;
import com.aptana.editor.php.internal.model.impl.EntryBasedType;

public class SourceTypeElementInfo extends MemberElementInfo implements ISourceType
{

	protected static final EntryBasedField[] NO_FIELDS = new EntryBasedField[0];

	protected static final EntryBasedMethod[] NO_METHODS = new EntryBasedMethod[0];

	protected static final EntryBasedType[] NO_TYPES = new EntryBasedType[0];

	/**
	 * The name of the superclasses for this type.
	 */
	protected String[] superclassNames;

	/**
	 * Backpointer to my type handle - useful for translation from info to handle.
	 */
	protected IType handle = null;

	/**
	 * Sets the handle for this type info
	 */
	protected void setHandle(IType handle)
	{
		this.handle = handle;
	}

	public void setSuperclassNames(String[] superclassNames)
	{
		this.superclassNames = superclassNames;
	}

	public String[] getSuperclassNames()
	{
		return superclassNames;
	}

	public ISourceType getEnclosingType()
	{
		IModelElement parent = this.handle.getParent();
		if (parent != null && parent.getElementType() == IModelElement.TYPE)
		{
			return (ISourceType) ((AbstractModelElement) parent).getElementInfo();
		}
		else
		{
			return null;
		}
	}

	public ISourceField[] getFields()
	{
		EntryBasedField[] fieldHandles = getFieldHandles();
		int length = fieldHandles.length;
		ISourceField[] fields = new ISourceField[length];
		for (int i = 0; i < length; i++)
		{
			ISourceField field = (ISourceField) fieldHandles[i].getElementInfo();
			fields[i] = field;
		}
		return fields;
	}

	public ISourceType[] getMemberTypes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ISourceMethod[] getMethods()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public char[] getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public char[] getSuperclassName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public char[][][] getTypeParameterBounds()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public char[][] getTypeParameterNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isBinaryType()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public char[] getFileName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public EntryBasedField[] getFieldHandles()
	{
		int length = size();
		if (length == 0)
			return NO_FIELDS;
		EntryBasedField[] fields = new EntryBasedField[length];
		int fieldIndex = 0;
		for (int i = 0; i < length; i++)
		{
			IModelElement child = get(i);
			if (child instanceof EntryBasedField)
				fields[fieldIndex++] = (EntryBasedField) child;
		}
		if (fieldIndex == 0)
			return NO_FIELDS;
		if (fieldIndex < length)
			System.arraycopy(fields, 0, fields = new EntryBasedField[fieldIndex], 0, fieldIndex);
		return fields;
	}

	public IType getHandle()
	{
		return this.handle;
	}
}
