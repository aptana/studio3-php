/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation Jens
 * Lukowski/Innoopract - initial renaming/restructuring
 *
 *******************************************************************************/
package org2.eclipse.php.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

@SuppressWarnings({"restriction", "unchecked", "rawtypes"})
public class StringUtils {
    protected static final String AMPERSTAND = "&"; //$NON-NLS-1$

    protected static final String AMPERSTAND_ENTITY = "&&;"; //$NON-NLS-1$

    protected static final String CARRIAGE_RETURN = "\r"; //$NON-NLS-1$

    protected static final String CARRIAGE_RETURN_ENTITY = "\\r"; //$NON-NLS-1$

    protected static final String CR = "\r"; //$NON-NLS-1$

    protected static final String CRLF = "\r\n"; //$NON-NLS-1$

    protected static final String DELIMITERS = " \t\n\r\f"; //$NON-NLS-1$

    protected static final String DOUBLE_QUOTE = "\""; //$NON-NLS-1$

    protected static final char DOUBLE_QUOTE_CHAR = '\"';

    protected static final String DOUBLE_QUOTE_ENTITY = "&quot;"; //$NON-NLS-1$

    protected static final String EQUAL_SIGN = "="; //$NON-NLS-1$

    protected static final String EQUAL_SIGN_ENTITY = "&#61;"; //$NON-NLS-1$

    private static final String FALSE = "false"; //$NON-NLS-1$

    protected static final String GREATER_THAN = ">"; //$NON-NLS-1$

    protected static final String GREATER_THAN_ENTITY = "&gt;"; //$NON-NLS-1$

    protected static final String LESS_THAN = "<"; //$NON-NLS-1$

    protected static final String LESS_THAN_ENTITY = "&lt;"; //$NON-NLS-1$

    protected static final String LF = "\n"; //$NON-NLS-1$

    protected static final String LINE_FEED = "\n"; //$NON-NLS-1$

    protected static final String LINE_FEED_ENTITY = "\\n"; //$NON-NLS-1$

    protected static final String LINE_FEED_TAG = "<dl>"; //$NON-NLS-1$

    protected static final String LINE_TAB = "\t"; //$NON-NLS-1$

    protected static final String LINE_TAB_ENTITY = "\\t"; //$NON-NLS-1$

    protected static final String LINE_TAB_TAG = "<dd>"; //$NON-NLS-1$

    protected static final String SINGLE_QUOTE = "'"; //$NON-NLS-1$

    protected static final char SINGLE_QUOTE_CHAR = '\'';

    protected static final String SINGLE_QUOTE_ENTITY = "&#039;"; //$NON-NLS-1$

    protected static final String SPACE = " "; //$NON-NLS-1$

    protected static final String SPACE_ENTITY = "&nbsp;"; //$NON-NLS-1$

    private static final String TRUE = "true"; //$NON-NLS-1$

    /**
     * Append appendString to the end of aString only if aString does not end
     * with the insertString.
     */
    public static String appendIfNotEndWith(final String aString, final String appendString) {
        if (aString != null && appendString != null)
            if (aString.endsWith(appendString))
                return aString;
            else
                return aString + appendString;
        else
            return aString;
    }

    /**
     * Breaks out space-separated words into an array of words. For example:
     * <code>"no comment"</code> into an array <code>a[0]="no"</code> and
     * <code>a[1]= "comment"</code>.
     *
     * @param value
     *            the string to be converted
     * @return the list of words
     */
    public static String[] asArray(final String value) {
        ArrayList list = new ArrayList();
        StringTokenizer stok = new StringTokenizer(value);
        while (stok.hasMoreTokens()) {
            list.add(stok.nextToken());
        }
        String result[] = new String[list.size()];
        list.toArray(result);
        return result;
    }

    /**
     * Breaks out delim-separated words into an array of words. For example:
     * <code>"no comment"</code> into an array <code>a[0]="no"</code> and
     * <code>a[1]= "comment"</code>.
     *
     * @param value
     *            the string to be converted
     * @return the list of words
     */
    public static String[] asArray(final String value, final String delim) {
        return asArray(value, delim, false);
    }

