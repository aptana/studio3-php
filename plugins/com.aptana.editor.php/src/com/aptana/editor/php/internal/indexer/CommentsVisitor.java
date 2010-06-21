package com.aptana.editor.php.internal.indexer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.php.internal.core.ast.nodes.Comment;
import org.eclipse.php.internal.core.ast.visitor.AbstractVisitor;

/**
 * PHP Module comments collector.
 * 
 * @author Denis Denisenko
 */
public class CommentsVisitor extends AbstractVisitor
{
	/**
	 * Collected comments.
	 */
	private List<Comment> comments = new ArrayList<Comment>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(Comment comment)
	{
		comments.add(comment);
		return false;
	}

	/**
	 * Gets collected comments.
	 * 
	 * @return collected comments
	 */
	public List<Comment> getComments()
	{
		return comments;
	}
}