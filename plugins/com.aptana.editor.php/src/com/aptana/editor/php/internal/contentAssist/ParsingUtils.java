/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.contentAssist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TypedRegion;
import org2.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.contentassist.ILexemeProvider;
import com.aptana.editor.common.contentassist.LexemeProvider;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.parsing.lexer.Range;

/**
 * Utilities for parsing.
 * 
 * @author Denis Denisenko
 */
public final class ParsingUtils
{
	/**
	 * Create a {@link LexemeProvider} for the given partition that is at the offset of the given document.
	 * 
	 * @param document
	 * @param offset
	 * @return A new {@link LexemeProvider} for the partition on that offset.
	 */
	public static ILexemeProvider<PHPTokenType> createLexemeProvider(IDocument document, int offset)
	{
		if (offset == document.getLength() && offset > 0)
		{
			offset--;
		}
		return new LexemeProvider<PHPTokenType>(document, offset, new PHPScopeScanner())
		{
			@Override
			protected PHPTokenType getTypeFromData(Object data)
			{
				if (data != null)
				{
					return new PHPTokenType(data.toString());
				}
				else
				{
					return new PHPTokenType(PHPRegionTypes.UNKNOWN_TOKEN);
				}
			}
		};
	}

	/**
	 * Create a {@link LexemeProvider} for the entire given document.
	 * 
	 * @param document
	 * @return A new {@link LexemeProvider} for the document.
	 */
	public static ILexemeProvider<PHPTokenType> createLexemeProvider(IDocument document)
	{
		return new LexemeProvider<PHPTokenType>(document, new Range(0, document.getLength() - 1), new PHPScopeScanner())
		{
			@Override
			protected PHPTokenType getTypeFromData(Object data)
			{
				if (data != null)
				{
					return new PHPTokenType(data.toString());
				}
				else
				{
					return new PHPTokenType(PHPRegionTypes.UNKNOWN_TOKEN);
				}
			}
		};
	}

	/**
	 * Create a {@link LexemeProvider} for the given range in the document.
	 * 
	 * @param document
	 * @param start
	 *            - start offset
	 * @param end
	 *            - end offset
	 * @return A new {@link LexemeProvider} for the document.
	 */
	public static ILexemeProvider<PHPTokenType> createLexemeProvider(IDocument document, int start, int end)
	{
		if (end == document.getLength() && end > 0)
		{
			end--;
		}
		return new LexemeProvider<PHPTokenType>(document, new Range(start, end), new PHPScopeScanner())
		{
			@Override
			protected PHPTokenType getTypeFromData(Object data)
			{
				if (data != null)
				{
					return new PHPTokenType(data.toString());
				}
				else
				{
					return new PHPTokenType(PHPRegionTypes.UNKNOWN_TOKEN);
				}
			}
		};
	}

