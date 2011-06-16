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

	private static final Pattern CONSTANT_PATTERN = Pattern.compile("[A-Z_][\\dA-Z_]*");

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

	private static Set<String> BUILTIN_FUNCTIONS = new HashSet<String>();
	static
	{
		BUILTIN_FUNCTIONS.add("class_exists");
		BUILTIN_FUNCTIONS.add("debug_backtrace");
		BUILTIN_FUNCTIONS.add("defined");
		BUILTIN_FUNCTIONS.add("function_exists");
		BUILTIN_FUNCTIONS.add("func_get_args");
		BUILTIN_FUNCTIONS.add("func_num_args");
		BUILTIN_FUNCTIONS.add("get_object_vars");
		BUILTIN_FUNCTIONS.add("strlen");
	}

	private static Set<String> STRING_OPERATORS = new HashSet<String>();
	static
	{
		STRING_OPERATORS.add(".");
		STRING_OPERATORS.add("dirname");
		STRING_OPERATORS.add("explode");
		STRING_OPERATORS.add("implode");
		STRING_OPERATORS.add("stripslashes");
		STRING_OPERATORS.add("strpos");
		STRING_OPERATORS.add("strrpos");
		STRING_OPERATORS.add("strtolower");
		STRING_OPERATORS.add("strtoupper");
		STRING_OPERATORS.add("str_replace");
		STRING_OPERATORS.add("substr");
		STRING_OPERATORS.add("trim");
	}

	private static Set<String> ARRAY_FUNCTIONS = new HashSet<String>();
	static
	{
		ARRAY_FUNCTIONS.add("array_key_exists");
		ARRAY_FUNCTIONS.add("array_map");
		ARRAY_FUNCTIONS.add("array_merge");
		ARRAY_FUNCTIONS.add("array_shift");
		ARRAY_FUNCTIONS.add("array_values");
		ARRAY_FUNCTIONS.add("arsort");
		ARRAY_FUNCTIONS.add("asort");
		ARRAY_FUNCTIONS.add("count");
		ARRAY_FUNCTIONS.add("in_array");
	}

	private static Set<String> TYPE_FUNCTIONS = new HashSet<String>();
	static
	{
		TYPE_FUNCTIONS.add("is_array");
		TYPE_FUNCTIONS.add("is_numeric");
		TYPE_FUNCTIONS.add("is_object");
		TYPE_FUNCTIONS.add("is_string");
	}

	private static Set<String> FILE_FUNCTIONS = new HashSet<String>();
	static
	{
		FILE_FUNCTIONS.add("fclose");
		FILE_FUNCTIONS.add("file_get_contents");
		FILE_FUNCTIONS.add("file_put_contents");
		FILE_FUNCTIONS.add("fopen");
		FILE_FUNCTIONS.add("fwrite");
		FILE_FUNCTIONS.add("unlink");
	}

	private static Set<String> FILESTAT_FUNCTIONS = new HashSet<String>();
	static
	{
		FILESTAT_FUNCTIONS.add("filemtime");
		FILESTAT_FUNCTIONS.add("file_exists");
		FILESTAT_FUNCTIONS.add("is_dir");
		FILESTAT_FUNCTIONS.add("is_file");
		FILESTAT_FUNCTIONS.add("is_writable");
	}

	private static Set<String> BASIC_FUNCTIONS = new HashSet<String>();
	static
	{
		BASIC_FUNCTIONS.add("getenv");
		BASIC_FUNCTIONS.add("ini_get");
		BASIC_FUNCTIONS.add("print_r");
	}

	private static Set<String> SUPPORT_CONSTANTS = new HashSet<String>();
	static
	{
		SUPPORT_CONSTANTS.add("DEFAULT_INCLUDE_PATH");
		SUPPORT_CONSTANTS.add("PHP_VERSION");
		SUPPORT_CONSTANTS.add("PEAR_EXTENSION_DIR");
		SUPPORT_CONSTANTS.add("PEAR_INSTALL_DIR");
		SUPPORT_CONSTANTS.add("PHP_SYSCONFDIR");
		SUPPORT_CONSTANTS.add("PHP_OUTPUT_HANDLER_START");
		SUPPORT_CONSTANTS.add("PHP_OUTPUT_HANDLER_END");
		SUPPORT_CONSTANTS.add("PHP_OUTPUT_HANDLER_CONT");
		SUPPORT_CONSTANTS.add("PHP_OS");
		SUPPORT_CONSTANTS.add("PHP_BINDIR");
		SUPPORT_CONSTANTS.add("PHP_CONFIG_FILE_PATH");
		SUPPORT_CONSTANTS.add("PHP_DATADIR");
		SUPPORT_CONSTANTS.add("PHP_EOL");
		SUPPORT_CONSTANTS.add("PHP_EXTENSION_DIR");
		SUPPORT_CONSTANTS.add("PHP_LIBDIR");
		SUPPORT_CONSTANTS.add("PHP_LOCALSTATEDIR");
		SUPPORT_CONSTANTS.add("E_ALL");
		SUPPORT_CONSTANTS.add("E_COMPILE_ERROR");
		SUPPORT_CONSTANTS.add("E_COMPILE_WARNING");
		SUPPORT_CONSTANTS.add("E_CORE_ERROR");
		SUPPORT_CONSTANTS.add("E_CORE_WARNING");
		SUPPORT_CONSTANTS.add("E_RECOVERABLE_ERROR");
		SUPPORT_CONSTANTS.add("E_ERROR");
		SUPPORT_CONSTANTS.add("E_NOTICE");
		SUPPORT_CONSTANTS.add("E_PARSE");
		SUPPORT_CONSTANTS.add("E_STRICT");
		SUPPORT_CONSTANTS.add("E_USER_ERROR");
		SUPPORT_CONSTANTS.add("E_USER_NOTICE");
		SUPPORT_CONSTANTS.add("E_USER_WARNING");
		SUPPORT_CONSTANTS.add("E_USER_DEPRECATED");
		SUPPORT_CONSTANTS.add("E_WARNING");
		SUPPORT_CONSTANTS.add("E_DEPRECATED");
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
			return scanner.getToken("punctuation.terminator.expression.php");
		}
		// Operators
		if (ASSIGNMENTS.contains(tokenContent))
		{
			return scanner.getToken("keyword.operator.assignment.php");
		}
		if (LOGICAL_OPERATORS.contains(tokenContent))
		{
			return scanner.getToken("keyword.operator.logical.php");
		}
		if (COMPARISON_OPERATORS.contains(tokenContent))
		{
			return scanner.getToken("keyword.operator.comparison.php");
		}
		if (ARITHMETIC_OPERATORS.contains(tokenContent))
		{
			return scanner.getToken("keyword.operator.arithmetic.php");
		}
		if (BITWISE_OPERATORS.contains(tokenContent))
		{
			return scanner.getToken("keyword.operator.bitwise.php");
		}
		if (STRING_OPERATORS.contains(tokenContent))
		{
			return scanner.getToken("keyword.operator.string.php");
		}
		if (INC_DEC_OPERATORS.contains(tokenContent))
		{
			return scanner.getToken("keyword.operator.increment-decrement.php");
		}
		// Support functions
		if (BUILTIN_FUNCTIONS.contains(tokenContent))
		{
			return scanner.getToken("support.function.builtin_functions.php");
		}
		if (FILE_FUNCTIONS.contains(tokenContent))
		{
			return scanner.getToken("support.function.file.php");
		}
		if (FILESTAT_FUNCTIONS.contains(tokenContent))
		{
			return scanner.getToken("support.function.filestat.php");
		}
		if (BASIC_FUNCTIONS.contains(tokenContent))
		{
			return scanner.getToken("support.function.basic_functions.php");
		}
		if (ARRAY_FUNCTIONS.contains(tokenContent))
		{
			return scanner.getToken("support.function.array.php");
		}
		if (TYPE_FUNCTIONS.contains(tokenContent))
		{
			return scanner.getToken("support.function.type.php");
		}
		if ("version_compare".equals(tokenContent))
		{
			return scanner.getToken("support.function.versioning.php");
		}
		if ("microtime".equals(tokenContent))
		{
			return scanner.getToken("support.function.microtime.php");
		}
		if ("glob".equals(tokenContent))
		{
			return scanner.getToken("support.function.dir.php");
		}
		if ("time".equals(tokenContent) || "strtotime".equals(tokenContent))
		{
			return scanner.getToken("support.function.php_date.php");
		}
		if ("preg_replace".equals(tokenContent))
		{
			return scanner.getToken("support.function.php_pcre.php");
		}
		if ("htmlspecialchars".equals(tokenContent))
		{
			return scanner.getToken("support.function.html.php");
		}
		if ("urlencode".equals(tokenContent))
		{
			return scanner.getToken("support.function.url.php");
		}
		if ("http_build_query".equals(tokenContent))
		{
			return scanner.getToken("support.function.http.php");
		}
		// Support constants
		if (SUPPORT_CONSTANTS.contains(tokenContent))
		{
			return scanner.getToken("support.constant.core.php");
		}
		// All uppercase is constant
		if (CONSTANT_PATTERN.matcher(tokenContent).matches())
		{
			return scanner.getToken("constant.other.php");
		}
		return scanner.getToken(""); //$NON-NLS-1$
	}
}
