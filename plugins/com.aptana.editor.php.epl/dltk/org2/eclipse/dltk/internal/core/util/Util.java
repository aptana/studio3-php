/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org2.eclipse.dltk.internal.core.util;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UTFDataFormatException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org2.eclipse.dltk.compiler.CharOperation;

import com.aptana.editor.php.epl.PHPEplPlugin;

/**
 * This code is taken and trimmed from the DLTK project.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class Util
{
	private static final int DEFAULT_READING_SIZE = 8192;

	public interface Displayable
	{
		String displayString(Object o);
	}

	public interface Comparer
	{
		/**
		 * Returns 0 if a and b are equal, >0 if a is greater than b, or <0 if a is less than b.
		 */
		int compare(Object a, Object b);
	}

	private static final char NEW_FORMAT_MARK = '+';
	private static final char ARGUMENTS_DELIMITER = '#';
	private static final String ARGUMENTS_DELIMITER_STR = String.valueOf(ARGUMENTS_DELIMITER);
	private static final String EMPTY_ARGUMENT = "   "; //$NON-NLS-1$
	public final static String UTF_8 = "UTF-8"; //$NON-NLS-1$

	/**
	 * Converts an array of Objects into String.
	 */
	public static String toString(Object[] objects)
	{
		return toString(objects, new Displayable()
		{
			public String displayString(Object o)
			{
				if (o == null)
					return "null"; //$NON-NLS-1$
				return o.toString();
			}
		});
	}

	/**
	 * Converts an array of Objects into String.
	 */
	public static String toString(Object[] objects, Displayable renderer)
	{
		if (objects == null)
			return ""; //$NON-NLS-1$
		StringBuffer buffer = new StringBuffer(10);
		for (int i = 0; i < objects.length; i++)
		{
			if (i > 0)
				buffer.append(", "); //$NON-NLS-1$
			buffer.append(renderer.displayString(objects[i]));
		}
		return buffer.toString();
	}

	/**
	 * Combines two hash codes to make a new one.
	 */
	public static int combineHashCodes(int hashCode1, int hashCode2)
	{
		return hashCode1 * 17 + hashCode2;
	}

	/**
	 * Sort the strings in the given collection.
	 */
	private static void quickSort(String[] sortedCollection, int left, int right)
	{
		int original_left = left;
		int original_right = right;
		String mid = sortedCollection[(left + right) / 2];
		do
		{
			while (sortedCollection[left].compareTo(mid) < 0)
			{
				left++;
			}
			while (mid.compareTo(sortedCollection[right]) < 0)
			{
				right--;
			}
			if (left <= right)
			{
				String tmp = sortedCollection[left];
				sortedCollection[left] = sortedCollection[right];
				sortedCollection[right] = tmp;
				left++;
				right--;
			}
		}
		while (left <= right);
		if (original_left < right)
		{
			quickSort(sortedCollection, original_left, right);
		}
		if (left < original_right)
		{
			quickSort(sortedCollection, left, original_right);
		}
	}

	/**
	 * Compares two arrays using equals() on the elements. Neither can be null. Only the first len elements are
	 * compared. Return false if either array is shorter than len.
	 */
	public static boolean equalArrays(Object[] a, Object[] b, int len)
	{
		if (a == b)
			return true;
		if (a.length < len || b.length < len)
			return false;
		for (int i = 0; i < len; ++i)
		{
			if (a[i] == null)
			{
				if (b[i] != null)
					return false;
			}
			else
			{
				if (!a[i].equals(b[i]))
					return false;
			}
		}
		return true;
	}

	/**
	 * Compares two arrays using equals() on the elements. Either or both arrays may be null. Returns true if both are
	 * null. Returns false if only one is null. If both are arrays, returns true iff they have the same length and all
	 * elements compare true with equals.
	 */
	public static boolean equalArraysOrNull(Object[] a, Object[] b)
	{
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		int len = a.length;
		if (len != b.length)
			return false;
		for (int i = 0; i < len; ++i)
		{
			if (a[i] == null)
			{
				if (b[i] != null)
					return false;
			}
			else
			{
				if (!a[i].equals(b[i]))
					return false;
			}
		}
		return true;
	}

	private static boolean isNewProblemArgumentsFormat(String[] arguments)
	{
		for (int i = 0; i < arguments.length; ++i)
		{
			if (arguments[i].indexOf(ARGUMENTS_DELIMITER) != -1)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Put all the arguments in one String.
	 */
	public static String getProblemArgumentsForMarker(String[] arguments)
	{
		if (isNewProblemArgumentsFormat(arguments))
		{
			return encodeProblemArguments(arguments);
		}
		StringBuffer args = new StringBuffer(10);
		args.append(arguments.length);
		args.append(':');
		for (int j = 0; j < arguments.length; j++)
		{
			if (j != 0)
				args.append(ARGUMENTS_DELIMITER);
			if (arguments[j].length() == 0)
			{
				args.append(EMPTY_ARGUMENT);
			}
			else
			{
				args.append(arguments[j]);
			}
		}
		return args.toString();
	}

	/**
	 * @param arguments
	 * @return
	 */
	private static String encodeProblemArguments(String[] arguments)
	{
		StringBuffer args = new StringBuffer();
		args.append(NEW_FORMAT_MARK);
		args.append(arguments.length);
		for (int j = 0; j < arguments.length; j++)
		{
			args.append(ARGUMENTS_DELIMITER);
			args.append(arguments[j].length());
			args.append(ARGUMENTS_DELIMITER);
			args.append(arguments[j]);
		}
		return args.toString();
	}

	public static String[] getProblemArgumentsFromMarker(String argumentsString)
	{
		if (argumentsString == null || argumentsString.length() == 0)
			return null;
		if (argumentsString.charAt(0) == NEW_FORMAT_MARK)
		{
			return decodeProblemArguments(argumentsString);
		}
		int index = argumentsString.indexOf(':');
		if (index == -1)
			return null;

		int length = argumentsString.length();
		int numberOfArg;
		try
		{
			numberOfArg = Integer.parseInt(argumentsString.substring(0, index));
		}
		catch (NumberFormatException e)
		{
			return null;
		}
		argumentsString = argumentsString.substring(index + 1, length);

		String[] args = new String[length];
		int count = 0;

		StringTokenizer tokenizer = new StringTokenizer(argumentsString, ARGUMENTS_DELIMITER_STR);
		while (tokenizer.hasMoreTokens())
		{
			String argument = tokenizer.nextToken();
			if (argument.equals(EMPTY_ARGUMENT))
				argument = ""; //$NON-NLS-1$
			args[count++] = argument;
		}

		if (count != numberOfArg)
			return null;

		System.arraycopy(args, 0, args = new String[count], 0, count);
		return args;
	}

	/**
	 * @param s
	 * @return
	 */
	private static String[] decodeProblemArguments(String s)
	{
		int begin = 1;
		int pos = s.indexOf(ARGUMENTS_DELIMITER, begin);
		if (pos == -1)
		{
			return null;
		}
		final int numberOfArg;
		try
		{
			numberOfArg = Integer.parseInt(s.substring(begin, pos));
		}
		catch (NumberFormatException e)
		{
			return null;
		}
		begin = pos;
		final String[] args = new String[numberOfArg];
		final int length = s.length();
		for (int i = 0; i < numberOfArg; ++i)
		{
			if (begin >= length || s.charAt(begin) != ARGUMENTS_DELIMITER)
			{
				return null;
			}
			++begin;
			pos = s.indexOf(ARGUMENTS_DELIMITER, begin);
			if (pos == -1)
			{
				return null;
			}
			final int argLen;
			try
			{
				argLen = Integer.parseInt(s.substring(begin, pos));
			}
			catch (NumberFormatException e)
			{
				return null;
			}
			begin = pos + 1;
			if (begin + argLen > length)
			{
				return null;
			}
			args[i] = s.substring(begin, begin + argLen);
			begin += argLen;
		}
		if (begin != length)
		{
			return null;
		}
		return args;
	}

	/**
	 * Finds the first line separator used by the given text.
	 * 
	 * @return </code>"\n"</code> or </code>"\r"</code> or </code>"\r\n"</code>, or <code>null</code> if none found
	 */
	public static String findLineSeparator(char[] text)
	{
		// find the first line separator
		int length = text.length;
		if (length > 0)
		{
			char nextChar = text[0];
			for (int i = 0; i < length; i++)
			{
				char currentChar = nextChar;
				nextChar = i < length - 1 ? text[i + 1] : ' ';
				switch (currentChar)
				{
					case '\n':
						return "\n"; //$NON-NLS-1$
					case '\r':
						return nextChar == '\n' ? "\r\n" : "\r"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		// not found
		return null;
	}

	public static void verbose(String log)
	{
		verbose(log, System.out);
	}

	public static synchronized void verbose(String log, PrintStream printStream)
	{
		int start = 0;
		do
		{
			int end = log.indexOf('\n', start);
			printStream.print(Thread.currentThread());
			printStream.print(" "); //$NON-NLS-1$
			printStream.print(log.substring(start, end == -1 ? log.length() : end + 1));
			start = end + 1;
		}
		while (start != 0);
		printStream.println();
	}

	private static boolean isFatalException(CoreException e)
	{
		return e.getCause() instanceof FileNotFoundException;
	}

	public static char[] getResourceContentsAsCharArray(IFile file, String encoding) throws CoreException
	{
		// Get resource contents
		InputStream stream = null;
		int tryCount = 10;
		try
		{
			while (stream == null)
			{
				try
				{
					stream = file.getContents(true);
				}
				catch (CoreException e)
				{
					// Some times for RSE we can get here if connection is not
					// established yet, or if connection are lost.
					if (isFatalException(e) || --tryCount == 0)
					{
						throw new CoreException(new Status(IStatus.ERROR, PHPEplPlugin.PLUGIN_ID,
								"Could not read the file content")); //$NON-NLS-1$
					}
				}
			}
			char[] data = getInputStreamAsCharArray(stream, -1, encoding);
			return data;
		}
		catch (IOException ioe)
		{
			throw new CoreException(new Status(IStatus.ERROR, PHPEplPlugin.PLUGIN_ID, ioe.getMessage()));
		}
		finally
		{
			if (stream != null)
			{
				try
				{
					stream.close();
				}
				catch (IOException e)
				{
					// ignore
				}
			}
		}
	}

	/*
	 * a character array. If a length is specified (ie. if length != -1), this represents the number of bytes in the
	 * stream. Note this doesn't close the stream. @throws IOException if a problem occured reading the stream.
	 */
	public static char[] getInputStreamAsCharArray(InputStream stream, int length, String encoding) throws IOException
	{
		InputStreamReader reader = null;
		try
		{
			try
			{
				reader = encoding == null ? new InputStreamReader(toBufferedInputStream(stream))
						: new InputStreamReader(stream, encoding);
			}
			catch (UnsupportedEncodingException e)
			{
				// encoding is not supported
				reader = new InputStreamReader(toBufferedInputStream(stream));
			}
			char[] contents;
			int totalRead = 0;
			if (length == -1)
			{
				contents = CharOperation.NO_CHAR;
			}
			else
			{
				// length is a good guess when the encoding produces less or the
				// same amount of characters than the file length
				contents = new char[length]; // best guess
			}

			while (true)
			{
				int amountRequested;
				if (totalRead < length)
				{
					// until known length is met, reuse same array sized eagerly
					amountRequested = length - totalRead;
				}
				else
				{
					// reading beyond known length
					int current = reader.read();
					if (current < 0)
						break;

					amountRequested = Math.max(stream.available(), DEFAULT_READING_SIZE); // read at least 8K

					// resize contents if needed
					if (totalRead + 1 + amountRequested > contents.length)
						System.arraycopy(contents, 0, contents = new char[totalRead + 1 + amountRequested], 0,
								totalRead);

					// add current character
					contents[totalRead++] = (char) current; // coming from
					// totalRead==length
				}
				// read as many chars as possible
				int amountRead = reader.read(contents, totalRead, amountRequested);
				if (amountRead < 0)
					break;
				totalRead += amountRead;
			}

			// Do not keep first character for UTF-8 BOM encoding
			int start = 0;
			if (totalRead > 0 && UTF_8.equals(encoding))
			{
				if (contents[0] == 0xFEFF)
				{ // if BOM char then skip
					totalRead--;
					start = 1;
				}
			}

			// resize contents if necessary
			if (totalRead < contents.length)
				System.arraycopy(contents, start, contents = new char[totalRead], 0, totalRead);

			return contents;
		}
		finally
		{
			if (reader != null)
			{
				reader.close();
			}
		}
	}

	private static InputStream toBufferedInputStream(InputStream stream)
	{
		if (stream instanceof BufferedInputStream)
		{
			return stream;
		}
		else
		{
			return new BufferedInputStream(stream, DEFAULT_READING_SIZE);
		}
	}

	/**
	 * Returns the toString() of the given full path minus the first given number of segments. The returned string is
	 * always a relative path (it has no leading slash)
	 */
	public static String relativePath(IPath fullPath, int skipSegmentCount)
	{
		boolean hasTrailingSeparator = fullPath.hasTrailingSeparator();
		String[] segments = fullPath.segments();
		// compute length
		int length = 0;
		int max = segments.length;
		if (max > skipSegmentCount)
		{
			for (int i1 = skipSegmentCount; i1 < max; i1++)
			{
				length += segments[i1].length();
			}
			// add the separator lengths
			length += max - skipSegmentCount - 1;
		}
		if (hasTrailingSeparator)
			length++;
		char[] result = new char[length];
		int offset = 0;
		int len = segments.length - 1;
		if (len >= skipSegmentCount)
		{
			// append all but the last segment, with separators
			for (int i = skipSegmentCount; i < len; i++)
			{
				int size = segments[i].length();
				segments[i].getChars(0, size, result, offset);
				offset += size;
				result[offset++] = '/';
			}
			// append the last segment
			int size = segments[len].length();
			segments[len].getChars(0, size, result, offset);
			offset += size;
		}
		if (hasTrailingSeparator)
			result[offset++] = '/';
		return new String(result);
	}

	private static void quickSort(char[][] list, int left, int right)
	{
		int original_left = left;
		int original_right = right;
		char[] mid = list[(left + right) / 2];
		do
		{
			while (compare(list[left], mid) < 0)
			{
				left++;
			}
			while (compare(mid, list[right]) < 0)
			{
				right--;
			}
			if (left <= right)
			{
				char[] tmp = list[left];
				list[left] = list[right];
				list[right] = tmp;
				left++;
				right--;
			}
		}
		while (left <= right);
		if (original_left < right)
		{
			quickSort(list, original_left, right);
		}
		if (left < original_right)
		{
			quickSort(list, left, original_right);
		}
	}

	/**
	 * Sort the comparable objects in the given collection.
	 */
	private static void quickSort(Comparable[] sortedCollection, int left, int right)
	{
		int original_left = left;
		int original_right = right;
		Comparable mid = sortedCollection[(left + right) / 2];
		do
		{
			while (sortedCollection[left].compareTo(mid) < 0)
			{
				left++;
			}
			while (mid.compareTo(sortedCollection[right]) < 0)
			{
				right--;
			}
			if (left <= right)
			{
				Comparable tmp = sortedCollection[left];
				sortedCollection[left] = sortedCollection[right];
				sortedCollection[right] = tmp;
				left++;
				right--;
			}
		}
		while (left <= right);
		if (original_left < right)
		{
			quickSort(sortedCollection, original_left, right);
		}
		if (left < original_right)
		{
			quickSort(sortedCollection, left, original_right);
		}
	}

	private static void quickSort(int[] list, int left, int right)
	{
		int original_left = left;
		int original_right = right;
		int mid = list[(left + right) / 2];
		do
		{
			while (list[left] < mid)
			{
				left++;
			}
			while (mid < list[right])
			{
				right--;
			}
			if (left <= right)
			{
				int tmp = list[left];
				list[left] = list[right];
				list[right] = tmp;
				left++;
				right--;
			}
		}
		while (left <= right);
		if (original_left < right)
		{
			quickSort(list, original_left, right);
		}
		if (left < original_right)
		{
			quickSort(list, left, original_right);
		}
	}

	/**
	 * Sort the objects in the given collection using the given comparer.
	 */
	private static void quickSort(Object[] sortedCollection, int left, int right, Comparer comparer)
	{
		int original_left = left;
		int original_right = right;
		Object mid = sortedCollection[(left + right) / 2];
		do
		{
			while (comparer.compare(sortedCollection[left], mid) < 0)
			{
				left++;
			}
			while (comparer.compare(mid, sortedCollection[right]) < 0)
			{
				right--;
			}
			if (left <= right)
			{
				Object tmp = sortedCollection[left];
				sortedCollection[left] = sortedCollection[right];
				sortedCollection[right] = tmp;
				left++;
				right--;
			}
		}
		while (left <= right);
		if (original_left < right)
		{
			quickSort(sortedCollection, original_left, right, comparer);
		}
		if (left < original_right)
		{
			quickSort(sortedCollection, left, original_right, comparer);
		}
	}

	public static void sort(char[][] list)
	{
		if (list.length > 1)
			quickSort(list, 0, list.length - 1);
	}

	/**
	 * Sorts an array of Comparable objects in place.
	 */
	public static void sort(Comparable[] objects)
	{
		if (objects.length > 1)
			quickSort(objects, 0, objects.length - 1);
	}

	public static void sort(int[] list)
	{
		if (list.length > 1)
			quickSort(list, 0, list.length - 1);
	}

	/**
	 * Sorts an array of objects in place. The given comparer compares pairs of items.
	 */
	public static void sort(Object[] objects, Comparer comparer)
	{
		if (objects.length > 1)
			quickSort(objects, 0, objects.length - 1, comparer);
	}

	/**
	 * Sorts an array of strings in place using quicksort.
	 */
	public static void sort(String[] strings)
	{
		if (strings.length > 1)
			quickSort(strings, 0, strings.length - 1);
	}

	/**
	 * Sorts an array of Comparable objects, returning a new array with the sorted items. The original array is left
	 * untouched.
	 */
	public static Comparable[] sortCopy(Comparable[] objects)
	{
		int len = objects.length;
		Comparable[] copy = new Comparable[len];
		System.arraycopy(objects, 0, copy, 0, len);
		sort(copy);
		return copy;
	}

	/**
	 * Sorts an array of Strings, returning a new array with the sorted items. The original array is left untouched.
	 */
	public static Object[] sortCopy(Object[] objects, Comparer comparer)
	{
		int len = objects.length;
		Object[] copy = new Object[len];
		System.arraycopy(objects, 0, copy, 0, len);
		sort(copy, comparer);
		return copy;
	}

	/**
	 * Sorts an array of Strings, returning a new array with the sorted items. The original array is left untouched.
	 */
	public static String[] sortCopy(String[] objects)
	{
		int len = objects.length;
		String[] copy = new String[len];
		System.arraycopy(objects, 0, copy, 0, len);
		sort(copy);
		return copy;
	}

	/**
	 * Compares two byte arrays. Returns <0 if a byte in a is less than the corresponding byte in b, or if a is shorter,
	 * or if a is null. Returns >0 if a byte in a is greater than the corresponding byte in b, or if a is longer, or if
	 * b is null. Returns 0 if they are equal or both null.
	 */
	public static int compare(byte[] a, byte[] b)
	{
		if (a == b)
			return 0;
		if (a == null)
			return -1;
		if (b == null)
			return 1;
		int len = Math.min(a.length, b.length);
		for (int i = 0; i < len; ++i)
		{
			int diff = a[i] - b[i];
			if (diff != 0)
				return diff;
		}
		if (a.length > len)
			return 1;
		if (b.length > len)
			return -1;
		return 0;
	}

	/**
	 * Compares two strings lexicographically. The comparison is based on the Unicode value of each character in the
	 * strings.
	 * 
	 * @return the value <code>0</code> if the str1 is equal to str2; a value less than <code>0</code> if str1 is
	 *         lexicographically less than str2; and a value greater than <code>0</code> if str1 is lexicographically
	 *         greater than str2.
	 */
	public static int compare(char[] str1, char[] str2)
	{
		int len1 = str1.length;
		int len2 = str2.length;
		int n = Math.min(len1, len2);
		int i = 0;
		while (n-- != 0)
		{
			char c1 = str1[i];
			char c2 = str2[i++];
			if (c1 != c2)
			{
				return c1 - c2;
			}
		}
		return len1 - len2;
	}

	/**
	 * Returns true if the given folder name is valid for a package, false if it is not.
	 */
	public static boolean isValidFolderNameForPackage(String folderName)
	{
		// TODO check if folder is valid for a package
		return true;
	}

	/**
	 * Return a new array which is the split of the given string using the given divider. The given end is exclusive and
	 * the given start is inclusive. <br>
	 * <br>
	 * For example:
	 * <ol>
	 * <li>
	 * 
	 * <pre>
	 * divider = 'b'
	 *       string = &quot;abbaba&quot;
	 *       start = 2
	 *       end = 5
	 *       result =&gt; { &quot;&quot;, &quot;a&quot;, &quot;&quot; }
	 * </pre>
	 * 
	 * </li>
	 * </ol>
	 * 
	 * @param divider
	 *            the given divider
	 * @param string
	 *            the given string
	 * @param start
	 *            the given starting index
	 * @param end
	 *            the given ending index
	 * @return a new array which is the split of the given string using the given divider
	 * @throws ArrayIndexOutOfBoundsException
	 *             if start is lower than 0 or end is greater than the array length
	 */
	public static final String[] splitOn(char divider, String string, int start, int end)
	{
		int length = string == null ? 0 : string.length();
		if (length == 0 || start > end)
			return CharOperation.NO_STRINGS;
		int wordCount = 1;
		for (int i = start; i < end; i++)
			if (string.charAt(i) == divider)
				wordCount++;
		String[] split = new String[wordCount];
		int last = start, currentWord = 0;
		for (int i = start; i < end; i++)
		{
			if (string.charAt(i) == divider)
			{
				split[currentWord++] = string.substring(last, i);
				last = i + 1;
			}
		}
		split[currentWord] = string.substring(last, end);
		return split;
	}

	/**
	 * Returns the concatenation of the given array parts using the given separator between each part. <br>
	 * <br>
	 * For example:<br>
	 * <ol>
	 * <li>
	 * 
	 * <pre>
	 * array = {&quot;a&quot;, &quot;b&quot;}
	 *       separator = '.'
	 *       =&gt; result = &quot;a.b&quot;
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 * array = {}
	 *       separator = '.'
	 *       =&gt; result = &quot;&quot;
	 * </pre>
	 * 
	 * </li>
	 * </ol>
	 * 
	 * @param array
	 *            the given array
	 * @param separator
	 *            the given separator
	 * @return the concatenation of the given array parts using the given separator between each part
	 */
	public static final String concatWith(String[] array, char separator)
	{
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = array.length; i < length; i++)
		{
			buffer.append(array[i]);
			if (i < length - 1)
				buffer.append(separator);
		}
		return buffer.toString();
	}

	/**
	 * Returns the concatenation of the given array parts using the given separator between each part and appending the
	 * given name at the end. <br>
	 * <br>
	 * For example:<br>
	 * <ol>
	 * <li>
	 * 
	 * <pre>
	 * name = &quot;c&quot;
	 *       array = { &quot;a&quot;, &quot;b&quot; }
	 *       separator = '.'
	 *       =&gt; result = &quot;a.b.c&quot;
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 * name = null
	 *       array = { &quot;a&quot;, &quot;b&quot; }
	 *       separator = '.'
	 *       =&gt; result = &quot;a.b&quot;
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 * name = &quot; c&quot;
	 *       array = null
	 *       separator = '.'
	 *       =&gt; result = &quot;c&quot;
	 * </pre>
	 * 
	 * </li>
	 * </ol>
	 * 
	 * @param array
	 *            the given array
	 * @param name
	 *            the given name
	 * @param separator
	 *            the given separator
	 * @return the concatenation of the given array parts using the given separator between each part and appending the
	 *         given name at the end
	 */
	public static final String concatWith(String[] array, String name, char separator)
	{
		if (array == null || array.length == 0)
			return name;
		if (name == null || name.length() == 0)
			return concatWith(array, separator);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = array.length; i < length; i++)
		{
			buffer.append(array[i]);
			buffer.append(separator);
		}
		buffer.append(name);
		return buffer.toString();
	}

	/**
	 * Returns a new array adding the second array at the end of first array. It answers null if the first and second
	 * are null. If the first array is null or if it is empty, then a new array is created with second. If the second
	 * array is null, then the first array is returned. <br>
	 * <br>
	 * For example:
	 * <ol>
	 * <li>
	 * 
	 * <pre>
	 * first = null
	 *       second = &quot;a&quot;
	 *       =&gt; result = {&quot;a&quot;}
	 * </pre>
	 * <li>
	 * 
	 * <pre>
	 * first = {&quot;a&quot;}
	 *       second = null
	 *       =&gt; result = {&quot;a&quot;}
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 * first = {&quot;a&quot;}
	 *       second = {&quot;b&quot;}
	 *       =&gt; result = {&quot;a&quot;, &quot;b&quot;}
	 * </pre>
	 * 
	 * </li>
	 * </ol>
	 * 
	 * @param first
	 *            the first array to concatenate
	 * @param second
	 *            the array to add at the end of the first array
	 * @return a new array adding the second array at the end of first array, or null if the two arrays are null.
	 */
	public static final String[] arrayConcat(String[] first, String second)
	{
		if (second == null)
			return first;
		if (first == null)
			return new String[] { second };
		int length = first.length;
		if (first.length == 0)
		{
			return new String[] { second };
		}
		String[] result = new String[length + 1];
		System.arraycopy(first, 0, result, 0, length);
		result[length] = second;
		return result;
	}

	public static boolean isReadOnly(IResource resource)
	{
		if (resource != null)
		{
			ResourceAttributes resourceAttributes = resource.getResourceAttributes();
			if (resourceAttributes == null)
				return false; // not supported on this platform for this
			// resource
			return resourceAttributes.isReadOnly();
		}
		return true;
	}

	public static void setReadOnly(IResource resource, boolean readOnly)
	{
		ResourceAttributes resourceAttributes = resource.getResourceAttributes();
		if (resourceAttributes == null)
			return; // not supported on this platform for this resource
		resourceAttributes.setReadOnly(readOnly);
		try
		{
			resource.setResourceAttributes(resourceAttributes);
		}
		catch (CoreException e)
		{
			// ignore
		}
	}

	/*
	 * Returns whether the given compound name starts with the given prefix. Returns true if the n first elements of the
	 * prefix are equals and the last element of the prefix is a prefix of the corresponding element in the compound
	 * name.
	 */
	public static boolean startsWithIgnoreCase(String[] compoundName, String[] prefix)
	{
		int prefixLength = prefix.length;
		int nameLength = compoundName.length;
		if (prefixLength > nameLength)
			return false;
		for (int i = 0; i < prefixLength - 1; i++)
		{
			if (!compoundName[i].equalsIgnoreCase(prefix[i]))
				return false;
		}
		return compoundName[prefixLength - 1].toLowerCase().startsWith(prefix[prefixLength - 1].toLowerCase());
	}

	/**
	 * Reads in a string from the specified data input stream. The string has been encoded using a modified UTF-8
	 * format.
	 * <p>
	 * The first two bytes are read as if by <code>readUnsignedShort</code>. This value gives the number of following
	 * bytes that are in the encoded string, not the length of the resulting string. The following bytes are then
	 * interpreted as bytes encoding characters in the UTF-8 format and are converted into characters.
	 * <p>
	 * This method blocks until all the bytes are read, the end of the stream is detected, or an exception is thrown.
	 * 
	 * @param in
	 *            a data input stream.
	 * @return a Unicode string.
	 * @exception EOFException
	 *                if the input stream reaches the end before all the bytes.
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @exception UTFDataFormatException
	 *                if the bytes do not represent a valid UTF-8 encoding of a Unicode string.
	 * @see java.io.DataInputStream#readUnsignedShort()
	 */
	public final static char[] readUTF(DataInput in) throws IOException
	{
		int utflen = in.readUnsignedShort();
		char str[] = new char[utflen];
		int count = 0;
		int strlen = 0;
		while (count < utflen)
		{
			int c = in.readUnsignedByte();
			int char2, char3;
			switch (c >> 4)
			{
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					// xxxxxxx
					count++;
					str[strlen++] = (char) c;
					break;
				case 12:
				case 13:
					// 110x xxxx 10xx xxxx
					count += 2;
					if (count > utflen)
						throw new UTFDataFormatException();
					char2 = in.readUnsignedByte();
					if ((char2 & 0xC0) != 0x80)
						throw new UTFDataFormatException();
					str[strlen++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
					break;
				case 14:
					// 1110 xxxx 10xx xxxx 10xx xxxx
					count += 3;
					if (count > utflen)
						throw new UTFDataFormatException();
					char2 = in.readUnsignedByte();
					char3 = in.readUnsignedByte();
					if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
						throw new UTFDataFormatException();
					str[strlen++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
					break;
				default:
					// 10xx xxxx, 1111 xxxx
					throw new UTFDataFormatException();
			}
		}
		if (strlen < utflen)
		{
			System.arraycopy(str, 0, str = new char[strlen], 0, strlen);
		}
		return str;
	}

	/**
	 * Writes a string to the given output stream using UTF-8 encoding in a machine-independent manner.
	 * <p>
	 * First, two bytes are written to the output stream as if by the <code>writeShort</code> method giving the number
	 * of bytes to follow. This value is the number of bytes actually written out, not the length of the string.
	 * Following the length, each character of the string is output, in sequence, using the UTF-8 encoding for the
	 * character.
	 * 
	 * @param str
	 *            a string to be written.
	 * @return the number of bytes written to the stream.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	public static int writeUTF(OutputStream out, char[] str) throws IOException
	{
		int strlen = str.length;
		int utflen = 0;
		for (int i = 0; i < strlen; i++)
		{
			int c = str[i];
			if ((c >= 0x0001) && (c <= 0x007F))
			{
				utflen++;
			}
			else if (c > 0x07FF)
			{
				utflen += 3;
			}
			else
			{
				utflen += 2;
			}
		}
		if (utflen > 65535)
			throw new UTFDataFormatException();
		out.write((utflen >>> 8) & 0xFF);
		out.write((utflen >>> 0) & 0xFF);
		if (strlen == utflen)
		{
			for (int i = 0; i < strlen; i++)
				out.write(str[i]);
		}
		else
		{
			for (int i = 0; i < strlen; i++)
			{
				int c = str[i];
				if ((c >= 0x0001) && (c <= 0x007F))
				{
					out.write(c);
				}
				else if (c > 0x07FF)
				{
					out.write(0xE0 | ((c >> 12) & 0x0F));
					out.write(0x80 | ((c >> 6) & 0x3F));
					out.write(0x80 | ((c >> 0) & 0x3F));
				}
				else
				{
					out.write(0xC0 | ((c >> 6) & 0x1F));
					out.write(0x80 | ((c >> 0) & 0x3F));
				}
			}
		}
		return utflen + 2; // the number of bytes written to the stream
	}

	/**
	 * Scans the given string for an identifier starting at the given index and returns the index of the last character.
	 * Stop characters are: ";", ":", "&lt;", "&gt;", "/", ".".
	 * 
	 * @param string
	 *            the signature string
	 * @param start
	 *            the 0-based character index of the first character
	 * @return the 0-based character index of the last character
	 * @exception IllegalArgumentException
	 *                if this is not an identifier
	 */
	public static int scanIdentifier(char[] string, int start)
	{
		// need a minimum 1 char
		if (start >= string.length)
		{
			throw new IllegalArgumentException();
		}
		int p = start;
		while (true)
		{
			char c = string[p];
			if (c == '<' || c == '>' || c == ':' || c == ';' || c == '.' || c == '/')
			{
				return p - 1;
			}
			p++;
			if (p == string.length)
			{
				return p - 1;
			}
		}
	}

}
