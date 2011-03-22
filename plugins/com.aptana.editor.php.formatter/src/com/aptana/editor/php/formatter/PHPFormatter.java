/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter;

import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_BLOCK;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_BLOCK_IN_CASE;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_BLOCK_IN_SWITCH;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_FUNCTION_DECLARATION;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_TYPE_DECLARATION;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.FORMATTER_INDENTATION_SIZE;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.FORMATTER_TAB_CHAR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.FORMATTER_TAB_SIZE;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_BLOCKS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_BREAK_IN_CASE;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_CASE_BODY;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_FUNCTION_BODY;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_NAMESPACE_BLOCKS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_SWITCH_BODY;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_TYPE_BODY;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.LINES_AFTER_FUNCTION_DECLARATION;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.LINES_AFTER_TYPE_DECLARATION;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.NEW_LINES_BEFORE_CATCH_STATEMENT;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.NEW_LINES_BEFORE_DO_WHILE_STATEMENT;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.NEW_LINES_BEFORE_ELSE_STATEMENT;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.NEW_LINES_BEFORE_IF_IN_ELSEIF_STATEMENT;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.NEW_LINES_BETWEEN_ARRAY_CREATION_ELEMENTS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.PRESERVED_LINES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_ARITHMETIC_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_ARROW_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_ASSIGNMENT_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_CASE_COLON_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_COLON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_COMMAS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_CONCATENATION_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_CONDITIONAL_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_FOR_SEMICOLON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_KEY_VALUE_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_NAMESPACE_SEPARATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_POSTFIX_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_PREFIX_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_RELATIONAL_OPERATORS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_SEMICOLON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_STATIC_INVOCATION_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_UNARY_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_ARITHMETIC_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_ARROW_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_ASSIGNMENT_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_CASE_COLON_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_COLON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_COMMAS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_CONCATENATION_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_CONDITIONAL_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_FOR_SEMICOLON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_KEY_VALUE_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_NAMESPACE_SEPARATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_POSTFIX_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_PREFIX_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_RELATIONAL_OPERATORS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_SEMICOLON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_STATIC_INVOCATION_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_UNARY_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.WRAP_COMMENTS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.WRAP_COMMENTS_LENGTH;

import java.io.StringReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org2.eclipse.php.internal.core.ast.match.ASTMatcher;
import org2.eclipse.php.internal.core.ast.nodes.Program;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.internal.parser.PHPMimeType;
import com.aptana.editor.php.internal.parser.PHPParser;
import com.aptana.editor.php.internal.parser.nodes.PHPASTWrappingNode;
import com.aptana.formatter.AbstractScriptFormatter;
import com.aptana.formatter.FormatterDocument;
import com.aptana.formatter.FormatterIndentDetector;
import com.aptana.formatter.FormatterUtils;
import com.aptana.formatter.FormatterWriter;
import com.aptana.formatter.IFormatterContext;
import com.aptana.formatter.IScriptFormatter;
import com.aptana.formatter.epl.FormatterPlugin;
import com.aptana.formatter.nodes.IFormatterContainerNode;
import com.aptana.formatter.ui.FormatterException;
import com.aptana.formatter.ui.FormatterMessages;
import com.aptana.formatter.ui.ScriptFormattingContextProperties;
import com.aptana.parsing.ast.IParseRootNode;
import com.aptana.parsing.ast.ParseNode;
import com.aptana.parsing.ast.ParseRootNode;
import com.aptana.ui.util.StatusLineMessageTimerManager;

