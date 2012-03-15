package com.aptana.editor.php.internal.ui.editor.formatting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org2.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.common.contentassist.ILexemeProvider;
import com.aptana.editor.common.contentassist.LexemeProvider;
import com.aptana.editor.common.preferences.IPreferenceConstants;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.internal.contentAssist.PHPTokenType;
import com.aptana.editor.php.internal.contentAssist.ParsingUtils;
import com.aptana.editor.php.internal.ui.editor.PHPSourceViewerConfiguration;
import com.aptana.editor.php.util.StringUtils;
import com.aptana.parsing.lexer.Lexeme;

/**
 * Base class for PHP auto-indent strategies.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class AbstractPHPAutoEditStrategy implements IAutoEditStrategy
{
	/**
	 * A set of PHP alternate end types, such as endif, endfor and such.
	 */
	protected static Set<String> ALTERNATIVE_END_STYLES = new HashSet<String>(Arrays.asList(
			PHPRegionTypes.PHP_ENDDECLARE, PHPRegionTypes.PHP_ENDFOR, PHPRegionTypes.PHP_ENDFOREACH,
			PHPRegionTypes.PHP_ENDIF, PHPRegionTypes.PHP_ENDSWITCH, PHPRegionTypes.PHP_ENDWHILE));
	/**
	 * A set of possible PHP alternate start types, such as if, for and such.
	 */
	protected static Set<String> ALTERNATIVE_START_STYLES = new HashSet<String>(Arrays.asList(
			PHPRegionTypes.PHP_DECLARE, PHPRegionTypes.PHP_FOR, PHPRegionTypes.PHP_FOREACH, PHPRegionTypes.PHP_IF,
			PHPRegionTypes.PHP_SWITCH, PHPRegionTypes.PHP_WHILE));

	/**
	 * A set of PHP block types, such as for, while and such.
	 */
	protected static Set<String> BLOCK_TYPES = new HashSet<String>(Arrays.asList(PHPRegionTypes.PHP_IF,
			PHPRegionTypes.PHP_FOR, PHPRegionTypes.PHP_FOREACH, PHPRegionTypes.PHP_ELSEIF, PHPRegionTypes.PHP_ELSE,
			PHPRegionTypes.PHP_SWITCH, PHPRegionTypes.PHP_WHILE, PHPRegionTypes.PHP_CASE, PHPRegionTypes.PHP_DEFAULT));
	/**
	 * When we hit those while searching for a pair match, we can tell for sure we are out of the search scope.
	 */
	protected static Set<String> TERMINATORS = new HashSet<String>(Arrays.asList(PHPRegionTypes.PHP_FUNCTION,
			PHPRegionTypes.PHP_CLASS, PHPRegionTypes.PHP_PUBLIC, PHPRegionTypes.PHP_PROTECTED,
			PHPRegionTypes.PHP_PRIVATE));
	/**
	 * Holds the open pair match for the closing php element
	 */
	protected static Map<String, String> PAIR_MATCH = new HashMap<String, String>();
	static
	{
		// Load the pair matches
		PAIR_MATCH.put(PHPRegionTypes.PHP_ENDIF, PHPRegionTypes.PHP_IF);
		PAIR_MATCH.put(PHPRegionTypes.PHP_ENDDECLARE, PHPRegionTypes.PHP_DECLARE);
		PAIR_MATCH.put(PHPRegionTypes.PHP_ENDFOR, PHPRegionTypes.PHP_FOR);
		PAIR_MATCH.put(PHPRegionTypes.PHP_ENDFOREACH, PHPRegionTypes.PHP_FOREACH);
		PAIR_MATCH.put(PHPRegionTypes.PHP_ENDSWITCH, PHPRegionTypes.PHP_SWITCH);
		PAIR_MATCH.put(PHPRegionTypes.PHP_ENDWHILE, PHPRegionTypes.PHP_WHILE);
	}

	/**
	 * To avoid a performance issue of lexing very big files on every type, we limit the lexer to a maximum of 1000
	 * chars from the current offset of the edit.<br>
	 * This should cover most cases, even when we have a comment region that we scan trough.
	 */
	protected static final int MAX_CHARS_TO_LEX_BACK = 1000;
	/**
	 * spaces
	 */
	protected String spaces = "                                                                            "; //$NON-NLS-1$
	protected ILexemeProvider<PHPTokenType> lexemeProvider;
	protected String contentType;
	protected PHPSourceViewerConfiguration configuration;
	protected ISourceViewer sourceViewer;

	/**
	 * Construct a new PHPAutoEditStrategy
	 * 
	 * @param contentType
	 * @param configuration
	 * @param sourceViewer
	 */
	public AbstractPHPAutoEditStrategy(String contentType, PHPSourceViewerConfiguration configuration,
			ISourceViewer sourceViewer)
	{
		this.contentType = contentType;
		this.configuration = configuration;
		this.sourceViewer = sourceViewer;

	}

	/**
	 * Returns a cached lexeme-provider, or create and return a new one.<br>
	 * In case the includeOtherPartitions is true, the returned lexeme list will hold lexemes from other partition types
	 * that are located <b>above</b> the given offset. Otherwise, only the lexeme provider will only hold the lexemes in
	 * the partition that contains the offset.
	 * 
	 * @param document
	 * @param offset
	 * @param includeOtherPartitions
	 * @return A {@link LexemeProvider}
	 * @see #setLexemeProvider(LexemeProvider)
	 */
	protected ILexemeProvider<PHPTokenType> getLexemeProvider(IDocument document, int offset,
			boolean includeOtherPartitions)
	{
		if (lexemeProvider == null)
		{
			if (includeOtherPartitions)
			{
				lexemeProvider = ParsingUtils.createLexemeProvider(document,
						Math.max(0, offset - MAX_CHARS_TO_LEX_BACK), offset);
			}
			else
			{
				lexemeProvider = ParsingUtils.createLexemeProvider(document, offset);
			}
		}
		return lexemeProvider;
	}

	/**
	 * Set a lexeme-provider for use with this auto-edit strategy class.<br>
	 * This method can be called from other auto-edit strategies to avoid the expensive re-computation of the lexemes in
	 * case we already have them in hand.
	 * 
	 * @param lexemeProvider
	 * @see #getLexemeProvider(IDocument)
	 */
	protected void setLexemeProvider(ILexemeProvider<PHPTokenType> lexemeProvider)
	{
		this.lexemeProvider = lexemeProvider;
	}

	/**
	 * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument,
	 *      org.eclipse.jface.text.DocumentCommand)
	 */
	public void customizeDocumentCommand(IDocument document, DocumentCommand command)
	{
		if (command.text == null || command.length > 0 || !isAutoIndentEnabled())
		{
			return;
		}

		String[] lineDelimiters = document.getLegalLineDelimiters();
		int index = TextUtilities.endsWith(lineDelimiters, command.text);
		if (index > -1)
		{
			// ends with line delimiter
			if (lineDelimiters[index].equals(command.text))
			{
				indentAfterNewLine(document, command);
			}
			return;
		}
		// // todo: ensure we actually need this here
		//		else if (command.text.equals("\t")) //$NON-NLS-1$
		// {
		// if (configuration instanceof UnifiedConfiguration)
		// {
		// UnifiedConfiguration uc = (UnifiedConfiguration) configuration;
		// if (uc.useSpacesAsTabs())
		// {
		// command.text = uc.getTabAsSpaces();
		// }
		// }
		// }
		else if (command.text.length() == 1 && isAutoInsertCharacter(command.text.charAt(0)) && isAutoInsertEnabled()
				&& isValidAutoInsertLocation(document, command))
		{
			char current = command.text.charAt(0);

			if (overwriteBracket(current, document, command))
			{
				return;
			}
		}
	}

	/**
	 * Match the indentation of the inserted keyword to a previous keyword. This methods traverse the lines up one by
	 * one and look into the first lexeme at each line. It stops and add indentation to the buffer when it hits a type
	 * that matches an item from the sameIntentItems list or the lowerIndentItems list.
	 * 
	 * @param document
	 * @param indentationBuffer
	 * @param lineNumber
	 * @param offset
	 * @param sameIndentItems
	 *            - a set of items that are defined to be on the same level of indentation.
	 * @param lowerIndentItems
	 *            - a set of items that are defined to be one level of indentation less.
	 * @throws BadLocationException
	 */
	protected void matchIndent(IDocument document, StringBuilder indentationBuffer, int lineNumber, int offset,
			Set<String> sameIndentItems, Set<String> lowerIndentItems) throws BadLocationException
	{
		IRegion currentLineInfo = document.getLineInformationOfOffset(offset);
		int lineStartOffset = currentLineInfo.getOffset();
		if (lineStartOffset == 0)
		{
			return;
		}
		do
		{
			currentLineInfo = document.getLineInformationOfOffset(lineStartOffset - 1);
			lineStartOffset = currentLineInfo.getOffset();

			Lexeme<PHPTokenType> firstLexemeInLine = getFirstLexemeInLine(document, lexemeProvider, lineStartOffset);
			if (firstLexemeInLine != null)
			{
				String type = firstLexemeInLine.getType().getType();
				if (TERMINATORS.contains(type))
				{
					return;
				}
				// We check for lexeme that ends a block (either a closing bracket or an endxxx lexeme), we have
				// to find the matching lexeme and skip the entire section of lexemes in between.
				String pairToFind = null;
				if ("}".equals(firstLexemeInLine.getText())) { //$NON-NLS-1$
					pairToFind = "{"; //$NON-NLS-1$
				}
				else
				{
					pairToFind = getLexemePair(firstLexemeInLine);
				}

				// We can do this section of code only if we are certain that we are on the same level
				if (pairToFind == null)
				{
					if (sameIndentItems != null && sameIndentItems.contains(type))
					{
						indentationBuffer
								.append(getIndentationAtOffset(document, firstLexemeInLine.getStartingOffset()));
						return;
					}
					if (lowerIndentItems != null && lowerIndentItems.contains(type))
					{
						String indent = getIndentationAtOffset(document, firstLexemeInLine.getStartingOffset());
						indent += configuration.getIndent(); // $codepro.audit.disable stringConcatenationInLoop
						indentationBuffer.append(indent);
						return;
					}
				}
				else
				{
					// Do a detailed scan lexeme-by-lexeme until we find a match.
					// In case we can't find any, -1 is returned and will cause this loop to end.
					lineStartOffset = getPervPairMatchOffset(pairToFind, currentLineInfo.getOffset(), document);
					if (lineStartOffset > 0)
					{
						lineStartOffset++;
					}
				}
			}
		}
		while (lineStartOffset > 0);
	}

	/**
	 * Do a more precise scan backwards for the opening match of the given pair string.
	 * 
	 * @param pairToFind
	 * @param offset
	 *            - The offset to start scanning from (The scan is done backward)
	 * @param document
	 * @return the offset of the start line we found the pair at; -1, in case we could not locate the match
	 * @throws BadLocationException
	 */
	protected int getPervPairMatchOffset(String pairToFind, int offset, IDocument document) throws BadLocationException
	{
		// We have to maintain a stack of elements. There is always a chance that we have more blocks in our search
		// scope.
		Stack<String> stack = new Stack<String>();
		stack.push(pairToFind);
		Lexeme<PHPTokenType> lexeme = lexemeProvider.getFloorLexeme(offset);
		while (!stack.isEmpty() && lexeme != null && lexeme.getStartingOffset() > 0)
		{
			String type = lexeme.getType().getType();
			String nextToMatch = stack.peek(); // there is always something in this stack
			if (nextToMatch.equals(type) || nextToMatch.equals(lexeme.getText()))
			{
				// found a match!
				// We just need to check we have only one item in the stack to be certain we got the pair
				if (stack.size() == 1)
				{
					IRegion lexemeLine = document.getLineInformationOfOffset(lexeme.getStartingOffset());
					return lexemeLine.getOffset();
				}
				else
				{
					// just pop an element
					stack.pop();
				}
			}
			else
			{
				// we need to check if we got another block ending here
				if ("}".equals(lexeme.getText())) { //$NON-NLS-1$
					stack.push("{"); //$NON-NLS-1$
				}
				else
				{
					String pair = getLexemePair(lexeme);
					if (pair != null)
					{
						stack.push(pair);
					}
				}
			}
			lexeme = lexemeProvider.getFloorLexeme(lexeme.getStartingOffset() - 1);
		}
		return -1;
	}

	protected Lexeme<PHPTokenType> getPreviousNonWhitespaceLexeme(int offset)
	{
		int index = lexemeProvider.getLexemeFloorIndex(offset);
		Lexeme<PHPTokenType> lexeme = lexemeProvider.getLexeme(index);
		while (lexeme != null && PHPRegionTypes.WHITESPACE.equals(lexeme.getType().getType()) && index > 0)
		{
			index--;
			lexeme = lexemeProvider.getLexeme(index);
		}
		if (lexeme != null && !PHPRegionTypes.WHITESPACE.equals(lexeme.getType().getType()))
		{
			return lexeme;
		}
		return null;
	}

	/**
	 * Returns the open pair type for the given lexeme (in case it represents a closing element)
	 * 
	 * @param lexeme
	 * @return An open element type, or null.
	 */
	protected String getLexemePair(Lexeme<PHPTokenType> lexeme)
	{
		return PAIR_MATCH.get(lexeme.getType().getType());
	}

	/**
	 * isValidAutoInsertLocation
	 * 
	 * @param d
	 * @param c
	 * @return boolean
	 */
	protected boolean isValidAutoInsertLocation(IDocument document, DocumentCommand command)
	{
		return true;
	}

	/**
	 * overwriteBracket
	 * 
	 * @param bracket
	 * @param document
	 * @param command
	 * @param ll
	 * @return boolean
	 */
	public boolean overwriteBracket(char bracket, IDocument document, DocumentCommand command)
	{
		// if next character is "closing" char, overwrite
		if (canOverwriteBracket(bracket, command.offset, document))
		{
			command.text = StringUtils.EMPTY;
			command.shiftsCaret = false;
			command.caretOffset = command.offset + 1;
			return true;
		}

		return false;
	}

	/**
	 * Returns the first, non-whitespace, lexeme in the line of the given offset.
	 * 
	 * @param document
	 * @param lexemeProvider
	 * @param startingOffset
	 * @return The first non-whitespace lexeme in the given line. Null, if none is found.
	 * @throws BadLocationException
	 */
	protected Lexeme<PHPTokenType> getFirstLexemeInLine(IDocument document,
			ILexemeProvider<PHPTokenType> lexemeProvider, int offset) throws BadLocationException
	{
		if (offset < 0)
		{
			return null;
		}
		IRegion lineRegion = document.getLineInformationOfOffset(offset);
		Lexeme<PHPTokenType> lexeme = lexemeProvider.getCeilingLexeme(lineRegion.getOffset());
		if (lexeme == null || !PHPRegionTypes.WHITESPACE.equals(lexeme.getType().getType()))
		{
			return lexeme;
		}
		// The first non-whitespace lexeme should be on our right
		int index = lexemeProvider.getLexemeIndex(lexeme.getStartingOffset());
		if (index + 1 < lexemeProvider.size())
		{
			lexeme = lexemeProvider.getLexeme(index + 1);
			if (lexeme.getStartingOffset() < lineRegion.getOffset() + lineRegion.getLength())
			{
				return lexeme;
			}
		}
		return null;
	}

	/**
	 * Returns the last, non-whitespace, lexeme in the line of the given offset.
	 * 
	 * @param document
	 * @param lexemeProvider
	 * @param startingOffset
	 * @return The last non-whitespace lexeme in the given line. Null, if none is found.
	 * @throws BadLocationException
	 */
	protected Lexeme<PHPTokenType> getLastLexemeInLine(IDocument document,
			ILexemeProvider<PHPTokenType> lexemeProvider, int offset) throws BadLocationException
	{
		IRegion lineRegion = document.getLineInformationOfOffset(offset);
		int lastCharInLine = lineRegion.getOffset() + lineRegion.getLength();
		if (lineRegion.getLength() > 0)
		{
			lastCharInLine--;
		}
		Lexeme<PHPTokenType> lexeme = lexemeProvider.getFloorLexeme(lastCharInLine);
		if (lexeme == null || !PHPRegionTypes.WHITESPACE.equals(lexeme.getType().getType()))
		{
			return lexeme;
		}
		// The first non-whitespace lexeme should be on our left
		int index = lexemeProvider.getLexemeIndex(lexeme.getStartingOffset());
		if (index - 1 > 0)
		{
			lexeme = lexemeProvider.getLexeme(index - 1);
			if (lexeme.getStartingOffset() > lineRegion.getOffset())
			{
				return lexeme;
			}
		}
		return null;
	}

	/**
	 * Returns the first, non-whitespace, lexeme in the first non-empty line <b>above</b> the line at the given offset.
	 * 
	 * @param document
	 * @param lexemeProvider
	 * @param startingOffset
	 * @return The first non-whitespace lexeme in the first, non-empty, line above the offset.
	 * @throws BadLocationException
	 */
	protected Lexeme<PHPTokenType> getFirstLexemeInNonEmptyLine(IDocument document,
			ILexemeProvider<PHPTokenType> lexemeProvider, int offset) throws BadLocationException
	{
		IRegion lineInfo = null;
		Lexeme<PHPTokenType> lexeme = null;
		do
		{
			lineInfo = document.getLineInformationOfOffset(offset);
			lexeme = getFirstLexemeInLine(document, lexemeProvider, lineInfo.getOffset() - 1);
			if (lineInfo != null)
			{
				offset = lineInfo.getOffset() - 1;
			}
		}
		while (lexeme == null && lineInfo != null && lineInfo.getOffset() > 0 && offset > 0);
		return lexeme;
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
	public boolean canOverwriteBracket(char bracket, int offset, IDocument document)
	{
		if (offset < document.getLength())
		{
			char[] autoOverwriteChars = getAutoOverwriteCharacters();
			Arrays.sort(autoOverwriteChars);

			if (Arrays.binarySearch(autoOverwriteChars, bracket) < 0)
			{
				return false;
			}

			// If the next char is a ">", our tag is already closed
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

	/**
	 * getAutoOverwriteCharacters
	 * 
	 * @return char[]
	 */
	protected char[] getAutoOverwriteCharacters()
	{
		return new char[] { ')', '>', ']', '"', '\'', '}' };
	}

	/**
	 * Returns the preference value of the auto insert.
	 * 
	 * @return True by default.
	 */
	protected boolean isAutoInsertEnabled()
	{
		// TODO: Shalom Attach this to the php/studio preferences.
		return true;
	}

	/**
	 * Returns the preference value of auto insert indents
	 */

	protected boolean isAutoIndentEnabled()
	{
		return PHPEplPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.EDITOR_AUTO_INDENT);
	}

	/**
	 * indentAfterNewLine
	 * 
	 * @param d
	 * @param c
	 */
	protected void indentAfterNewLine(IDocument d, DocumentCommand c)
	{
		String indentString = configuration.getIndent();

		// nothing to add if nothing to add
		if (indentString.equals(StringUtils.EMPTY))
		{
			return;
		}

		int offset = c.offset;

		if (offset == -1 || d.getLength() == 0)
		{
			return;
		}

		c.text += getIndentationAtOffset(d, offset);

		return;
	}

	/**
	 * getIndentationAtOffset
	 * 
	 * @param d
	 * @param offset
	 * @return String
	 */
	protected String getIndentationAtOffset(IDocument d, int offset)
	{
		String indentation = StringUtils.EMPTY;
		try
		{
			int p = ((offset == d.getLength()) ? offset - 1 : offset);
			IRegion line = d.getLineInformationOfOffset(p);

			int lineOffset = line.getOffset();
			int firstNonWS = findEndOfWhiteSpace(d, lineOffset, offset);

			indentation = getIndentationString(d, lineOffset, firstNonWS);

		}
		catch (BadLocationException excp)
		{
			IdeLog.logWarning(
					PHPEditorPlugin.getDefault(),
					"PHP Auto Edit Strategy - Bad location while computing the indentation at offset (getIndentationAtOffset)", //$NON-NLS-1$
					excp, PHPEditorPlugin.DEBUG_SCOPE);
		}

		return indentation;
	}

	protected int findEndOfWhiteSpace(IDocument document, int offset, int end) throws BadLocationException
	{
		while (offset < end)
		{
			char c = document.getChar(offset);
			if (c != ' ' && c != '\t')
			{
				return offset;
			}
			offset++;
		}
		return end;
	}

	/**
	 * @param c
	 * @return
	 */
	private boolean isAutoInsertCharacter(char c)
	{
		int val = Arrays.binarySearch(getAutoInsertCharacters(), c);
		return val >= 0;
	}

	/**
	 * getAutoInsertCharacters
	 * 
	 * @return char[]
	 */
	protected char[] getAutoInsertCharacters()
	{
		return new char[] { '(', '<', '[', '"', '\'', '{' };
	}

	/**
	 * Calculates the whitespace prefix based on user prefs and the existing line. Eg: if the line prefix is five
	 * spaces, and user pref is tabs of width 4, then the result is "/t ".
	 * 
	 * @param d
	 * @param lineOffset
	 * @param firstNonWS
	 * @return Returns the whitespace prefix based on user prefs and the existing line.
	 */
	protected String getIndentationString(IDocument d, int lineOffset, int firstNonWS)
	{
		String lineIndent = StringUtils.EMPTY;
		try
		{
			lineIndent = d.get(lineOffset, firstNonWS - lineOffset);
		}
		catch (BadLocationException e1)
		{
			IdeLog.logWarning(PHPEditorPlugin.getDefault(),
					"PHP Auto Edit Strategy - Bad location while computing a line indentation (getIndentationString)", //$NON-NLS-1$
					e1, PHPEditorPlugin.DEBUG_SCOPE);
		}
		if (lineIndent.equals(StringUtils.EMPTY))
		{
			return lineIndent;
		}

		int indentSize = 0;
		int tabWidth = Math.max(1, this.configuration.getTabWidth(sourceViewer));
		char[] indentChars = lineIndent.toCharArray();
		for (int i = 0; i < indentChars.length; i++)
		{
			char e = indentChars[i];
			if (e == '\t')
			{
				indentSize += tabWidth - (indentSize % tabWidth);
			}
			else
			{
				indentSize++;
			}
		}
		String indentString = configuration.getIndent();
		int indentStringWidth = (indentString.equals("\t")) ? tabWidth : indentString.length(); //$NON-NLS-1$
		// return in case tab width is zero
		if (indentStringWidth == 0)
		{
			return StringUtils.EMPTY;
		}
		int indentCount = (int) Math.floor(indentSize / indentStringWidth); // assume no dived by zero from above tests

		StringBuilder indentation = new StringBuilder();
		for (int i = 0; i < indentCount; i++)
		{
			indentation.append(indentString);
		}
		// here we might want to allow one tab when there are three spaces on the previous line when tabwdith = 4
		// logic is just get the ending from the previous line
		int extra = indentSize % indentStringWidth;
		indentation.append(spaces.substring(0, extra));// lineIndent.substring(lineIndent.length() - extra);

		return indentation.toString();
	}

}
