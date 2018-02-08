/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org2.eclipse.php.internal.core.ast.locator.Locator;
import org2.eclipse.php.internal.core.ast.nodes.ASTNode;
import org2.eclipse.php.internal.core.ast.nodes.Comment;
import org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor;
import org2.eclipse.php.internal.core.compiler.ast.nodes.VarComment;

/**
 * PHP AST comments collector.<br>
 * This collector is enhanced with an ability to map the ASTNodes parents for any special @var comments that exist in
 * the code (e.g. <code><b>@var $variable ClassType</b></code>)
 * 
 * @author Shalom Gibly
 */
public class CommentsVisitor extends AbstractVisitor
{
	/**
	 * Collected comments.
	 */
	private List<Comment> comments;
	private Map<ASTNode, List<VarComment>> resolvedVarComments;
	private boolean isResolvingVarComments;

	/**
	 * Constructs a new AST CommentsVisitor.<br>
	 * By default, no {@link VarComment} parent-resolving is performed.
	 */
	public CommentsVisitor()
	{
		this(false);
	}

	/**
	 * Constructs a new AST CommentsVisitor.
	 * 
	 * @param resolveVarComments
	 *            Indicate if the visitor should resolve the parents of any detected {@link VarComment}.
	 * @see #getResolvedVarComments()
	 */
	public CommentsVisitor(boolean resolveVarComments)
	{
		isResolvingVarComments = resolveVarComments;
		comments = new ArrayList<Comment>();
		resolvedVarComments = new HashMap<ASTNode, List<VarComment>>();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.Comment
	 * )
	 */
	@Override
	public boolean visit(Comment comment)
	{
		comments.add(comment);
		if (isResolvingVarComments && comment instanceof VarComment)
		{
			// We set up a mapping from the parent node to all his child VarComments.
			// That way we can later locate them while creating the scopes. Note that comments are not inserted into
			// the AST, they are basically provided as a list.
			ASTNode parent = Locator.locateNode(comment.getProgramRoot(), Math.max(0, comment.getStart() - 1));
			if (parent != null)
			{
				if (parent.getType() == ASTNode.BLOCK)
				{
					parent = parent.getParent();
				}
				List<VarComment> varComments = resolvedVarComments.get(parent);
				if (varComments == null)
				{
					varComments = new ArrayList<VarComment>(5);
					resolvedVarComments.put(parent, varComments);
				}
				varComments.add((VarComment) comment);
			}
		}
		return false;
	}

	/**
	 * Returns the AST's comments.
	 * 
	 * @return The comments
	 */
	public List<Comment> getComments()
	{
		return comments;
	}

	/**
	 * Returns a mapping from an ASTNode to its child-VarComments.
	 * 
	 * @return A mapping from nodes to VarComments. Empty mapping in case no VarComments detected in the AST, or when
	 *         the <code>resolveVarComments</code> that was passed when constructing this CommentsVisitor was
	 *         <code>false</code>.
	 */
	public Map<ASTNode, List<VarComment>> getResolvedVarComments()
	{
		return resolvedVarComments;
	}
}