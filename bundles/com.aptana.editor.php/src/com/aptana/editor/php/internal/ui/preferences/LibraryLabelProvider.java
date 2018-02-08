/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.preferences;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.internal.builder.IPHPLibrary;
import com.aptana.editor.php.internal.builder.PHPLibrary;

/**
 * @author Pavel Petrochenko
 */
public final class LibraryLabelProvider extends LabelProvider
{

	static Image uLibraryImage = AbstractUIPlugin.imageDescriptorFromPlugin(PHPEplPlugin.PLUGIN_ID,
			"/icons/full/obj16/library_obj.gif").createImage(); //$NON-NLS-1$

	private final Map<URL,Image> images;

	public LibraryLabelProvider(Map<URL,Image> images)
	{
		this.images = images;
	}

	/**
	 * Returns an {@link Image}.<br>
	 * The returned library image can be grabbed from an icon attributes that was set on the library extension.
	 */
	public Image getImage(Object element)
	{
		if (element instanceof PHPLibrary)
		{
			PHPLibrary lib = (PHPLibrary) element;
			URL icon = lib.getIcon();
			if (icon != null)
			{
				Image toRet = images.get(icon);
				if (toRet != null)
				{
					return toRet;
				}
				try
				{
					Image image = new Image(Display.getCurrent(), icon.openStream());
					images.put(icon, image);
					return image;
				}
				catch (IOException e)
				{
					IdeLog.logError(PHPEditorPlugin.getDefault(),
							"PHP library label provider - error getting a library image", e); //$NON-NLS-1$
					return null;
				}
			}
		}
		return uLibraryImage;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
	 */
	@Override
	public void dispose()
	{
		if (images != null)
		{
			for (Image image : images.values())
			{
				image.dispose();
			}
		}
	}

	public String getText(Object element)
	{
		IPHPLibrary lib = (IPHPLibrary) element;
		String name = lib.getName();

		return name;
	}
}