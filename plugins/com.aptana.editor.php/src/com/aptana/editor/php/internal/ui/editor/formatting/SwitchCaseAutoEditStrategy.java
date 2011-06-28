package com.aptana.editor.php.internal.ui.editor.formatting;

import java.util.Arrays;
import java.util.HashSet;
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

public class SwitchCaseAutoEditStrategy extends AbstractPHPAutoEditStrategy
{
	// maintained buffer for optimization
	private StringBuilder buffer;
	private Set<String> sameIndentMatch;
	private Set<String> lowerIndentMatch;

	public SwitchCaseAutoEditStrategy(String contentType, PHPSourceViewerConfiguration configuration,
			ISourceViewer sourceViewer)
	{
		super(contentType, configuration, sourceViewer);
		buffer = new StringBuilder();
		sameIndentMatch = new HashSet<String>(Arrays.asList(PHPRegionTypes.PHP_CASE, PHPRegionTypes.PHP_DEFAULT));
		lowerIndentMatch = new HashSet<String>(Arrays.asList(PHPRegionTypes.PHP_SWITCH));
	}

	/**
	 * Returns true in case the command's text is suspected to complete the word 'case' or 'default'.
	 * 
	 * @see com.aptana.editor.php.internal.ui.editor.formatting.AbstractPHPAutoEditStrategy#isValidAutoInsertLocation(org.eclipse.jface.text.IDocument,
	 *      org.eclipse.jface.text.DocumentCommand)
	 */
	@Override
	protected boolean isValidAutoInsertLocation(IDocument document, DocumentCommand command)
	{
		if (command.text == null)
		{
			return false;
		}
		return command.text.endsWith("e") || command.text.endsWith("t"); //$NON-NLS-1$//$NON-NLS-2$
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
		try
		{
			// At this stage, we already know that the command's text ends with 'e' or 't'
			String incompleteWord = null;
			String completeWord = null;
			if (command.text.endsWith("t")) { //$NON-NLS-1$
				incompleteWord = "defaul"; //$NON-NLS-1$
				completeWord = "default"; //$NON-NLS-1$
			}
			else if (command.text.endsWith("e")) { //$NON-NLS-1$
				incompleteWord = "cas"; //$NON-NLS-1$
				completeWord = "case"; //$NON-NLS-1$
			}
			else
			{
				return;
			}
			String type = document.getContentType(command.offset);
			if (!IPHPConstants.DEFAULT.equals(type))
			{
				return;
			}
			if (command.offset < incompleteWord.length() + 1)
			{
				return;
			}
			int length = incompleteWord.length();
			if (!document.get(command.offset - length, length).equals(incompleteWord))
			{
				return;
			}
			indentSwitchCase(completeWord, document, command);
		}
		catch (BadLocationException e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error in the PHP 'switch-case' auto-indent strategy", e); //$NON-NLS-1$
		}
	}

	/*
	 * Indent the switch case
	 */
	private void indentSwitchCase(String completeWord, IDocument document, DocumentCommand command)
			throws BadLocationException
	{
		// We add one, as the letter is just being type
		int startOffset = command.offset - completeWord.length() + 1;
		int lineNumber = document.getLineOfOffset(command.offset);
		IRegion lineInfo = document.getLineInformation(lineNumber);
		int lineOffset = lineInfo.getOffset();
		String startLine = document.get(lineOffset, startOffset - lineOffset);
		// only if the 'case' or 'default' are the first non-whitespace words
		if (startLine.trim().length() == 0)
		{
			buffer.setLength(0);
			// match the indentation of the inserted 'case' or 'default' to a previous 'case' or 'switch'
			matchIndent(document, buffer, lineNumber, startOffset, sameIndentMatch, lowerIndentMatch);
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
