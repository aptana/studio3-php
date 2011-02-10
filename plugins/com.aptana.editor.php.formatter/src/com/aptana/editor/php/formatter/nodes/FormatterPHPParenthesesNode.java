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

/**
 * A PHP node formatter for parentheses, which can be used for any other single char open and close pair, such as
 * brackets etc.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPParenthesesNode extends FormatterBlockWithBeginEndNode
{

	private boolean asWrapper;

	/**
	 * Constructs a new FormatterPHPParenthesesNode
	 * 
	 * @param document
	 * @param asWrapper
	 *            Indicate that these parentheses do not have an open and close brackets, but is acting as a wrapper
	 *            node for an expression that appears inside it. For example, an 'echo' statement without the
	 *            parentheses.
	 */
	public FormatterPHPParenthesesNode(IFormatterDocument document, boolean asWrapper)
	{
		this(document);
		this.asWrapper = asWrapper;
	}

	/**
	 * Constructs a new FormatterPHPParenthesesNode
	 * 
	 * @param document
	 */
	public FormatterPHPParenthesesNode(IFormatterDocument document)
	{
		super(document);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#getSpacesCountBefore()
	 */
	@Override
	public int getSpacesCountBefore()
	{
		if (isAsWrapper())
		{
			return 1;
		}
		return getInt(PHPFormatterConstants.SPACES_BEFORE_PARENTHESES);
	}

	/* (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#getSpacesCountAfter()
	 */
	@Override
	public int getSpacesCountAfter()
	{
		if (isAsWrapper())
		{
			return 0;
		}
		return getInt(PHPFormatterConstants.SPACES_AFTER_PARENTHESES);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#shouldConsumePreviousWhiteSpaces()
	 */
	@Override
	public boolean shouldConsumePreviousWhiteSpaces()
	{
		return true;
	}

	/**
	 * @return the asWrapper
	 */
	public boolean isAsWrapper()
	{
		return asWrapper;
	}
}
