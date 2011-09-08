/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.php.internal.text.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

/**
 * @author Max Stepanov
 *
 */
public class DoubleQuotedStringRule extends MultiLineRule {

	private static final IPredicateRule COMPLEX_VARIABLE_RULE = new SingleLineRule("{$", "}", new Token("CVT"), '\\'); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * @param token
	 */
	public DoubleQuotedStringRule(IToken token) {
		super("\"", "\"", token, '\\', true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.PatternRule#endSequenceDetected(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	@Override
	protected boolean endSequenceDetected(ICharacterScanner scanner) {
		int c;
		while ((c = scanner.read()) != ICharacterScanner.EOF) {
			if (c == '{') {
				scanner.unread();
				if (COMPLEX_VARIABLE_RULE.evaluate(scanner).isUndefined()) {
					scanner.read();
				}
			} else if ((c == fEndSequence[0] && sequenceDetected(scanner, fEndSequence, fBreaksOnEOF))) {
				break;
			} else if (c == fEscapeCharacter) {
				scanner.read();
			}
		}
		return true;
	}
}
