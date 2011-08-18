/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
// $codepro.audit.disable platformSpecificLineSeparator
package com.aptana.editor.php.util;

public class StringUtils
{

	/**
	 * BULLET
	 */
	public static final String BULLET = "\u2022 "; //$NON-NLS-1$

	/**
	 * EMPTY
	 */
	public static final String EMPTY = ""; //$NON-NLS-1$

	/**
	 * LINE_DELIMITER
	 */
	public static final String LINE_DELIMITER = System.getProperty("line.separator", "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Removes any leading or trailing [] on the string.
	 * 
	 * @param stringToTrim
	 *            The string to trim
	 * @return String
	 */
	public static String trimBrackets(String stringToTrim)
	{

		if (stringToTrim == null)
		{
			return null;
		}

		String trimmed = stringToTrim.trim();

		if (trimmed.startsWith("[")) //$NON-NLS-1$
		{
			trimmed = trimmed.substring(1);
		}

		if (trimmed.endsWith("]")) //$NON-NLS-1$
		{
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}

		return trimmed;
	}

	/**
	 * Removes the HTML, extra whitespace and carriage returns from a string
	 * 
	 * @param text
	 *            The text to strip
	 * @return The new text, reformatted
	 */
	public static String formatAsPlainText(String text)
	{
		String tempText = StringUtils.stripCarriageReturns(text);
		tempText = StringUtils.stripWhitespace(tempText);
		tempText = StringUtils.replace(tempText, "</li>", StringUtils.EMPTY); //$NON-NLS-1$
		tempText = StringUtils.replace(tempText, "<li>", LINE_DELIMITER + "\t" + BULLET); //$NON-NLS-1$ //$NON-NLS-2$
		tempText = StringUtils.replace(tempText, "<p>", LINE_DELIMITER); //$NON-NLS-1$
		tempText = StringUtils.stripHTML(tempText);
		return tempText.trim();
	}

	/**
	 * Strips HTML tags from text
	 * 
	 * @param text
	 *            Text to strip
	 * @return the text, minus any tags
	 */
	public static String stripHTML(String text)
	{
		if (text == null)
		{
			return null;
		}

		String tempText = text.replaceAll("<p>", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return tempText.replaceAll("\\<.*?\\>", StringUtils.EMPTY); //$NON-NLS-1$
	}

	/**
	 * Replace one string with another
	 * 
	 * @param str
	 * @param pattern
	 * @param replace
	 * @return String
	 */
	public static String replace(String str, String pattern, String replace)
	{

		int s = 0;
		int e = 0;
		StringBuffer result = new StringBuffer();

		while ((e = str.indexOf(pattern, s)) >= 0) // $codepro.audit.disable assignmentInCondition
		{
			result.append(str.substring(s, e));
			result.append(replace);
			s = e + pattern.length();
		}
		result.append(str.substring(s));
		return result.toString();

	}

	/**
	 * Removes all extra whitespace (multiple spaces or tabs) from a string
	 * 
	 * @param text
	 *            The string to strip of '\n'
	 * @return The string minus the whitespace
	 */
	public static String stripWhitespace(String text)
	{
		if (text == null)
		{
			return null;
		}

		return text.replaceAll("\\s+", " "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Removes all carriage returns from a string
	 * 
	 * @param text
	 *            The string to strip of '\n'
	 * @return The string minus the carriage returns
	 */
	public static String stripCarriageReturns(String text)
	{
		if (text == null)
		{
			return null;
		}

		return text.replaceAll("\n", StringUtils.EMPTY); //$NON-NLS-1$
	}

	/**
	 * Trim a start and end quotes from a given string.<br>
	 * We trim only if the string's length is larger then one, and the string has a matching quotes of either <code>'</code> or
	 * <code>"</code>.
	 * 
	 * @param iniIncludes
	 * @return
	 */
	public static String trimStringQuotes(String str)
	{
		if (str == null || str.length() < 2)
		{
			return str;
		}
		int stringStart = 0;
		int end = str.length() - 1;
		char startChar = str.charAt(stringStart);
		char endChar = str.charAt(end);
		if (startChar == endChar && startChar == '\'' || startChar == '\"')
		{
			return str.substring(1, end - 1);
		}
		return str;
	}

}
