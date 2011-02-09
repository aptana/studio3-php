/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

import com.aptana.formatter.IFormatterDocument;

/**
 * A PHP formatter node for keywords, such as modifiers (e.g. 'public', 'private', 'static' etc.), 'echo', 'const' etc.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPKeywordNode extends FormatterPHPTextNode
{

	private boolean isFirstInLine;

	/**
	 * Constructs a new FormatterPHPKeywordNode
	 * 
	 * @param document
	 * @param isFirstInLine
	 *            Flag this keyword as the first in the line. This will the value that the
	 *            {@link #isAddingBeginNewLine()} returns. When it's false, previous white spaces will be consumed.
	 */
	public FormatterPHPKeywordNode(IFormatterDocument document, boolean isFirstInLine)
	{
		// We only consume the previous spaces if this modifier is not the first one in the line.
		super(document, !isFirstInLine);
		this.isFirstInLine = isFirstInLine;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isAddingBeginNewLine()
	 */
	@Override
	protected boolean isAddingBeginNewLine()
	{
		return this.isFirstInLine;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.formatter.nodes.FormatterPHPTextNode#getSpacesCountBefore()
	 */
	@Override
	public int getSpacesCountBefore()
	{
		if (isFirstInLine)
		{
			return 0;
		}
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#getSpacesCountAfter()
	 */
	public int getSpacesCountAfter()
	{
		return (this.getStartOffset() == this.getEndOffset()) ? 0 : 1;
	}
}
