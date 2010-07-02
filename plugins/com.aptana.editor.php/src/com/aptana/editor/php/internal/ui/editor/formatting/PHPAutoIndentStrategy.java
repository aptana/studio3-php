package com.aptana.editor.php.internal.ui.editor.formatting;

import java.util.Arrays;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;

import com.aptana.editor.common.contentassist.LexemeProvider;
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
		if (TextUtilities.endsWith(document.getLegalLineDelimiters(), command.text) != -1)
		{
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

				if (IPHPConstants.PHP_COMMENT.equals(regionType))
				{
					// commentStrategy.customizeDocumentCommand(document, command);
				}
				getLexemeProvider(document, command.offset, true);
				Lexeme<PHPTokenType> floorLexeme = lexemeProvider.getFloorLexeme(command.offset);
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
					if (lexemeText.equals(";")) //$NON-NLS-1$
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
					Lexeme<PHPTokenType> firstLexemeInLine = getFirstLexemeInLine(document, lexemeProvider, floorLexeme
							.getStartingOffset());
					if (firstLexemeInLine == null)
					{
						return;
					}
					// Check if it's one of our supported types. If so, indent.
					String type = firstLexemeInLine.getType().getType();
					String indent = configuration.getIndent();
					indentAfterNewLine(document, command);
					// We also check ')' against the BLOCK_TYPES set, although it contains some types that
					// are not 'legally' allowed here (such as 'case')
					if (BLOCK_TYPES.contains(type))
					{
						command.text += indent;
					}
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
				else if (indentAfterOpenBrace(document, command) == false)
				{
					// command.text += getIndentationAtOffset(document, floorLexeme.getStartingOffset());
				}
			}
			catch (BadLocationException e)
			{
				PHPEditorPlugin.logError(e);
				return;
			}
		}
		else
		{
			// deal with cases where we would like to reduce the indentation.
			if (alternativeSyntaxAutoEditStrategy.isValidAutoInsertLocation(document, command))
			{
				alternativeSyntaxAutoEditStrategy.setLexemeProvider(getLexemeProvider(document, command.offset, true));
				alternativeSyntaxAutoEditStrategy.customizeDocumentCommand(document, command);
			}
			if (switchCaseAutoEditStrategy.isValidAutoInsertLocation(document, command))
			{
				switchCaseAutoEditStrategy.setLexemeProvider(getLexemeProvider(document, command.offset, true));
				switchCaseAutoEditStrategy.customizeDocumentCommand(document, command);
			}

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
			do
			{
				firstLexemeInLine = getFirstLexemeInNonEmptyLine(document, lexemeProvider, lineInfo.getOffset());
				// Check if the line of lexeme ends with with a curly bracket or a colon.
				// If so, we have a block that span more then one line.
				lineInfo = document.getLineInformationOfOffset(firstLexemeInLine.getStartingOffset());
				Lexeme<PHPTokenType> lastLexemeInLine = getLastLexemeInLine(document, lexemeProvider, lineInfo
						.getOffset());
				if (lastLexemeInLine != null && firstLexemeInLine != null
						&& BLOCK_TYPES.contains(firstLexemeInLine.getType().getType()))
				{
					if ("{".equals(lastLexemeInLine.getText()) || ":".equals(lastLexemeInLine.getText())) //$NON-NLS-1$ //$NON-NLS-2$
					{
						// it's a block, so return what we have
						return indent;
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
			String currentLineIndent = getIndentForCurrentLine(d, command);
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
					command.offset = offset;
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
								if (next == '\n' || next == '\r' || !Character.isWhitespace(next))
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
						command.caretOffset = command.offset + startIndent.length();

						result = true;
					}
				}
			}
			catch (BadLocationException e)
			{
				PHPEditorPlugin.logError(e);
			}
		}
		return result;
	}

	private void customizeCloseCurly(IDocument document, DocumentCommand command, Lexeme<PHPTokenType> curlyLexeme)
	{
		try
		{
			int curlyOpenOffset = getForPairMatchOffset("{", curlyLexeme.getStartingOffset() - 1, document); //$NON-NLS-1$
			Lexeme<PHPTokenType> firstLexemeInLine = getFirstLexemeInLine(document, lexemeProvider, curlyOpenOffset);
			command.text += getIndentationAtOffset(document, firstLexemeInLine.getStartingOffset());
		}
		catch (BadLocationException e)
		{
			PHPEditorPlugin.logError(e);
		}

	}

	/**
	 * Handles the
	 * 
	 * @param d
	 *            Document
	 * @param command
	 *            DocumentCommand
	 * @param oldStyleLexeme
	 * @return True if it succeeded
	 */
	protected boolean indentAfterOldStyle(IDocument d, DocumentCommand command, Lexeme<PHPTokenType> oldStyleLexeme)
	{
		int offset = command.offset;
		boolean result = false;

		if (offset != -1 && d.getLength() != 0)
		{
			String indent = getIndentForCurrentLine(d, command);
			String newline = command.text;
			String tab = configuration.getIndent();
			try
			{
				if (command.offset > 0)
				{
					char c = d.getChar(command.offset - 1);
					if (c == ':')
					{
						String startIndent = newline + indent + tab;

						if (isAutoInsertEnabled())
						{

							command.text = startIndent + newline + indent // +
									// newline
									// +
									// startIndent
									+ "end" + oldStyleLexeme.getText(); //$NON-NLS-1$
						}
						else
						{
							command.text = startIndent;// + newline + indent; //
							// + newline +
							// startIndent

						}

						command.shiftsCaret = false;
						command.caretOffset = command.offset + startIndent.length();

						result = true;
					}
					else
					{
						command.text = newline + indent;
					}
				}
			}
			catch (BadLocationException e)
			{
				PHPEditorPlugin.logError(e);
			}
		}

		return result;
	}

	/**
	 * Copies the indentation of the previous line.
	 * 
	 * @param d
	 *            the document to work on
	 * @param c
	 *            the command to deal with
	 * @return String
	 */
	protected String getIndentForCurrentLine(IDocument d, DocumentCommand c)
	{

		if (c.offset == -1 || d.getLength() == 0)
		{
			return StringUtils.EMPTY;
		}

		try
		{
			// find start of line
			int p = (c.offset == d.getLength() ? c.offset - 1 : c.offset);
			IRegion info = d.getLineInformationOfOffset(p);
			int start = info.getOffset();

			// find white spaces
			int end = findEndOfWhiteSpace(d, start, c.offset);

			StringBuffer buf = new StringBuffer();
			if (end > start)
			{
				// append to input
				buf.append(d.get(start, end - start));
			}

			return buf.toString();

		}
		catch (BadLocationException excp)
		{
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
	public boolean canOverwriteBracket(char bracket, int offset, IDocument document, LexemeProvider<PHPTokenType> ll)
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
