/**
 * Aptana Studio
 * Copyright (c) 2005-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.nodes.FormatterBlockWithBeginEndNode;

/**
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class FormatterPHPTraitPrecedenceWrapperNode extends FormatterBlockWithBeginEndNode
{

	/**
	 * @param document
	 */
	public FormatterPHPTraitPrecedenceWrapperNode(IFormatterDocument document)
	{
		super(document);
	}

	@Override
	protected boolean isAddingBeginNewLine()
	{
		return true;
	}
}
