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
import com.aptana.formatter.nodes.FormatterBlockWithBeginEndNode;
import com.aptana.formatter.ui.CodeFormatterConstants;

/**
 * A generic PHP block formatter node.<br>
 * This node represents a body part between open and close curly-brackets.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPBlockNode extends FormatterBlockWithBeginEndNode
{

	protected boolean isStandAloneBlock;

	/**
	 * @param document
	 */
	public FormatterPHPBlockNode(IFormatterDocument document, boolean isStandAloneBlock)
	{
		super(document);
		this.isStandAloneBlock = isStandAloneBlock;
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
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isAddingBeginNewLine()
	 */
	@Override
	protected boolean isAddingBeginNewLine()
	{
		return isStandAloneBlock
				|| CodeFormatterConstants.NEW_LINE.equals(getDocument().getString(
						PHPFormatterConstants.BRACE_POSITION_BLOCK));
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isAddingEndNewLine()
	 */
	protected boolean isAddingEndNewLine()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isIndenting()
	 */
	@Override
	protected boolean isIndenting()
	{
		return getDocument().getBoolean(PHPFormatterConstants.INDENT_CURLY_BLOCKS);
	}
}
