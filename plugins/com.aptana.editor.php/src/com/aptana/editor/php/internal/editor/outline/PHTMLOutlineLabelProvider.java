package com.aptana.editor.php.internal.editor.outline;

import org.eclipse.swt.graphics.Image;

import com.aptana.editor.html.Activator;
import com.aptana.editor.html.outline.HTMLOutlineLabelProvider;
import com.aptana.editor.php.internal.parser.PHPMimeType;
import com.aptana.editor.php.internal.parser.nodes.PHPBaseParseNode;
import com.aptana.parsing.IParseState;

public class PHTMLOutlineLabelProvider extends HTMLOutlineLabelProvider
{

	private static final Image PHP_ICON = Activator.getImage("icons/element.gif"); //$NON-NLS-1$

	private static final int TRIM_TO_LENGTH = 20;

	private IParseState fParseState;

	public PHTMLOutlineLabelProvider(IParseState parseState)
	{
		fParseState = parseState;
		addSubLanguage(PHPMimeType.MimeType, new PHPOutlineLabelProvider());
	}

	@Override
	protected Image getDefaultImage(Object element)
	{
//		if (element instanceof ERBScript)
//		{
//			return ERB_ICON;
//		}
		return super.getDefaultImage(element);
	}

	@Override
	protected String getDefaultText(Object element)
	{
		if (element instanceof PHPBaseParseNode)
		{
			return getDisplayText((PHPBaseParseNode) element);
		}
		return super.getDefaultText(element);
	}

	private String getDisplayText(PHPBaseParseNode element)
	{
		return element.getNodeName();
	}

//	private String getDisplayText(ERBScript script)
//	{
//		StringBuilder text = new StringBuilder();
//		text.append(script.getStartTag()).append(" "); //$NON-NLS-1$
//		String source = new String(fParseState.getSource());
//		// locates the ruby source
//		IRubyScript ruby = script.getScript();
//		source = source.substring(ruby.getStartingOffset(), ruby.getEndingOffset());
//		// gets the first line of the ruby source
//		StringTokenizer st = new StringTokenizer(source, "\n\r\f"); //$NON-NLS-1$
//		source = st.nextToken();
//		if (source.length() <= TRIM_TO_LENGTH)
//		{
//			text.append(source);
//		}
//		else
//		{
//			text.append(source.substring(0, TRIM_TO_LENGTH - 1)).append("..."); //$NON-NLS-1$
//		}
//		text.append(" ").append(script.getEndTag()); //$NON-NLS-1$
//		return text.toString();
//	}
}