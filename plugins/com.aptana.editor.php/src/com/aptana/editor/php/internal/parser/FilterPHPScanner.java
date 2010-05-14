package com.aptana.editor.php.internal.parser;

import com.aptana.editor.common.parsing.IScannerSwitchStrategy;
import com.aptana.editor.common.parsing.ScannerSwitchStrategy;
import com.aptana.editor.html.parsing.HTMLScanner;
import com.aptana.editor.html.parsing.lexer.HTMLTokens;

/**
 * An HTML scanner that filters out PHP blocks by marking them as a comment.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FilterPHPScanner extends HTMLScanner
{
	private static final String[] PHP_ENTER_TOKENS = new String[] { FilterPHPTokenScanner.PHP_START };
	private static final String[] PHP_EXIT_TOKENS = new String[] { FilterPHPTokenScanner.PHP_END };

	private static final IScannerSwitchStrategy PHP_STRATEGY = new ScannerSwitchStrategy(PHP_ENTER_TOKENS,
			PHP_EXIT_TOKENS);

	public FilterPHPScanner()
	{
		super(new FilterPHPTokenScanner(), new IScannerSwitchStrategy[] { PHP_STRATEGY });
	}

	public short getTokenType(Object data)
	{
		IScannerSwitchStrategy strategy = getCurrentSwitchStrategy();
		if (strategy == null)
		{
			return super.getTokenType(data);
		}
		if (strategy == PHP_STRATEGY)
		{
			return HTMLTokens.COMMENT;
		}
		return super.getTokenType(data);
	}
}
