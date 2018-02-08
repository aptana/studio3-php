/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import com.aptana.formatter.ExcludeRegionList.EXCLUDE_STRATEGY;
import com.aptana.formatter.IFormatterContext;
import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.IFormatterWriter;
import com.aptana.formatter.nodes.FormatterTextNode;

/**
 * A PHP formatter node for HEREDOC and NOWDOC nodes.<br>
 * These nodes should not be formatted when written back.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPHeredocNode extends FormatterTextNode
{

	private IRegion heredocRegion;

	/**
	 * Constructs a new FormatterPHPHeredocNode
	 * 
	 * @param document
	 * @param startOffset
	 * @param endOffset
	 */
	public FormatterPHPHeredocNode(IFormatterDocument document, int startOffset, int endOffset)
	{
		super(document, startOffset, endOffset);
		heredocRegion = new Region(startOffset, endOffset - startOffset);
	}

	/**
	 * Override the default implementation to exclude the content from begin indented/formatted. (non-Javadoc)
	 * 
	 * @see com.aptana.formatter.nodes.FormatterTextNode#accept(com.aptana.formatter.IFormatterContext,
	 *      com.aptana.formatter.IFormatterWriter)
	 */
	public void accept(IFormatterContext context, IFormatterWriter visitor) throws Exception
	{
		IFormatterContext heredocContext = context.copy();
		heredocContext.setIndenting(false);
		visitor.excludeRegion(heredocRegion, EXCLUDE_STRATEGY.WRITE_AS_IS);
		visitor.write(heredocContext, getStartOffset(), getEndOffset());
	}
}
