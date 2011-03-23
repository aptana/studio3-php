/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.nodes.FormatterBlockNode;

/**
 * A PHP formatter root node.<br>
 * This node will make sure that in case the PHP partition is nested in HTML, we prefix the formatted output with a new
 * line on the top.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPRootNode extends FormatterBlockNode
{

	/**
	 * @param document
	 */
	public FormatterPHPRootNode(IFormatterDocument document)
	{
		super(document);
	}
}
