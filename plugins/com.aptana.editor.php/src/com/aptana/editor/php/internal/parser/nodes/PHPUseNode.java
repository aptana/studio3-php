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
 * @author Pavel Petrochenko
 */
public class PHPUseNode extends PHPBaseParseNode
{

	/**
	 * @param startOffset
	 * @param endOffset
	 * @param name
	 * @param includeType
	 */
	public PHPUseNode(int startOffset, int endOffset, String name, String includeType)
	{
		super(PHPBaseParseNode.USE_NODE, 0, startOffset, endOffset, includeType + ' ' + name);
	}

}
