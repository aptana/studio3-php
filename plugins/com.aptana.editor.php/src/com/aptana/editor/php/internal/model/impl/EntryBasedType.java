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
package com.aptana.editor.php.internal.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.php.core.compiler.PHPFlags;

import com.aptana.editor.php.core.model.IField;
import com.aptana.editor.php.core.model.IMethod;
import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.ISourceRange;
import com.aptana.editor.php.core.model.IType;
import com.aptana.editor.php.core.model.env.ModelElementInfo;
import com.aptana.editor.php.indexer.EntryUtils;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.indexer.ClassPHPEntryValue;
import com.aptana.editor.php.internal.model.impl.env.SourceTypeElementInfo;
import com.aptana.editor.php.internal.model.utils.ModelUtils;
import com.aptana.editor.php.internal.model.utils.TypeHierarchyUtils;
import com.aptana.editor.php.internal.model.utils.TypeUtils;

/**
 * EntryBasedType
 * 
 * @author Denis Denisenko
 */
public class EntryBasedType extends AbstractMember implements IType
{
	/**
	 * Entry value.
	 */
	private ClassPHPEntryValue entryValue;

	/**
	 * EntryBasedType constructor.
	 * 
	 * @param typeEntry
	 *            - type entry.
	 */
	public EntryBasedType(IElementEntry typeEntry)
	{
		super(typeEntry);

		if (!EntryUtils.isType(typeEntry))
		{
			throw new IllegalArgumentException("type entry required"); //$NON-NLS-1$
		}

		this.entryValue = (ClassPHPEntryValue) typeEntry.getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public IField getField(String fieldName)
	{
		List<IField> fields = getFields();
		for (IField field : fields)
		{
			if (fieldName.equals(field.getElementName()))
			{
				return field;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IField> getFields()
	{
		List<IElementEntry> fieldEntries = TypeUtils.getFields(getEntry());
		List<IModelElement> converted = ModelUtils.convertEntries(fieldEntries);
		List<IField> result = new ArrayList<IField>();
		for (IModelElement element : converted)
		{
			if (element instanceof IField)
			{
				result.add((IField) element);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getInterfaceNames()
	{
		return entryValue.getInterfaces();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getInterfaces()
	{
		List<String> interfaceNames = getInterfaceNames();
		if (interfaceNames == null || interfaceNames.size() == 0)
		{
			return Collections.emptyList();
		}

		List<IElementEntry> ancestors = TypeHierarchyUtils.getDirectClassAncestors(getEntry().getModule(),
				getElementName(), PHPGlobalIndexer.getInstance().getIndex());

		List<IType> result = new ArrayList<IType>();
		for (IElementEntry entry : ancestors)
		{
			if (EntryUtils.isInterface(entry))
			{
				result.add((IType) ModelUtils.convertEntry(entry));
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IMethod> getMethods()
	{
		List<IElementEntry> methodEntries = TypeUtils.getMethods(getEntry());
		List<IModelElement> converted = ModelUtils.convertEntries(methodEntries);
		List<IMethod> result = new ArrayList<IMethod>();
		for (IModelElement element : converted)
		{
			if (element instanceof IMethod)
			{
				result.add((IMethod) element);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IMethod> getMethods(String methodName)
	{
		List<IMethod> methods = getMethods();
		List<IMethod> result = new ArrayList<IMethod>();
		for (IMethod method : methods)
		{
			if (methodName.equals(method.getElementName()))
			{
				result.add(method);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInterface()
	{
		return PHPFlags.isInterface(entryValue.getModifiers());
	}

	/**
	 * {@inheritDoc}
	 */
	public int getFlags()
	{
		return entryValue.getModifiers();
	}

	/**
	 * {@inheritDoc}
	 */
	public ISourceRange getNameRange()
	{
		// TODO add name length
		return new SourceRange(entryValue.getStartOffset());
	}

	/**
	 * {@inheritDoc}
	 */
	public int getElementType()
	{
		return TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getSuperClasses()
	{
		List<String> superClassNames = getSuperClassNames();
		if (superClassNames == null || superClassNames.size() == 0)
		{
			return null;
		}

		List<IElementEntry> ancestors = TypeHierarchyUtils.getDirectClassAncestors(getEntry().getModule(),
				getElementName(), PHPGlobalIndexer.getInstance().getIndex());

		for (IElementEntry entry : ancestors)
		{
			if (!EntryUtils.isInterface(entry))
			{
				List<IType> result = new ArrayList<IType>();
				result.add((IType) ModelUtils.convertEntry(entry));
				return result;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getSuperClassNames()
	{
		String superClassName = entryValue.getSuperClassname();
		if (superClassName == null)
		{
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<String>();
		result.add(superClassName);

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getSuperTypes()
	{
		List<String> interfaceNames = getInterfaceNames();
		List<String> superClassNames = getSuperClassNames();

		if ((interfaceNames == null || interfaceNames.size() == 0)
				&& (superClassNames == null || superClassNames.size() == 0))
		{
			return Collections.emptyList();
		}

		List<IElementEntry> ancestors = TypeHierarchyUtils.getDirectClassAncestors(getEntry().getModule(),
				getElementName(), PHPGlobalIndexer.getInstance().getIndex());

		List<IType> result = new ArrayList<IType>();
		for (IElementEntry entry : ancestors)
		{
			result.add((IType) ModelUtils.convertEntry(entry));
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return getElementName();
	}

	/**
	 * {@inheritDoc}
	 */
	public ModelElementInfo getElementInfo()
	{
		SourceTypeElementInfo info = new SourceTypeElementInfo();
		info.setFlags(getFlags());
		info.setNameSourceStart(getSourceRange().getOffset());

		List<IModelElement> children = getChildren();
		if (children != null && children.size() != 0)
		{
			info.setChildren(children);
		}

		List<String> superClassNames = getSuperClassNames();
		if (superClassNames != null && superClassNames.size() != 0)
		{
			info.setSuperclassNames(superClassNames.toArray(new String[superClassNames.size()]));
		}

		return info;
	}
}
