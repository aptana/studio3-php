package com.aptana.editor.php.internal.indexer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.php.internal.core.ast.nodes.Comment;
import com.aptana.editor.php.utils.PHPASTVisitorStub;

/**
 * PHP Module comments collector.
 * @author Denis Denisenko
 *
 */
public class CommentsVisitor extends PHPASTVisitorStub
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
	 * @return collected comments
	 */
	public List<Comment> getComments()
	{
		return comments;
	}
}