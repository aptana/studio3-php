package com.aptana.editor.php.internal.ui.hover;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org2.eclipse.php.internal.ui.text.PHPWordFinder;

import com.aptana.editor.common.contentassist.CommonTextHover;

/**
 * Base class for PHP information hovers
 */
public abstract class AbstractPHPTextHover extends CommonTextHover implements ITextHover, ITextHoverExtension,
		ITextHoverExtension2
{
	/*
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset)
	{
		return PHPWordFinder.findWord(textViewer.getDocument(), offset);
	}

	/**
	 * Returns the PHP elements at the given hover region.
	 * 
	 * @param textViewer
	 *            the text viewer
	 * @param hoverRegion
	 *            the hover region
	 * @return the array with the Java elements or <code>null</code>
	 */
	protected Object[] getPHPElementsAt(ITextViewer textViewer, IRegion hoverRegion)
	{
		return null;
	}
}
