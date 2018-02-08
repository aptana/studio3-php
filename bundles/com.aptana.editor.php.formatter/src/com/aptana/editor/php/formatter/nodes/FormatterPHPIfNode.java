/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

import org2.eclipse.php.internal.core.ast.nodes.ASTNode;
import org2.eclipse.php.internal.core.ast.nodes.IfStatement;

import com.aptana.editor.php.formatter.PHPFormatterConstants;
import com.aptana.formatter.IFormatterDocument;

/**
 * PHP if-statement formatter node
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPIfNode extends FormatterPHPDeclarationNode
{

	private boolean inElseIf;

	/**
	 * @param document
	 * @param hasBlockedChild
	 * @param node
	 */
	public FormatterPHPIfNode(IFormatterDocument document, boolean hasBlockedChild, ASTNode node)
	{
		super(document, hasBlockedChild, node);
		// Check if this node is located in the 'false' block of a parent 'if'. In that case, we can say for sure that
		// this 'if' arrives right after an 'else'.
		if (node.getParent().getType() == ASTNode.IF_STATEMENT)
		{
			IfStatement parentIfNode = (IfStatement) node.getParent();
			inElseIf = parentIfNode.getFalseStatement() == node;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.js.formatter.nodes.FormatterPHPDeclarationNode#isIndenting()
	 */
	@Override
	protected boolean isIndenting()
	{
		return !hasBlockedChild;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#shouldConsumePreviousWhiteSpaces()
	 */
	@Override
	public boolean shouldConsumePreviousWhiteSpaces()
	{
		return inElseIf;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#getSpacesCountBefore()
	 */
	@Override
	public int getSpacesCountBefore()
	{
		if (shouldConsumePreviousWhiteSpaces())
		{
			return 1;
		}
		return super.getSpacesCountBefore();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.js.formatter.nodes.FormatterPHPDeclarationNode#isAddingBeginNewLine()
	 */
	@Override
	protected boolean isAddingBeginNewLine()
	{
		if (inElseIf)
		{
			// This node may be an "elseif" representation, so we check for the first char in the text.
			if (getDocument().charAt(getStartOffset()) == 'e')
			{
				// Check if we need to add a new line before an 'else' expression (we treat it as one in this case)
				return getDocument().getBoolean(PHPFormatterConstants.NEW_LINES_BEFORE_ELSE_STATEMENT);
			}
			return getDocument().getBoolean(PHPFormatterConstants.NEW_LINES_BEFORE_IF_IN_ELSEIF_STATEMENT);
		}
		return true;
	}
}
