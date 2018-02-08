/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

/**
 * Represents PHP Block
 * 
 * @author Pavel Petrochenko
 */
public class PHPBlockNode extends PHPNonOutlineParseNode
{

	/**
	 * Constructs new PHP Block Node
	 * 
	 * @param startOffset
	 * @param endOffset
	 * @param name
	 */
	public PHPBlockNode(int startOffset, int endOffset, String name)
	{
		super(PHPBaseParseNode.BLOCK_NODE, 0, startOffset, endOffset, name);
		setNodeName("<?php"); //$NON-NLS-1$
	}

}
