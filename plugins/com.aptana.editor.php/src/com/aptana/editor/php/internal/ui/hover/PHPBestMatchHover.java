/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.hover;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.ui.IEditorPart;

import com.aptana.editor.common.hover.AbstractDocumentationHover;
import com.aptana.editor.common.hover.CustomBrowserInformationControl;
import com.aptana.editor.php.PHPEditorPlugin;

/**
 * Best match hover information provider.<br>
 * This provider will try several hover contributers until a valid information is retrieved.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPBestMatchHover extends AbstractPHPTextHover
{
	private static final String DEBUG_HOVER_ID = "com.aptana.php.debug.debugHover"; //$NON-NLS-1$
	private List<PHPTextHoverDescriptor> textHoverDescriptors;
	private List<AbstractPHPTextHover> instantiatedTextHovers;
	private AbstractPHPTextHover bestHover;

	/**
	 * Constructs a new best match text hover.
	 */
	public PHPBestMatchHover()
	{
		this.textHoverDescriptors = PHPHoverRegistry.getInstance().getTextHoversDescriptors();
		this.instantiatedTextHovers = new ArrayList<AbstractPHPTextHover>(this.textHoverDescriptors.size());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.internal.ui.hover.AbstractPHPTextHover#getHoverInfo2(org.eclipse.jface.text.ITextViewer,
	 * org.eclipse.jface.text.IRegion)
	 */
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion)
	{
		checkHovers();
		for (AbstractPHPTextHover hover : instantiatedTextHovers)
		{
			hover.setEditor(getEditor());
			Object info = hover.getHoverInfo2(textViewer, hoverRegion);
			if (info != null)
			{
				bestHover = hover;
				return info;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer,
	 * org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
	{
		Object info = getHoverInfo2(textViewer, hoverRegion);
		if (info != null)
		{
			return info.toString();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.ui.hover.AbstractPHPTextHover#getHoverControlCreator()
	 */
	public IInformationControlCreator getHoverControlCreator()
	{
		if (bestHover instanceof ITextHoverExtension)
		{
			return ((ITextHoverExtension) bestHover).getHoverControlCreator();
		}
		return super.getHoverControlCreator();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.ui.hover.AbstractPHPTextHover#getInformationPresenterControlCreator()
	 */
	public IInformationControlCreator getInformationPresenterControlCreator()
	{
		if (bestHover instanceof IInformationProviderExtension2)
		{
			return ((IInformationProviderExtension2) bestHover).getInformationPresenterControlCreator();
		}

		return super.getInformationPresenterControlCreator();
	}

	/**
	 * Check that we have instances of the text hovers. In case we don't have an instance for a descriptor that we have,
	 * we check if we can instantiate it, and then decide whether or not to create it. This is done to avoid any
	 * unnecessary plug-in loading.
	 */
	protected void checkHovers()
	{
		for (PHPTextHoverDescriptor descriptor : new ArrayList<PHPTextHoverDescriptor>(textHoverDescriptors))
		{
			try
			{
				AbstractPHPTextHover hover = descriptor.createTextHover();
				if (hover != null)
				{
					// Make sure that the debug-hover is first.
					if (DEBUG_HOVER_ID.equals(descriptor.getId()))
					{
						instantiatedTextHovers.add(0, hover);
					}
					else
					{
						instantiatedTextHovers.add(hover);
					}
					textHoverDescriptors.remove(descriptor);
				}
			}
			catch (Exception e)
			{
				PHPEditorPlugin.logError(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.hover.AbstractDocumentationHover#getHeader(java.lang.Object,
	 * org.eclipse.ui.IEditorPart, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public String getHeader(Object element, IEditorPart editorPart, IRegion hoverRegion)
	{
		if (bestHover instanceof AbstractDocumentationHover)
		{
			return ((AbstractDocumentationHover) bestHover).getHeader(element, editorPart, hoverRegion);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.hover.AbstractDocumentationHover#getDocumentation(java.lang.Object,
	 * org.eclipse.ui.IEditorPart, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public String getDocumentation(Object element, IEditorPart editorPart, IRegion hoverRegion)
	{
		if (bestHover instanceof AbstractDocumentationHover)
		{
			return ((AbstractDocumentationHover) bestHover).getDocumentation(element, editorPart, hoverRegion);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.hover.AbstractDocumentationHover#populateToolbarActions(org.eclipse.jface.action.
	 * ToolBarManager, com.aptana.editor.common.hover.CustomBrowserInformationControl)
	 */
	@Override
	public void populateToolbarActions(ToolBarManager tbm, CustomBrowserInformationControl iControl)
	{
		if (bestHover instanceof AbstractDocumentationHover)
		{
			((AbstractDocumentationHover) bestHover).populateToolbarActions(tbm, iControl);
		}
	}
}
