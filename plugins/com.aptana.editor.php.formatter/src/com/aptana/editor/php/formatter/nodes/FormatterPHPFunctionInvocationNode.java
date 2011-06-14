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
 * A function invocation formatter node.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPFunctionInvocationNode extends FormatterBlockWithBeginNode
{

	private final ASTNode invocationNode;

	/**
	 * @param document
	 * @param invocationNode
	 * @param hasSemicolon
	 */
	public FormatterPHPFunctionInvocationNode(IFormatterDocument document, ASTNode invocationNode)
	{
		super(document);
		this.invocationNode = invocationNode;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#shouldConsumePreviousWhiteSpaces()
	 */
	@Override
	public boolean shouldConsumePreviousWhiteSpaces()
	{
		switch (invocationNode.getParent().getType())
		{
			case ASTNode.STATIC_METHOD_INVOCATION:
			case ASTNode.METHOD_INVOCATION:
			case ASTNode.ASSIGNMENT:
			case ASTNode.INFIX_EXPRESSION:
			case ASTNode.POSTFIX_EXPRESSION:
			case ASTNode.PREFIX_EXPRESSION:
			case ASTNode.CONDITIONAL_EXPRESSION:
			case ASTNode.ARRAY_ACCESS:
			case ASTNode.LIST_VARIABLE:
			case ASTNode.REFERENCE:
			case ASTNode.FOR_STATEMENT:
			case ASTNode.WHILE_STATEMENT:
			case ASTNode.FOR_EACH_STATEMENT:
			case ASTNode.ARRAY_ELEMENT:
			case ASTNode.CLASS_INSTANCE_CREATION:
			case ASTNode.FUNCTION_INVOCATION:
			case ASTNode.IF_STATEMENT:
			case ASTNode.UNARY_OPERATION:
			case ASTNode.RETURN_STATEMENT:
				return true;
			default:
				return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#getSpacesCountBefore()
	 */
	@Override
	public int getSpacesCountBefore()
	{
		switch (invocationNode.getParent().getType())
		{
			case ASTNode.STATIC_METHOD_INVOCATION:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_STATIC_INVOCATION_OPERATOR);
			case ASTNode.METHOD_INVOCATION:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_ARROW_OPERATOR);
			default:
				return super.getSpacesCountBefore();
		}
	}
}
