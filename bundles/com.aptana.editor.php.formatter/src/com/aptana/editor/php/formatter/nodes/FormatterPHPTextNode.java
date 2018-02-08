/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.nodes.FormatterBlockWithBeginNode;

/**
 * A generic PHP text node formatter.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPTextNode extends FormatterBlockWithBeginNode
{

	private boolean shouldConsumePreviousSpaces;
	private int spacesCountBefore;
	private int spacesCountAfter;

	/**
	 * @param document
	 */
	public FormatterPHPTextNode(IFormatterDocument document)
	{
		super(document);
	}

	/**
	 * @param document
	 * @param shouldConsumePreviousSpaces
	 */
	public FormatterPHPTextNode(IFormatterDocument document, boolean shouldConsumePreviousSpaces)
	{
		this(document);
		this.shouldConsumePreviousSpaces = shouldConsumePreviousSpaces;
	}

	/**
	 * @param document
	 * @param shouldConsumePreviousSpaces
	 * @param spacesCountBefore
	 */
	public FormatterPHPTextNode(IFormatterDocument document, boolean shouldConsumePreviousSpaces,
			int spacesCountBefore, int spacesCountAfter)
	{
		this(document, shouldConsumePreviousSpaces);
		this.spacesCountBefore = spacesCountBefore;
		this.spacesCountAfter = spacesCountAfter;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#shouldConsumePreviousWhiteSpaces()
	 */
	@Override
	public boolean shouldConsumePreviousWhiteSpaces()
	{
		return shouldConsumePreviousSpaces;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#getSpacesCountBefore()
	 */
	@Override
	public int getSpacesCountBefore()
	{
		return spacesCountBefore;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.AbstractFormatterNode#getSpacesCountAfter()
	 */
	@Override
	public int getSpacesCountAfter()
	{
		return spacesCountAfter;
	}

}
