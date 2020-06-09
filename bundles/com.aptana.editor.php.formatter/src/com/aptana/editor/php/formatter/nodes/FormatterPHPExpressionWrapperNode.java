/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.nodes.FormatterBlockWithBeginEndNode;

/**
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPExpressionWrapperNode extends FormatterBlockWithBeginEndNode
{


	/**
	 * @param document
	 * @param endsWithSemicolon
	 */
	public FormatterPHPExpressionWrapperNode(IFormatterDocument document)
	{
		super(document);
	}

}
