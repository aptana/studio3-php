/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.parser2;

import com.aptana.editor.php.internal.parser.PHPMimeType;
import com.aptana.ide.core.StringUtils;
import com.aptana.ide.editor.html.parsing.HTMLParser;
import com.aptana.ide.editor.js.parsing.JSMimeType;
import com.aptana.ide.editor.js.parsing.JSParser;
import com.aptana.ide.parsing.IParseState;
import com.aptana.ide.parsing.IParser;
import com.aptana.ide.parsing.ParserInitializationException;

/**
 * @author Kevin Lindsey
 */
public class ExtendedHTMLParser extends HTMLParser
{
	/**
	 * ExtendedHTMLParser
	 * 
	 * @throws ParserInitializationException
	 */
	public ExtendedHTMLParser() throws ParserInitializationException
	{
		super();
	}

	/**
	 * @see com.aptana.ide.editor.html.parsing.HTMLParser#createParseState(com.aptana.ide.parsing.IParseState)
	 */
	public IParseState createParseState(IParseState parent)
	{
		IParseState createParseState = super.createParseState(parent);
		IParser phpParser = this.getParserForMimeType(PHPMimeType.MimeType);

		// NOTE: The following code should make use of the JS nested_language extension point
		// to break the direct dependence of this plugin on the JS plugin
		IParser parserForMimeType = this.getParserForMimeType(JSMimeType.MimeType);

		if (parserForMimeType != null)
		{
			JSParser js = (JSParser) parserForMimeType;
			
			js.setPiLanguage(PHPMimeType.MimeType);
		}

		// add their parse states, if they exist
		if (phpParser != null)
		{
			IParseState phpState = phpParser.createParseState(createParseState);

			createParseState.addChildState(phpState);
			createParseState.getParseState(JSMimeType.MimeType).addChildState(phpState);
		}

		return createParseState;
	}

	/**
	 * @see com.aptana.ide.editor.html.parsing.HTMLParser#addChildParsers()
	 */
	protected void addChildParsers() throws ParserInitializationException
	{
		super.addChildParsers();

		PHPParser parser = new PHPBackgroundParser();

		String piName = "<?php"; //$NON-NLS-1$
		this.languageRegistry.setHandlesEOF(piName, StringUtils.EMPTY, StringUtils.EMPTY, true);
		this.languageRegistry.setRegistryEntry(piName, StringUtils.EMPTY, StringUtils.EMPTY, parser);
		this.addChildParser(parser);

		piName = "<?"; //$NON-NLS-1$
		this.languageRegistry.setHandlesEOF(piName, StringUtils.EMPTY, StringUtils.EMPTY, true);
		this.languageRegistry.setRegistryEntry(piName, StringUtils.EMPTY, StringUtils.EMPTY, parser);
		this.addChildParser(parser);
	}
}
