/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.php.internal.text.rules;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import com.aptana.editor.common.text.rules.QueuedTokenScanner;
import com.aptana.editor.epl.BufferedDocumentScanner;
import com.aptana.editor.php.internal.parser.PHPTokenType;
import com.aptana.editor.php.internal.ui.editor.scanner.tokenMap.PHPTokenMapperFactory;

/**
 * @author Max Stepanov
 * 
 */
public class FastPHPStringTokenScanner extends QueuedTokenScanner {

	private static final IToken TOKEN_BEGIN_QUOTE = getToken(PHPTokenType.PUNCTUATION_STRING_BEGIN);
	private static final IToken TOKEN_END_QUOTE = getToken(PHPTokenType.PUNCTUATION_STRING_END);
	private static final IToken TOKEN_ESCAPE_CHARACTER = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.CHARACTER_ESCAPE);
	private static final IToken TOKEN_VARIABLE_OTHER = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.VARIABLE_OTHER);
	private static final IToken TOKEN_VARIABLE_GLOBAL = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.VARIABLE_OTHER_GLOBAL);
	private static final IToken TOKEN_VARIABLE_OTHER_PUNCTUATION = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.VARIABLE_OTHER, PHPTokenType.VARIABLE_PUNCTUATION);
	private static final IToken TOKEN_VARIABLE_GLOBAL_PUNCTUATION = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.VARIABLE_OTHER_GLOBAL, PHPTokenType.VARIABLE_PUNCTUATION);
	private static final IToken TOKEN_NUMERIC = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.NUMERIC);
	private static final IToken TOKEN_CLASS_OPERATOR = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.CLASS_OPERATOR);
	private static final IToken TOKEN_ARRAY_BEGIN = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.PUNCTUATION_LBRACKET);
	private static final IToken TOKEN_ARRAY_END = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.PUNCTUATION_RBRACKET);
	private static final IToken TOKEN_VARIABLE_PUNCTUATION = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.VARIABLE_PUNCTUATION);
	private static final IToken TOKEN_FUNCTION_PUNCTUATION = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.FUNCTION_PUNCTUATION);
	private static final IToken TOKEN_STATIC_PUNCTUATION = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.STATIC_PUNCTUATION);
	private static final IToken TOKEN_SINGLE_QUOTED = getToken(PHPTokenType.STRING_SINGLE);
	private static final IToken TOKEN_BEGIN_QUOTE_INNER = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.VARIABLE_OTHER, PHPTokenType.STRING_DOUBLE, PHPTokenType.PUNCTUATION_STRING_BEGIN);
	private static final IToken TOKEN_END_QUOTE_INNER = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.VARIABLE_OTHER, PHPTokenType.STRING_DOUBLE,  PHPTokenType.PUNCTUATION_STRING_END);
	private static final IToken TOKEN_DOUBLE_QUOTED_INNER = getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE, PHPTokenType.VARIABLE_OTHER, PHPTokenType.STRING_DOUBLE, PHPTokenType.META_STRING_CONTENTS_DOUBLE);

	private final IToken fDefaultToken;
	private final BufferedDocumentScanner fScanner = new BufferedDocumentScanner(100);

	/**
	 * 
	 */
	public FastPHPStringTokenScanner(IToken defaultToken) {
		fDefaultToken = defaultToken;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(org.eclipse.jface .text.IDocument, int, int)
	 */
	public void setRange(IDocument document, int offset, int length) {
		super.setRange(document, offset, length);
		fScanner.setRange(document, offset, length);
		if (fScanner.read() == '"') {
			queueToken(TOKEN_BEGIN_QUOTE, fScanner.getOffset()-1, 1);
		} else {
			fScanner.unread();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
	 */
	public IToken nextToken() {
		IToken token = super.nextToken();
		if (!token.isEOF()) {
			return token;
		}
		resumeTokenScan();
		return super.nextToken();
	}

	private void resumeTokenScan() {
		int startOffset = fScanner.getOffset();
		int ch = fScanner.read();
		switch (ch) {
			case '\\':
				readEscape(startOffset);
				break;
			case '$':
				fScanner.unread();
				readSimpleVariable(startOffset);
				break;
			case '{':
				ch = fScanner.read();
				if (ch == '$') {
					fScanner.unread();
					readComplexVariable(startOffset);
				} else if (ch != ICharacterScanner.EOF) {
					fScanner.unread();
					readDefault(startOffset);
				}
				break;
			case '"':
				if (fScanner.read() == ICharacterScanner.EOF) {
					queueToken(TOKEN_END_QUOTE, startOffset, 1);
				} else {
					fScanner.unread();
					readDefault(startOffset);
				}
				break;
			case ICharacterScanner.EOF:
				break;
			default:
				readDefault(startOffset);
				break;
		}
	}

	private void readDefault(int offset) {
		int ch = fScanner.read();
		while (ch != '\\' && ch != '$' && ch != '{' && ch != '"' && ch != ICharacterScanner.EOF) {
			ch = fScanner.read();
		}
		if (ch != ICharacterScanner.EOF) {
			fScanner.unread();
		}
		queueToken(fDefaultToken, offset, fScanner.getOffset() - offset);
	}

	private void readEscape(int offset) {
		int ch = fScanner.read();
		int count = 2, radix = 8;
		switch (ch) {
		case 'f':
		case 'v':
		case 't':
		case 'r':
		case 'n':
		case '\\':
		case '"':
		case '$':
			break;
		case 'x':
			ch = fScanner.read();
			count = 1;
			radix = 16;
		default: // $codepro.audit.disable nonTerminatedCaseClause
			while (Character.digit(ch, radix) >= 0 && count > 0) {
				ch = fScanner.read();
				--count;
			}
			if (Character.digit(ch, radix) < 0 && ch != ICharacterScanner.EOF) {
				fScanner.unread();
			}
			break;
		}
		queueToken(TOKEN_ESCAPE_CHARACTER, offset, fScanner.getOffset() - offset);
	}

	private void readSimpleVariable(int offset) {
		int ch = fScanner.read();
		int unread = 0;
		if (ch == '$') {
			ch = fScanner.read();
			if (ch != ICharacterScanner.EOF) {
				++unread;
			}
		}
		if (Character.isLetter(ch) || ch == '_') {
			StringBuilder name = new StringBuilder();
			name.append('$').append((char)ch);
			ch = fScanner.read();
			while (Character.isLetterOrDigit(ch) || ch == '_') {
				name.append((char)ch);
				ch = fScanner.read();
			}
			if (ch != ICharacterScanner.EOF) {
				fScanner.unread();
			}
			IToken token = PHPTokenMapperFactory.GLOBALS.contains(name.toString()) ? TOKEN_VARIABLE_GLOBAL : TOKEN_VARIABLE_OTHER;
			if (unread != 0) {
				queueToken(token == TOKEN_VARIABLE_GLOBAL ? TOKEN_VARIABLE_GLOBAL_PUNCTUATION : TOKEN_VARIABLE_OTHER_PUNCTUATION, offset, 1);
				++offset;
			}
			queueToken(token, offset, fScanner.getOffset() - offset);
			readVariableOperator(fScanner.getOffset());
			return;
		} else if (ch == '{') { // we have ${
			queueToken(TOKEN_VARIABLE_PUNCTUATION, offset, fScanner.getOffset() - offset);
			readLiteral(fScanner.getOffset());
			readDefaultUntil('}', TOKEN_VARIABLE_PUNCTUATION, fScanner.getOffset());
			return;
		}
		while (unread-- > 0) {
			fScanner.unread();
		}
		readDefault(offset);
	}

	private void readComplexVariable(int offset) {
		queueToken(TOKEN_VARIABLE_PUNCTUATION, offset, fScanner.getOffset() - offset);
		offset = fScanner.getOffset();
		Assert.isTrue(fScanner.read() == '$');
		int ch = fScanner.read();
		if (Character.isLetter(ch) || ch == '_') {
			StringBuilder name = new StringBuilder();
			name.append('$').append((char)ch);
			ch = fScanner.read();
			while (Character.isLetterOrDigit(ch) || ch == '_') {
				name.append((char)ch);
				ch = fScanner.read();
			}
			if (ch != ICharacterScanner.EOF) {
				fScanner.unread();
			}
			// FIXME We really need to delegate to PHPCodeScanner to properly get the correct tokens here...
			IToken token = PHPTokenMapperFactory.GLOBALS.contains(name.toString()) ? TOKEN_VARIABLE_GLOBAL : TOKEN_VARIABLE_OTHER;
			queueToken(token == TOKEN_VARIABLE_GLOBAL ? TOKEN_VARIABLE_GLOBAL_PUNCTUATION : TOKEN_VARIABLE_OTHER_PUNCTUATION, offset, 1);
			queueToken(token, offset + 1, fScanner.getOffset() - offset - 1);
			readVariableOperator(fScanner.getOffset());
		} else if (ch == '{') { // we have ${
			queueToken(TOKEN_VARIABLE_PUNCTUATION, offset, fScanner.getOffset() - offset);
			readLiteral(fScanner.getOffset());
			readDefaultUntil('}', TOKEN_VARIABLE_PUNCTUATION, fScanner.getOffset());
		}
		readDefaultUntil('}', TOKEN_VARIABLE_PUNCTUATION, fScanner.getOffset());
	}

	private void readVariableOperator(int offset) {
		int unread = 1;
		int ch = fScanner.read();
		if (ch == '-') {
			ch = fScanner.read();
			++unread;
			if (ch == '>') {
				queueToken(TOKEN_CLASS_OPERATOR, offset, fScanner.getOffset() - offset);
				readSimpleVariable(fScanner.getOffset());
				readVariableOperator(fScanner.getOffset());
				unread = 0;
			}
		} else if (ch == '[') {
			queueToken(TOKEN_ARRAY_BEGIN, offset, fScanner.getOffset() - offset);
			offset = fScanner.getOffset();
			ch = fScanner.read();
			if (ch == '$') {
				readSimpleVariable(offset);
			} else if (Character.isDigit(ch)) {
				readNumeric(offset);
			} else if (Character.isLetter(ch)) {
				readLiteral(offset);
			} else if (ch == '\'') {
				readSingleQuotedString(offset);
			} else if (ch == '"') {
				readDoubleQuotedString(offset);
			}
			offset = fScanner.getOffset();
			ch = fScanner.read();
			if (ch == ']') {
				queueToken(TOKEN_ARRAY_END, offset, fScanner.getOffset() - offset);
				readVariableOperator(fScanner.getOffset());
				unread = 0;
			}
		} else if (ch == '(' || ch == ')') {
			queueToken(TOKEN_FUNCTION_PUNCTUATION, offset, fScanner.getOffset() - offset);
			readVariableOperator(fScanner.getOffset());
			readDefaultUntil(')', TOKEN_FUNCTION_PUNCTUATION, fScanner.getOffset());
			unread = 0;
		} else if (ch == ':') {
			ch = fScanner.read();
			++unread;
			if (ch == ':') {
				queueToken(TOKEN_STATIC_PUNCTUATION, offset, fScanner.getOffset() - offset);
				readSimpleVariable(fScanner.getOffset());
				unread = 0;
			}
		}
		if (unread == 0) {
			readVariableOperator(fScanner.getOffset());
		}
		while (ch != ICharacterScanner.EOF && unread-- > 0) {
			fScanner.unread();
		}
	}

	private void readDefaultUntil(char target, IToken token, int offset) {
		int ch = fScanner.read();
		int unread = 1;
		while (ch != target && ch != ICharacterScanner.EOF) {
			ch = fScanner.read();
			++unread;
		}
		if (ch == target) {
			int length = fScanner.getOffset() - offset - 1;
			if (length > 0) {
				queueToken(fDefaultToken, offset, length);
			}
			offset = fScanner.getOffset() - 1;
			queueToken(token, offset, fScanner.getOffset() - offset);
		} else {
			if (ch == ICharacterScanner.EOF) {
				--unread;
			}
			while (unread-- > 0) {
				fScanner.unread();
			}
		}
	}

	private void readNumeric(int offset) {
		int ch = fScanner.read();
		while (Character.isDigit(ch)) {
			ch = fScanner.read();
		}
		if (ch != ICharacterScanner.EOF) {
			fScanner.unread();
		}
		queueToken(TOKEN_NUMERIC, offset, fScanner.getOffset() - offset);
	}

	private void readLiteral(int offset) {
		int ch = fScanner.read();
		while (Character.isLetterOrDigit(ch) || ch == '_') {
			ch = fScanner.read();
		}
		if (ch != ICharacterScanner.EOF) {
			fScanner.unread();
		}
		if (ch == '$') {
			readSimpleVariable(offset);
		} else {
			queueToken(TOKEN_VARIABLE_OTHER, offset, fScanner.getOffset() - offset);
			readVariableOperator(fScanner.getOffset());
		}
	}

	private void readSingleQuotedString(int offset) {
		int ch = fScanner.read();
		while (ch != '\'' && ch != ICharacterScanner.EOF) {
			ch = fScanner.read();
		}
		queueToken(TOKEN_SINGLE_QUOTED, offset, fScanner.getOffset() - offset);
	}

	private void readDoubleQuotedString(int offset) {
		int firstOffset = offset;
		int firstLength = fScanner.getOffset() - offset;
		offset = fScanner.getOffset();
		int ch = fScanner.read();
		while (ch != '"' && ch != ICharacterScanner.EOF) {
			ch = fScanner.read();
		}
		if (ch == ICharacterScanner.EOF) {
			return;
		}
		queueToken(TOKEN_BEGIN_QUOTE_INNER, firstOffset, firstLength);
		queueToken(TOKEN_DOUBLE_QUOTED_INNER, offset, fScanner.getOffset() - 1 - offset);
		queueToken(TOKEN_END_QUOTE_INNER, fScanner.getOffset() - 1, 1);
	}

	private static IToken getToken(PHPTokenType... type) {
		StringBuilder sb = new StringBuilder();
		for (PHPTokenType i : type) {
			sb.append(i.toString()).append(' ');
		}
		return new Token(sb.toString().trim());
	}

}
