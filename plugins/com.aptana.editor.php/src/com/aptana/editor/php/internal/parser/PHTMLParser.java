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
package com.aptana.editor.php.internal.parser;

import java.io.IOException;

import beaver.Symbol;

import com.aptana.editor.common.parsing.CompositeParser;
import com.aptana.editor.html.parsing.IHTMLParserConstants;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.ParseNode;
import com.aptana.parsing.ast.ParseRootNode;

public class PHTMLParser extends CompositeParser
{

	public PHTMLParser()
	{
		super(new PHTMLParserScanner(), IHTMLParserConstants.LANGUAGE);
	}

	@Override
	protected IParseNode processEmbeddedlanguage(IParseState parseState) throws Exception
	{
		String source = new String(parseState.getSource());
		int startingOffset = parseState.getStartingOffset();
		IParseNode root = null;

		advance();
		short id = getCurrentSymbol().getId();
		while (id != PHTMLTokens.EOF)
		{
			// only cares about ruby tokens
			switch (id)
			{
				case PHTMLTokens.PHP:
					if (root == null)
					{
						root = new ParseRootNode(PHPMimeType.MIME_TYPE, new ParseNode[0], startingOffset,
								startingOffset + source.length());
					}
					processPHPBlock(root);
					break;
			}
			advance();
			id = getCurrentSymbol().getId();
		}
		return root;
	}

	private void processPHPBlock(IParseNode root) throws IOException, Exception
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

		IParseNode result = getParseResult(PHPMimeType.MIME_TYPE, start, end);
		if (result != null)
		{
			Symbol endTag = getCurrentSymbol();
			ParseNode phpNode = new ParseNode(PHPMimeType.MIME_TYPE);
			phpNode.setLocation(startTag.getStart(), endTag.getEnd());
			root.addChild(phpNode);
		}
	}
}
