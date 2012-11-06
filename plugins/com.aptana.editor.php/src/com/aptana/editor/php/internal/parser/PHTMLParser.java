/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser;

import java.io.IOException;

import beaver.Symbol;

import com.aptana.editor.common.parsing.CompositeParser;
import com.aptana.editor.html.IHTMLConstants;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.WorkingParseResult;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.ParseNode;

public class PHTMLParser extends CompositeParser
{
	public PHTMLParser()
	{
		super(new PHTMLParserScanner(), IHTMLConstants.CONTENT_TYPE_HTML);
	}

	@Override
	protected IParseNode processEmbeddedlanguage(IParseState parseState, WorkingParseResult working) throws Exception // $codepro.audit.disable
	// declaredExceptions
	{
		String source = parseState.getSource();
		int sourceLength = source.length();
		int startingOffset = parseState.getStartingOffset();
		IParseNode root = null;

		advance();
		short id = getCurrentSymbol().getId();
		while (id != PHTMLTokens.EOF)
		{
			// only cares about PHP tokens
			switch (id)
			{
				case PHTMLTokens.PHP:
					if (root == null)
					{
						root = new PHPParseRootNode(PHPParser.NO_CHILDREN, startingOffset, startingOffset
								+ sourceLength - 1);
					}
					processPHPBlock(root, sourceLength);
					break;
			}
			advance();
			id = getCurrentSymbol().getId();
		}
		return root;
	}

	private void processPHPBlock(IParseNode root, int sourceLength) throws IOException, Exception // $codepro.audit.disable
	{
		Symbol startTag = getCurrentSymbol();
		advance();

		// finds the entire php block
		int start = getCurrentSymbol().getStart();
		int end = start;
		short id = getCurrentSymbol().getId();
		while (id != PHTMLTokens.PHP_END && id != PHTMLTokens.EOF)
		{
			end = getCurrentSymbol().getEnd();
			advance();
			id = getCurrentSymbol().getId();
		}

		IParseNode result = getParseResult(IPHPConstants.CONTENT_TYPE_PHP, start, end).getRootNode();
		if (result != null)
		{
			Symbol endTag = getCurrentSymbol();
			ParseNode phpNode = new PHPParseNode();
			int endOffset;
			if ("?>".equals(endTag.value)) //$NON-NLS-1$
			{
				endOffset = endTag.getEnd() + 1;
			}
			else
			{
				endOffset = endTag.getEnd();
			}
			phpNode.setLocation(startTag.getStart(), Math.min(endOffset, sourceLength - 1));
			root.addChild(phpNode);
		}
	}
}
