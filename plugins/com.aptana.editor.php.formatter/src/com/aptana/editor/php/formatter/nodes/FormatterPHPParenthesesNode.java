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
import com.aptana.formatter.nodes.NodeTypes.TypeBracket;

/**
 * A PHP node formatter for parentheses, which can be used for any other single char open and close pair, such as
 * brackets etc.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPParenthesesNode extends FormatterBlockWithBeginEndNode
{

	private boolean asWrapper;
	private boolean newLineBeforeClosing;
	private TypeBracket parenthesesType;
	private int containedElementsCount;

	/**
	 * Constructs a new FormatterPHPParenthesesNode
	 * 
	 * @param document
	 * @param asWrapper
	 *            Indicate that these parentheses do not have an open and close brackets, but is acting as a wrapper
	 *            node for an expression that appears inside it. For example, an 'echo' statement without the
	 * @param forceSameLine
	 *            Force the open and close parentheses.
	 * @param type
	 *            The bracket (parentheses) type - a {@link TypeBracket} value.
	 */
	public FormatterPHPParenthesesNode(IFormatterDocument document, boolean asWrapper, int containedElementsCount,
			TypeBracket type)
	{
		super(document);
		this.asWrapper = asWrapper;
		this.containedElementsCount = containedElementsCount;
		this.parenthesesType = type;
	}

	/**
	 * Constructs a new FormatterPHPParenthesesNode
	 * 
	 * @param document
	 */
	public FormatterPHPParenthesesNode(IFormatterDocument document, TypeBracket type)
	{
		this(document, false, 0, type);
	}

	/**
	 * Force a new line before the closing parentheses.<br>
	 * The new line will only be inserted when this node is <b>not</b> a wrapper node.
	 * 
	 * @param newLineBeforeClosing
	 */
	public void setNewLineBeforeClosing(boolean newLineBeforeClosing)
	{
		this.newLineBeforeClosing = newLineBeforeClosing;
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

	/*
	 * (non-Javadoc)
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

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isIndenting()
	 */
	@Override
	protected boolean isIndenting()
	{
		switch (parenthesesType)
		{
			case ARRAY_PARENTHESIS:
				return getDocument().getBoolean(PHPFormatterConstants.NEW_LINES_BETWEEN_ARRAY_CREATION_ELEMENTS);
		}
		return super.isIndenting();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isAddingEndNewLine()
	 */
	@Override
	protected boolean isAddingEndNewLine()
	{
		if (!asWrapper && newLineBeforeClosing)
		{
			return true;
		}
		switch (parenthesesType)
		{
			case ARRAY_PARENTHESIS:
				if (containedElementsCount > 1)
				{
					return getDocument().getBoolean(PHPFormatterConstants.NEW_LINES_BETWEEN_ARRAY_CREATION_ELEMENTS);
				}
		}
		return false;

	}
}
