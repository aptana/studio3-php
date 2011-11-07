package com.aptana.editor.php.internal.ui.editor.formatting;

import java.util.Arrays;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org2.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.common.contentassist.ILexemeProvider;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.contentAssist.PHPTokenType;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.editor.php.internal.ui.editor.PHPSourceViewerConfiguration;
import com.aptana.editor.php.util.StringUtils;
import com.aptana.parsing.lexer.Lexeme;

public class PHPAutoIndentStrategy extends AbstractPHPAutoEditStrategy
{
	// PHPCommentAutoIndentStrategy commentStrategy;
	private AbstractPHPAutoEditStrategy multiLineCommentStrategy;
	private AbstractPHPAutoEditStrategy switchCaseAutoEditStrategy;
	private AbstractPHPAutoEditStrategy alternativeSyntaxAutoEditStrategy;

	/**
	 * Constructs a new PHP Auto-Indent-Strategy
	 * 
	 * @param contentType
	 * @param configuration
	 * @param sourceViewer
	 */
	public PHPAutoIndentStrategy(String contentType, PHPSourceViewerConfiguration configuration,
			ISourceViewer sourceViewer)
	{
		super(contentType, configuration, sourceViewer);
		this.switchCaseAutoEditStrategy = new SwitchCaseAutoEditStrategy(contentType, configuration, sourceViewer);
		this.alternativeSyntaxAutoEditStrategy = new BlockEndingSyntaxAutoEditStrategy(contentType, configuration,
				sourceViewer);
		// this.commentStrategy = new PHPCommentAutoIndentStrategy(contentType, configuration, sourceViewer);
		this.multiLineCommentStrategy = new PhpDocAutoIndentStrategy(contentType, configuration, sourceViewer);
	}

	/**
	 * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument,
	 *      org.eclipse.jface.text.DocumentCommand)
	 */
	public void customizeDocumentCommand(IDocument document, DocumentCommand command)
	{
		if (!isAutoIndentEnabled())
		{
			return;
		}
		innerCustomizeDocumentCommand(document, command);
		// we have to reset if for the next run
		this.lexemeProvider = null;
	}

