package com.aptana.editor.php.internal.ui.editor.outline;

import com.aptana.editor.html.outline.HTMLOutlineLabelProvider;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.parsing.IParseState;

public class PHTMLOutlineLabelProvider extends HTMLOutlineLabelProvider
{

	protected IParseState parseState;

	/**
	 * Constructs a new PHP - HTML outline label provider
	 */
	public PHTMLOutlineLabelProvider()
	{
		addSubLanguage(IPHPConstants.CONTENT_TYPE_PHP, new PHPOutlineLabelProvider());
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