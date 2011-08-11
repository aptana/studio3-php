/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

import com.aptana.editor.html.parsing.HTMLTokenScanner;
import com.aptana.editor.html.parsing.lexer.HTMLTokenType;

public class PHTMLTokenScanner extends HTMLTokenScanner
{

	@SuppressWarnings("nls")
	private static final String[] PHP_START = { "<?php", "<?=", "<?" };
	@SuppressWarnings("nls")
	private static final String[] PHP_END = new String[] { "?>" };

	public PHTMLTokenScanner()
	{
		List<IRule> rules = new ArrayList<IRule>();
		// adds rules for finding the PHP start and end sequences
		WordRule wordRule = new WordRule(new PHPStartDetector(), Token.UNDEFINED);
		IToken token = createToken(getTokenName(PHTMLTokens.PHP));
		for (String word : PHP_START)
		{
			wordRule.addWord(word, token);
		}
		rules.add(wordRule);
		wordRule = new WordRule(new PHPEndDetector(), Token.UNDEFINED);
		token = createToken(getTokenName(PHTMLTokens.PHP_END));
		for (String word : PHP_END)
		{
			wordRule.addWord(word, token);
		}
		rules.add(wordRule);

		// Special heredoc and nowdoc rule
		rules.add(new HeredocRule(createToken(getTokenName(PHTMLTokens.PHP_HEREDOC)), false));
		rules.add(new HeredocRule(createToken(getTokenName(PHTMLTokens.PHP_HEREDOC)), true));

		// Add rule for double quotes
		rules.add(new MultiLineRule("\"", "\"", createToken(HTMLTokenType.DOUBLE_QUOTED_STRING.getScope()), '\\')); //$NON-NLS-1$ //$NON-NLS-2$
		// Add a rule for single quotes
		rules.add(new MultiLineRule("'", "'", createToken(HTMLTokenType.SINGLE_QUOTED_STRING.getScope()), '\\')); //$NON-NLS-1$ //$NON-NLS-2$

		for (IRule rule : fRules)
		{
			rules.add(rule);
		}

		setRules(rules.toArray(new IRule[rules.size()]));
	}

	private static String getTokenName(short token) // $codepro.audit.disable
	{
		return PHTMLTokens.getTokenName(token);
	}

	private static final class PHPStartDetector implements IWordDetector
	{

		public boolean isWordPart(char c)
		{
			switch (c)
			{
				case '<':
				case '?':
				case '%':
				case 'p':
				case 'h':
				case '=':
					return true;
			}
			return false;
		}

		public boolean isWordStart(char c)
		{
			return c == '<';
		}
	}

	private static final class PHPEndDetector implements IWordDetector
	{

		public boolean isWordPart(char c)
		{
			switch (c)
			{
				case '?':
				case '%':
				case '>':
					return true;
			}
			return false;
		}

		public boolean isWordStart(char c)
		{
			return c == '?' || c == '%';
		}
	}
}
