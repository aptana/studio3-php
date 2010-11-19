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

import org.eclipse.php.internal.core.ast.nodes.ASTNode;

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
		// To change this behavior, it's recommended to create a designated subclass and override this method to return
		// the value set in the preferences.
		// if (node instanceof JSBinaryOperatorNode)
		// {
		// return false;
		// }
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
	public static boolean isPartOfExpression(ASTNode node)
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
			case ASTNode.METHOD_DECLARATION:
				return true;
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
