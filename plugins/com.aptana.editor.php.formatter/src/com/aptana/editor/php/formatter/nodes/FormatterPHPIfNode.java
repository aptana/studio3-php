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
import org.eclipse.php.internal.core.ast.nodes.IfStatement;

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
			return getDocument().getBoolean(PHPFormatterConstants.NEW_LINES_BEFORE_IF_IN_ELSEIF_STATEMENT);
		}
		return true;
	}
}
