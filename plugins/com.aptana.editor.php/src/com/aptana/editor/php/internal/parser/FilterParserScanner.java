package com.aptana.editor.php.internal.parser;

import com.aptana.editor.html.parsing.HTMLParserScanner;

/**
 * An HTML parser scanner that is part of the system that finds an filter out any PHP blocks from the HTML parser.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FilterParserScanner extends HTMLParserScanner
{
	public FilterParserScanner()
	{
		super(new FilterPHPScanner());
	}
}