    /**
     * Breaks out delim-separated words into an array of words. For example:
     * <code>"no comment"</code> into an array <code>a[0]="no"</code> and
     * <code>a[1]= "comment"</code>.
     *
     * @param value
     *            the string to be converted
     * @return the list of words
     */
    public static String[] asArray(final String value, final String delim, final boolean returnTokens) {
        ArrayList list = new ArrayList();
        StringTokenizer stok = new StringTokenizer(value, delim, returnTokens);
        while (stok.hasMoreTokens()) {
            list.add(stok.nextToken());
        }
        String result[] = new String[list.size()];
        list.toArray(result);
        return result;
    }

    /**
     * Breaks out delim-separated words into an array of words. For example:
     * <code>"abc,,def"</code> into an array <code>a[0]="abc"</code>,
     * <code>a[1]=null</code>, and <code>a[2]= "def"</code> where "," is the
     * delim.
     *
     * @param value
     *            the string to be converted
     * @return the list of words
     */
    public static String[] asFixedArray(final String value, final String delim) {
        String array[] = asArray(value, delim, true);
        int arrayLength = array.length;
        boolean stringFound = false;
        ArrayList list = new ArrayList();

        for (int i = 0; i < arrayLength; i++) {
            String token = array[i];
            if (token.compareTo(delim) == 0) {
                if (!stringFound) {
                    list.add(null);
                }
                stringFound = false;
            } else {
                list.add(token);
                stringFound = true;
            }
        }
        // add one more null if last token is the delim
        if (!stringFound) {
            list.add(null);
        }

        String result[] = new String[list.size()];
        list.toArray(result);
        return result;
    }

    public static String chop(final String source) {
        return chop(source, "/"); //$NON-NLS-1$
    }

    public static String chop(final String source, final String delimiter) {
        return source.substring(0, source.lastIndexOf(delimiter));
    }

