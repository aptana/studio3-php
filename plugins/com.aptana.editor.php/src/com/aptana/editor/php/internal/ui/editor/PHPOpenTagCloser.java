package com.aptana.editor.php.internal.ui.editor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;

import com.aptana.editor.html.Activator;
import com.aptana.editor.html.OpenTagCloser;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.core.IPHPConstants;

/**
 * An open tag closer that work within PHP files.<br>
 * This tag closer overrides the HTML open tag closer by not treating the PHP tags, or HEREDOC tags, as open tags.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
@SuppressWarnings("nls")
public class PHPOpenTagCloser implements VerifyKeyListener
{
	private ITextViewer textViewer;
	private OpenTagCloser openTagCloser;

	public PHPOpenTagCloser(ITextViewer textViewer)
	{
		this.textViewer = textViewer;
		openTagCloser = new OpenTagCloser(textViewer);
	}

	public static PHPOpenTagCloser install(ITextViewer textViewer)
	{
		PHPOpenTagCloser pairMatcher = new PHPOpenTagCloser(textViewer);
		textViewer.getTextWidget().addVerifyKeyListener(pairMatcher);
		return pairMatcher;
	}

	@Override
	public void verifyKey(VerifyEvent event)
	{
		// early pruning to slow down normal typing as little as possible
		if (!isAutoInsertEnabled() || !isAutoInsertCharacter(event.character))
		{
			return;
		}

		IDocument document = textViewer.getDocument();
		final Point selection = textViewer.getSelectedRange();
		int offset = selection.x;
		try
		{
			String openTag = getOpenTag(document, offset, event);
			if (openTag == null || startsWithPhpOpen(openTag))
			{
				return;
			}
			// Execute the regular HTML open-tag closer.
			openTagCloser.verifyKey(event);
		}
		catch (BadLocationException e)
		{
			PHPEditorPlugin.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
	}

	protected boolean startsWithPhpOpen(String openTag)
	{
		return openTag.startsWith("<?") || openTag.startsWith("<%");
	}

	protected String getOpenTag(IDocument document, int offset, VerifyEvent event) throws BadLocationException
	{
		ITypedRegion[] typedRegions = document.getDocumentPartitioner().computePartitioning(offset, 0);
		if (typedRegions != null && typedRegions.length > 0)
		{
			if (IPHPConstants.DEFAULT.equals(typedRegions[0].getType()))
			{
				return null;
			}
		}
		int start = offset - 1;
		boolean foundFirstChar = false;
		for (int i = offset - 1; i >= 0; i--)
		{
			char c = document.getChar(i);
			if (c == '<')
			{
				start = i;
				break;
			}
			// if last non-WS char is slash, tag is closed
			else if (!Character.isWhitespace(c) && !foundFirstChar)
			{
				if (c == '/')
					return null;
				foundFirstChar = true;
			}
		}
		// document.getDocumentPartitioner().computePartitioning(offset, 0)
		int length = offset - start;
		if (length <= 0)
			return null;
		String tagName = document.get(start, length).trim();
		return tagName;
	}

	protected boolean isAutoInsertCharacter(char character)
	{
		return character == '>';
	}

	protected boolean isAutoInsertEnabled()
	{
		// TODO - Attach this to a preference?
		return true;
	}
}