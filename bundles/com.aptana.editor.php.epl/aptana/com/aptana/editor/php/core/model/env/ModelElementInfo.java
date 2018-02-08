/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package com.aptana.editor.php.core.model.env;

import java.util.ArrayList;
import java.util.List;

import com.aptana.editor.php.core.model.IModelElement;

/**
 * Holds cached structure and properties for a model element. Subclassed to
 * carry properties for specific kinds of elements.
 */
public class ModelElementInfo {

	/**
	 * Collection of handles of immediate children of this object. This is an
	 * empty array if this element has no children.
	 */
	private List<IModelElement> children;

	/**
	 * ModelElementInfo protected constructor.
	 */
	public ModelElementInfo() 
	{
	}

	/**
	 * Adds child.
	 * @param child - child to add.
	 */
	public void addChild(IModelElement child) 
	{
		if (this.children == null) 
		{
			this.children = new ArrayList<IModelElement>(5);
		}
		if (!this.children.contains(child)) 
		{
			this.children.add(child);
		}
	}

	/**
	 * Gets number of children.
	 * @return number of children.
	 */
	public int size() 
	{
		if (this.children == null)
		{
			return 0;
		}
		return this.children.size();
	}

	/**
	 * Gets child.
	 * @param i - child index.
	 * @return child or null.
	 */
	protected IModelElement get(int i) 
	{
		if (this.children == null)
		{
			return null;
		}
		return (IModelElement) children.get(i);
	}

	/**
	 * Gets children.
	 * @return children.
	 */
	public IModelElement[] getChildren() 
	{
		if (children == null)
		{
			return new IModelElement[0];
		}
		return (IModelElement[]) this.children
				.toArray(new IModelElement[this.children.size()]);
	}

	/**
	 * Removes child.
	 * @param child - child to remove.
	 */
	public void removeChild(IModelElement child) 
	{
		if (this.children != null) 
		{
			this.children.remove(child);
		}
	}

	/**
	 * Sets children.
	 * @param children - children to set.
	 */
	public void setChildren(IModelElement[] children) 
	{
		if (children == null) 
		{
			this.children = null;
		} 
		else 
		{
			this.children = new ArrayList<IModelElement>(children.length);
			for (int i = 0; i < children.length; i++) {
				this.children.add(children[i]);
			}
		}
	}
	
	public void setChildren(List<IModelElement> children)
	{
		if (children == null) 
		{
			this.children = null;
		} 
		else 
		{
			this.children = new ArrayList<IModelElement>(children.size());
			this.children.addAll(children);
		}
	}

}