/**
 * PHP code formatter.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPFormatter extends AbstractScriptFormatter implements IScriptFormatter
{

	/**
	 * Brace positions constants
	 */
	protected static final String[] BRACE_POSITIONS = { BRACE_POSITION_BLOCK, BRACE_POSITION_BLOCK_IN_CASE,
			BRACE_POSITION_BLOCK_IN_SWITCH, BRACE_POSITION_FUNCTION_DECLARATION, BRACE_POSITION_TYPE_DECLARATION };

	/**
	 * New-lines constants
	 */
	protected static final String[] NEW_LINES_POSITIONS = { NEW_LINES_BEFORE_CATCH_STATEMENT,
			NEW_LINES_BEFORE_DO_WHILE_STATEMENT, NEW_LINES_BEFORE_ELSE_STATEMENT,
			NEW_LINES_BEFORE_IF_IN_ELSEIF_STATEMENT, NEW_LINES_BETWEEN_ARRAY_CREATION_ELEMENTS };

	/**
	 * Indentation constants
	 */
	protected static final String[] INDENTATIONS = { INDENT_BLOCKS, INDENT_NAMESPACE_BLOCKS, INDENT_CASE_BODY,
			INDENT_SWITCH_BODY, INDENT_FUNCTION_BODY, INDENT_TYPE_BODY, INDENT_BREAK_IN_CASE };

	/**
	 * Spaces constants
	 */
	protected static final String[] SPACES = { SPACES_AFTER_STATIC_INVOCATION_OPERATOR,
			SPACES_BEFORE_STATIC_INVOCATION_OPERATOR, SPACES_BEFORE_ASSIGNMENT_OPERATOR,
			SPACES_AFTER_ASSIGNMENT_OPERATOR, SPACES_BEFORE_COMMAS, SPACES_AFTER_COMMAS,
			SPACES_BEFORE_CASE_COLON_OPERATOR, SPACES_AFTER_CASE_COLON_OPERATOR, SPACES_BEFORE_COLON,
			SPACES_AFTER_COLON, SPACES_BEFORE_SEMICOLON, SPACES_AFTER_SEMICOLON, SPACES_BEFORE_CONCATENATION_OPERATOR,
			SPACES_AFTER_CONCATENATION_OPERATOR, SPACES_BEFORE_ARROW_OPERATOR, SPACES_AFTER_ARROW_OPERATOR,
			SPACES_BEFORE_KEY_VALUE_OPERATOR, SPACES_AFTER_KEY_VALUE_OPERATOR, SPACES_BEFORE_RELATIONAL_OPERATORS,
			SPACES_AFTER_RELATIONAL_OPERATORS, SPACES_BEFORE_CONDITIONAL_OPERATOR, SPACES_AFTER_CONDITIONAL_OPERATOR,
			SPACES_BEFORE_POSTFIX_OPERATOR, SPACES_AFTER_POSTFIX_OPERATOR, SPACES_BEFORE_PREFIX_OPERATOR,
			SPACES_AFTER_PREFIX_OPERATOR, SPACES_BEFORE_ARITHMETIC_OPERATOR, SPACES_AFTER_ARITHMETIC_OPERATOR,
			SPACES_BEFORE_UNARY_OPERATOR, SPACES_AFTER_UNARY_OPERATOR, SPACES_BEFORE_NAMESPACE_SEPARATOR,
			SPACES_AFTER_NAMESPACE_SEPARATOR, SPACES_BEFORE_PARENTHESES, SPACES_AFTER_PARENTHESES,
			SPACES_BEFORE_FOR_SEMICOLON, SPACES_AFTER_FOR_SEMICOLON };

	// PHP basic prefix
	private static final String PHP_PREFIX = "<?php\n"; //$NON-NLS-1$
	// Regex patterns
	private static final Pattern PHP_OPEN_TAG_PATTERNS = Pattern.compile("<\\?php|<\\?=|<%=|<\\?|<\\%"); //$NON-NLS-1$

	private String lineSeparator;

	/**
	 * Constructor.
	 * 
	 * @param preferences
	 */
	protected PHPFormatter(String lineSeparator, Map<String, String> preferences, String mainContentType)
	{
		super(preferences, mainContentType);
		this.lineSeparator = lineSeparator;
	}

	/**
	 * Detects the indentation level.
	 */
	public int detectIndentationLevel(IDocument document, int offset, boolean isSelection,
			IFormattingContext formattingContext)
	{
		int indent = 0;
		if (isSelection)
		{
			offset = ((IRegion) formattingContext.getProperty(FormattingContextProperties.CONTEXT_REGION)).getOffset();
		}
		try
		{
			// detect the indentation offset with the parser, only if the given offset is not the first one in the
			// current
			// partition.
			ITypedRegion partition = document.getPartition(offset);
			if (partition != null && partition.getOffset() == offset)
			{
				int indentationLevel = super.detectIndentationLevel(document, offset);
				// Do some checks to see if we need to return a reduced indentation level.
				// In php, we don't want the indent addition at the beginning.
				char onOffset = document.getChar(offset);
				if (onOffset == '\r')
				{
					if (document.getChar(offset + 1) != '\n')
					{
						return indentationLevel - 1;
					}
				}
				else if (onOffset == '\n')
				{
					return indentationLevel - 1;
				}
				return indentationLevel;
			}
			String source = document.get();
			PHPParser parser = (PHPParser) checkoutParser();
			Program ast = parser.parseAST(new StringReader(source));
			if (ast != null)
			{
				// we wrap the Program with a parser root node to match the API
				IParseRootNode rootNode = new ParseRootNode(PHPMimeType.MIME_TYPE, new ParseNode[0], ast.getStart(),
						ast.getEnd());
				rootNode.addChild(new PHPASTWrappingNode(ast));
				final PHPFormatterNodeBuilder builder = new PHPFormatterNodeBuilder();
				final FormatterDocument formatterDocument = createFormatterDocument(source, offset);
				IFormatterContainerNode root = builder.build(rootNode, formatterDocument);
				new PHPFormatterNodeRewriter(rootNode, formatterDocument).rewrite(root);
				IFormatterContext context = new PHPFormatterContext(0);
				FormatterIndentDetector detector = new FormatterIndentDetector(offset);
				try
				{
					root.accept(context, detector);
					return detector.getLevel();
				}
				catch (Exception e)
				{
					// ignore
				}
			}
		}
		catch (Throwable t)
		{
			return super.detectIndentationLevel(document, offset);
		}
		return indent;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.IScriptFormatter#format(java.lang.String, int, int, int, boolean,
	 * org.eclipse.jface.text.formatter.IFormattingContext, java.lang.String)
	 */
	public TextEdit format(String source, int offset, int length, int indentationLevel, boolean isSelection,
			IFormattingContext context, String indentSufix) throws FormatterException
	{
		int offsetIncludedOpenTag = offset;
		String input;
		int spacesCount = -1;
		if (isSelection)
		{
			IRegion region = (IRegion) context.getProperty(FormattingContextProperties.CONTEXT_REGION);
			offset = region.getOffset();
			length = region.getLength();
			// we need to prepend a <?php prefix to the input. Otherwise, the AST will not get generated.
			input = source.substring(offset, offset + length);
			spacesCount = countLeftWhitespaceChars(input);
			input = PHP_PREFIX + input;
		}
		else
		{
			offsetIncludedOpenTag = Math.max(0, findOpenTagOffset(source, offset));
			input = source.substring(offsetIncludedOpenTag, offset + length);
		}
		// We do not use a parse-state for the PHP, since we are just interested in the AST and do not want to update
		// anything in the indexing.
		try
		{
			PHPParser parser = (PHPParser) checkoutParser(PHPMimeType.MIME_TYPE);
			Program ast = parser.parseAST(new StringReader(input));
			checkinParser(parser);
			if (ast != null)
			{
				// we wrap the Program with a parser root node to match the API
				IParseRootNode rootNode = new ParseRootNode(PHPMimeType.MIME_TYPE, new ParseNode[0], ast.getStart(),
						ast.getEnd());
				rootNode.addChild(new PHPASTWrappingNode(ast));
				String output = format(input, rootNode, indentationLevel, offsetIncludedOpenTag, isSelection,
						indentSufix);
				if (output != null)
				{
					if (!input.equals(output))
					{
						if (equalContent(ast, output))
						{
							// We match the output to all possible PHP open-tags and then trim it to remove it with any
							// other white-space that appear before it.
							// For example, this output:
							// <?php
							// function foo() {}
							// Will be trimmed to:
							// <-- new-line
							// function foo() {}
							if (isSelection)
							{
								String trimmedOutput = output.trim();
								if (trimmedOutput.length() >= PHP_PREFIX.length())
								{
									output = leftTrim(trimmedOutput.substring(PHP_PREFIX.length()), spacesCount);
								}
								else
								{
									output = StringUtil.EMPTY;
								}
							}
							else
							{
								Matcher matcher = PHP_OPEN_TAG_PATTERNS.matcher(output);
								if (matcher.find())
								{
									output = output.substring(matcher.end());
								}
							}
							return new ReplaceEdit(offset, length, output);
						}
						else
						{
							logError(input, output);
						}
					}
					else
					{
						return new MultiTextEdit(); // NOP
					}
				}
			}
		}
		catch (FormatterException e)
		{
			StatusLineMessageTimerManager.setErrorMessage(
					NLS.bind(FormatterMessages.Formatter_formatterParsingErrorStatus, e.getMessage()),
					ERROR_DISPLAY_TIMEOUT, true);
		}
		catch (Exception e)
		{
			StatusLineMessageTimerManager.setErrorMessage(FormatterMessages.Formatter_formatterErrorStatus,
					ERROR_DISPLAY_TIMEOUT, true);
			FormatterPlugin.logError(e);
		}
		return null;
	}

	/**
	 * Check if the formatter did not mess with the AST structure of the code.
	 * 
	 * @param inputAST
	 *            The pre-formatted AST (never null)
	 * @param output
	 *            The output string that the formatter generated.
	 * @return true, if the new AST is equals to the original one; False, otherwise.
	 */
	private boolean equalContent(Program inputAST, String output)
	{
		if (output == null)
		{
			return false;
		}
		output = output.trim();
		PHPParser parser = (PHPParser) checkoutParser(PHPMimeType.MIME_TYPE);
		Program outputAST = parser.parseAST(new StringReader(output));
		checkinParser(parser);
		if (outputAST == null)
		{
			// the inputAST is never null, so we can just return false here
			return false;
		}
		ASTMatcher matcher = new ASTMatcher(true);
		return matcher.match(inputAST.getProgramRoot(), outputAST.getProgramRoot());
	}

	/**
	 * Returns the offset of the PHP open tag that that precedes the given offset location.
	 * 
	 * @param source
	 * @param offset
	 * @return
	 */
	private int findOpenTagOffset(String source, int offset)
	{
		// We just look for the '<' char and that should cover all cases.
		int openOffset = source.lastIndexOf('<', offset);
		if (openOffset > -1)
		{
			return openOffset;
		}
		return offset;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.ui.IScriptFormatter#getIndentSize()
	 */
	public int getIndentSize()
	{
		return getInt(FORMATTER_INDENTATION_SIZE);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.ui.IScriptFormatter#getIndentType()
	 */
	public String getIndentType()
	{
		return getString(FORMATTER_TAB_CHAR);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.ui.IScriptFormatter#getTabSize()
	 */
	public int getTabSize()
	{
		return getInt(FORMATTER_TAB_SIZE);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.IScriptFormatter#getEditorSpecificTabWidth()
	 */
	public int getEditorSpecificTabWidth()
	{
		return FormatterUtils.getEditorTabWidth(PHPEplPlugin.getDefault().getPreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.IScriptFormatter#isEditorInsertSpacesForTabs()
	 */
	public boolean isEditorInsertSpacesForTabs()
	{
		return FormatterUtils.isInsertSpacesForTabs(PHPEplPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Do the actual formatting of the PHP.
	 * 
	 * @param input
	 *            The String input
	 * @param parseResult
	 *            A PHP parser result - {@link com.aptana.parsing.ast.IParseNode}
	 * @param indentationLevel
	 *            The indentation level to start from
	 * @return A formatted string
	 * @throws Exception
	 */
	private String format(String input, IParseRootNode parseResult, int indentationLevel, int offset,
			boolean isSelection, String indentSufix) throws Exception
	{
		final PHPFormatterNodeBuilder builder = new PHPFormatterNodeBuilder();
		final FormatterDocument document = createFormatterDocument(input, offset);
		IFormatterContainerNode root = builder.build(parseResult, document);
		new PHPFormatterNodeRewriter(parseResult, document).rewrite(root);
		IFormatterContext context = new PHPFormatterContext(indentationLevel);
		FormatterWriter writer = new FormatterWriter(document, lineSeparator, createIndentGenerator());
		writer.setWrapLength(getInt(WRAP_COMMENTS_LENGTH));
		writer.setLinesPreserve(getInt(PRESERVED_LINES));
		root.accept(context, writer);
		writer.flush(context);
		// Unlike other formatters, we allow errors in the PHP AST for now.
		// We just notify the user that there were errors in the PHP file.
		if (builder.hasErrors())
		{
			StatusLineMessageTimerManager.setErrorMessage(
					FormatterMessages.Formatter_formatterErrorCompletedWithErrors, ERROR_DISPLAY_TIMEOUT, true);
		}
		return writer.getOutput();
	}

	private FormatterDocument createFormatterDocument(String input, int offset)
	{
		FormatterDocument document = new FormatterDocument(input);
		document.setInt(FORMATTER_TAB_SIZE, getInt(FORMATTER_TAB_SIZE));
		document.setBoolean(WRAP_COMMENTS, getBoolean(WRAP_COMMENTS));
		document.setInt(LINES_AFTER_TYPE_DECLARATION, getInt(LINES_AFTER_TYPE_DECLARATION));
		document.setInt(LINES_AFTER_FUNCTION_DECLARATION, getInt(LINES_AFTER_FUNCTION_DECLARATION));
		document.setInt(ScriptFormattingContextProperties.CONTEXT_ORIGINAL_OFFSET, offset);

		// Set the indentation values
		for (String key : INDENTATIONS)
		{
			document.setBoolean(key, getBoolean(key));
		}
		// Set the new-lines values
		for (String key : NEW_LINES_POSITIONS)
		{
			document.setBoolean(key, getBoolean(key));
		}
		// Set the braces values
		for (String key : BRACE_POSITIONS)
		{
			document.setString(key, getString(key));
		}
		// Set the spaces values
		for (String key : SPACES)
		{
			document.setInt(key, getInt(key));
		}
		return document;
	}
}
