package com.aptana.editor.php.internal.parser;

import com.aptana.editor.common.parsing.IScannerSwitchStrategy;
import com.aptana.editor.html.parsing.HTMLScanner;
import com.aptana.editor.php.core.preferences.PHPVersionProvider;
import com.aptana.editor.php.internal.editor.scanner.PHPTokenScanner;

public class PHTMLScanner extends HTMLScanner
{
	public PHTMLScanner()
	{
		// TODO: Shalom - Pass the current project.
		super(new PHPTokenScanner(PHPVersionProvider.getPHPVersion(null)), new IScannerSwitchStrategy[] {});
	}
}
