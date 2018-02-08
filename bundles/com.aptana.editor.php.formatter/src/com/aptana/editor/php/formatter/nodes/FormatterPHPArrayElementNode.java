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
 * An array-element wrapper node.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class FormatterPHPArrayElementNode extends FormatterBlockWithBeginEndNode
{
	private boolean hasCommentBefore;
	private boolean isSingleElement;

	/**
	 * Constructs a new FormatterPHPArrayElementNode.
	 * 
	 * @param document
	 * @param isSingleElement
	 *            Indicate that this element is the only element in the list of the array's declaration elements.
	 * @param hasCommentBefore
	 */
	public FormatterPHPArrayElementNode(IFormatterDocument document, boolean isSingleElement, boolean hasCommentBefore)
	{
		super(document);
		this.isSingleElement = isSingleElement;
		this.hasCommentBefore = hasCommentBefore;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isAddingBeginNewLine()
	 */
	@Override
	protected boolean isAddingBeginNewLine()
	{
		return hasCommentBefore
				|| (getDocument().getBoolean(PHPFormatterConstants.NEW_LINES_BETWEEN_ARRAY_CREATION_ELEMENTS) && !isSingleElement);
	}

	protected boolean isAddingEndNewLine()
	{

		return false;
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

}
