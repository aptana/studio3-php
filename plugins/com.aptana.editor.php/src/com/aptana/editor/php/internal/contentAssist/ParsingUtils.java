package com.aptana.editor.php.internal.contentAssist;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for parsing.
 * 
 * @author Denis Denisenko
 */
public final class ParsingUtils
{

	/**
	 * Gets dereferencing parts.
	 * 
	 * @param content
	 *            - content.
	 * @param possibleReferenceOperators
	 *            - possible reference operators.
	 * @param offset
	 *            - offset to start parsing from.
	 * @return list where the full dereferencing path is encoded or null In example for the dereferencing
	 *         "A::constField->method()->field2" there would be ["A", "::", "constField", "->", "method()", "->",
	 *         "field2"] The last entry may be an empty string if we are completing right after the reference operator.
	 */
	public static List<String> parseCallPath(String content, int offset, String[] possibleReferenceOperators)
	{
		return parseCallPath(content, offset, possibleReferenceOperators, false);
	}

	/**
	 * Gets dereferencing parts.
	 * 
	 * @param content
	 *            - content.
	 * @param possibleReferenceOperators
	 *            - possible reference operators.
	 * @param offset
	 *            - offset to start parsing from.
	 * @param skipInitialSpaces
	 *            - whether to skip initial spaces while parsing.
	 * @return list where the full dereferencing path is encoded or null In example for the dereferencing
	 *         "A::constField->method()->field2" there would be ["A", "::", "constField", "->", "method()", "->",
	 *         "field2"] The last entry may be an empty string if we are completing right after the reference operator.
	 */
	public static List<String> parseCallPath(String content, int offset, String[] possibleReferenceOperators,
			boolean skipInitialSpaces)
	{
		try
		{
			List<String> result = new ArrayList<String>();
			if (content.length() == 0)
			{
				result.add(""); //$NON-NLS-1$
				return result;
			}
			offset = fixOffset(content, offset);
			int currentPos = offset;
			// skipping initial white spaces
			if (skipInitialSpaces)
			{
				skipWhiteSpaces(offset, content);
			}

			// parsing right-most name
			String entry = parseFunctionCall(content, currentPos, false);
			if (entry == null)
			{
				entry = parseName(content, currentPos, false);
			}

			currentPos -= entry.length();
			result.add(entry);

			// parsing pairs [name, reference]
			while (true)
			{
				// allowing whites paces before the reference operator
				currentPos = skipWhiteSpaces(currentPos, content);

				// parsing for reference
				String operator = parseReferenceOperator(possibleReferenceOperators, currentPos, content);

				// if no reference found, returning the result
				if (operator == null)
				{
					return result;
				}
				currentPos -= operator.length();

				// allowing whites paces before the reference operator
				currentPos = skipWhiteSpaces(currentPos, content);

				// parsing for method call or identifier
				entry = parseFunctionCall(content, currentPos, false);
				if (entry == null)
				{
					entry = parseName(content, currentPos, false);
				}

				// if no entry found, returning null as call path is incomplete.
				if (entry == null || entry.length() == 0)
				{
					return null;
				}
				currentPos -= entry.length();

				// adding reference operator and the entry to the result.
				result.add(0, operator);
				result.add(0, entry);
			}
		}
		catch (Throwable th)
		{
			return null;
		}
	}

	/*
	 * Fix the offset by searching the closing bracket of a function call.
	 * @param content
	 * @param offset
	 * @return The fixed offset (the closing bracket offset); Or the original offset in case of an error or in case no
	 * fix was made.
	 */
	private static int fixOffset(String content, int offset)
	{
		int originalOffset = offset;
		char ch = content.charAt(offset);
		if (ch == '(')
		{
			// scan to the closing and continue
			int openings = 1;
			int start = offset + 1;
			int searchLimit = Math.min(start + 500, content.length());
			for (int c = start; c < searchLimit; c++)
			{
				offset++;
				ch = content.charAt(c);
				if (ch == '(')
					openings++;
				else if (ch == ')')
					openings--;
				if (openings == 0)
					break;
			}
			if (openings != 0)
				return originalOffset; // The function is not closed
		}
		return offset;
	}

	/**
	 * Checks if entry is a function call. Method can only be applied to {@link #parseCallPath(String, int, String[])}
	 * method result entries.
	 * 
	 * @param call
	 *            - possible call entry.
	 * @return true if function call, false otherwise.
	 */
	public static boolean isFunctionCall(String call)
	{
		return call.indexOf("(") >= 1; //$NON-NLS-1$
	}

	/**
	 * Gets function name from function call. Method can only be applied to
	 * {@link #parseCallPath(String, int, String[])} method result entries.
	 * 
	 * @param call
	 *            - call string.
	 * @return function name or null.
	 */
	public static String getFunctionNameFromCall(String call)
	{
		int openingBracketIndex = call.indexOf("("); //$NON-NLS-1$
		if (openingBracketIndex < 1)
		{
			return null;
		}

		return call.substring(0, openingBracketIndex);
	}

