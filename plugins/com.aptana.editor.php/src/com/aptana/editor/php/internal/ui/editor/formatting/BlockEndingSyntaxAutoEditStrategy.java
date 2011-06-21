package com.aptana.editor.php.internal.ui.editor.formatting;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import org2.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.editor.php.internal.ui.editor.PHPSourceViewerConfiguration;

/**
 * A PHP auto-edit strategy for indenting alternative syntax and block-ending elements while typing.<br>
 * The alternative syntax that we are dealing with includes: endif, endwhile, endfor, endforeach and endswitch <br>
 * The block-ending syntax that we are dealing with includes: elseif and else
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class BlockEndingSyntaxAutoEditStrategy extends AbstractPHPAutoEditStrategy
{

	private Map<Character, List<String>> suffixToIncompleteWords;
	private StringBuilder buffer;
	private Map<String, Set<String>> sameIndentMatch;

	/**
	 * Constructs a new BlockEndingSyntaxAutoEditStrategy
	 * 
	 * @param contentType
	 * @param configuration
	 * @param sourceViewer
	 */
	public BlockEndingSyntaxAutoEditStrategy(String contentType, PHPSourceViewerConfiguration configuration,
			ISourceViewer sourceViewer)
	{
		super(contentType, configuration, sourceViewer);
		buffer = new StringBuilder();
		suffixToIncompleteWords = new HashMap<Character, List<String>>();
		suffixToIncompleteWords.put('f', Arrays.asList("endi", "elsei")); //$NON-NLS-1$ //$NON-NLS-2$
		suffixToIncompleteWords.put('e', Arrays.asList("endwhil", "els")); //$NON-NLS-1$ //$NON-NLS-2$
		suffixToIncompleteWords.put('r', Arrays.asList("endfo")); //$NON-NLS-1$
		suffixToIncompleteWords.put('h', Arrays.asList("endforeac", "endswitc")); //$NON-NLS-1$ //$NON-NLS-2$
		
		// create the maps for the indentation matching of a block ending word to it's initial block
		sameIndentMatch = new HashMap<String, Set<String>>();
		sameIndentMatch.put("endif", new HashSet<String>(Arrays.asList(PHPRegionTypes.PHP_IF, PHPRegionTypes.PHP_ELSE, PHPRegionTypes.PHP_ELSEIF))); //$NON-NLS-1$
		sameIndentMatch.put("else", new HashSet<String>(Arrays.asList(PHPRegionTypes.PHP_IF, PHPRegionTypes.PHP_ELSEIF))); //$NON-NLS-1$
		sameIndentMatch.put("elseif", new HashSet<String>(Arrays.asList(PHPRegionTypes.PHP_IF, PHPRegionTypes.PHP_ELSEIF))); //$NON-NLS-1$
		sameIndentMatch.put("endwhile", new HashSet<String>(Arrays.asList(PHPRegionTypes.PHP_WHILE))); //$NON-NLS-1$
		sameIndentMatch.put("endfor", new HashSet<String>(Arrays.asList(PHPRegionTypes.PHP_FOR))); //$NON-NLS-1$
		sameIndentMatch.put("endforeach", new HashSet<String>(Arrays.asList(PHPRegionTypes.PHP_FOREACH))); //$NON-NLS-1$
		sameIndentMatch.put("endswitch", new HashSet<String>(Arrays.asList(PHPRegionTypes.PHP_SWITCH))); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.internal.ui.editor.formatting.AbstractPHPAutoEditStrategy#isValidAutoInsertLocation(org
	 * .eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
	 */
	@Override
	protected boolean isValidAutoInsertLocation(IDocument document, DocumentCommand command)
	{
		if (command.text == null || command.text.length() == 0)
		{
			return false;
		}
		return suffixToIncompleteWords.containsKey(command.text.charAt(command.text.length() - 1));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.internal.ui.editor.formatting.AbstractPHPAutoEditStrategy#customizeDocumentCommand(org.
	 * eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
	 */
	@Override
	public void customizeDocumentCommand(IDocument document, DocumentCommand command)
	{
		// At this point, we only know that the command text ends with f, e, r or h. We need to make sure it's actually
		// an alternative syntax or a block ending word.
		try
		{
			char lastChar = command.text.charAt(command.text.length() - 1);
			List<String> incompleteWords = suffixToIncompleteWords.get(lastChar);
			String incompleteWord = null;
			String completeWord = null;
			for (String word : incompleteWords)
			{
				// For each word that ends with that char, check if the document contains it.
				incompleteWord = word;
				int length = incompleteWord.length();
				if (command.offset >= length + 1)
				{
					if (document.get(command.offset - length, length).equals(incompleteWord))
					{
						// we found a possible match in the document
						completeWord = incompleteWord + lastChar;
					}
				}
			}

			if (completeWord == null)
			{
				return;
			}

			String type = document.getContentType(command.offset);
			if (!IPHPConstants.DEFAULT.equals(type))
			{
				return;
			}

			indentBlockEnding(completeWord, document, command);
		}
		catch (BadLocationException e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error customizing a PHP block-ending command", e); //$NON-NLS-1$
		}
	}

	@SuppressWarnings("unchecked")
	private void indentBlockEnding(String completeWord, IDocument document, DocumentCommand command) throws BadLocationException
	{
		// We add one, as the letter is just being type
		int startOffset = command.offset - completeWord.length() + 1;
		int lineNumber = document.getLineOfOffset(command.offset);
		IRegion lineInfo = document.getLineInformation(lineNumber);
		int lineOffset = lineInfo.getOffset();
		String startLine = document.get(lineOffset, startOffset - lineOffset);
		// only if the word is the first non-whitespace words
		if (startLine.trim().length() == 0)
		{
			buffer.setLength(0);
			// match the indentation of the inserted word to a previous block start word
			matchIndent(document, buffer, lineNumber, startOffset, sameIndentMatch.get(completeWord), Collections.EMPTY_SET);
			String bufferString = buffer.toString();
			if (!bufferString.equals(startLine))
			{
				// we remove some spaces before the text, and practically dedent.
				command.length += (command.offset - lineOffset);
				command.offset = lineOffset;
				command.text = bufferString + completeWord;
			}
		}
	}
}
