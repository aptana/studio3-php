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
import com.aptana.formatter.nodes.NodeTypes.TypePunctuation;
import com.aptana.formatter.ui.CodeFormatterConstants;

/**
 * A PHP case-colon formatter node.<br>
 * This formatter node handled the colon spacing when appears in a switch-case statements.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPCaseColonNode extends FormatterPHPPunctuationNode
{

	private final boolean caseInBlock;

	/**
	 * Constructs a new FormatterPHPCommaNode.
	 * 
	 * @param document
	 * @param caseInBlock
	 *            Indicate that the case/default content that comes after this colon is inside a curly block.
	 */
	public FormatterPHPCaseColonNode(IFormatterDocument document, boolean caseInBlock)
	{
		super(document, TypePunctuation.CASE_COLON);
		this.caseInBlock = caseInBlock;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isAddingEndNewLine()
	 */
	protected boolean isAddingEndNewLine()
	{
		return !caseInBlock
				|| CodeFormatterConstants.NEW_LINE.equals(getDocument().getString(
						PHPFormatterConstants.BRACE_POSITION_BLOCK_IN_CASE));
	}
}
