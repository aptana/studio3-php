package com.aptana.editor.php.internal.ui.editor;

import org.eclipse.jface.text.ITextViewer;

import com.aptana.editor.html.OpenTagCloser;

/**
 * An open tag closer that work within PHP files.<br>
 * This tag closer overrides the HTML open tag closer by not treating the PHP tags, or HEREDOC tags, as open tags.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
@SuppressWarnings("nls")
public class PHPOpenTagCloser extends OpenTagCloser
{

	public PHPOpenTagCloser(ITextViewer textViewer)
	{
		super(textViewer);
	}

	public static PHPOpenTagCloser install(ITextViewer textViewer)
	{
		PHPOpenTagCloser pairMatcher = new PHPOpenTagCloser(textViewer);
		textViewer.getTextWidget().addVerifyKeyListener(pairMatcher);
		return pairMatcher;
	}

	@Override
	protected boolean skipOpenTag(String openTag)
	{
		return super.skipOpenTag(openTag) || openTag.startsWith("<?");
	}
}