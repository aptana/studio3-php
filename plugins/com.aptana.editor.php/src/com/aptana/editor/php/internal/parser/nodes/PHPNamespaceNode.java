/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

/**
 * @author Pavel Petrochenko
 */
public class PHPNamespaceNode extends PHPBaseParseNode
{

	/**
	 * @param startOffset
	 * @param endOffset
	 * @param name
	 * @param includeType
	 */
	public PHPNamespaceNode(int startOffset, int endOffset, String name, String includeType)
	{
		super(PHPBaseParseNode.NAMESPACE_NODE, 0, startOffset, endOffset, includeType + ' ' + name);
	}

}
