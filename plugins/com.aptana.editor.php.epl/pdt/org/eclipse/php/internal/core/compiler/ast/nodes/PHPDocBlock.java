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
package org.eclipse.php.internal.core.compiler.ast.nodes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.php.internal.core.ast.nodes.AST;
import org.eclipse.php.internal.core.ast.nodes.Comment;
import org.eclipse.php.internal.core.documentModel.phpElementData.IPHPDoc;
import org.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocTag;

public class PHPDocBlock extends Comment implements IPHPDoc {

	private String shortDescription;
	private IPHPDocTag[] tags;

	public PHPDocBlock(int start, int end, AST ast, String shortDescription, IPHPDocTag[] tags) throws IOException {
		super(start, end, ast, Comment.TYPE_PHPDOC);
		this.shortDescription = shortDescription;
		this.tags = tags;
	}

//	public void traverse(ASTVisitor visitor) throws Exception {
//		boolean visit = visitor.visit(this);
//		if (visit) {
//			for (IPHPDocTag tag : tags) {
//				tag.traverse(visitor);
//			}
//		}
//		visitor.endvisit(this);
//	}

	public int getKind() {
		return ASTNodeKinds.PHP_DOC_BLOCK;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public IPHPDocTag[] getTags() {
		return tags;
	}
	
	public PHPDocTag[] getTags(int kind) {
		List<IPHPDocTag> res = new LinkedList<IPHPDocTag>();
		if (tags != null) {
			for (IPHPDocTag tag : tags) {
				if (tag.getTagKind() == kind) {
					res.add(tag);
				}
			}
		}
		return res.toArray(new PHPDocTag[res.size()]);
	}

//	public void adjustStart(int start){
//		setStart(sourceStart() + start);
//		setEnd(sourceEnd() + start);
//		
//		for (IPHPDocTag tag : tags) {
//			tag.adjustStart(start);
//		}
//	}
		
}
