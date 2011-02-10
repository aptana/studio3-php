/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

/**
 * Represents PHP Constant
 * 
 * @author Pavel Petrochenko
 */
public class PHPConstantNode extends PHPVariableParseNode
{

	/**
	 * @param startPosition
	 * @param endPosition
	 * @param name
	 */
	public PHPConstantNode(int startPosition, int endPosition, String name)
	{
		super(0, startPosition, endPosition, name);
	}

}
