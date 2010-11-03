/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter;

import org.eclipse.php.internal.core.ast.nodes.Program;

import com.aptana.editor.php.formatter.nodes.FormatterPHPRootNode;
import com.aptana.editor.php.internal.parser.nodes.PHPASTWrappingNode;
import com.aptana.formatter.FormatterDocument;
import com.aptana.formatter.nodes.AbstractFormatterNodeBuilder;
import com.aptana.formatter.nodes.IFormatterContainerNode;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.ParseRootNode;

/**
 * JS formatter node builder.<br>
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
