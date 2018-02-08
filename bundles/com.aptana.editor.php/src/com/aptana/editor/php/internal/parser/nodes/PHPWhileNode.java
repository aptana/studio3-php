/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

/**
 * PHP 'while' parse node.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class PHPWhileNode extends PHPNonOutlineParseNode
{
	/**
	 * Constructs a new PHPWhileNode.
	 * 
	 * @param start
	 * @param end
	 */
	public PHPWhileNode(int start, int end)
	{
		super(IPHPParseNode.WHILE_NODE, 0, start, end, "while"); //$NON-NLS-1$
	}
}
