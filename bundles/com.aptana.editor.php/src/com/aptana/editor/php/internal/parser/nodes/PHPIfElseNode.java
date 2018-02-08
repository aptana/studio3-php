/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

/**
 * PHP 'if'/'else' parse node.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class PHPIfElseNode extends PHPNonOutlineParseNode
{
	/**
	 * Constructs an 'if' / 'else' parse node.
	 * 
	 * @param start
	 * @param end
	 * @param type
	 */
	public PHPIfElseNode(int start, int end, String type)
	{
		super(PHPBaseParseNode.IF_ELSE_NODE, 0, start, end, type);
	}

}
