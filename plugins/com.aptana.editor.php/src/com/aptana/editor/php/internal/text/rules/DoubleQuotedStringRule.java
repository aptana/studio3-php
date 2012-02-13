/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.php.internal.text.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;

/**
 * @author Max Stepanov
 *
 */
public class DoubleQuotedStringRule extends MultiLineRule {

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
		int openBrackets = 0;
		while ((c = scanner.read()) != ICharacterScanner.EOF) {
			if (c == fEscapeCharacter) {
				scanner.read();
			} else if (openBrackets > 0) {
				if (c == '{') {
					++openBrackets;
				} else if (c == '}') {
					--openBrackets;
				}
			} else if (c == '{') {
				if ((c = scanner.read()) == '$') {
					openBrackets = 1;
				} else if (c != ICharacterScanner.EOF) {
					scanner.unread();
				}
			} else if ((c == fEndSequence[0] && sequenceDetected(scanner, fEndSequence, fBreaksOnEOF))) {
				break;
			}
		}
		return true;
	}
}
