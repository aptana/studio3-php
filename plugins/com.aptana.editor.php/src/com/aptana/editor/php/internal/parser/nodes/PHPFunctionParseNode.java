/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents PHP Function
 * 
 * @author Pavel Petrochenko
 */
public class PHPFunctionParseNode extends PHPBaseParseNode
{

	boolean isMethod;
	private List<Parameter> parameters;

	/**
	 * @param modifiers
	 * @param startOffset
	 * @param endOffset
	 * @param className
	 */
	public PHPFunctionParseNode(int modifiers, int startOffset, int endOffset, String className)
	{
		super(PHPBaseParseNode.FUNCTION_NODE, modifiers, startOffset, endOffset, className);
		// super.setNodeName("function"); //$NON-NLS-1$
	}

	/**
	 * @return is this function class method or global function
	 */
	public boolean isMethod()
	{
		return isMethod;
	}

	/**
	 * @param isMethod
	 */
	public void setMethod(boolean isMethod)
	{
		this.isMethod = isMethod;
	}

	/**
	 * @param parameters
	 */
	public void setParameters(List<Parameter> parameters)
	{
		this.parameters = new ArrayList<Parameter>(parameters);
	}

	/**
	 * @return method signature
	 */
	public String getSignature()
	{
		StringBuffer bf = new StringBuffer();
		bf.append(getNodeName());
		bf.append('(');
		int size = parameters.size();

		for (int a = 0; a < size; a++)
		{
			Parameter p = parameters.get(a);
			p.addLabel(bf);
			if (a != size - 1)
			{
				bf.append(", "); //$NON-NLS-1$
			}
		}
		bf.append(')');
		return bf.toString();
	}

	/**
	 * @return method/function parameters
	 */
	public Parameter[] getParameters()
	{
		Parameter[] result = new Parameter[parameters.size()];
		parameters.toArray(result);
		return result;
	}

}
