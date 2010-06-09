package com.aptana.editor.php.internal.indexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.php.internal.core.ast.nodes.Comment;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocTag;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.contentAssist.ContentAssistUtils;
import com.aptana.editor.php.internal.indexer.language.PHPBuiltins;
import com.aptana.editor.php.internal.parser.phpdoc.FunctionDocumentation;
import com.aptana.editor.php.internal.parser.phpdoc.TypedDescription;
import com.aptana.editor.php.util.EncodingUtils;

/**
 * PHPDoc utilities.
 * 
 * @author Denis Denisenko
 */
public final class PHPDocUtils
{
	private static final String OPEN_BRACKET = "{"; //$NON-NLS-1$
	private static final String CLOSE_BRACKET = "}"; //$NON-NLS-1$
	private static final String DOLLAR = "$"; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public static PHPDocBlock findFunctionPHPDocComment(IElementEntry entry, int offset)
	{
		if (entry.getModule() != null)
		{
			return findFunctionPHPDocComment(entry.getModule(), offset);
		}
		// In case that the entry module is null, it's probably a PHP API documentation item, so
		// parse the right item.
		try
		{
			String entryPath = entry.getEntryPath();
			if (entryPath != null)
			{
				InputStream stream = PHPBuiltins.getInstance().getBuiltinResourceStream(entryPath);
				if (stream != null)
				{
					BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
					return innerParsePHPDoc(offset, reader);
				}
			}
		}
		catch (Exception ex)
		{
			PHPEditorPlugin.logError("Failed locating the PHP function doc", ex); //$NON-NLS-1$
			return null;
		}
		return null;
	}

