/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

/**
 * A Trait parse node.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class PHPTraitParseNode extends PHPClassParseNode
{

	/**
	 * Constructs a new PHP Trait parse node.
	 * 
	 * @param modifiers
	 * @param startOffset
	 * @param endOffset
	 * @param className
	 */
	public PHPTraitParseNode(int modifiers, int startOffset, int endOffset, String className)
	{
		super(PHPBaseParseNode.TRAIT_NODE, modifiers, startOffset, endOffset, className);
	}
}
