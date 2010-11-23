/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

import com.aptana.editor.php.formatter.PHPFormatterConstants;
import com.aptana.editor.php.formatter.nodes.NodeTypes.TypeOperator;
import com.aptana.formatter.IFormatterDocument;

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
			case TYPE_EQUAL:
			case NOT_EQUAL:
			case NOT_TYPE_EQUAL:
			case GREATER_THAN:
			case LESS_THAN:
			case GREATER_THAN_OR_EQUAL:
			case LESS_THAN_OR_EQUAL:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_RELATIONAL_OPERATORS);
			case DOT:
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
			case TYPE_EQUAL:
			case NOT_EQUAL:
			case NOT_TYPE_EQUAL:
			case GREATER_THAN:
			case LESS_THAN:
			case GREATER_THAN_OR_EQUAL:
			case LESS_THAN_OR_EQUAL:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_RELATIONAL_OPERATORS);
			case DOT:
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
			case TILDE:
			case NOT:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_UNARY_OPERATOR);
			default:
				return super.getSpacesCountBefore();
		}
	}
}
