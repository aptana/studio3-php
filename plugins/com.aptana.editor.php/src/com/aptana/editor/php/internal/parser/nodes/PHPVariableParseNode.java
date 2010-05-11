/**
 * Copyright (c) 2005-2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

/**
 * Represents PHP variable
 * 
 * @author Pavel Petrochenko
 */
public class PHPVariableParseNode extends PHPBaseParseNode
{

	private boolean field;
	private boolean parameter;
	private boolean localVariable;

	/**
	 * @param modifiers
	 * @param startOffset
	 * @param endOffset
	 * @param className
	 */
	public PHPVariableParseNode(int modifiers, int startOffset, int endOffset, String className)
	{
		super(PHPBaseParseNode.VAR_NODE, modifiers, startOffset, endOffset, className);
	}

	/**
	 * @return is class member or usual variable
	 */
	public boolean isField()
	{
		return field;
	}

	/**
	 * @return is it parameter node
	 */
	public boolean isParameter()
	{
		return parameter;
	}

	/**
	 * set it to parameter node
	 * 
	 * @param parameter
	 */
	public void setParameter(boolean parameter)
	{
		this.parameter = parameter;
	}

	/**
	 * @param field
	 */
	public void setField(boolean field)
	{
		this.field = field;
	}

	/**
	 * @return it it local variable
	 */
	public boolean isLocalVariable()
	{
		return localVariable;
	}

	/**
	 * @param localVariable
	 */
	public void setLocalVariable(boolean localVariable)
	{
		this.localVariable = localVariable;
	}

}
