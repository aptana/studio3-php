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

/**
 * A Namespace block formatter node.<br>
 * This block appears after a 'namespace MyNamespace\Classes;' expressions.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterPHPNamespaceBlockNode extends FormatterPHPBlockNode
{

	/**
	 * @param document
	 * @param isStandAloneBlock
	 */
	public FormatterPHPNamespaceBlockNode(IFormatterDocument document)
	{
		super(document, true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.formatter.nodes.FormatterPHPBlockNode#isAddingBeginNewLine()
	 */
	protected boolean isAddingBeginNewLine()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.formatter.nodes.FormatterPHPBlockNode#isIndenting()
	 */
	protected boolean isIndenting()
	{
		return getDocument().getBoolean(PHPFormatterConstants.INDENT_NAMESPACE_BLOCKS);
	}
}
