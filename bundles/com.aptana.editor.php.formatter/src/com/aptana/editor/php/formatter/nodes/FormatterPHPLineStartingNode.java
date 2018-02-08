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
 * A PHP formatter node that provides new line at the beginning, but no new line at the end of the begin-node.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPLineStartingNode extends FormatterPHPTextNode
{

	/**
	 * @param document
	 */
	public FormatterPHPLineStartingNode(IFormatterDocument document)
	{
		super(document);
	}

	/* (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterBlockNode#isAddingBeginNewLine()
	 */
	@Override
	protected boolean isAddingBeginNewLine()
	{
		return true;
	}

}