	/**
	 * Checks whether current content is an identifier.
	 * 
	 * @param content
	 *            - current content.
	 * @param offset
	 *            - current offset.
	 * @param skipInitialWhitespaces
	 *            - whether to skip initial white spaces.
	 * @return identifier or null
	 */
	private static String parseName(String content, int offset, boolean skipInitialWhitespaces)
	{
		if (content.length() == 0)
		{
			return null;
		}
		if (offset < 0)
		{
			return null;
		}

		StringBuffer name = new StringBuffer();

		int start = offset;
		if (skipInitialWhitespaces)
		{
			for (int i = start; i >= 0; i--)
			{
				char ch = content.charAt(i);
				if (!Character.isWhitespace(ch))
				{
					start = i;
					break;
				}
			}
		}

		for (int i = start; i >= 0; i--)
		{
			char ch = content.charAt(i);

			if (Character.isJavaIdentifierPart(ch))
			{
				name.insert(0, ch);
			}
			else if (ch == '\\')
			{
				name.insert(0, ch);
			}
			else if (ch == '$')
			{
				name.insert(0, ch);
				return name.toString();
			}
			else
			{
				return name.toString();
			}
		}

		return name.toString();
	}

	/**
	 * Parses function call from position.
	 * 
	 * @param content
	 *            - current content.
	 * @param offset
	 *            - current offset.
	 * @param skipInitialWhitespaces
	 *            - whether to skip initial white spaces.
	 * @return identifier or null
	 */
	private static String parseFunctionCall(String content, int offset, boolean skipInitialWhitespaces)
	{
		if (offset < 0)
		{
			return null;
		}

		StringBuffer call = new StringBuffer();

		int start = offset;
		if (skipInitialWhitespaces)
		{
			start = skipWhiteSpaces(start, content);
		}

		// initial state
		final int initialState = 0;
		// state indicating closing bracket is met
		final int insideMethodArguments = 1;
		// state indicating opening bracket is met
		final int argumentsEndMet = 2;

		int state = initialState;

		int level = 0;

		for (int i = start; i >= 0; i--)
		{
			char ch = content.charAt(i);

			switch (state)
			{
				case initialState:
					if (ch != ')')
					{
						return null;
					}
					call.insert(0, ch);
					state = insideMethodArguments;
					level++;
					break;
				case insideMethodArguments:
					if (ch == '(')
					{
						level--;
						call.insert(0, ch);
						if (level == 0)
						{
							state = argumentsEndMet;
						}
					}
					else if (ch == ')')
					{
						level++;
						call.insert(0, ch);
					}
					else
					{
						call.insert(0, ch);
					}
					break;
				case argumentsEndMet:
					if (Character.isJavaIdentifierPart(ch))
					{
						call.insert(0, ch);
					}
					else
					{
						if (isFunctionCall(call.toString()))
						{
							return call.toString();
						}
						else
						{
							return null;
						}
					}
					break;
			}
		}

		// if we haven't met the opening bracket, function call is not parsed
		if (state != argumentsEndMet)
		{
			return null;
		}

		// checking if we have one or more symbols before the opening bracket
		// (method name is not empty)
		if (!isFunctionCall(call.toString()))
		{
			return null;
		}

		return call.toString();
	}

	/**
	 * Parses references.
	 * 
	 * @param possibleOperators
	 *            - possible reference operators to parse.
	 * @param pos
	 *            - position.
	 * @param contents
	 *            - contents.
	 * @return parsed reference.
	 */
	private static String parseReferenceOperator(String[] possibleOperators, int pos, String contents)
	{
		for (String possibleReference : possibleOperators)
		{
			if (parseConstantString(possibleReference, pos, contents) != -1)
			{
				return possibleReference;
			}
		}

		return null;
	}

	/**
	 * Skips white spaces.
	 * 
	 * @param pos
	 *            - position to start from.
	 * @param contents
	 *            - contents.
	 * @return the position right before spaces.
	 */
	private static int skipWhiteSpaces(int pos, String contents)
	{
		if (pos < 0)
		{
			return pos;
		}

		for (int i = pos; i >= 0; i--)
		{
			if (!Character.isWhitespace(contents.charAt(i)))
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * Parses simple reference.
	 * 
	 * @param constant
	 *            - constant to parse
	 * @param start
	 *            - position to start parsing from.
	 * @param contents
	 *            - contents to parse.
	 * @return the offset right before the end of the constant or -1 if constant not found.
	 */
	private static int parseConstantString(String constant, int start, String contents)
	{
		if (start < constant.length() - 1)
		{
			return -1;
		}

		int posInConstant = constant.length() - 1;
		for (int i = start; i >= 0; i--)
		{
			if (posInConstant == -1)
			{
				return i;
			}

			char contentsChar = contents.charAt(i);
			char constantChar = constant.charAt(posInConstant);
			if (contentsChar != constantChar)
			{
				return -1;
			}

			posInConstant--;
		}

		return -1;
	}

	/**
	 * ParsingUtils parsing constructor.
	 */
	private ParsingUtils()
	{
	}
}