	protected void innerCustomizeDocumentCommand(IDocument document, DocumentCommand command)
	{
		if (command.text == null || command.length > 0)
		{
			return;
		}
		if (command.text.equals("}")) //$NON-NLS-1$
		{
			getLexemeProvider(document, command.offset, true);
			customizeCloseCurly(document, command, lexemeProvider.getFloorLexeme(command.offset));
		}
		try
		{
			ITypedRegion region = document.getPartition(command.offset);
			String regionType = region.getType();
			if (IPHPConstants.PHP_DOC_COMMENT.equals(regionType)
					|| IPHPConstants.PHP_MULTI_LINE_COMMENT.equals(regionType))
			{
				multiLineCommentStrategy.customizeDocumentCommand(document, command);
				return;
			}

			// if (IPHPConstants.PHP_SLASH_LINE_COMMENT.equals(regionType)
			// || IPHPConstants.PHP_HASH_LINE_COMMENT.equals(regionType))
			// {
			// TODO
			// commentStrategy.customizeDocumentCommand(document, command);
			// }
			if (TextUtilities.endsWith(document.getLegalLineDelimiters(), command.text) != -1)
			{

				getLexemeProvider(document, command.offset, true);
				Lexeme<PHPTokenType> floorLexeme = lexemeProvider.getFloorLexeme(command.offset);
				if (floorLexeme == null || floorLexeme.getType() == null)
				{
					return;
				}
				int commandLine = document.getLineOfOffset(command.offset);
				if (PHPRegionTypes.WHITESPACE.equals(floorLexeme.getType().getType())
						&& floorLexeme.getStartingOffset() > region.getOffset())
				{
					// Get the previous lexeme
					floorLexeme = lexemeProvider.getFloorLexeme(floorLexeme.getStartingOffset() - 1);
					int lexemeLine = document.getLineOfOffset(floorLexeme.getStartingOffset());
					if (commandLine - lexemeLine > 0)
					{
						indentAfterNewLine(document, command);
						return;
					}
				}
				String lexemeText = floorLexeme.getText();
				if (lexemeText.equals(":") || lexemeText.equals(";") || lexemeText.equals(")")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				{
					// First, check for a case where we have a 'one-line-block' such as 'if' or 'while' without
					// any curly brackets of colon.
					if (lexemeText.equals(":")) //$NON-NLS-1$
					{
						String indent = indentAfterOneLineBlock(floorLexeme.getStartingOffset(), document);
						if (indent != null)
						{
							command.text += indent;
							return;
						}
					}
					// The colon char can appear in a switch-case blocks and when using an old-style php if-else, loops
					// or switch-case blocks.
					// To decide what is the case here, we look at the first word in the current line.
					Lexeme<PHPTokenType> firstLexemeInLine = getFirstLexemeInLine(document, lexemeProvider,
							floorLexeme.getStartingOffset());
					if (firstLexemeInLine == null)
					{
						return;
					}
					if (lexemeText.equals(";")) { //$NON-NLS-1$
						Lexeme<PHPTokenType> previousNonWhitespaceLexeme = getPreviousNonWhitespaceLexeme(firstLexemeInLine
								.getStartingOffset() - 1);
						if (previousNonWhitespaceLexeme != null)
						{
							String indent = indentAfterOneLineBlock(previousNonWhitespaceLexeme.getStartingOffset(),
									document);
							if (indent != null)
							{
								command.text += indent;
								return;
							}
						}
					}
					if (lexemeText.equals(")")) //$NON-NLS-1$
					{
						indentAfterNewLine(document, command);
						// command.text += configuration.getIndent();
						return;
					}
					indentAfterNewLine(document, command);
					// This will cause a line after a 'block-type' to be indented, even when the
					// type has no open bracket. For now, it's disabled. If we would like to have it enabled, we should
					// consider de-denting the line back if the user start typing a curly-open on that line.

					// Check if it's one of our supported types. If so, indent.
					// String type = firstLexemeInLine.getType().getType();
					// String indent = configuration.getIndent();
					// if (BLOCK_TYPES.contains(type))
					// {
					// command.text += indent;
					// }
					return;
				}
				if (lexemeText.equals("else") || lexemeText.equals("elseif")) //$NON-NLS-1$ //$NON-NLS-2$
				{
					// handle 'else' and 'elseif' blocks that do not have curly-brackets
					indentAfterNewLine(document, command);
					command.text += configuration.getIndent();
				}
				else if (lexemeText.equals("}")) //$NON-NLS-1$
				{
					if (command.offset == floorLexeme.getStartingOffset())
					{
						// check if the caret is right in between the brackets
						Lexeme<PHPTokenType> previousNonWhitespaceLexeme = getPreviousNonWhitespaceLexeme(floorLexeme
								.getStartingOffset() - 1);
						if (previousNonWhitespaceLexeme != null && "{".equals(previousNonWhitespaceLexeme.getText())) //$NON-NLS-1$
						{
							indentAfterOpenBrace(document, command);
						}
						else
						{
							customizeCloseCurly(document, command, floorLexeme);
						}
					}
					else
					{
						customizeCloseCurly(document, command, floorLexeme);
					}
				}
				else if (!indentAfterOpenBrace(document, command))
				{
					command.text += copyIntentationFromPreviousLine(document, command);
					// command.text += getIndentationAtOffset(document, floorLexeme.getStartingOffset());
				}

			}
			else
			{
				// deal with cases where we would like to reduce the indentation.
				if (alternativeSyntaxAutoEditStrategy.isValidAutoInsertLocation(document, command))
				{
					alternativeSyntaxAutoEditStrategy.setLexemeProvider(getLexemeProvider(document, command.offset,
							true));
					alternativeSyntaxAutoEditStrategy.customizeDocumentCommand(document, command);
				}
				if (switchCaseAutoEditStrategy.isValidAutoInsertLocation(document, command))
				{
					switchCaseAutoEditStrategy.setLexemeProvider(getLexemeProvider(document, command.offset, true));
					switchCaseAutoEditStrategy.customizeDocumentCommand(document, command);
				}

			}
		}
		catch (BadLocationException e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error in the PHP auto-indent strategy", e); //$NON-NLS-1$
			return;
		}
	}

	/**
	 * Compute the indentation in cases where a semicolon is entered and we are entering a line under a single-line
	 * block, such as 'if' with no curly brackets, and this is the second line entered under that if.<br>
	 * In that case, we 'dedent' the line to fit under the top-most one-line we have on top of us.
	 * 
	 * @param offset
	 *            - The start offset to begin the lookup
	 * @param document
	 * @return The line indentation string; Null, if no one-line block was identified.
	 * @throws BadLocationException
	 */
	protected String indentAfterOneLineBlock(int offset, IDocument document) throws BadLocationException
	{
		// we need to check at least two, non-empty, lines above this offset to verify
		// if we need to 'dedent'
		IRegion lineInfo = document.getLineInformationOfOffset(offset);
		String indent = null;
		if (lineInfo.getOffset() > 0)
		{
			Lexeme<PHPTokenType> firstLexemeInLine = null;
			int count = 10; // place it here to avoid any unexpected infinite loops..
			do
			{
				if (count-- == 0)
				{
					IdeLog.logWarning(
							PHPEditorPlugin.getDefault(),
							"Stopped a possible infinite loop in the PHPAutoIndentStrategy", PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
					break;
				}
				firstLexemeInLine = getFirstLexemeInLine(document, lexemeProvider, lineInfo.getOffset());
				// Check if the line of lexeme ends with with a curly bracket or a colon.
				// If so, we have a block that span more then one line.
				lineInfo = document.getLineInformationOfOffset(firstLexemeInLine.getStartingOffset());
				Lexeme<PHPTokenType> lastLexemeInLine = getLastLexemeInLine(document, lexemeProvider,
						lineInfo.getOffset());
				if (lastLexemeInLine != null
						&& firstLexemeInLine != null
						&& (BLOCK_TYPES.contains(firstLexemeInLine.getType().getType()) || BLOCK_TYPES
								.contains(lastLexemeInLine.getType().getType())))
				{
					if ("{".equals(lastLexemeInLine.getText()) || ":".equals(lastLexemeInLine.getText())) //$NON-NLS-1$//$NON-NLS-2$
					{
						// it's a block, so return what we have
						if (indent == null)
						{
							return configuration.getIndent()
									+ getIndentationAtOffset(document, firstLexemeInLine.getStartingOffset());
						}
						return indent;
					}
					if ("else".equals(lastLexemeInLine.getText())) //$NON-NLS-1$
					{
						return getIndentationAtOffset(document, firstLexemeInLine.getStartingOffset());
					}
					indent = getIndentationAtOffset(document, firstLexemeInLine.getStartingOffset());
				}
				else
				{
					break;
				}
			}
			while (firstLexemeInLine != null && lineInfo != null);
		}
		return indent;
	}

	/**
	 * Handles the case where we just added a brace, and we want to return and indent to the next line
	 * 
	 * @param d
	 *            Document
	 * @param command
	 *            DocumentCommand
	 * @return True if it succeeded
	 */
	protected boolean indentAfterOpenBrace(IDocument d, DocumentCommand command)
	{
		int offset = command.offset;
		boolean result = false;

		if (offset != -1 && d.getLength() != 0)
		{
			String currentLineIndent = copyIntentationFromPreviousLine(d, command);
			String newline = command.text;
			String indent = configuration.getIndent();
			try
			{
				if (offset > 0)
				{
					// find the first non-whitespace char
					char c;
					do
					{
						c = d.getChar(offset - 1);
						if (Character.isWhitespace(c))
						{
							offset--;
						}
						else
						{
							break;
						}
					}
					while (offset > 0);
					int offsetShift = command.offset - offset;
					if (c == '{')
					{
						String startIndent = newline + currentLineIndent + indent;
						boolean hasClosing = false;
						if (offset < d.getLength())
						{
							int charOffset = offset;
							int charsToDelete = 0;
							int docLen = d.getLength();
							while (charOffset < docLen)
							{
								char next = d.getChar(charOffset);
								if (next == '}')
								{
									hasClosing = true;
									break;
								}
								if (next == '\n' || next == '\r' || !Character.isWhitespace(next)) // $codepro.audit.disable
																									// platformSpecificLineSeparator
								{
									// we could not find any closing bracket
									break;
								}
								charsToDelete++;
								charOffset++;
							}
							if (hasClosing)
							{
								// delete any whitespace chars the we have between the open brace and the close brace
								// before inserting the new lines
								d.replace(offset, charsToDelete, StringUtils.EMPTY);
							}
						}
						if (offset < d.getLength() && d.getChar(offset) == '}')
						{
							command.text = startIndent + newline + currentLineIndent;
						}
						else
						{
							command.text = startIndent;
						}

						command.shiftsCaret = false;
						command.caretOffset = command.offset + startIndent.length() - offsetShift;

						result = true;
					}
				}
			}
			catch (BadLocationException e)
			{
				IdeLog.logError(PHPEditorPlugin.getDefault(), "Error in the PHP auto-indent strategy", e); //$NON-NLS-1$
			}
		}
		if (result)
		{
			command.offset = offset;
		}
		return result;
	}

	private void customizeCloseCurly(IDocument document, DocumentCommand command, Lexeme<PHPTokenType> curlyLexeme)
	{
		try
		{
			if (!"}".equals(command.text)) //$NON-NLS-1$
			{
				int curlyOpenOffset = getPervPairMatchOffset("{", curlyLexeme.getStartingOffset() - 1, document); //$NON-NLS-1$
				Lexeme<PHPTokenType> firstLexemeInLine = getFirstLexemeInLine(document, lexemeProvider, curlyOpenOffset);
				if (firstLexemeInLine != null)
				{
					command.text += getIndentationAtOffset(document, firstLexemeInLine.getStartingOffset());
				}
			}
			else
			{
				Lexeme<PHPTokenType> nonWhitespaceLexeme = getFirstLexemeInLine(document, lexemeProvider,
						command.offset);
				if (nonWhitespaceLexeme == null)
				{
					// cut the spaces before the curly to 'dedent'
					IRegion lineRegion = document.getLineInformationOfOffset(command.offset);
					if (command.offset > lineRegion.getOffset())
					{
						int length = command.offset - lineRegion.getOffset();
						command.offset -= length;
						document.replace(lineRegion.getOffset(), length, StringUtils.EMPTY);
						int curlyOpenOffset = getPervPairMatchOffset("{", command.offset - 1, document); //$NON-NLS-1$
						if (curlyOpenOffset < 0)
						{
							command.text = copyIntentationFromPreviousLine(document, command) + command.text;
						}
						else
						{
							Lexeme<PHPTokenType> firstLexemeInLine = getFirstLexemeInLine(document, lexemeProvider,
									curlyOpenOffset);
							if (firstLexemeInLine != null)
							{
								command.text = getIndentationAtOffset(document, firstLexemeInLine.getStartingOffset())
										+ command.text;
							}
						}
					}
					else
					{
						// we might need to add some indent here. check if this curly closes another curly on a line
						// above us
						int curlyOpenOffset = getPervPairMatchOffset("{", command.offset - 1, document); //$NON-NLS-1$
						if (curlyOpenOffset < 0)
						{
							String indentForCurrentLine = this.copyIntentationFromPreviousLine(document, command);
							command.text = indentForCurrentLine + command.text;
						}
						else
						{
							Lexeme<PHPTokenType> firstLexemeInLine = getFirstLexemeInLine(document, lexemeProvider,
									curlyOpenOffset);
							if (firstLexemeInLine != null)
							{
								command.text = getIndentationAtOffset(document, firstLexemeInLine.getStartingOffset())
										+ command.text;
							}
						}
					}
				}
			}
		}
		catch (BadLocationException e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error in the PHP auto-indent strategy", e); //$NON-NLS-1$
		}

	}

	/**
	 * Copies the indentation of the previous line.
	 * 
	 * @param document
	 *            the document to work on
	 * @param command
	 *            the command to deal with
	 * @return String
	 */
	protected String copyIntentationFromPreviousLine(IDocument document, DocumentCommand command)
	{

		if (command.offset == -1 || document.getLength() == 0)
		{
			return StringUtils.EMPTY;
		}

		try
		{
			// find start of line
			int p = command.offset;
			if (p > 0)
			{
				p--;
			}
			IRegion info = document.getLineInformationOfOffset(p);
			int start = info.getOffset();

			// find white spaces
			int end = findEndOfWhiteSpace(document, start, command.offset);

			StringBuffer buf = new StringBuffer();
			if (end > start)
			{
				// append to input
				buf.append(document.get(start, end - start));
			}

			return buf.toString();

		}
		catch (BadLocationException excp)
		{
			IdeLog.logWarning(
					PHPEditorPlugin.getDefault(),
					"PHP Auto Edit Strategy - Bad location while computing an indentation (copyIntentationFromPreviousLine)", //$NON-NLS-1$
					excp, PHPEditorPlugin.DEBUG_SCOPE);
		}

		return StringUtils.EMPTY;

	}

	/**
	 * canOverwriteBracket
	 * 
	 * @param bracket
	 * @param offset
	 * @param document
	 * @param ll
	 * @return boolean
	 */
	public boolean canOverwriteBracket(char bracket, int offset, IDocument document, ILexemeProvider<PHPTokenType> ll)
	{
		if (offset < document.getLength())
		{
			char[] autoOverwriteChars = getAutoOverwriteCharacters();
			Arrays.sort(autoOverwriteChars);

			if (Arrays.binarySearch(autoOverwriteChars, bracket) < 0)
			{
				return false;
			}
			try
			{
				char sibling = document.getChar(offset);
				return sibling == bracket;
			}
			catch (BadLocationException ex)
			{
				return false;
			}
		}
		return false;
	}
}
