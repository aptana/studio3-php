/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
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
	 * @param modifiers
	 * @param startOffset
	 * @param endOffset
	 * @param className
	 * @param isField
	 */
	public PHPVariableParseNode(int modifiers, int startOffset, int endOffset, String className, boolean isField)
	{
		this(modifiers, startOffset, endOffset, className);
		setField(isField);
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
