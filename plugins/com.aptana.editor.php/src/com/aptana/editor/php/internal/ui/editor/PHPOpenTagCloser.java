package com.aptana.editor.php.internal.ui.editor;

import org.eclipse.jface.text.ITextViewer;

import com.aptana.editor.html.HTMLOpenTagCloser;

/**
 * An open tag closer that work within PHP files.<br>
 * This tag closer overrides the HTML open tag closer by not treating the PHP tags, or HEREDOC tags, as open tags.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
@SuppressWarnings("nls")
public class PHPOpenTagCloser extends HTMLOpenTagCloser
{

	public PHPOpenTagCloser(ITextViewer textViewer)
	{
		super(textViewer);
	}

	@Override
	protected boolean skipOpenTag(String openTag)
	{
		return super.skipOpenTag(openTag) || openTag.startsWith("<?");
	}
}