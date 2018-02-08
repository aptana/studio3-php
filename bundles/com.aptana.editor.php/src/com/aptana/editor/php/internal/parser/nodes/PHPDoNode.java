/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

/**
 * PHP 'do' parse node.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class PHPDoNode extends PHPNonOutlineParseNode
{
	/**
	 * Constructs a new PHPDoNode.
	 * 
	 * @param start
	 * @param end
	 */
	public PHPDoNode(int start, int end)
	{
		super(IPHPParseNode.DO_NODE, 0, start, end, "do"); //$NON-NLS-1$
	}
}
