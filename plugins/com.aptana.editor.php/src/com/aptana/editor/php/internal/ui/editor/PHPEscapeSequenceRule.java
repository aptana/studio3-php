/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.editor;

import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;

import com.aptana.editor.common.text.rules.ExtendedWordRule;

public class PHPEscapeSequenceRule extends ExtendedWordRule
{
	private static final String REGEXP = "\\\\(x[0-9a-fA-F]{2}|[0-7]{1,3}|\"|\\$|\\\\|[fvtrn])"; //$NON-NLS-1$
	private static Pattern pattern;

	/**
	 * getPattern
	 * 
	 * @return
	 */
	private synchronized static Pattern getPattern()
	{
		if (pattern == null)
		{
			pattern = Pattern.compile(REGEXP);
		}
		return pattern;
	}

	/**
	 * PHPEscapeSequenceRule
	 * 
	 * @param token
	 */
	public PHPEscapeSequenceRule(IToken token)
	{
		super(new PHPEscapeSequenceDetector(), token, false);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.text.rules.ExtendedWordRule#wordOK(java.lang.String,
	 * org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	@Override
	protected boolean wordOK(String word, ICharacterScanner scanner)
	{
		return getPattern().matcher(word).matches();
	}
}