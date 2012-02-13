/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;

import com.aptana.editor.common.text.rules.CharacterMapRule;
import com.aptana.editor.php.internal.parser.PHPTokenType;

/**
 * Keep state to toggle scope for beginning and end quotes.
 * 
 * @author cwilliams
 */
final class SingleQuotedStringScanner extends RuleBasedScanner
{
	/**
	 * The scopes used inside single quoted strings.
	 */
	private static final IToken BEGIN_QUOTE = PHPSourceConfiguration.getToken(PHPTokenType.PUNCTUATION_STRING_BEGIN);
	private static final IToken CONTENTS = PHPSourceConfiguration.getToken(PHPTokenType.META_STRING_CONTENTS_SINGLE);
	private static final IToken END_QUOTE = PHPSourceConfiguration.getToken(PHPTokenType.PUNCTUATION_STRING_END);

	private boolean firstQuote;

	SingleQuotedStringScanner()
	{
		firstQuote = true;
		setDefaultReturnToken(CONTENTS);
		CharacterMapRule rule = new CharacterMapRule();
		rule.add('\'', BEGIN_QUOTE);
		setRules(new IRule[] { rule });
	}

	@Override
	public IToken nextToken()
	{
		IToken token = super.nextToken();
		if (token.isOther() && PHPTokenType.PUNCTUATION_STRING_BEGIN.toString().equals(token.getData()))
		{
			if (firstQuote)
			{
				firstQuote = !firstQuote;
			}
			else
			{
				token = END_QUOTE;
			}
		}
		return token;
	}

	@Override
	public void setRange(IDocument document, int offset, int length)
	{
		super.setRange(document, offset, length);
		firstQuote = true;
	}
}