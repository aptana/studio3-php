package com.aptana.editor.php.internal.parser;

import com.aptana.editor.html.parsing.HTMLParserScanner;

public class PHTMLParserScanner extends HTMLParserScanner
{

	public PHTMLParserScanner()
	{
		super(new PHTMLScanner());
	}
}
