package com.aptana.editor.php.internal.indexer;

///**
// * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
// * dual-licensed under both the Aptana Public License and the GNU General
// * Public license. You may elect to use one or the other of these licenses.
// * 
// * This program is distributed in the hope that it will be useful, but
// * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
// * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
// * the GPL or APL you select, is prohibited.
// *
// * 1. For the GPL license (GPL), you can redistribute and/or modify this
// * program under the terms of the GNU General Public License,
// * Version 3, as published by the Free Software Foundation.  You should
// * have received a copy of the GNU General Public License, Version 3 along
// * with this program; if not, write to the Free Software Foundation, Inc., 51
// * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
// * 
// * Aptana provides a special exception to allow redistribution of this file
// * with certain other free and open source software ("FOSS") code and certain additional terms
// * pursuant to Section 7 of the GPL. You may view the exception and these
// * terms on the web at http://www.aptana.com/legal/gpl/.
// * 
// * 2. For the Aptana Public License (APL), this program and the
// * accompanying materials are made available under the terms of the APL
// * v1.0 which accompanies this distribution, and is available at
// * http://www.aptana.com/legal/apl/.
// * 
// * You may view the GPL, Aptana's exception and additional terms, and the
// * APL in the file titled license.html at the root of the corresponding
// * plugin containing this source file.
// * 
// * Any modifications to this file must keep this entire header intact.
// */
//package com.aptana.editor.php.internal.indexer;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.StringReader;
//import java.util.List;
//
//import org.eclipse.php.internal.core.ast.nodes.Comment;
//import org.eclipse.php.internal.core.ast.nodes.Program;
//import org.eclipse.php.internal.core.ast.parser.ASTParser;
//import org.eclipse.php.internal.core.phpModel.parser.phpdoc.DocumentorLexer;
//import org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock;
//import org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocTag;
//
//import com.aptana.editor.php.builder.IModule;
//import com.aptana.editor.php.indexer.IElementEntry;
//
///**
// * PHPDoc utilities.
// * 
// * @author Denis Denisenko
// */
//public final class PHPDocUtils
//{
//	private static final String OPEN_BRACKET = "{"; //$NON-NLS-1$
//	private static final String CLOSE_BRACKET = "}"; //$NON-NLS-1$
//	private static final String DOLLAR = "$"; //$NON-NLS-1$
//	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
//
//	public static String findFunctionPHPDocComment(IElementEntry entry, int offset)
//	{
//		if (entry.getModule() != null)
//		{
//			return findFunctionPHPDocComment(entry.getModule(), offset);
//		}
//		// In case that the entry module is null, it's probably a PHP API documentation item, so
//		// parse the right item.
//		try
//		{
//			String entryPath = entry.getEntryPath();
//			if (entryPath != null)
//			{
//				InputStream stream = PHPBuiltins.getInstance().getBuiltinResourceStream(entryPath);
//				if (stream != null)
//				{
//					BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
//					return innerParsePHPDoc(offset, reader);
//				}
//			}
//		}
//		catch (Exception ex)
//		{
//			IdeLog.logError(PHPPlugin.getDefault(), "Failed locating the PHP function doc", ex);
//			return null;
//		}
//		return null;
//	}
//	
//	/**
//	 * Finds a PHPDoc comment above the offset in the module specified.
//	 * 
//	 * @param module
//	 *            - module.
//	 * @param offset
//	 *            - offset.
//	 * @return comment contents or null if not found.
//	 */
//	public static String findFunctionPHPDocComment(IModule module, int offset)
//	{
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(module.getContents(), EncodingUtils.getModuleEncoding(module)));
//			return innerParsePHPDoc(offset, reader);
//		}
//		catch (Exception ex)
//		{
//			return null;
//		}
//	}
//	
//	/**
//	 * Finds a PHPDoc comment above the offset in the source that is read from the given BufferedReader.
//	 * 
//	 * @param offset
//	 * @param reader
//	 * @return
//	 * @throws IOException
//	 * @throws Exception
//	 */
//	private static String innerParsePHPDoc(int offset, BufferedReader reader) throws IOException, Exception
//	{
//		StringBuffer moduleData = new StringBuffer();
//		char[] buf = new char[1024];
//		int numRead = 0;
//		while ((numRead = reader.read(buf)) != -1)
//		{
//			String readData = String.valueOf(buf, 0, numRead);
//			moduleData.append(readData);
//			buf = new char[1024];
//		}
//		reader.close();
//
//		String contents = moduleData.toString();
//		int b = -1;
//		for (int a = offset; a >= 0; a--)
//		{
//			char c = contents.charAt(a);
//			if (c == '(')
//			{
//				b = a;
//				break;
//			}
//			if (c == '\r' || c == '\n')
//			{
//				b = a;
//				break;
//			}
//		}
//		if (b != -1)
//		{
//			String str = contents.substring(b, offset);
//			if (str.indexOf(';') == -1)
//			{
//				offset = b;
//			}
//			// System.out.println(str);
//		}
//		Program program = ASTParser.parse(new StringReader(contents));
//
//		CommentsVisitor commentsVisitor = new CommentsVisitor();
//		PHPASTVisitorProxy commentsProxy = new PHPASTVisitorProxy(commentsVisitor);
//		commentsProxy.visit(program);
//		List<Comment> _comments = commentsVisitor.getComments();
//
//		Comment nearestComment = null;
//		for (Comment comment : _comments) // TODO SG: Run a binary search!
//		{
//			if (comment.getStart() > offset)
//			{
//				break;
//			}
//
//			nearestComment = comment;
//		}
//
//		if (nearestComment == null)
//		{
//			return null;
//		}
//
//		if (nearestComment.getCommentType() != Comment.TYPE_PHPDOC)
//		{
//			return null;
//		}
//
//		// checking if we have anything but white spaces between comment end and offset
//		for (int i = nearestComment.getEnd() + 1; i < offset - 1; i++)
//		{
//			char ch = contents.charAt(i);
//			if (!Character.isWhitespace(ch))
//			{
//				return null;
//			}
//		}
//
//		return contents.substring(nearestComment.getStart(), nearestComment.getEnd());
//	}
//
//	/**
//	 * Parses function PHPDoc with both parser that supports original PHPDoc specification and Aptana JS-like PHPDoc
//	 * format.
//	 * 
//	 * @param contents
//	 *            - contents to parse.
//	 * @return FunctionDocumentation or null.
//	 */
//	public static FunctionDocumentation parseFunctionPHPDoc(String contents)
//	{
//		PHPDocBlock block;
//
//		try
//		{
//			DocumentorLexer lexer = new DocumentorLexer(new StringReader(contents));
//			block = lexer.parse(contents);
//		}
//		catch (Throwable ex)
//		{
//			return null;
//		}
//		FunctionDocumentation result = new FunctionDocumentation();
//
//		result.setDescription(block.getShortDescription());
//
//		PHPDocTag[] tags = block.getTagsAsArray();
//		if (tags != null)
//		{
//			for (PHPDocTag tag : tags)
//			{
//				switch (tag.getID())
//				{
//					case PHPDocTag.VAR:
//					{
//						String value = tag.getValue();
//						if (value == null)
//						{
//							continue;
//						}
//						TypedDescription typeDescr = new TypedDescription();
//						typeDescr.addType(value);
//						result.addVar(typeDescr);
//						break;
//					}
//					case PHPDocTag.PARAM:
//						String value = tag.getValue();
//						if (value == null)
//						{
//							continue;
//						}
//						String[] parsedValue = parseParamTagValue(value);
//						TypedDescription typeDescr = new TypedDescription();
//						typeDescr.setName(parsedValue[0]);
//						if (parsedValue[1] != null)
//						{
//							typeDescr.addType(parsedValue[1]);
//						}
//						if (parsedValue[2] != null)
//						{
//							typeDescr.setDescription(parsedValue[2]);
//						}
//						result.addParam(typeDescr);
//						break;
//					case PHPDocTag.RETURN:
//						String returnTagValue = tag.getValue().trim();
//						if (returnTagValue == null)
//						{
//							continue;
//						}
//						String[] returnTypes = returnTagValue.split("\\|"); //$NON-NLS-1$
//						for (String returnType : returnTypes)
//						{
//							returnTagValue = clean(returnType.trim());
//							returnTagValue = firstWord(returnTagValue);
//							result.getReturn().addType(returnTagValue);
//						}
//						break;
//				}
//			}
//		}
//
//		return result;
//	}
//
//	/**
//	 * Gets the first word of a sentence.
//	 * 
//	 * @param str
//	 *            - string.
//	 * @return first word of a sentence.
//	 */
//	private static String firstWord(String str)
//	{
//		int firstSpacePos = -1;
//		for (int i = 0; i < str.length(); i++)
//		{
//			char ch = str.charAt(i);
//			if (Character.isWhitespace(ch))
//			{
//				firstSpacePos = i;
//				break;
//			}
//		}
//
//		if (firstSpacePos == -1)
//		{
//			return str;
//		}
//		else if (firstSpacePos == 0)
//		{
//			return EMPTY_STRING;
//		}
//		else
//		{
//			return str.substring(0, firstSpacePos);
//		}
//	}
//
//	/**
//	 * Parses parameter tag value.
//	 * 
//	 * @param toParse
//	 * @return array of parse results: first element is parameter name (without the $ symbol), next is parameter type if
//	 *         available and the third is parameter description if available.
//	 */
//	private static String[] parseParamTagValue(String toParse)
//	{
//		if (toParse == null || toParse.length() == 0)
//		{
//			return null;
//		}
//
//		String[] parts = toParse.split("\\s+"); //$NON-NLS-1$
//		if (parts == null || parts.length == 0)
//		{
//			return null;
//		}
//
//		String[] result = new String[3];
//
//		boolean isJSLike = false;
//		if (parts[0].contains(OPEN_BRACKET) || parts[0].contains(CLOSE_BRACKET))
//		{
//			isJSLike = true;
//		}
//		if (parts[0].contains(DOLLAR))
//		{
//			isJSLike = true;
//		}
//		if (parts.length > 1 && (parts[1].contains(DOLLAR)))
//		{
//			isJSLike = true;
//		}
//
//		if (isJSLike)
//		{
//			if (parts.length == 1)
//			{
//				result[0] = clean(parts[0]);
//			}
//			else if (parts.length == 2)
//			{
//				result[0] = clean(parts[1]);
//				result[1] = clean(parts[0]);
//			}
//			else
//			{
//				result[0] = clean(parts[1]);
//				result[1] = clean(parts[0]);
//				StringBuffer buf = new StringBuffer();
//				for (int i = 2; i < parts.length; i++)
//				{
//					buf.append(parts[i]);
//					if (i != parts.length)
//					{
//						buf.append(' ');
//					}
//				}
//				result[2] = buf.toString();
//			}
//		}
//		else
//		{
//			if (parts.length == 1)
//			{
//				result[0] = clean(parts[0]);
//			}
//			else if (parts.length == 2)
//			{
//				result[0] = clean(parts[0]);
//				result[1] = clean(parts[1]);
//			}
//			else
//			{
//				result[0] = clean(parts[0]);
//				result[1] = clean(parts[1]);
//				StringBuffer buf = new StringBuffer();
//				for (int i = 2; i < parts.length; i++)
//				{
//					buf.append(parts[i]);
//					if (i != parts.length)
//					{
//						buf.append(' ');
//					}
//				}
//				result[2] = buf.toString();
//			}
//		}
//
//		return result;
//	}
//
//	/**
//	 * Removes curves around the string.
//	 * 
//	 * @param in
//	 *            - input.
//	 * @return string with curves removed.
//	 */
//	private static String removeCurves(String in)
//	{
//		String result = in;
//		if (result.startsWith(OPEN_BRACKET))
//		{
//			result = result.substring(1);
//		}
//
//		if (result.equals(CLOSE_BRACKET))
//		{
//			return EMPTY_STRING;
//		}
//
//		if (result.endsWith(CLOSE_BRACKET))
//		{
//			result = result.substring(0, result.length() - 1);
//		}
//
//		return result;
//	}
//
//	/**
//	 * Cleans the string from curves and dollar.
//	 * 
//	 * @param in
//	 *            - input.
//	 * @return cleansed string
//	 */
//	private static String clean(String in)
//	{
//		String res1 = removeCurves(in);
//		return removeDollar(res1);
//	}
//
//	/**
//	 * Removes dollar symbol.
//	 * 
//	 * @param in
//	 *            - input string.
//	 * @return cleansed string.
//	 */
//	private static String removeDollar(String in)
//	{
//		if (in.startsWith(DOLLAR))
//		{
//			return in.substring(1);
//		}
//
//		return in;
//	}
// }
