package com.aptana.editor.php.internal.ui.hover;

import org.eclipse.jface.action.ToolBarManager;
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

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.hover.AbstractDocumentationHover#populateToolbarActions(org.eclipse.jface.action.
	 * ToolBarManager)
	 */
	@Override
	public void populateToolbarActions(ToolBarManager tbm)
	{
		// Empty
	}

	// /*
	// * @see ITextHoverExtension#getHoverControlCreator()
	// * @since 3.0
	// */
	// public IInformationControlCreator getHoverControlCreator()
	// {
	// return new IInformationControlCreator()
	// {
	// public IInformationControl createInformationControl(Shell parent)
	// {
	// return new DefaultInformationControl(parent, EditorsUI.getTooltipAffordanceString());
	// }
	// };
	// }
	//
	// /*
	// * @see org.eclipse.jface.text.ITextHoverExtension2#getInformationPresenterControlCreator()
	// * @since 3.4
	// */
	// public IInformationControlCreator getInformationPresenterControlCreator()
	// {
	// return new IInformationControlCreator()
	// {
	// public IInformationControl createInformationControl(Shell shell)
	// {
	// return new DefaultInformationControl(shell, true);
	// }
	// };
	// }
}
