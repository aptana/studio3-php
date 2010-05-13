package com.aptana.editor.php.internal.parser;
import com.aptana.editor.common.parsing.IScannerSwitchStrategy;
import com.aptana.editor.common.parsing.ScannerSwitchStrategy;
import com.aptana.editor.html.parsing.HTMLScanner;
import com.aptana.editor.php.core.preferences.PHPVersionProvider;
import com.aptana.editor.php.internal.editor.scanner.PHPTokenScanner;

public class PHTMLScanner extends HTMLScanner
{

	private static final String[] PHP_ENTER_TOKENS = new String[] { "PHP_START" }; //$NON-NLS-1$
	private static final String[] PHP_EXIT_TOKENS = new String[] { "PHP_END" }; //$NON-NLS-1$

	private static final IScannerSwitchStrategy PHP_STRATEGY = new ScannerSwitchStrategy(PHP_ENTER_TOKENS,
			PHP_EXIT_TOKENS);

	private boolean isInPHP;

	public PHTMLScanner()
	{
		// TODO: Shalom - Pass the current project.
		super(new PHPTokenScanner(PHPVersionProvider.getPHPVersion(null)), new IScannerSwitchStrategy[] { PHP_STRATEGY });
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
