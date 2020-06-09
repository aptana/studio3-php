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

/**
 * A PHP formatter node for punctuation elements, such as commas, colons etc.<br>
 * A punctuation node is defined, by default, to consume all white spaces in front of it.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPPunctuationNode extends FormatterPHPTextNode
{

	private TypePunctuation nodeType;
	private boolean forceLineTermination;

	/**
	 * Constructs a new FormatterPHPCommaNode.
	 * 
	 * @param document
	 * @param nodeType
	 */
	public FormatterPHPPunctuationNode(IFormatterDocument document, TypePunctuation nodeType)
	{
		super(document, true);
		this.nodeType = nodeType;
	}

	/**
	 * Constructs a new FormatterPHPCommaNode.
	 * 
	 * @param document
	 * @param nodeType
	 * @param forceLineTermination
	 *            - Force this node to terminate with a new line
	 */
	public FormatterPHPPunctuationNode(IFormatterDocument document, TypePunctuation nodeType,
			boolean forceLineTermination)
	{
		this(document, nodeType);
		this.forceLineTermination = forceLineTermination;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.formatter.nodes.FormatterPHPTextNode#getSpacesCountBefore()
	 */
	@Override
	public int getSpacesCountBefore()
	{
		switch (nodeType)
		{
			case CASE_COLON:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_CASE_COLON_OPERATOR);
			case GOTO_COLON:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_COLON);
			case COMMA:
			case ARRAY_COMMA:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_COMMAS);
			case SEMICOLON:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_SEMICOLON);
			case FOR_SEMICOLON:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_FOR_SEMICOLON);
			case NAMESPACE_SEPARATOR:
				return getDocument().getInt(PHPFormatterConstants.SPACES_BEFORE_NAMESPACE_SEPARATOR);
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
		switch (nodeType)
		{
			case CASE_COLON:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_CASE_COLON_OPERATOR);
			case GOTO_COLON:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_COLON);
			case COMMA:
			case ARRAY_COMMA:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_COMMAS);
			case SEMICOLON:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_SEMICOLON);
			case FOR_SEMICOLON:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_FOR_SEMICOLON);
			case NAMESPACE_SEPARATOR:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_NAMESPACE_SEPARATOR);
			default:
				return super.getSpacesCountAfter();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isAddingEndNewLine()
	 */
	@Override
	protected boolean isAddingEndNewLine()
	{
		return (forceLineTermination || super.isAddingEndNewLine());
	}
}