    public static boolean contains(final String[] arrayOfStrings, final String needle, final boolean caseSensitive) {
        boolean result = false;
        if (needle == null)
            return false;
        if (arrayOfStrings == null)
            return false;

        if (caseSensitive) {
            for (String arrayOfString : arrayOfStrings) {
                if (needle.equals(arrayOfString)) {
                    result = true;
                    break;
                }
            }
        } else {
            for (String arrayOfString : arrayOfStrings) {
                if (needle.equalsIgnoreCase(arrayOfString)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public static boolean containsLetters(final String fullValue) {

        if (fullValue == null || fullValue.length() == 0)
            return false;

        char[] chars = fullValue.toCharArray();
        for (int i = 0; i < fullValue.length(); i++)
            if (Character.isLetter(chars[i]))
                return true;

        return false;
    }

    public static boolean containsLineDelimiter(final String aString) {
        return indexOfLineDelimiter(aString) != -1;
    }

    public static String convertLineDelimiters(final String allText, String lineDelimiterToUse)
            throws BadLocationException {
        IDocument tempDoc = new Document(allText);

        if (lineDelimiterToUse == null) {
            lineDelimiterToUse = System.getProperty("line.separator"); //$NON-NLS-1$
        }

        String newText = ""; //$NON-NLS-1$
        int lineCount = tempDoc.getNumberOfLines();
        for (int i = 0; i < lineCount; i++) {
            org.eclipse.jface.text.IRegion lineInfo = tempDoc.getLineInformation(i);
            int lineStartOffset = lineInfo.getOffset();
            int lineLength = lineInfo.getLength();
            int lineEndOffset = lineStartOffset + lineLength;
            newText += allText.substring(lineStartOffset, lineEndOffset);

            if (i < lineCount - 1 && tempDoc.getLineDelimiter(i) != null) {
                newText += lineDelimiterToUse;
            }
        }

        return newText;
    }

    /**
     * Replaces all instances of special HTML characters with the appropriate
     * HTML entity equivalent. WARNING only use this method for strings that
     * dont already have HTML-specific items such as tags and entities.
     *
     * @param String
     *            content String to convert
     *
     * @return String the converted string
     * @see HTMLPrinter#convertToHTMLContent(String content)
     */
    public static String convertToHTMLContent(String content) {
        content = replace(content, AMPERSTAND, AMPERSTAND_ENTITY);
        content = replace(content, LESS_THAN, LESS_THAN_ENTITY);
        content = replace(content, GREATER_THAN, GREATER_THAN_ENTITY);
        content = replace(content, LINE_FEED, LINE_FEED_TAG);
        content = replace(content, LINE_TAB, LINE_TAB_TAG);
        content = replace(content, SINGLE_QUOTE, SINGLE_QUOTE_ENTITY);
        content = replace(content, DOUBLE_QUOTE, DOUBLE_QUOTE_ENTITY);
        content = replace(content, SPACE + SPACE, SPACE_ENTITY + SPACE_ENTITY); // replacing
        // every
        // space
        // would
        // be
        // too
        // much
        return content;
    }

    /**
     * Converts a string into a form that will not conflict with saving it into
     * an INI file.
     */
    public static String escape(final String normalString) {
        if (normalString == null)
            return null;
        StringBuffer escapedBuffer = new StringBuffer();
        StringTokenizer toker = new StringTokenizer(normalString, EQUAL_SIGN + LINE_FEED + CARRIAGE_RETURN + LINE_TAB,
                true);
        String chunk = null;
        while (toker.hasMoreTokens()) {
            chunk = toker.nextToken();
            if (chunk.equals(EQUAL_SIGN)) {
                escapedBuffer.append(EQUAL_SIGN_ENTITY);
            } else if (chunk.equals(LINE_FEED)) {
                escapedBuffer.append(LINE_FEED_ENTITY);
            } else if (chunk.equals(CARRIAGE_RETURN)) {
                escapedBuffer.append(CARRIAGE_RETURN_ENTITY);
            } else if (chunk.equals(LINE_TAB)) {
                escapedBuffer.append(LINE_TAB_ENTITY);
            } else {
                escapedBuffer.append(chunk);
            }
        }
        return escapedBuffer.toString();
    }

    /**
     * Returns the first line of the given text without a trailing delimiter.
     *
     * @param text
     * @return
     */
    public static String firstLineOf(final String text) {
        if (text == null || text.length() < 1)
            return text;
        IDocument doc = new Document(text);
        try {
            int lineNumber = doc.getLineOfOffset(0);
            IRegion line = doc.getLineInformation(lineNumber);
            return doc.get(line.getOffset(), line.getLength());
        } catch (BadLocationException e) {
            // do nothing
        }
        return text;
    }

    public static int indexOfLastLineDelimiter(final String aString) {
        return indexOfLastLineDelimiter(aString, aString.length());
    }

    public static int indexOfLastLineDelimiter(final String aString, final int offset) {
        int index = -1;

        if (aString != null && aString.length() > 0) {
            index = aString.lastIndexOf(CRLF, offset);
            if (index == -1) {
                index = aString.lastIndexOf(CR, offset);
                if (index == -1) {
                    index = aString.lastIndexOf(LF, offset);
                }
            }
        }

        return index;
    }

    public static int indexOfLineDelimiter(final String aString) {
        return indexOfLineDelimiter(aString, 0);
    }

    public static int indexOfLineDelimiter(final String aString, final int offset) {
        int index = -1;

        if (aString != null && aString.length() > 0) {
            index = aString.indexOf(CRLF, offset);
            if (index == -1) {
                index = aString.indexOf(CR, offset);
                if (index == -1) {
                    index = aString.indexOf(LF, offset);
                }
            }
        }

        return index;
    }

    public static int indexOfNonblank(final String aString) {
        return indexOfNonblank(aString, 0);
    }

    public static int indexOfNonblank(final String aString, final int offset) {
        int index = -1;

        if (aString != null && aString.length() > 0) {
            for (int i = offset; i < aString.length(); i++) {
                if (DELIMITERS.indexOf(aString.substring(i, i + 1)) == -1) {
                    index = i;
                    break;
                }
            }
        }

        return index;
    }

    /**
     * Insert insertString to the beginning of aString only if aString does not
     * start with the insertString.
     */
    public static String insertIfNotStartWith(final String aString, final String insertString) {
        if (aString != null && insertString != null)
            if (aString.startsWith(insertString))
                return aString;
            else
                return insertString + aString;
        else
            return aString;
    }

    public static boolean isQuoted(final String string) {
        if (string == null || string.length() < 2)
            return false;

        int lastIndex = string.length() - 1;
        char firstChar = string.charAt(0);
        char lastChar = string.charAt(lastIndex);

        return firstChar == SINGLE_QUOTE_CHAR && lastChar == SINGLE_QUOTE_CHAR || firstChar == DOUBLE_QUOTE_CHAR
                && lastChar == DOUBLE_QUOTE_CHAR;
    }

    /**
     * Unit tests.
     *
     * @param args
     *            java.lang.String[]
     */
    public static void main(final String[] args) {
        // testPaste();
        testStripNonLetterDigits();
    }

    /*
     * Returns the merged form of both strings
     */
    public static String merge(final String newStart, final String newEnd) {
        String[] regions = overlapRegions(newStart, newEnd);
        return regions[0] + regions[1] + regions[2];
    }

    public static int occurrencesOf(final String searchString, final char targetChar) {
        int result = 0;
        int len = searchString.length();
        for (int i = 0; i < len; i++) {
            if (targetChar == searchString.charAt(i)) {
                result++;
            }
        }
        return result;
    }

    /**
     *
     * @return java.lang.String[]
     * @param start
     *            java.lang.String
     * @param end
     *            java.lang.String
     *
     *            Returns a 3 String array containing unique text from the
     *            start, duplicated text that overlaps the start and end, and
     *            the unique text from the end.
     */
    private static String[] overlapRegions(final String start, final String end) {
        String[] results = null;
        if (start != null && end == null) {
            results = new String[] { start, "", "" }; //$NON-NLS-2$//$NON-NLS-1$
        } else if (start == null && end != null) {
            results = new String[] { "", "", end }; //$NON-NLS-2$//$NON-NLS-1$
        } else if (start == null && end == null) {
            results = new String[] { "", "", "" }; //$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
        } else if (start != null && end != null) {

            int startLength = start.length();
            int endLength = end.length();

            if (startLength == 0 || endLength == 0) {
                results = new String[] { "", "", "" }; //$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
            } else {
                results = new String[3];
                String testStart = ""; //$NON-NLS-1$
                String testEnd = ""; //$NON-NLS-1$
                int mergeLength = Math.min(startLength, endLength);
                boolean finished = false;
                while (mergeLength > 0 && !finished) {
                    testStart = start.substring(startLength - mergeLength);
                    testEnd = end.substring(0, mergeLength);
                    // case sensitive
                    if (testStart.equals(testEnd)) {
                        finished = true;
                        results[0] = start.substring(0, startLength - mergeLength);
                        results[1] = start.substring(startLength - mergeLength);
                        results[2] = end.substring(mergeLength);
                    }
                    mergeLength--;
                }
                if (!finished) {
                    results[0] = start;
                    results[1] = ""; //$NON-NLS-1$
                    results[2] = end;
                }
            }
        }
        return results;
    }

    /**
     * Packs an array of Strings into a single comma delimited String.
     *
     * @param strings
     * @return
     * @todo Generated comment
     */
    public static String pack(final String[] strings) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            buf.append(StringUtils.replace(strings[i], ",", "&comma;")); //$NON-NLS-1$ //$NON-NLS-2$
            if (i < strings.length - 1) {
                buf.append(","); //$NON-NLS-1$
            }
        }
        return buf.toString();
    }

    /*
     * Pastes the new text into the old at the start position, replacing text
     * implied by length.
     */
    public static String paste(final String oldText, final String newText, final int start, final int length) {
        String result = null;
        StringBuffer sb = new StringBuffer();
        int startIndex = start;
        int endIndex = start + length;
        if (startIndex > oldText.length()) {
            startIndex = oldText.length();
        }
        sb.append(oldText.substring(0, startIndex));
        // null or empty new text accompliches a delete
        if (newText != null) {
            sb.append(newText);
        }
        if (endIndex < oldText.length()) {

            sb.append(oldText.substring(endIndex));
        }
        result = sb.toString();
        return result;
    }

    /**
     * Replace matching literal portions of a string with another string
     */
    public static String replace(final String aString, final String source, final String target) {
        if (aString == null)
            return null;
        String normalString = ""; //$NON-NLS-1$
        int length = aString.length();
        int position = 0;
        int previous = 0;
        int spacer = source.length();
        while (position + spacer - 1 < length && aString.indexOf(source, position) > -1) {
            position = aString.indexOf(source, previous);
            normalString = normalString + aString.substring(previous, position) + target;
            position += spacer;
            previous = position;
        }
        normalString = normalString + aString.substring(position, aString.length());

        return normalString;
    }

    /**
     * Restore the entity references for markup delimiters in text where they
     * have been replaced by the proper Unicode values through a DOM text
     * parser.
     */
    public static String restoreMarkers(final String text) {
        String content = text;
        content = replace(content, AMPERSTAND, AMPERSTAND_ENTITY);
        content = replace(content, LESS_THAN, LESS_THAN_ENTITY);
        content = replace(content, GREATER_THAN, GREATER_THAN_ENTITY);
        return content;
    }

    /**
     * Removes extra whitespace characters and quotes
     */
    public static String strip(final String quotedString) {
        if (quotedString == null || quotedString.length() == 0)
            return quotedString;
        String trimmed = quotedString.trim();
        if (trimmed.length() < 2)
            return quotedString;

        char first = trimmed.charAt(0);
        char nextToLast = trimmed.charAt(trimmed.length() - 2);
        char last = trimmed.charAt(trimmed.length() - 1);

        if (first == '\"' && last == '\"' && nextToLast != '\\' || first == '\'' && last == '\'' && nextToLast != '\\')
            return trimmed.substring(1, trimmed.length() - 1);
        return trimmed;
    }

    /**
     * This method strips anything from the beginning and end of a string that
     * is not a letter or digit. It is used by some encoding detectors to come
     * up with the encoding name from illformed input (e.g in <?xml
     * encoding="abc?> -- where final quote is left off, the '>' is returned
     * with the rest of the attribute value 'abc').
     */
    public static String stripNonLetterDigits(final String fullValue) {
        if (fullValue == null || fullValue.length() == 0)
            return fullValue;
        int fullValueLength = fullValue.length();
        int firstPos = 0;
        while (firstPos < fullValueLength && !Character.isLetterOrDigit(fullValue.charAt(firstPos))) {
            firstPos++;
        }
        int lastPos = fullValueLength - 1;
        while (lastPos > firstPos && !Character.isLetterOrDigit(fullValue.charAt(lastPos))) {
            lastPos--;
        }
        String result = fullValue;
        if (firstPos != 0 || lastPos != fullValueLength) {
            result = fullValue.substring(firstPos, lastPos + 1);
        }
        return result;
    }

    /**
     * Similar to strip, except quotes don't need to match such as "UTF' is
     * still stripped of both quotes. (Plus, this one does not detect escaped
     * quotes)
     */
    public static String stripQuotes(final String quotedValue) {
        if (quotedValue == null)
            return null;
        // normally will never have leading or trailing blanks,
        // but if it does, we'll do lenient interpretation
        return stripQuotesLeaveInsideSpace(quotedValue).trim();
    }

    /**
     * Like strip quotes, except leaves the start and end space inside the
     * quotes.
     *
     * @param quotedValue
     * @return
     */
    public static String stripQuotesLeaveInsideSpace(final String quotedValue) {
        if (quotedValue == null)
            return null;
        // nomally will never have leading or trailing blanks ... but just in
        // case.
        String result = quotedValue.trim();
        int len = result.length();
        if (len > 0) {
            char firstChar = result.charAt(0);
            if (firstChar == SINGLE_QUOTE_CHAR || firstChar == DOUBLE_QUOTE_CHAR) {
                result = result.substring(1, len);
            }
            len = result.length();
            if (len > 0) {
                char lastChar = result.charAt(len - 1);
                if (lastChar == SINGLE_QUOTE_CHAR || lastChar == DOUBLE_QUOTE_CHAR) {
                    result = result.substring(0, len - 1);
                }
            }
        }
        return result;
    }

    public static void testPaste() {
        String testString = "The quick brown fox ..."; //$NON-NLS-1$
        System.out.println(paste(testString, null, 4, 5));
        System.out.println(paste(testString, null, 4, 6));
        System.out.println(paste(testString, "", 4, 6)); //$NON-NLS-1$
        System.out.println(paste(testString, "fast", 4, 6)); //$NON-NLS-1$
        System.out.println(paste(testString, "fast ", 4, 6)); //$NON-NLS-1$
        System.out.println(paste(testString, "But ", 0, 0)); //$NON-NLS-1$
        System.out.println(paste("", "burp", 4, 6)); //$NON-NLS-2$//$NON-NLS-1$
    }

    public static void testStripNonLetterDigits() {
        String testString = "abc"; //$NON-NLS-1$
        System.out.println(testString + " -->" + stripNonLetterDigits(testString) + "<--"); //$NON-NLS-1$ //$NON-NLS-2$
        testString = ""; //$NON-NLS-1$
        System.out.println(testString + " -->" + stripNonLetterDigits(testString) + "<--"); //$NON-NLS-1$ //$NON-NLS-2$
        testString = "\"abc\""; //$NON-NLS-1$
        System.out.println(testString + " -->" + stripNonLetterDigits(testString) + "<--"); //$NON-NLS-1$ //$NON-NLS-2$
        testString = "\"ab-c1?"; //$NON-NLS-1$
        System.out.println(testString + " -->" + stripNonLetterDigits(testString) + "<--"); //$NON-NLS-1$ //$NON-NLS-2$
        testString = "+++"; //$NON-NLS-1$
        System.out.println(testString + " -->" + stripNonLetterDigits(testString) + "<--"); //$NON-NLS-1$ //$NON-NLS-2$
        testString = "abc="; //$NON-NLS-1$
        System.out.println(testString + " -->" + stripNonLetterDigits(testString) + "<--"); //$NON-NLS-1$ //$NON-NLS-2$
        testString = "abc "; //$NON-NLS-1$
        System.out.println(testString + " -->" + stripNonLetterDigits(testString) + "<--"); //$NON-NLS-1$ //$NON-NLS-2$

    }

    public static String toString(final boolean booleanValue) {
        if (booleanValue)
            return TRUE;
        else
            return FALSE;
    }

    /**
     * Remove "escaped" chars from a string.
     */
    public static String unescape(final String aString) {
        if (aString == null)
            return null;
        String normalString = replace(aString, EQUAL_SIGN_ENTITY, EQUAL_SIGN);
        normalString = replace(normalString, LINE_FEED_ENTITY, LINE_FEED);
        normalString = replace(normalString, CARRIAGE_RETURN_ENTITY, CARRIAGE_RETURN);
        normalString = replace(normalString, LINE_TAB_ENTITY, LINE_TAB);
        return normalString;
    }

    public static String uniqueEndOf(final String newStart, final String newEnd) {
        String[] regions = overlapRegions(newStart, newEnd);
        return regions[2];
    }

    /**
     * Unpacks a comma delimited String into an array of Strings.
     *
     * @param s
     * @return
     * @todo Generated comment
     */
    public static String[] unpack(final String s) {
        if (s == null)
            return new String[0];
        StringTokenizer toker = new StringTokenizer(s, ","); //$NON-NLS-1$
        List list = new ArrayList();
        while (toker.hasMoreTokens()) {
            // since we're separating the values with ',', escape ',' in the
            // values
            list.add(StringUtils.replace(toker.nextToken(), "&comma;", ",").trim()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return (String[]) list.toArray(new String[0]);
    }

    /**
     * StringUtils constructor comment.
     */
    private StringUtils() {
        super();
    }

	public static String join(String joinStr, String[] strings)
	{
		StringBuilder builder = new StringBuilder();
		for (String str : strings)
		{
			builder.append(str);
			builder.append(joinStr);
		}
		if (builder.length() > 0)
		{
			int end = builder.length() - 1;
			builder.delete(end - joinStr.length(), end);
		}
		return builder.toString();
	}

}
