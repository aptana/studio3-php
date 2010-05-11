/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

import java.util.List;

/**
 * Represents PHP Class
 * 
 * @author Pavel Petrochenko
 */
public class PHPClassParseNode extends PHPBaseParseNode
{
	/**
	 * Name of the superclass.
	 */
	private String superClassName;

	/**
	 * Interface names.
	 */
	private List<String> interfaces;

	/**
	 * @param modifiers
	 * @param startOffset
	 * @param endOffset
	 * @param className
	 */
	public PHPClassParseNode(int modifiers, int startOffset, int endOffset, String className)
	{
		super(PHPBaseParseNode.CLASS_NODE, modifiers, startOffset, endOffset, className);
		// super.setNodeName("class"); //$NON-NLS-1$
	}

	/**
	 * Sets interface names.
	 * 
	 * @param interfaces
	 *            - interfaces.
	 */
	public void setInterfaces(List<String> interfaces)
	{
		this.interfaces = interfaces;
	}

	/**
	 * Gets superclass name.
	 * 
	 * @return superclass name.
	 */
	public String getSuperClassname()
	{
		return superClassName;
	}

	/**
	 * Sets superclass name.
	 * 
	 * @param name
	 *            - superclass name.
	 */
	public void setSuperClassName(String superClassname)
	{
		this.superClassName = superClassname;
	}

	/**
	 * Gets interfaces.
	 * 
	 * @return interfaces. might be null.
	 */
	public List<String> getInterfaces()
	{
		return interfaces;
	}

}
