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

import com.aptana.editor.php.formatter.PHPFormatterConstants;
import com.aptana.editor.php.formatter.nodes.NodeTypes.TypePunctuation;
import com.aptana.formatter.IFormatterDocument;

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
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_COMMAS);
			case SEMICOLON:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_SEMICOLON);
			case FOR_SEMICOLON:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_FOR_SEMICOLON);
			case NAMESPACE_SEPARATOR:
				return getDocument().getInt(PHPFormatterConstants.SPACES_AFTER_NAMESPACE_SEPARATOR);
			default:
				return super.getSpacesCountBefore();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isAddingEndNewLine()
	 */
	@Override
	protected boolean isAddingEndNewLine()
	{
		return forceLineTermination || super.isAddingEndNewLine();
	}
}
