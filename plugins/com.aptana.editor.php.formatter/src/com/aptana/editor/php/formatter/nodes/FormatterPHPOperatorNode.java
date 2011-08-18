/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

import com.aptana.editor.php.formatter.PHPFormatterConstants;
import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.nodes.NodeTypes.TypeOperator;

/**
 * A PHP formatter node for operator elements, such as assignments, arrows etc.<br>
 * An operator node is defined, by default, to consume all white spaces in front of it.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPOperatorNode extends FormatterPHPTextNode
{
	private final TypeOperator nodeType;
	private boolean isUnary;

	/**
	 * Constructs a new FormatterPHPOperatorNode.
	 * 
	 * @param document
	 */
	public FormatterPHPOperatorNode(IFormatterDocument document, TypeOperator nodeType, boolean isUnary)
	{
		super(document, true);
		this.nodeType = nodeType;
		this.isUnary = isUnary;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.formatter.nodes.FormatterPHPTextNode#getSpacesCountBefore()
	 */
	@Override
	public int getSpacesCountBefore()
	{
		if (isUnary)
		{
			return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_UNARY_OPERATOR);
		}
		switch (nodeType)
		{
			case ARROW:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_ARROW_OPERATOR);
			case STATIC_INVOCATION:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_STATIC_INVOCATION_OPERATOR);
			case KEY_VALUE:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_KEY_VALUE_OPERATOR);
			case ASSIGNMENT:
			case DOT_EQUAL:
			case PLUS_EQUAL:
			case MINUS_EQUAL:
			case MULTIPLY_EQUAL:
			case DIVIDE_EQUAL:
			case OR_EQUAL:
			case AND_EQUAL:
			case TILDE_EQUAL:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_ASSIGNMENT_OPERATOR);
			case EQUAL:
			case IDENTICAL:
			case NOT_EQUAL:
			case NOT_EQUAL_ALTERNATE:
			case NOT_IDENTICAL:
			case GREATER_THAN:
			case LESS_THAN:
			case GREATER_THAN_OR_EQUAL:
			case LESS_THAN_OR_EQUAL:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_RELATIONAL_OPERATORS);
			case DOT_CONCATENATION:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_CONCATENATION_OPERATOR);
			case CONDITIONAL:
			case CONDITIONAL_COLON:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_CONDITIONAL_OPERATOR);
			case POSTFIX_DECREMENT:
			case POSTFIX_INCREMENT:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_POSTFIX_OPERATOR);
			case PREFIX_DECREMENT:
			case PREFIX_INCREMENT:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_PREFIX_OPERATOR);
			case MULTIPLY:
			case PLUS:
			case MINUS:
			case DIVIDE:
			case MODULUS:
			case XOR:
			case BINARY_AND:
			case BINARY_OR:
			case AND:
			case OR:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_ARITHMETIC_OPERATOR);
			case OR_LITERAL:
			case AND_LITERAL:
			case XOR_LITERAL:
				// We need at least one space for the literal boolean operators
				return Math.max(1, getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_ARITHMETIC_OPERATOR));
			case TILDE:
			case NOT:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_UNARY_OPERATOR);
			default:
				return super.getSpacesCountBefore();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#getSpacesCountAfter()
	 */
	@Override
	public int getSpacesCountAfter()
	{
		if (isUnary)
		{
			return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_UNARY_OPERATOR);
		}
		switch (nodeType)
		{
			case ARROW:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_ARROW_OPERATOR);
			case STATIC_INVOCATION:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_STATIC_INVOCATION_OPERATOR);
			case KEY_VALUE:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_KEY_VALUE_OPERATOR);
			case ASSIGNMENT:
			case DOT_EQUAL:
			case PLUS_EQUAL:
			case MINUS_EQUAL:
			case MULTIPLY_EQUAL:
			case DIVIDE_EQUAL:
			case OR_EQUAL:
			case AND_EQUAL:
			case TILDE_EQUAL:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_ASSIGNMENT_OPERATOR);
			case EQUAL:
			case IDENTICAL:
			case NOT_EQUAL:
			case NOT_EQUAL_ALTERNATE:
			case NOT_IDENTICAL:
			case GREATER_THAN:
			case LESS_THAN:
			case GREATER_THAN_OR_EQUAL:
			case LESS_THAN_OR_EQUAL:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_RELATIONAL_OPERATORS);
			case DOT_CONCATENATION:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_CONCATENATION_OPERATOR);
			case CONDITIONAL:
			case CONDITIONAL_COLON:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_CONDITIONAL_OPERATOR);
			case POSTFIX_DECREMENT:
			case POSTFIX_INCREMENT:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_POSTFIX_OPERATOR);
			case PREFIX_DECREMENT:
			case PREFIX_INCREMENT:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_PREFIX_OPERATOR);
			case MULTIPLY:
			case PLUS:
			case MINUS:
			case DIVIDE:
			case MODULUS:
			case XOR:
			case BINARY_AND:
			case BINARY_OR:
			case AND:
			case OR:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_ARITHMETIC_OPERATOR);
			case OR_LITERAL:
			case AND_LITERAL:
			case XOR_LITERAL:
				// We need at least one space for the literal boolean operators
				return Math.max(1, getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_ARITHMETIC_OPERATOR));
			case TILDE:
			case NOT:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_UNARY_OPERATOR);
			default:
				return super.getSpacesCountBefore();
		}
	}
}
