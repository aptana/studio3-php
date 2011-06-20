/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

import org2.eclipse.php.internal.core.ast.nodes.ASTNode;

import com.aptana.editor.php.formatter.PHPFormatterConstants;
import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.nodes.FormatterBlockWithBeginNode;

/**
 * A PHP declaration formatter node.<br>
 * This node represents a declaration part of a PHP block. It can be a function declaration, an if statement part, a
 * while statement declaration etc. Everything up to the open bracket (if exists) will be in this 'declaration'.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPDeclarationNode extends FormatterBlockWithBeginNode
{

	protected boolean hasBlockedChild;
	protected ASTNode node;

	/**
	 * @param document
	 * @param hasBlockedChild
	 * @param noNewLine
	 *            Provide a hint flag to block any new line added before this node. Note that this is just a hint which
	 *            can be overwritten by a preference setting.
	 * @param node
	 */
	public FormatterPHPDeclarationNode(IFormatterDocument document, boolean hasBlockedChild, ASTNode node)
	{
		super(document);
		this.hasBlockedChild = hasBlockedChild;
		this.node = node;
	}

	/**
	 * For a declaration, when this call returns true, a new line is added <b>before</b> the declaration.
	 * 
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isAddingBeginNewLine()
	 */
	@Override
	protected boolean isAddingBeginNewLine()
	{
		if (isPartOfExpression(node))
		{
			return false;
		}
		switch (node.getType())
		{
			case ASTNode.CATCH_CLAUSE:
				return getDocument().getBoolean(PHPFormatterConstants.NEW_LINES_BEFORE_CATCH_STATEMENT);
			case ASTNode.LAMBDA_FUNCTION_DECLARATION:
				return false;
			case ASTNode.FUNCTION_DECLARATION:
			case ASTNode.ARRAY_CREATION:
				if (isPartOfExpression(node.getParent()))
				{
					return false;
				}
		}
		return true;
	}

	/**
	 * Returns true id the given node has a type that is part of an expression. This will help us avoid breaking the
	 * line that it is located at and keep the 'declaration' in original expression code.
	 * 
	 * @param node
	 * @return
	 */
	public boolean isPartOfExpression(ASTNode node)
	{
		if (node == null)
		{
			return false;
		}
		switch (node.getType())
		{
			case ASTNode.CLASS_NAME:
			case ASTNode.ASSIGNMENT:
			case ASTNode.RETURN_STATEMENT:
			case ASTNode.GOTO_STATEMENT:
			case ASTNode.CONDITIONAL_EXPRESSION:
			case ASTNode.SINGLE_FIELD_DECLARATION:
			case ASTNode.ARRAY_CREATION:
			case ASTNode.FUNCTION_INVOCATION:
				return true;
			case ASTNode.METHOD_DECLARATION:
				return node.getStart() != this.node.getStart();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#getSpacesCountBefore()
	 */
	@Override
	public int getSpacesCountBefore()
	{
		if (isPartOfExpression(node.getParent()))
		{
			return 0;
		}
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#shouldConsumePreviousWhiteSpaces()
	 */
	@Override
	public boolean shouldConsumePreviousWhiteSpaces()
	{
		return !isAddingBeginNewLine();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isIndenting()
	 */
	@Override
	protected boolean isIndenting()
	{
		return !hasBlockedChild;
	}
}
