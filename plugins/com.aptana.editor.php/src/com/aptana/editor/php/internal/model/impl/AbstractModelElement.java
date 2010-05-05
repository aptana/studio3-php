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

import com.aptana.editor.php.internal.model.ModelManager;
import com.aptana.editor.php.model.IModelElement;
import com.aptana.editor.php.model.IModelElementVisitor;
import com.aptana.editor.php.model.IParent;
import com.aptana.editor.php.model.ISourceModel;
import com.aptana.editor.php.model.ISourceProject;
import com.aptana.editor.php.model.env.ModelElementInfo;

/**
 * AbstractModelElement
 * @author Denis Denisenko
 */
public abstract class AbstractModelElement implements IModelElement
{

	/**
	 * {@inheritDoc}
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

	/**
	 * {@inheritDoc}
	 */
	public ISourceProject getSourceProject()
	{
		return (ISourceProject) getAncestor(IModelElement.PROJECT);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ISourceModel getModel()
	{
		return ModelManager.getInstance().getModel();
	}

	/**
	 * {@inheritDoc}
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
	
	/**
	 * Gets model element info.
	 * @return model element info.
	 */
	public ModelElementInfo getElementInfo()
	{
		//return null by default
		return null;
	}
	
	/**
	 * Gets element debug string.
	 * @return element debug string
	 */
	public String toDebugString() 
	{
		
		StringBuffer buffer = new StringBuffer();
		this.toStringInfo(0, buffer, true/* show resolved info */);
		return buffer.toString();
	}
	
	/**
	 * 
	 * @param tab
	 * @param buffer
	 * @param showResolvedInfo
	 */
	protected void toStringInfo(int tab, StringBuffer buffer, boolean showResolvedInfo) 
	{
		buffer.append(this.tabString(tab));
		toStringName(buffer);
	}
	/**
	 * Adds element name to a buffer.
	 * @param buffer - buffer.
	 */
	protected void toStringName(StringBuffer buffer) {
		buffer.append(getElementName());
	}
	
	/**
	 * Gets string consisting from tabs.
	 * @param tab - number of tabs.
	 * @return string
	 */
	protected String tabString(int tab) {
		StringBuffer buffer = new StringBuffer();
		for (int i = tab; i > 0; i--)
			buffer.append("  "); //$NON-NLS-1$
		return buffer.toString();
	}
	
	/**
	 * Debug.
	 */
	public String toStringWithAncestors() {
		return toStringWithAncestors(true/* show resolved info */);
	}

	/**
	 * Debug.
	 */
	public String toStringWithAncestors(boolean showResolvedInfo) {
		StringBuffer buffer = new StringBuffer();
		this.toStringInfo(0, buffer, showResolvedInfo);
		this.toStringAncestors(buffer);
		return buffer.toString();
	}
	
	protected void toStringAncestors(StringBuffer buffer) {
		AbstractModelElement parentElement = (AbstractModelElement) this.getParent();
		if (parentElement != null && parentElement.getParent() != null) {
			buffer.append(" [in "); //$NON-NLS-1$
			parentElement.toStringInfo(0, buffer, false); 
			parentElement.toStringAncestors(buffer);
			buffer.append("]"); //$NON-NLS-1$
		}
	}
}
