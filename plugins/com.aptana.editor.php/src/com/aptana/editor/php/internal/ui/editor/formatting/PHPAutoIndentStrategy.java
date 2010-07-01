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
	// PHPDocAutoIndentStrategy multiLineCommentStrategy;
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
		// this.multiLineCommentStrategy = new PHPDocAutoIndentStrategy(contentType, configuration, sourceViewer);
	}

	/**
	 * getAutoOverwriteCharacters
	 * 
	 * @return char[]
	 */
	protected char[] getAutoOverwriteCharacters()
	{
		return new char[] { ')', ']', '"', '\'', '}' };
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
		// Lex the partition
		if (command.text.equals("}")) //$NON-NLS-1$
		{
			customizeCloseCurly(document, command, getLexemeProvider(document, command.offset, true));
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
					// multiLineCommentStrategy.customizeDocumentCommand(document, command);
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
				String lexemeTest = floorLexeme.getText();
				if (lexemeTest.equals(":") || lexemeTest.equals(";")) //$NON-NLS-1$ //$NON-NLS-2$
				{
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
					if (BLOCK_TYPES.contains(type))
					{
						command.text += indent;
					}
				}
				else if (indentAfterOpenBrace(document, command) == false)
				{
					super.customizeDocumentCommand(document, command);
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
				if (command.offset > 0)
				{
					char c = d.getChar(command.offset - 1);

					if (c == '{')
					{
						String startIndent = newline + currentLineIndent + indent;

						if (command.offset < d.getLength() && d.getChar(command.offset) == '}')
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

	private void customizeCloseCurly(IDocument document, DocumentCommand command,
			LexemeProvider<PHPTokenType> lexemeProvider)
	{
		try
		{
			int lineOfOffset = document.getLineOfOffset(command.offset);
			IRegion lineInformation = document.getLineInformation(lineOfOffset);
			String string = document.get(lineInformation.getOffset(), lineInformation.getLength());
			if (string.trim().length() == 0)
			{
				int lexemeFloorIndex = lexemeProvider.getLexemeFloorIndex(command.offset);
				int level = 1;
				for (int a = lexemeFloorIndex; a >= 0; a--)
				{
					Lexeme<PHPTokenType> l = lexemeProvider.getLexeme(a);
					String tokenText = l.getText();
					if ("{".equals(tokenText)) //$NON-NLS-1$
					{
						level--;
						if (level == 0)
						{
							String indentationAtOffset = getIndentationAtOffset(document, l.getStartingOffset());
							command.text = indentationAtOffset + "}"; //$NON-NLS-1$
							command.offset = lineInformation.getOffset();
							command.length = string.length();
							return;
						}
					}
					if ("}".equals(tokenText)) //$NON-NLS-1$
					{
						level++;
					}
				}
			}
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
