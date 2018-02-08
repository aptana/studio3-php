/**
 * Aptana Studio
 * Copyright (c) 2005-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter;

import java.util.List;

import org2.eclipse.php.internal.core.ast.nodes.Comment;
import org2.eclipse.php.internal.core.ast.nodes.Program;

import com.aptana.editor.php.formatter.nodes.FormatterPHPRootNode;
import com.aptana.editor.php.internal.indexer.PHPDocUtils;
import com.aptana.editor.php.internal.parser.nodes.PHPASTWrappingNode;
import com.aptana.formatter.FormatterDocument;
import com.aptana.formatter.nodes.AbstractFormatterNodeBuilder;
import com.aptana.formatter.nodes.IFormatterContainerNode;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.ParseRootNode;

/**
 * PHP formatter node builder.<br>
 * This builder generates the formatter nodes that will then be processed by the {@link PHPFormatterNodeRewriter} to
 * produce the output for the code formatting process.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPFormatterNodeBuilder extends AbstractFormatterNodeBuilder
{

	private boolean hasErrors;

	/**
	 * @param parseResult
	 * @param document
	 * @return
	 */
	public IFormatterContainerNode build(IParseNode parseResult, FormatterDocument document)
	{
		final IFormatterContainerNode rootNode = new FormatterPHPRootNode(document);
		start(rootNode);
		ParseRootNode phpRootNode = (ParseRootNode) parseResult;
		// the root node should hold a single Program (AST) that was inserted as a ParseNode.
		if (phpRootNode.getChildCount() == 1)
		{
			IParseNode child = phpRootNode.getChild(0);
			if (child instanceof PHPASTWrappingNode)
			{
				Program ast = ((PHPASTWrappingNode) child).getAST();
				PHPFormatterVisitor visitor = new PHPFormatterVisitor(document, this, ast.comments());
				ast.accept(visitor);
				setOffOnRegions(visitor.getOnOffRegions());
			}
		}
		checkedPop(rootNode, document.getLength());
		return rootNode;
	}

	/**
	 * @return True, in case the AST contains errors; False, otherwise.
	 * @see #setHasErrors(boolean)
	 */
	public boolean hasErrors()
	{
		return hasErrors;
	}

	/**
	 * Set the error-state of the AST.
	 * 
	 * @param hasErrors
	 * @see #hasErrors
	 */
	public void setHasErrors(boolean hasErrors)
	{
		this.hasErrors = hasErrors;
	}

	/**
	 * Try to locate the given char by traversing backwards on the given document from the start offset.<br>
	 * In case no match is located, the original start offset is returned. This function skips any char that is detected
	 * inside a comment (any kind of PHP comment).
	 * 
	 * @param document
	 * @param c
	 * @param start
	 * @param comments
	 *            A comments list to skip when searching for the char.
	 * @return The char offset, and if not found - the original start offset.
	 */
	public static int locateCharBackward(FormatterDocument document, char c, int start, List<Comment> comments)
	{
		int startOffset = start;
		int commentIndex = PHPDocUtils.findComment(comments, startOffset);
		Comment nextComment = null;
		int nextCommentIndex = -1;
		if (commentIndex >= 0)
		{
			Comment commentAtOffset = comments.get(commentIndex);
			// Adjust the start offset to the comment's start (we are searching backwards).
			startOffset = commentAtOffset.getStart() - 1;
			if (commentIndex > 0)
			{
				nextCommentIndex = commentIndex - 1;
				nextComment = comments.get(nextCommentIndex);
			}
		}
		else
		{
			// We got a negative position. The value represents a (-(insertion point) -1) in the comments list, so in
			// this case of searching backwards we would like to get the positive value of that result, minus 2.
			nextCommentIndex = -commentIndex - 2;
			if (nextCommentIndex >= 0 && nextCommentIndex < comments.size())
			{
				nextComment = comments.get(nextCommentIndex);
			}
		}

		for (int offset = startOffset; offset >= 0; offset--)
		{
			if (nextComment != null && offset >= nextComment.getStart() && offset < nextComment.getEnd())
			{
				// The offset is inside a comment, so we need to adjust it again to skip the characters in the comment.
				offset = nextComment.getStart();
				nextCommentIndex--;
				if (nextCommentIndex >= 0)
				{
					nextComment = comments.get(nextCommentIndex);
				}
				else
				{
					nextComment = null;
				}
			}
			if (document.charAt(offset) == c)
			{

				return offset;
			}
		}
		// We did not locate the character, so we return the original start offset.
		return start;
	}

	/**
	 * Try to locate the given char by traversing forward on the given document from the start offset.<br>
	 * In case no match is located, the original start offset is returned. This function skips any char that is detected
	 * inside a comment (any kind of PHP comment).
	 * 
	 * @param document
	 * @param c
	 * @param start
	 * @param comments
	 *            A comments list to skip when searching for the char.
	 * @return The char offset, and if not found - the original start offset.
	 */
	public static int locateCharForward(FormatterDocument document, char c, int start, List<Comment> comments)
	{
		int startOffset = start;
		int commentIndex = PHPDocUtils.findComment(comments, startOffset);
		Comment nextComment = null;
		int nextCommentIndex = -1;
		if (commentIndex >= 0)
		{
			Comment commentAtOffset = comments.get(commentIndex);
			// Adjust the start offset to the comment's start (we are searching backwards).
			startOffset = commentAtOffset.getEnd() + 1;
			nextCommentIndex = commentIndex + 1;
			if (nextCommentIndex < comments.size())
			{
				nextComment = comments.get(nextCommentIndex);
			}
		}
		else
		{
			// We got a negative position, which means it's the offset of the nearest comment that has a bigger start
			// offset than the offset we searched for. In that case, we set the next comment to be the one after, if
			// possible.
			nextCommentIndex = -commentIndex;
			if (nextCommentIndex < comments.size())
			{
				nextComment = comments.get(nextCommentIndex);
			}
		}
		int length = document.getLength();
		for (int offset = startOffset; offset < length; offset++)
		{
			if (nextComment != null && offset >= nextComment.getStart() && offset < nextComment.getEnd())
			{
				// The offset is inside a comment, so we need to adjust it again to skip the characters in the comment.
				offset = nextComment.getEnd() + 1;
				nextCommentIndex++;
				if (nextCommentIndex < comments.size())
				{
					nextComment = comments.get(nextCommentIndex);
				}
				else
				{
					nextComment = null;
				}
			}
			if (offset >= length)
			{
				break;
			}
			if (document.charAt(offset) == c)
			{

				return offset;
			}
		}
		// We did not locate the character, so we return the original start offset.
		return start;
	}
}
