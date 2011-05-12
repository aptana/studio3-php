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
import com.aptana.formatter.ui.CodeFormatterConstants;

/**
 * Formatter node for 'case' that contains a block (in curly braces).
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPCaseBodyNode extends FormatterPHPBlockNode
{

	private boolean hasBlockedChild;

	/**
	 * @param document
	 * @param hasBlockedChild
	 * @param hasCommentBefore
	 */
	public FormatterPHPCaseBodyNode(IFormatterDocument document, boolean hasBlockedChild, boolean hasCommentBefore)
	{
		super(document, hasCommentBefore);
		this.hasBlockedChild = hasBlockedChild;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.js.formatter.nodes.FormatterPHPBlockNode#isIndenting()
	 */
	@Override
	protected boolean isIndenting()
	{
		return getDocument().getBoolean(PHPFormatterConstants.INDENT_CASE_BODY);
	}

	@Override
	protected boolean isAddingBeginNewLine()
	{
		return isStandAloneBlock
				|| !hasBlockedChild
				|| CodeFormatterConstants.NEW_LINE.equals(getDocument().getString(
						PHPFormatterConstants.BRACE_POSITION_BLOCK_IN_CASE));
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.formatter.nodes.FormatterPHPBlockNode#shouldConsumePreviousWhiteSpaces()
	 */
	@Override
	public boolean shouldConsumePreviousWhiteSpaces()
	{
		return true;
	}

}
