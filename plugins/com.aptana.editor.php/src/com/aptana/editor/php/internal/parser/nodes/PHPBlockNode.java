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
 * Represents PHP Block
 * 
 * @author Pavel Petrochenko
 */
public class PHPBlockNode extends PHPBaseParseNode
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