	/**
	 * Finds a PHPDoc comment above the offset in the module specified.
	 * 
	 * @param module
	 *            - module.
	 * @param offset
	 *            - offset.
	 * @return comment contents or null if not found.
	 */
	public static PHPDocBlock findFunctionPHPDocComment(IModule module, int offset)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(module.getContents(), EncodingUtils
					.getModuleEncoding(module)));
			return innerParsePHPDoc(offset, reader);
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	/**
	 * Finds a PHPDoc comment above the offset in the source that is read from the given BufferedReader.
	 * 
	 * @param offset
	 * @param reader
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	private static PHPDocBlock innerParsePHPDoc(int offset, BufferedReader reader) throws IOException, Exception
	{
		StringBuffer moduleData = new StringBuffer();
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1)
		{
			String readData = String.valueOf(buf, 0, numRead);
			moduleData.append(readData);
			buf = new char[1024];
		}
		reader.close();

		String contents = moduleData.toString();
		int b = -1;
		for (int a = offset; a >= 0; a--)
		{
			char c = contents.charAt(a);
			if (c == '(')
			{
				b = a;
				break;
			}
			if (c == '\r' || c == '\n')
			{
				b = a;
				break;
			}
		}
		if (b != -1)
		{
			String str = contents.substring(b, offset);
			if (str.indexOf(';') == -1)
			{
				offset = b;
			}
			// System.out.println(str);
		}
		// TODO: Shalom - Get the version from the module?
		PHPVersion version = PHPVersionProvider.getDefaultPHPVersion();
		ASTParser parser = ASTParser.newParser(new StringReader(contents), version);
		Program program = parser.createAST(null);

		CommentsVisitor commentsVisitor = new CommentsVisitor();
		program.accept(commentsVisitor);
		List<Comment> _comments = commentsVisitor.getComments();

		Comment nearestComment = null;
		for (Comment comment : _comments) // FIXME Shalom: Run a binary search!
		{
			if (comment.getStart() > offset)
			{
				break;
			}

			nearestComment = comment;
		}

		if (nearestComment == null)
		{
			return null;
		}

		if (nearestComment.getCommentType() != Comment.TYPE_PHPDOC)
		{
			return null;
		}

		// checking if we have anything but white spaces between comment end and offset
		for (int i = nearestComment.getEnd() + 1; i < offset - 1; i++)
		{
			char ch = contents.charAt(i);
			if (!Character.isWhitespace(ch))
			{
				return null;
			}
		}

		// return contents.substring(nearestComment.getStart(), nearestComment.getEnd());
		return (PHPDocBlock) nearestComment;
	}

	/**
	 * Returns the function documentation from a given {@link PHPDocBlock}.
	 * 
	 * @param block
	 *            - The block to convert to a {@link FunctionDocumentation}.
	 * @return FunctionDocumentation or null.
	 */
	public static FunctionDocumentation getFunctionDocumentation(PHPDocBlock block)
	{
		if (block == null)
		{
			return null;
		}
		FunctionDocumentation result = new FunctionDocumentation();

		result.setDescription(block.getShortDescription());

		PHPDocTag[] tags = block.getTags();
		if (tags != null)
		{
			for (PHPDocTag tag : tags)
			{
				switch (tag.getTagKind())
				{
					case PHPDocTag.VAR:
					{
						String value = tag.getValue();
						if (value == null)
						{
							continue;
						}
						TypedDescription typeDescr = new TypedDescription();
						typeDescr.addType(value);
						result.addVar(typeDescr);
						break;
					}
					case PHPDocTag.PARAM:
						String value = tag.getValue();
						if (value == null)
						{
							continue;
						}
						String[] parsedValue = parseParamTagValue(value);
						TypedDescription typeDescr = new TypedDescription();
						typeDescr.setName(parsedValue[0]);
						if (parsedValue[1] != null)
						{
							typeDescr.addType(parsedValue[1]);
						}
						if (parsedValue[2] != null)
						{
							typeDescr.setDescription(parsedValue[2]);
						}
						result.addParam(typeDescr);
						break;
					case PHPDocTag.RETURN:
						String returnTagValue = tag.getValue().trim();
						if (returnTagValue == null)
						{
							continue;
						}
						String[] returnTypes = returnTagValue.split("\\|"); //$NON-NLS-1$
						for (String returnType : returnTypes)
						{
							returnTagValue = clean(returnType.trim());
							returnTagValue = firstWord(returnTagValue);
							result.getReturn().addType(returnTagValue);
						}
						break;
				}
			}
		}

		return result;
	}

	/**
	 * Finds the PHPDoc comment that appears right above the given offset. In case there is no comment, or there are
	 * non-white characters between the offset and the comment, this method returns null.
	 * 
	 * @param comments
	 *            - The list of comments as parsed with the AST
	 * @param offset
	 *            - offset to start search from.
	 * @param content
	 *            - The file content
	 * @return PHPDocBlock The PhpDoc, or null.
	 */
	public static PHPDocBlock findPHPDocComment(List<Comment> comments, int offset, String content)
	{
		if (comments == null || comments.isEmpty())
		{
			return null;
		}

		Comment nearestComment = null;
		for (Comment comment : comments) // FIXME - Shalom: Use binary search!
		{
			if (comment.getStart() > offset)
			{
				break;
			}

			nearestComment = comment;
		}

		if (nearestComment == null)
		{
			return null;
		}

		if (nearestComment.getCommentType() != Comment.TYPE_PHPDOC)
		{
			return null;
		}

		if (content != null)
		{
			// checking if we have anything but whitespace between comment end and
			// offset
			if (offset - 2 < 0 || nearestComment.getEnd() >= content.length() || offset - 2 >= content.length())
			{
				return null;
			}

			// checking if we have anything but white spaces between comment end and offset
			for (int i = nearestComment.getEnd() + 1; i < offset - 1; i++)
			{
				char ch = content.charAt(i);
				if (!Character.isWhitespace(ch))
				{
					return null;
				}
			}
		}

		return (PHPDocBlock) nearestComment;
	}

	/**
	 * Gets the first word of a sentence.
	 * 
	 * @param str
	 *            - string.
	 * @return first word of a sentence.
	 */
	private static String firstWord(String str)
	{
		int firstSpacePos = -1;
		for (int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);
			if (Character.isWhitespace(ch))
			{
				firstSpacePos = i;
				break;
			}
		}

		if (firstSpacePos == -1)
		{
			return str;
		}
		else if (firstSpacePos == 0)
		{
			return EMPTY_STRING;
		}
		else
		{
			return str.substring(0, firstSpacePos);
		}
	}

	/**
	 * Parses parameter tag value.
	 * 
	 * @param toParse
	 * @return array of parse results: first element is parameter name (without the $ symbol), next is parameter type if
	 *         available and the third is parameter description if available.
	 */
	private static String[] parseParamTagValue(String toParse)
	{
		if (toParse == null || toParse.length() == 0)
		{
			return null;
		}

		String[] parts = toParse.split("\\s+"); //$NON-NLS-1$
		if (parts == null || parts.length == 0)
		{
			return null;
		}

		String[] result = new String[3];

		boolean isJSLike = false;
		if (parts[0].contains(OPEN_BRACKET) || parts[0].contains(CLOSE_BRACKET))
		{
			isJSLike = true;
		}
		if (parts[0].contains(DOLLAR))
		{
			isJSLike = true;
		}
		if (parts.length > 1 && (parts[1].contains(DOLLAR)))
		{
			isJSLike = true;
		}

		if (isJSLike)
		{
			if (parts.length == 1)
			{
				result[0] = clean(parts[0]);
			}
			else if (parts.length == 2)
			{
				result[0] = clean(parts[1]);
				result[1] = clean(parts[0]);
			}
			else
			{
				result[0] = clean(parts[1]);
				result[1] = clean(parts[0]);
				StringBuffer buf = new StringBuffer();
				for (int i = 2; i < parts.length; i++)
				{
					buf.append(parts[i]);
					if (i != parts.length)
					{
						buf.append(' ');
					}
				}
				result[2] = buf.toString();
			}
		}
		else
		{
			if (parts.length == 1)
			{
				result[0] = clean(parts[0]);
			}
			else if (parts.length == 2)
			{
				result[0] = clean(parts[0]);
				result[1] = clean(parts[1]);
			}
			else
			{
				result[0] = clean(parts[0]);
				result[1] = clean(parts[1]);
				StringBuffer buf = new StringBuffer();
				for (int i = 2; i < parts.length; i++)
				{
					buf.append(parts[i]);
					if (i != parts.length)
					{
						buf.append(' ');
					}
				}
				result[2] = buf.toString();
			}
		}

		return result;
	}

	/**
	 * Removes curves around the string.
	 * 
	 * @param in
	 *            - input.
	 * @return string with curves removed.
	 */
	private static String removeCurves(String in)
	{
		String result = in;
		if (result.startsWith(OPEN_BRACKET))
		{
			result = result.substring(1);
		}

		if (result.equals(CLOSE_BRACKET))
		{
			return EMPTY_STRING;
		}

		if (result.endsWith(CLOSE_BRACKET))
		{
			result = result.substring(0, result.length() - 1);
		}

		return result;
	}

	/**
	 * Cleans the string from curves and dollar.
	 * 
	 * @param in
	 *            - input.
	 * @return cleansed string
	 */
	private static String clean(String in)
	{
		String res1 = removeCurves(in);
		return removeDollar(res1);
	}

	/**
	 * Removes dollar symbol.
	 * 
	 * @param in
	 *            - input string.
	 * @return cleansed string.
	 */
	private static String removeDollar(String in)
	{
		if (in.startsWith(DOLLAR))
		{
			return in.substring(1);
		}

		return in;
	}

	public static String computeDocumentation(FunctionDocumentation documentation, String name)
	{
		String additionalInfo = Messages.PHPDocUtils_noAvailableDocs;
		StringBuilder bld = new StringBuilder();

		bld.append("<b>" + name //$NON-NLS-1$
				+ "</b><br>"); //$NON-NLS-1$

		if (documentation != null)
		{

			String longDescription = documentation.getDescription();
			longDescription = longDescription.replaceAll("\r\n", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
			longDescription = longDescription.replaceAll("\r", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
			longDescription = longDescription.replaceAll("\n", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$

			if (longDescription.length() > 0)
			{
				bld.append(longDescription);
				bld.append("<br>"); //$NON-NLS-1$
			}
			TypedDescription[] tagsAsArray = documentation.getParams();
			// buf.append("<br>"); //$NON-NLS-1$
			for (int a = 0; a < tagsAsArray.length; a++)
			{

				bld.append("<br>"); //$NON-NLS-1$

				bld.append("@<b>"); //$NON-NLS-1$
				bld.append("param "); //$NON-NLS-1$
				// buf.append();
				bld.append("</b>"); //$NON-NLS-1$
				bld.append(tagsAsArray[a].getName());
				bld.append(' ');
				for (String s : tagsAsArray[a].getTypes())
				{
					bld.append(s);
					bld.append(' ');
				}
				bld.append(' ');
				bld.append(ContentAssistUtils.truncateLineIfNeeded(tagsAsArray[a].getDescription()));
			}

		}
		else
		{
			bld.append(additionalInfo);
		}

		if (documentation != null)
		{
			TypedDescription return1 = documentation.getReturn();
			if (return1 != null)
			{
				String[] types = return1.getTypes();
				if (types.length > 0)
				{
					bld.append("<br>"); //$NON-NLS-1$
					bld.append("@<b>return </b>"); //$NON-NLS-1$
					bld.append(ContentAssistUtils.truncateLineIfNeeded(return1.getDescription()));

					StringBuilder typesBuilder = new StringBuilder();
					for (int a = 0; a < types.length; a++)
					{
						typesBuilder.append(types[a]);
						typesBuilder.append(' ');
					}
					bld.append(ContentAssistUtils.truncateLineIfNeeded(typesBuilder.toString()));
				}
			}

			ArrayList<TypedDescription> vars = documentation.getVars();
			if (vars != null)
			{
				for (TypedDescription var : vars)
				{

					if (var != null)
					{
						String[] types = var.getTypes();
						if (types.length > 0)
						{
							bld.append("<br>"); //$NON-NLS-1$
							bld.append("<b>"); //$NON-NLS-1$
							bld.append(Messages.PHPDocUtils_documentedType);
							bld.append("</b>"); //$NON-NLS-1$
							bld.append(var.getDescription());

							for (int a = 0; a < types.length; a++)
							{
								bld.append(types[a]);
								bld.append(' ');
							}
						}
					}
				}
			}
		}
		additionalInfo = bld.toString();
		return additionalInfo;
	}
}
