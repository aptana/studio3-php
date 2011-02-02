package com.aptana.editor.php.internal.ui.hover;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.php.internal.ui.text.PHPWordFinder;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.EditorsUI;

/**
 * Base class for PHP information hovers
 */
public abstract class AbstractPHPTextHover implements ITextHover, ITextHoverExtension, ITextHoverExtension2
{
	private IEditorPart fEditor;

	public void setEditor(IEditorPart editor)
	{
		fEditor = editor;
	}

	protected IEditorPart getEditor()
	{
		return fEditor;
	}

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
	 * @see ITextHoverExtension#getHoverControlCreator()
	 * @since 3.0
	 */
	public IInformationControlCreator getHoverControlCreator()
	{
		return new IInformationControlCreator()
		{
			public IInformationControl createInformationControl(Shell parent)
			{
				return new DefaultInformationControl(parent, EditorsUI.getTooltipAffordanceString());
			}
		};
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension2#getInformationPresenterControlCreator()
	 * @since 3.4
	 */
	public IInformationControlCreator getInformationPresenterControlCreator()
	{
		return new IInformationControlCreator()
		{
			public IInformationControl createInformationControl(Shell shell)
			{
				return new DefaultInformationControl(shell, true);
			}
		};
	}
}
