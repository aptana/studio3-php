package com.aptana.editor.php.internal.ui.editor.formatting;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org2.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.contentAssist.PHPTokenType;
import com.aptana.editor.php.internal.ui.editor.PHPSourceViewerConfiguration;
import com.aptana.parsing.lexer.Lexeme;

/**
 * PHPDoc indentation strategy
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PhpDocAutoIndentStrategy extends AbstractPHPAutoEditStrategy
{
	private static final String PHP_MULTILINE_COMMENT_MID = " * "; //$NON-NLS-1$
	private static final String PHP_MULTILINE_COMMENT_END = " */"; //$NON-NLS-1$

	/**
	 * PhpDocAutoIndentStrategy
	 * 
	 * @param contentType
	 * @param configuration
	 * @param sourceViewer
	 */
	public PhpDocAutoIndentStrategy(String contentType, PHPSourceViewerConfiguration configuration,
			ISourceViewer sourceViewer)
	{
		super(contentType, configuration, sourceViewer);
	}

	/*
	 * @see IAutoIndentStrategy#customizeDocumentCommand
	 */
	public void customizeDocumentCommand(IDocument document, DocumentCommand command)
	{
		innerCustomizeDocumentCommand(document, command);
		this.lexemeProvider = null;
	}

	/*
	 * Do the actual customization
	 * @param document
	 * @param command
	 */
	private void innerCustomizeDocumentCommand(IDocument document, DocumentCommand command)
	{
		if (command.text != null && command.length == 0)
		{
			if (TextUtilities.endsWith(document.getLegalLineDelimiters(), command.text) != -1)
			{
				int offset = command.offset;
				try
				{
					ITypedRegion region = document.getPartition(offset);
					if (region != null)
					{
						// We only need to ask for the lexemes in the comment and nothing more.
						getLexemeProvider(document, offset, false);
						Lexeme<PHPTokenType> lexeme = lexemeProvider.getFloorLexeme(offset);
						String lexemeType = lexeme.getType().getType();
						if (document.getLength() == offset
								&& (lexemeType.equals(PHPRegionTypes.PHPDOC_COMMENT_END) || lexemeType
										.equals(PHPRegionTypes.PHP_COMMENT_END))
								&& document.get(offset - 2, 2).equals("*/")) //$NON-NLS-1$
						{
							// get beginning of the multi-line comment or PHPDoc start.
							// Since we just asked for the lexemes in the offset-region, the lexeme provider holds the
							// open
							// lexeme as the first one.
							Lexeme<PHPTokenType> commentStartLexeme = lexemeProvider.getFirstLexeme();
							// Just append the indentation of that line
							String indent = getIndentationAtOffset(document, commentStartLexeme.getStartingOffset());
							// perform the actual work
							command.shiftsCaret = false;
							command.caretOffset = command.offset + indent.length();
							command.text += indent;
							return;
						}
					}
				}
				catch (BadLocationException e)
				{
					IdeLog.logError(PHPEditorPlugin.getDefault(),
							"Error customizing the auto-indent for a PHP documentation block", e); //$NON-NLS-1$
				}
				indentDocAfterNewLine(document, command);
			}
		}
	}

	/**
	 * Set the indentation from the previous line and adds a space + star.
	 * 
	 * @param document
	 *            the document to work on
	 * @param command
	 *            the command to deal with
	 */
	private void indentDocAfterNewLine(IDocument document, DocumentCommand command)
	{
		// we need to check if we have a comment end or not.
		// in case we don't, we add it. Otherwise, we just add a new line with a '*'.
		Lexeme<PHPTokenType> lastLexeme = this.lexemeProvider.getLastLexeme();
		Lexeme<PHPTokenType> firstLexeme = this.lexemeProvider.getFirstLexeme();
		String indent = getIndentationAtOffset(document, firstLexeme.getStartingOffset());
		String lexemeType = lastLexeme.getType().getType();
		StringBuilder builder = new StringBuilder(command.text);
		builder.append(indent);
		if (command.offset > firstLexeme.getEndingOffset()
				&& (PHPRegionTypes.PHPDOC_COMMENT_END.equals(lexemeType) || PHPRegionTypes.PHP_COMMENT_END
						.equals(lexemeType)))
		{
			// just add a star with the right indentation
			builder.append(PHP_MULTILINE_COMMENT_MID);
		}
		else
		{
			// We also need to add an ending to the comment
			command.shiftsCaret = false;
			command.caretOffset = command.offset + command.text.length() + indent.length()
					+ PHP_MULTILINE_COMMENT_MID.length();
			builder.append(PHP_MULTILINE_COMMENT_MID);
			builder.append(command.text);
			builder.append(indent);
			builder.append(PHP_MULTILINE_COMMENT_END);
		}
		command.text = builder.toString();
		// TODO: Shalom - Add the variables, in case we have a constructor, a function or a class method.
	}
}
