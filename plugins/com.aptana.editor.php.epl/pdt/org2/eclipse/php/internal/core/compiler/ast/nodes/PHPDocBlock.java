/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org2.eclipse.php.internal.core.compiler.ast.nodes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org2.eclipse.php.internal.core.ast.nodes.AST;
import org2.eclipse.php.internal.core.ast.nodes.Comment;
import org2.eclipse.php.internal.core.documentModel.phpElementData.IPHPDoc;
import org2.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocTag;

public class PHPDocBlock extends Comment implements IPHPDoc
{
	private String shortDescription;
	private String longDescription;
	private IPHPDocTag[] tags;

	public PHPDocBlock(int start, int end, AST ast, String shortDescription, String longDescription, IPHPDocTag[] tags)
			throws IOException
	{
		super(start, end, ast, Comment.TYPE_PHPDOC);
		this.shortDescription = shortDescription;
		this.longDescription = longDescription;
		this.tags = tags;
	}

	public int getKind()
	{
		return ASTNodeKinds.PHP_DOC_BLOCK;
	}

	public String getShortDescription()
	{
		return shortDescription;
	}

	public String getLongDescription()
	{
		return longDescription;
	}

	public IPHPDocTag[] getTags()
	{
		return tags;
	}

	public PHPDocTag[] getTags(int kind)
	{
		List<IPHPDocTag> res = new LinkedList<IPHPDocTag>();
		if (tags != null)
		{
			for (IPHPDocTag tag : tags)
			{
				if (tag.getTagKind() == kind)
				{
					res.add(tag);
				}
			}
		}
		return res.toArray(new PHPDocTag[res.size()]);
	}
}
