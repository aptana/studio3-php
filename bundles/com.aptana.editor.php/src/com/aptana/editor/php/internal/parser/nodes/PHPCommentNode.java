/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

import org2.eclipse.php.internal.core.ast.nodes.Comment;

public class PHPCommentNode extends PHPBaseParseNode
{

	/**
	 * Type of comment, use values from Comment, i.e. Comment.TYPE_*
	 */
	private int commentType;

	/**
	 * @param c
	 */
	public PHPCommentNode(Comment c)
	{
		super(PHPBaseParseNode.COMMENT_NODE, 0, c.getStart(), c.getEnd(), ""); //$NON-NLS-1$
		this.commentType = c.getCommentType();
	}

	public boolean isPHPDoc()
	{
		return commentType == Comment.TYPE_PHPDOC;
	}

	public boolean isMultiline()
	{
		return commentType == Comment.TYPE_MULTILINE;
	}

}
