/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

import com.aptana.editor.php.formatter.PHPFormatterConstants;
import com.aptana.formatter.IFormatterContext;
import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.IFormatterWriter;
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

		switch (parenthesesType)
		{
			case DECLARATION_PARENTHESIS:
				return getInt(PHPFormatterConstants.SPACES_BEFORE_OPENING_DECLARATION_PARENTHESES);
			case INVOCATION_PARENTHESIS:
			case ARRAY_PARENTHESIS:
				return getInt(PHPFormatterConstants.SPACES_BEFORE_OPENING_INVOCATION_PARENTHESES);
			case ARRAY_SQUARE:
				return getInt(PHPFormatterConstants.SPACES_BEFORE_OPENING_ARRAY_ACCESS_PARENTHESES);
			case CONDITIONAL_PARENTHESIS:
				return getInt(PHPFormatterConstants.SPACES_BEFORE_OPENING_CONDITIONAL_PARENTHESES);
			case LOOP_PARENTHESIS:
				return getInt(PHPFormatterConstants.SPACES_BEFORE_OPENING_LOOP_PARENTHESES);
			default:
				return 0;
		}
	}

	/**
	 * We override the acceptBody to control any spaces that should be added before or after the body.
	 * 
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#acceptBody(com.aptana.formatter.IFormatterContext,
	 *      com.aptana.formatter.IFormatterWriter)
	 */
	@Override
	protected void acceptBody(IFormatterContext context, IFormatterWriter visitor) throws Exception
	{
		int spacesBeforeBody = getSpacesBeforeBody();
		if (spacesBeforeBody > 0)
		{
			writeSpaces(visitor, context, spacesBeforeBody);
		}
		super.acceptBody(context, visitor);
		int spacesAfterBody = getSpacesAfterBody();
		if (spacesAfterBody > 0)
		{
			writeSpaces(visitor, context, spacesAfterBody);
		}
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

	/**
	 * @return The amount of spaces that we should insert before the body.
	 */
	private int getSpacesBeforeBody()
	{
		if (isAsWrapper())
		{
			return 0;
		}
		switch (parenthesesType)
		{
			case DECLARATION_PARENTHESIS:
				return getInt(PHPFormatterConstants.SPACES_AFTER_OPENING_DECLARATION_PARENTHESES);
			case INVOCATION_PARENTHESIS:
			case ARRAY_PARENTHESIS:
				return getInt(PHPFormatterConstants.SPACES_AFTER_OPENING_INVOCATION_PARENTHESES);
			case ARRAY_SQUARE:
				return getInt(PHPFormatterConstants.SPACES_AFTER_OPENING_ARRAY_ACCESS_PARENTHESES);
			case CONDITIONAL_PARENTHESIS:
				return getInt(PHPFormatterConstants.SPACES_AFTER_OPENING_CONDITIONAL_PARENTHESES);
			case LOOP_PARENTHESIS:
				return getInt(PHPFormatterConstants.SPACES_AFTER_OPENING_LOOP_PARENTHESES);
			default:
				return 0;
		}
	}

	/**
	 * @return The amount of spaces that we should insert after the body.
	 */
	private int getSpacesAfterBody()
	{
		if (isAsWrapper())
		{
			return 0;
		}
		switch (parenthesesType)
		{
			case DECLARATION_PARENTHESIS:
				return getInt(PHPFormatterConstants.SPACES_BEFORE_CLOSING_DECLARATION_PARENTHESES);
			case INVOCATION_PARENTHESIS:
			case ARRAY_PARENTHESIS:
				return getInt(PHPFormatterConstants.SPACES_BEFORE_CLOSING_INVOCATION_PARENTHESES);
			case ARRAY_SQUARE:
				return getInt(PHPFormatterConstants.SPACES_BEFORE_CLOSING_ARRAY_ACCESS_PARENTHESES);
			case CONDITIONAL_PARENTHESIS:
				return getInt(PHPFormatterConstants.SPACES_BEFORE_CLOSING_CONDITIONAL_PARENTHESES);
			case LOOP_PARENTHESIS:
				return getInt(PHPFormatterConstants.SPACES_BEFORE_CLOSING_LOOP_PARENTHESES);
			default:
				return 0;
		}
	}

}
