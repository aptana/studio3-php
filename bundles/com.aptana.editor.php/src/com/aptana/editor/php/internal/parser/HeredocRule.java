/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * PHP Heredoc and Nowdoc rule.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 * @bugfixer Max Stepanov
 */
public class HeredocRule implements IPredicateRule
{

	private IToken token;
	private boolean isNowdoc;
	private int readCount;

	/**
	 * Constructs a new HeredocRule
	 */
	public HeredocRule(IToken token, boolean isNowdoc)
	{
		this.token = token;
		this.isNowdoc = isNowdoc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IPredicateRule#getSuccessToken()
	 */
	public IToken getSuccessToken()
	{
		return token;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IPredicateRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner, boolean)
	 */
	public IToken evaluate(ICharacterScanner scanner, boolean resume)
	{
		if (resume) {
			return Token.UNDEFINED;
		}
		return evaluate(scanner);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	public IToken evaluate(ICharacterScanner scanner)
	{
		readCount = 0;
		if (startsWith(scanner))
		{
			// read the heredoc/nowdoc identifier (name)
			String identifier = readHeredocIdentifier(scanner);
			if (identifier != null && identifier.length() > 0)
			{
				// look for an identifier match which starts at the beginning of a line.
				findHeredocClose(scanner, identifier);
			}
			return token;
		}
		for (; readCount > 0; --readCount)
		{
			scanner.unread();
		}

		return Token.UNDEFINED;
	}

	/**
	 * @param scanner
	 * @return
	 */
	private boolean startsWith(ICharacterScanner scanner)
	{
		for (int heredocOpenCount = 3; heredocOpenCount > 0; --heredocOpenCount)
		{
			int character = scanner.read();
			if (character == ICharacterScanner.EOF)
			{
				return false;
			}
			++readCount;
			if (character != '<')
			{
				return false;
			}
		}
		int character = scanner.read();
		if (character == ICharacterScanner.EOF)
		{
			return false;
		}
		scanner.unread();
		return (isNowdoc && character == '\'')
			|| (!isNowdoc && (Character.isLetter(character) || character == '"'));
	}

	/**
	 * Reads and returns the HEREDOC/NOWDOC identifier. This identifier should appear in the same line as the 'heredoc'
	 * open mark, and should not contain any white-spaces before the line terminates.
	 * 
	 * @param scanner
	 * @return A heredoc/nowdoc identifier; Null if something is wrong.
	 */
	private String readHeredocIdentifier(ICharacterScanner scanner)
	{
		StringBuilder buffer = new StringBuilder();
		int character;
		while ((character = scanner.read()) != ICharacterScanner.EOF) // $codepro.audit.disable assignmentInCondition
		{
			++readCount;
			if (isNewLine(scanner, character))
			{
				break;
			}
			buffer.append((char) character);
		}
		if (isNowdoc)
		{
			buffer.deleteCharAt(0);
			if (buffer.charAt(buffer.length()-1) == '\'')
			{		
				buffer.deleteCharAt(buffer.length()-1);
			} else
			{
				return null;
			}
		} else
		{
			if (buffer.charAt(0) == '"')
			{
				buffer.deleteCharAt(0);
				if (buffer.charAt(buffer.length()-1) == '"')
				{		
					buffer.deleteCharAt(buffer.length()-1);
				} else
				{
					return null;
				}
			}
		}
		for (char ch : buffer.toString().toCharArray())
		{
			if (!Character.isLetterOrDigit(ch) && character != '_')
			{
				return null;
			}
		}
		return buffer.toString();
	}

	/**
	 * Returns true if the
	 * 
	 * @param identifier
	 * @return
	 */
	private void findHeredocClose(ICharacterScanner scanner, String identifier)
	{
		StringBuilder buffer = new StringBuilder();
		int character;
		while ((character = scanner.read()) != ICharacterScanner.EOF) // $codepro.audit.disable assignmentInCondition
		{
			++readCount;
			if (isNewLine(scanner, character))
			{
				String line = buffer.toString();
				if (line.equals(identifier) || line.equals(identifier + ';'))
				{
					scanner.unread(); // unread newline character
					if (line.charAt(line.length()-1) == ';')
					{
						scanner.unread(); // unread semicolon
					}
					break;
				}
				buffer.setLength(0);
			} else 
			{
				buffer.append((char) character);
			}			
		}
		// We define that in case we have an illegal HEREDOC/NOWDOC, we grab to the end of the file.
	}
	
	private static boolean isNewLine(ICharacterScanner characterScanner, int c) {
		for (char[] sequence : characterScanner.getLegalLineDelimiters()) {
			if (c == sequence[0]) {
				return true;
			}
		}
		return false;
	}

}
