/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

/**
 * PHP parse node for HTML content within PHP source files.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class PHPHTMLNode extends PHPNonOutlineParseNode
{
	/**
	 * @param startOffset
	 * @param endOffset
	 */
	public PHPHTMLNode(int startOffset, int endOffset)
	{
		super(PHPBaseParseNode.HTML_NODE, 0, startOffset, endOffset, "html"); //$NON-NLS-1$
	}

}
