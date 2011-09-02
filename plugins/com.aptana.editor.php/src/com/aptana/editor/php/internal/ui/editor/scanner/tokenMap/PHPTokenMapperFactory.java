/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.editor.scanner.tokenMap;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import java_cup.runtime.Symbol;

import org.eclipse.jface.text.rules.IToken;
import org2.eclipse.php.internal.core.PHPVersion;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.php.internal.parser.PHPTokenType;
import com.aptana.editor.php.internal.ui.editor.scanner.PHPCodeScanner;

/**
 * A PHP token mapper factory that returns the right {@link IPHPTokenMapper} according to the given {@link PHPVersion}.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 * @author cwilliams
 */
@SuppressWarnings("nls")
public class PHPTokenMapperFactory
{
	private static PHP4TokenMapper php4TokenMapper;
	private static PHP5TokenMapper php5TokenMapper;
	private static PHP53TokenMapper php53TokenMapper;

	private static final Pattern CONSTANT_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

	private static Set<String> ASSIGNMENTS = new HashSet<String>();
	static
	{
		ASSIGNMENTS.add("=");
		ASSIGNMENTS.add("|=");
		ASSIGNMENTS.add("&=");
		ASSIGNMENTS.add("^=");
		ASSIGNMENTS.add("%=");
		ASSIGNMENTS.add("/=");
		ASSIGNMENTS.add("*=");
	}
	private static Set<String> LOGICAL_OPERATORS = new HashSet<String>();
	static
	{
		LOGICAL_OPERATORS.add("!");
		LOGICAL_OPERATORS.add("&&");
		LOGICAL_OPERATORS.add("||");
		LOGICAL_OPERATORS.add("^");
		LOGICAL_OPERATORS.add("and");
		LOGICAL_OPERATORS.add("or");
		LOGICAL_OPERATORS.add("xor");
		LOGICAL_OPERATORS.add("as");
	}

	private static Set<String> INC_DEC_OPERATORS = new HashSet<String>();
	static
	{
		INC_DEC_OPERATORS.add("--");
		INC_DEC_OPERATORS.add("++");
	}

	private static Set<String> ARITHMETIC_OPERATORS = new HashSet<String>();
	static
	{
		ARITHMETIC_OPERATORS.add("-");
		ARITHMETIC_OPERATORS.add("+");
		ARITHMETIC_OPERATORS.add("*");
		ARITHMETIC_OPERATORS.add("/");
		ARITHMETIC_OPERATORS.add("%");
	}

	private static Set<String> BITWISE_OPERATORS = new HashSet<String>();
	static
	{
		BITWISE_OPERATORS.add("<<");
		BITWISE_OPERATORS.add(">>");
		BITWISE_OPERATORS.add("~");
		BITWISE_OPERATORS.add("^");
		BITWISE_OPERATORS.add("&");
		BITWISE_OPERATORS.add("|");
	}

	private static Set<String> COMPARISON_OPERATORS = new HashSet<String>();
	static
	{
		COMPARISON_OPERATORS.add("<");
		COMPARISON_OPERATORS.add("==");
		COMPARISON_OPERATORS.add("===");
		COMPARISON_OPERATORS.add("!==");
		COMPARISON_OPERATORS.add("!=");
		COMPARISON_OPERATORS.add("<=");
		COMPARISON_OPERATORS.add(">=");
		COMPARISON_OPERATORS.add("<>");
		COMPARISON_OPERATORS.add(">");
	}

	/**
	 * Returns the {@link IPHPTokenMapper} that match the given {@link PHPVersion}
	 * 
	 * @param phpVersion
	 * @return An {@link IPHPTokenMapper}
	 * @throws IllegalArgumentException
	 *             In case the PHP version is unknown.
	 */
	public static IPHPTokenMapper getMapper(PHPVersion phpVersion)
	{
		switch (phpVersion)
		{
			case PHP4:
				if (php4TokenMapper == null)
				{
					php4TokenMapper = new PHP4TokenMapper();
				}
				return php4TokenMapper;
			case PHP5:
				if (php5TokenMapper == null)
				{
					php5TokenMapper = new PHP5TokenMapper();
				}
				return php5TokenMapper;
			case PHP5_3:
				if (php53TokenMapper == null)
				{
					php53TokenMapper = new PHP53TokenMapper();
				}
				return php53TokenMapper;

		}
		throw new IllegalArgumentException("Unknown PHP version " + phpVersion.getAlias()); //$NON-NLS-1$
	}

	/**
	 * Common handling of fall-through cases for scopes across versions of PHP token mappers.
	 * 
	 * @param scanner
	 * @param sym
	 * @return
	 */
	public static IToken mapDefaultToken(PHPCodeScanner scanner, Symbol sym)
	{
		String tokenContent = scanner.getSymbolValue(sym);
		if (";".equals(tokenContent))
		{
			return scanner.getToken(PHPTokenType.PUNCTUATION_TERMINATOR.toString());
		}
		if ("(".equals(tokenContent))
		{
			return scanner.getToken(PHPTokenType.PUNCTUATION_PARAM_LEFT.toString());
		}
		if (")".equals(tokenContent))
		{
			return scanner.getToken(PHPTokenType.PUNCTUATION_PARAM_RIGHT.toString());
		}
		if ("[".equals(tokenContent))
		{
			return scanner.getToken(PHPTokenType.PUNCTUATION_LBRACKET.toString());
		}
		if ("]".equals(tokenContent))
		{
			return scanner.getToken(PHPTokenType.PUNCTUATION_RBRACKET.toString());
		}
		// Operators
		if (ASSIGNMENTS.contains(tokenContent))
		{
			return scanner.getToken(PHPTokenType.KEYWORD_OP_ASSIGN.toString());
		}
		if (LOGICAL_OPERATORS.contains(tokenContent))
		{
			return scanner.getToken(PHPTokenType.KEYWORD_OP_LOGICAL.toString());
		}
		if (COMPARISON_OPERATORS.contains(tokenContent))
		{
			return scanner.getToken(PHPTokenType.KEYWORD_OP_COMPARISON.toString());
		}
		if (ARITHMETIC_OPERATORS.contains(tokenContent))
		{
			return scanner.getToken(PHPTokenType.KEYWORD_OP_ARITHMETIC.toString());
		}
		if (BITWISE_OPERATORS.contains(tokenContent))
		{
			return scanner.getToken(PHPTokenType.KEYWORD_OP_BITWISE.toString());
		}
		if (INC_DEC_OPERATORS.contains(tokenContent))
		{
			return scanner.getToken(PHPTokenType.KEYWORD_OP_INC_DEC.toString());
		}
		// All uppercase is constant
		if (CONSTANT_PATTERN.matcher(tokenContent).matches())
		{
			return scanner.getToken(PHPTokenType.CONSTANT_OTHER.toString());
		}

		return scanner.getToken(StringUtil.EMPTY);
	}
}
