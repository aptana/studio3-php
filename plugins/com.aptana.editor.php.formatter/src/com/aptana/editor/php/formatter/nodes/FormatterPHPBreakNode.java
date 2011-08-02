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
import com.aptana.formatter.IFormatterContext;
import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.IFormatterWriter;
import com.aptana.formatter.nodes.FormatterBlockWithBeginNode;

/**
 * A PHP 'break' formatter node.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPBreakNode extends FormatterBlockWithBeginNode
{

	private ASTNode parentNode;

	/**
	 * Constructs a new FormatterPHPBreakNode.<br>
	 * A parent ASTNode should be provided to compute the formatting according to the preference that matches it. For
	 * example, a 'break' in a switch-case might behave differently then a 'break' in a 'for' statement.
	 * 
	 * @param document
	 * @param parentNode
	 *            The parent ASTNode of the 'break' statement.
	 */
	public FormatterPHPBreakNode(IFormatterDocument document, ASTNode parentNode)
	{
		super(document);
		this.parentNode = parentNode;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockWithBeginNode#accept(com.aptana.formatter.IFormatterContext,
	 * com.aptana.formatter.IFormatterWriter)
	 */
	@Override
	public void accept(IFormatterContext context, IFormatterWriter visitor) throws Exception
	{
		// We override the accept of the break to control the indentation of it.
		// We might want to de-dent it in some cases, such as a 'break' in a 'switch-case' block.
		boolean isDeDenting = false;
		if (parentNode.getType() == ASTNode.SWITCH_CASE)
		{
			if (!getDocument().getBoolean(PHPFormatterConstants.INDENT_BREAK_IN_CASE) && getDocument().getBoolean(PHPFormatterConstants.INDENT_CASE_BODY))
			{
				context.decIndent();
				isDeDenting = true;
			}
		}
		super.accept(context, visitor);
		if (isDeDenting)
		{
			context.incIndent();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isAddingBeginNewLine()
	 */
	@Override
	protected boolean isAddingBeginNewLine()
	{
		return true;
	}
}
