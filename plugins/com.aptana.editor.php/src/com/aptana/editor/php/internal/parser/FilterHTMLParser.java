package com.aptana.editor.php.internal.parser;

import com.aptana.editor.html.parsing.HTMLParser;

public class FilterHTMLParser extends HTMLParser
{
	public FilterHTMLParser()
	{
		super(new FilterParserScanner());
	}
}
