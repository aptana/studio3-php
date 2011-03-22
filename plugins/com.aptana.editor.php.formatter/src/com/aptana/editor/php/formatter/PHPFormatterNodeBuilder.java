/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter;

import org2.eclipse.php.internal.core.ast.nodes.Program;

import com.aptana.editor.php.formatter.nodes.FormatterPHPRootNode;
import com.aptana.editor.php.internal.parser.nodes.PHPASTWrappingNode;
import com.aptana.formatter.FormatterDocument;
import com.aptana.formatter.nodes.AbstractFormatterNodeBuilder;
import com.aptana.formatter.nodes.IFormatterContainerNode;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.ParseRootNode;

/**
 * PHP formatter node builder.<br>
 * This builder generates the formatter nodes that will then be processed by the {@link PHPFormatterNodeRewriter} to
 * produce the output for the code formatting process.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPFormatterNodeBuilder extends AbstractFormatterNodeBuilder
{

	private boolean hasErrors;

	/**
	 * @param parseResult
	 * @param document
	 * @return
	 */
	public IFormatterContainerNode build(IParseNode parseResult, FormatterDocument document)
	{
		final IFormatterContainerNode rootNode = new FormatterPHPRootNode(document);
		start(rootNode);
		ParseRootNode phpRootNode = (ParseRootNode) parseResult;
		// the root node should hold a single Program (AST) that was inserted as a ParseNode.
		if (phpRootNode.getChildCount() == 1)
		{
			IParseNode child = phpRootNode.getChild(0);
			if (child instanceof PHPASTWrappingNode)
			{
				Program ast = ((PHPASTWrappingNode) child).getAST();
				ast.accept(new PHPFormatterVisitor(document, this));
			}
		}
		checkedPop(rootNode, document.getLength());
		return rootNode;
	}

	/**
	 * @return True, in case the AST contains errors; False, otherwise.
	 * @see #setHasErrors(boolean)
	 */
	public boolean hasErrors()
	{
		return hasErrors;
	}

	/**
	 * Set the error-state of the AST.
	 * 
	 * @param hasErrors
	 * @see #hasErrors
	 */
	public void setHasErrors(boolean hasErrors)
	{
		this.hasErrors = hasErrors;
	}
}
