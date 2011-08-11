/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

/**
 * A PHP parse node that should not appear in the outline.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public abstract class PHPNonOutlineParseNode extends PHPBaseParseNode
{
	/**
	 * Constructor.
	 * 
	 * @param nodeType
	 * @param modifiers
	 * @param startOffset
	 * @param endOffset
	 * @param name
	 */
	protected PHPNonOutlineParseNode(short nodeType, int modifiers, int startOffset, int endOffset, String name)
	{
		super(nodeType, modifiers, startOffset, endOffset, name);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.parsing.ast.ParseNode#isFilteredFromOutline()
	 */
	@Override
	public boolean isFilteredFromOutline()
	{
		return true;
	}
}
