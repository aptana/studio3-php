/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser;

import com.aptana.editor.common.parsing.IScannerSwitchStrategy;
import com.aptana.editor.common.parsing.ScannerSwitchStrategy;
import com.aptana.editor.html.parsing.HTMLScanner;

public class PHTMLScanner extends HTMLScanner
{

	private static final String[] PHP_ENTER_TOKENS = new String[] { PHTMLTokens.getTokenName(PHTMLTokens.PHP) };
	private static final String[] PHP_EXIT_TOKENS = new String[] { PHTMLTokens.getTokenName(PHTMLTokens.PHP_END) };

	private static final IScannerSwitchStrategy PHP_STRATEGY = new ScannerSwitchStrategy(PHP_ENTER_TOKENS,
			PHP_EXIT_TOKENS);

	private boolean isInPHP;

	public PHTMLScanner()
	{
		super(new PHTMLTokenScanner(), new IScannerSwitchStrategy[] { PHP_STRATEGY });
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
			return PHTMLTokens.PHP;
		}
		if (strategy == null && isInPHP)
		{
			isInPHP = false;
			return PHTMLTokens.PHP_END;
		}
		return super.getTokenType(data);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.parsing.CompositeTokenScanner#reset()
	 */
	@Override
	protected void reset()
	{
		super.reset();
		isInPHP = false;
	}

}
