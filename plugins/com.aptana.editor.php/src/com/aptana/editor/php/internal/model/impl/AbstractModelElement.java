/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
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

import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.IModelElementVisitor;
import com.aptana.editor.php.core.model.IParent;
import com.aptana.editor.php.core.model.ISourceModel;
import com.aptana.editor.php.core.model.ISourceProject;
import com.aptana.editor.php.core.model.env.ModelElementInfo;
import com.aptana.editor.php.internal.model.ModelManager;

/**
 * Abstract model element
 * 
 * @author Denis Denisenko
 */
public abstract class AbstractModelElement implements IModelElement
{

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.model.IModelElement#getAncestor(int)
	 */
	public IModelElement getAncestor(int ancestorType)
	{
		IModelElement element = getParent();
		while (element != null)
		{
			if (element.getElementType() == ancestorType)
			{
				return element;
			}

			element = element.getParent();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.model.IModelElement#getSourceProject()
	 */
	public ISourceProject getSourceProject()
	{
		return (ISourceProject) getAncestor(IModelElement.PROJECT);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.model.IModelElement#accept(com.aptana.editor.php.core.model.IModelElementVisitor)
	 */
	public void accept(IModelElementVisitor visitor)
	{
		boolean result = visitor.visit(this);

		if (result && this instanceof IParent)
		{
			for (IModelElement child : ((IParent) this).getChildren())
			{
				child.accept(visitor);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.model.IModelElement#getModel()
	 */
	public ISourceModel getModel()
	{
		return ModelManager.getInstance().getModel();
	}

	/**
	 * Gets model element info.
	 * 
	 * @return model element info.
	 */
	public ModelElementInfo getElementInfo()
	{
		// return null by default
		return null;
	}

	/**
	 * Gets element debug string.
	 * 
	 * @return element debug string
	 */
	public String toDebugString()
	{

		StringBuilder builder = new StringBuilder();
		this.fillStringInfo(0, builder, true);
		return builder.toString();
	}

	/**
	 * @param tabsCount
	 * @param builder
	 * @param showResolvedInfo
	 */
	protected void fillStringInfo(int tabsCount, StringBuilder builder, boolean showResolvedInfo)
	{
		builder.append(this.getStringTabs(tabsCount));
		toStringName(builder);
	}

	/**
	 * Adds element name to a builder.
	 * 
	 * @param builder
	 *            - builder.
	 */
	protected void toStringName(StringBuilder builder)
	{
		builder.append(getElementName());
	}

	/**
	 * Gets string consisting from tabs.
	 * 
	 * @param count
	 *            - number of tabs.
	 * @return string
	 */
	protected String getStringTabs(int count)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = count; i > 0; i--)
			builder.append("  "); //$NON-NLS-1$
		return builder.toString();
	}

	/**
	 * Returns a String representation which includes the ancestors of this model element.<br>
	 * For debug purposes.
	 * 
	 * @return A string representation including the ancestors
	 */
	public String toStringIncludingAncestors()
	{
		return toStringIncludingAncestors(true);
	}

	/**
	 * Returns a String representation which includes the ancestors of this model element.<br>
	 * For debug purposes.
	 * 
	 * @param includeResolvedInfo
	 * @return A string representation including the ancestors
	 */
	public String toStringIncludingAncestors(boolean includeResolvedInfo)
	{
		StringBuilder builder = new StringBuilder();
		fillStringInfo(0, builder, includeResolvedInfo);
		fillStringAncestors(builder);
		return builder.toString();
	}

	/**
	 * Fill the StringBuilder with the ancestors strings.
	 * 
	 * @param builder
	 *            A {@link StringBuilder} to fill.
	 */
	protected void fillStringAncestors(StringBuilder builder)
	{
		AbstractModelElement parentElement = (AbstractModelElement) this.getParent();
		if (parentElement != null && parentElement.getParent() != null)
		{
			builder.append(" [ in "); //$NON-NLS-1$
			parentElement.fillStringInfo(0, builder, false);
			parentElement.fillStringAncestors(builder);
			builder.append(" ]"); //$NON-NLS-1$
		}
	}
}