	/**
	 * Gets dereferencing parts.
	 * 
	 * @param region
	 *            - An {@link ITypedRegion} that will bound the call path search to the region itself (can be null)
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
	public static List<String> parseCallPath(ITypedRegion region, String content, int offset,
			String[] possibleReferenceOperators)
	{
		return parseCallPath(region, content, offset, possibleReferenceOperators, false, null);
	}

	/**
	 * Gets dereferencing parts.
	 * 
	 * @param region
	 *            - An {@link ITypedRegion} that will bound the call path search to the region itself (can be null)
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
	public static List<String> parseCallPath(ITypedRegion region, String content, int offset,
			String[] possibleReferenceOperators, boolean skipInitialSpaces, IDocument document)
	{
		try
		{
			// Try to collect more PHP regions that exist before the given region. This resolve a situation where we get
			// PHP String regions that breaks the default regions into a few parts.
			if (region != null && document != null)
			{
				try
				{
					ITypedRegion prevRegion = null;
					int rOffset = region.getOffset() - 1;
					int totalLength = region.getLength();
					while (rOffset > 0)
					{
						ITypedRegion partition = document.getPartition(rOffset);
						if (!partition.getType().startsWith(IPHPConstants.PREFIX))
						{
							break;
						}
						else
						{
							prevRegion = partition;
							rOffset = prevRegion.getOffset() - 1;
							totalLength += prevRegion.getLength();
						}
					}
					if (prevRegion != null)
					{
						// Wrap all regions as one
						region = new TypedRegion(prevRegion.getOffset(), totalLength, region.getType());
					}
				}
				catch (BadLocationException e)
				{
					IdeLog.logError(PHPEditorPlugin.getDefault(), "Error while parsing the call path", e); //$NON-NLS-1$
				}
			}

			List<String> result = new ArrayList<String>();
			if (content.length() == 0)
			{
				result.add(StringUtil.EMPTY);
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
			String entry = parseFunctionCall(region, content, currentPos, false);
			if (entry == null)
			{
				entry = parseName(region, content, currentPos, false);
			}

			currentPos -= entry.length();
			result.add(entry);

			// parsing pairs [name, reference]
			while (true)
			{
				// allowing whites paces before the reference operator
				currentPos = skipWhiteSpaces(currentPos, content);

				// parsing for reference
				String operator = parseReferenceOperator(region, possibleReferenceOperators, currentPos, content);

				// if no reference found, returning the result
				if (operator == null)
				{
					return result;
				}
				currentPos -= operator.length();

				// allowing whites paces before the reference operator
				currentPos = skipWhiteSpaces(currentPos, content);

				// parsing for method call or identifier
				entry = parseFunctionCall(region, content, currentPos, false);
				if (entry == null)
				{
					entry = parseName(region, content, currentPos, false);
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
				{
					openings++;
				}
				else if (ch == ')')
				{
					openings--;
				}
				if (openings == 0)
				{
					break;
				}
			}
			if (openings != 0)
			{
				return originalOffset; // The function is not closed
			}
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
		return call.indexOf('(') >= 1;
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
		int openingBracketIndex = call.indexOf('(');
		if (openingBracketIndex < 1)
		{
			return null;
		}

		return call.substring(0, openingBracketIndex);
	}

	/**
	 * Checks whether current content is an identifier.
	 * 
	 * @param region
	 *            An {@link ITypedRegion} that can be used to limit the search range (can be null)
	 * @param content
	 *            - current content.
	 * @param offset
	 *            - current offset.
	 * @param skipInitialWhitespaces
	 *            - whether to skip initial white spaces.
	 * @return identifier or null
	 */
	private static String parseName(ITypedRegion region, String content, int offset, boolean skipInitialWhitespaces)
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
		int partitionStart = (region == null) ? 0 : region.getOffset();
		if (skipInitialWhitespaces)
		{
			for (int i = start; i >= partitionStart; i--)
			{
				char ch = content.charAt(i);
				if (!Character.isWhitespace(ch))
				{
					start = i;
					break;
				}
			}
		}

		for (int i = start; i >= partitionStart; i--)
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
	 * @param region
	 * @param content
	 *            - current content.
	 * @param offset
	 *            - current offset.
	 * @param skipInitialWhitespaces
	 *            - whether to skip initial white spaces.
	 * @return identifier or null
	 */
	private static String parseFunctionCall(ITypedRegion region, String content, int offset,
			boolean skipInitialWhitespaces)
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
		int partitionStart = (region == null) ? 0 : region.getOffset();
		for (int i = start; i >= partitionStart; i--)
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
	 * @param region
	 *            An {@link ITypedRegion} that will bound the reference search to the region itself (can be null)
	 * @param possibleOperators
	 *            - possible reference operators to parse.
	 * @param pos
	 *            - position.
	 * @param contents
	 *            - contents.
	 * @return parsed reference.
	 */
	private static String parseReferenceOperator(ITypedRegion region, String[] possibleOperators, int pos,
			String contents)
	{
		for (String possibleReference : possibleOperators)
		{
			if (parseConstantString(region, possibleReference, pos, contents) != -1)
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
	 * @param region
	 *            A region (partition) that bounds the search for the reference. The search will not go left to the
	 *            region's start offset (passing a null region will not limit the scan).
	 * @param constant
	 *            - constant to parse
	 * @param start
	 *            - position to start parsing from.
	 * @param contents
	 *            - contents to parse.
	 * @return the offset right before the end of the constant or -1 if constant not found.
	 */
	private static int parseConstantString(ITypedRegion region, String constant, int start, String contents)
	{
		if (start < constant.length() - 1)
		{
			return -1;
		}
		int partitionStart = (region == null) ? 0 : region.getOffset();
		int posInConstant = constant.length() - 1;
		for (int i = start; i >= partitionStart; i--)
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
