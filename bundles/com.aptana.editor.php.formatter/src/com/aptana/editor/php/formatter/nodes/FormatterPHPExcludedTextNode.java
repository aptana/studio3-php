/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

import org.eclipse.jface.text.Region;

import com.aptana.formatter.ExcludeRegionList.EXCLUDE_STRATEGY;
import com.aptana.formatter.IFormatterContext;
import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.IFormatterWriter;

/**
 * A PHP Formatter Text node that exclude itself when begin written, so it will be written as is.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class FormatterPHPExcludedTextNode extends FormatterPHPTextNode
{

	/**
	 * Constructs a new FormatterPHPExcludedTextNode
	 * 
	 * @param document
	 * @param shouldConsumePreviousSpaces
	 * @param spacesCountBefore
	 * @param spacesCountAfter
	 */
	public FormatterPHPExcludedTextNode(IFormatterDocument document, int spacesCountBefore, int spacesCountAfter)
	{
		super(document, true, spacesCountBefore, spacesCountAfter);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockWithBeginNode#accept(com.aptana.formatter.IFormatterContext,
	 * com.aptana.formatter.IFormatterWriter)
	 */
	@Override
	public void accept(IFormatterContext context, IFormatterWriter visitor) throws Exception
	{
		visitor.excludeRegion(new Region(getStartOffset(), getEndOffset() - getStartOffset()),
				EXCLUDE_STRATEGY.WRITE_AS_IS);
		super.accept(context, visitor);
	}
}
