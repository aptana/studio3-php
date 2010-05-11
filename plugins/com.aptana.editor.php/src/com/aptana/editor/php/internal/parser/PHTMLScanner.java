package com.aptana.editor.php.internal.parser;
import org.eclipse.php.internal.core.PHPVersion;

import com.aptana.editor.common.parsing.IScannerSwitchStrategy;
import com.aptana.editor.common.parsing.ScannerSwitchStrategy;
import com.aptana.editor.html.parsing.HTMLScanner;
import com.aptana.editor.php.internal.editor.scanner.PHPTokenScanner;

public class PHTMLScanner extends HTMLScanner
{

	private static final String[] PHP_ENTER_TOKENS = new String[] { "PHP_START" };
	private static final String[] PHP_EXIT_TOKENS = new String[] { "PHP_END" };

	private static final IScannerSwitchStrategy PHP_STRATEGY = new ScannerSwitchStrategy(PHP_ENTER_TOKENS,
			PHP_EXIT_TOKENS);

	private boolean isInPHP;

	public PHTMLScanner()
	{
		super(new PHPTokenScanner(PHPVersion.PHP5), new IScannerSwitchStrategy[] { PHP_STRATEGY });
	}

	public short getTokenType(Object data)
	{
		IScannerSwitchStrategy strategy = getCurrentSwitchStrategy();
		if (strategy == PHP_STRATEGY)
		{
			if (!isInPHP)
			{
				isInPHP = true;
			}
//			return ERBTokens.RUBY;
			return 11111;
		}
		if (strategy == null && isInPHP)
		{
			isInPHP = false;
//			return ERBTokens.RUBY_END;
			return 22222;
		}
		return super.getTokenType(data);
	}
}
