/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
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
	 * A constructor that allows passing a node-type. Note that this constructor should be used by subclasses only when
	 * needed.
	 * 
	 * @param nodeType
	 * @param modifiers
	 * @param startOffset
	 * @param endOffset
	 * @param className
	 */
	protected PHPClassParseNode(short nodeType, int modifiers, int startOffset, int endOffset, String className)
	{
		super(nodeType, modifiers, startOffset, endOffset, className);
	}

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
