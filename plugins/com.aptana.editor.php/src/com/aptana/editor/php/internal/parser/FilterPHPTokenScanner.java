package com.aptana.editor.php.internal.parser;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

import com.aptana.editor.html.parsing.HTMLTokenScanner;

/**
 * A Rule-based PHP token scanner. <br>
 * This token scanner is being used by the {@link FilterPHPScanner} to find all the PHP blocks and mark them as
 * comments, so the HTML parser will skip them.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FilterPHPTokenScanner extends HTMLTokenScanner
{
	public static final String PHP_START = "PHP_START"; //$NON-NLS-1$
	public static final String PHP_END = "PHP_END"; //$NON-NLS-1$
	@SuppressWarnings( { "nls" })
	private static final String[] PHP_START_WORDS = { "<?php", "<?", "<?=", "<%", "<%=" };
	@SuppressWarnings( { "nls" })
	private static final String[] PHP_END_WORDS = new String[] { "?>", "%>" };

	public FilterPHPTokenScanner()
	{
		IRule[] phpRules = new IRule[2 + fRules.length];
		// adds rules for finding the ruby start and end sequences
		WordRule wordRule = new WordRule(new PHPStartDetector(), Token.UNDEFINED);
		IToken token = createToken(PHP_START);
		for (String word : PHP_START_WORDS)
		{
			wordRule.addWord(word, token);
		}
		phpRules[0] = wordRule;
		wordRule = new WordRule(new PHPEndDetector(), Token.UNDEFINED);
		token = createToken(PHP_END);
		for (String word : PHP_END_WORDS)
		{
			wordRule.addWord(word, token);
		}
		phpRules[1] = wordRule;
		System.arraycopy(fRules, 0, phpRules, 2, fRules.length);
		setRules(phpRules);
	}

	private static final class PHPStartDetector implements IWordDetector
	{

		@Override
		public boolean isWordPart(char c)
		{
			switch (c)
			{
				case '<':
				case '?':
				case 'p':
				case 'h':
				case '=':
					return true;
			}
			return false;
		}

		@Override
		public boolean isWordStart(char c)
		{
			return c == '<';
		}
	}

	private static final class PHPEndDetector implements IWordDetector
	{

		@Override
		public boolean isWordPart(char c)
		{
			switch (c)
			{
				case '%':
				case '?':
				case '>':
					return true;
			}
			return false;
		}

		@Override
		public boolean isWordStart(char c)
		{
			return c == '?' || c == '%';
		}
	}
}
