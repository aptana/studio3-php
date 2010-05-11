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
