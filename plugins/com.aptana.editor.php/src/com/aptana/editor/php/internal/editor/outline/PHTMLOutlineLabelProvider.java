package com.aptana.editor.php.internal.editor.outline;

import com.aptana.editor.html.outline.HTMLOutlineLabelProvider;
import com.aptana.editor.php.internal.parser.PHPMimeType;
import com.aptana.parsing.IParseState;

public class PHTMLOutlineLabelProvider extends HTMLOutlineLabelProvider
{

	protected IParseState parseState;

	/**
	 * Constructs a new PHP - HTML outline label provider
	 */
	public PHTMLOutlineLabelProvider()
	{
		addSubLanguage(PHPMimeType.MimeType, new PHPOutlineLabelProvider());
	}

	/**
	 * Constructs a new PHP - HTML outline label provider with a given parse state.<br>
	 * 
	 * @param parseState
	 */
	public PHTMLOutlineLabelProvider(IParseState parseState)
	{
		this();
		this.parseState = parseState;
	}
}