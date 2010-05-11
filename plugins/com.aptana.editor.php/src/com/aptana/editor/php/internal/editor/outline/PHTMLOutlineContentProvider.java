package com.aptana.editor.php.internal.editor.outline;

import com.aptana.editor.html.outline.HTMLOutlineContentProvider;
import com.aptana.editor.php.internal.parser.PHPMimeType;

/**
 * An outline content provider for PHTML (PHP & HTML) content.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHTMLOutlineContentProvider extends HTMLOutlineContentProvider
{
	public PHTMLOutlineContentProvider()
	{
		addSubLanguage(PHPMimeType.MimeType, new PHPOutlineContentProvider());
	}

	@Override
	public Object[] getChildren(Object parentElement)
	{
		return super.getChildren(parentElement);
	}
	
}
