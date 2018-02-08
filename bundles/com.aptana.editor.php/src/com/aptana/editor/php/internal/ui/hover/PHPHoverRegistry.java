/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.hover;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.aptana.editor.php.PHPEditorPlugin;

/**
 * PHP text hovers registry.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPHoverRegistry
{
	private static final String EXTENSION_POINT_NAME = "phpTextHovers"; //$NON-NLS-1$
	private static final String EXTENSION_NAME = "textHover"; //$NON-NLS-1$

	private static PHPHoverRegistry instance;

	/**
	 * Returns an instance of the registry.
	 */
	public static PHPHoverRegistry getInstance()
	{
		if (instance == null)
		{
			instance = new PHPHoverRegistry();
		}
		return instance;
	}

	private List<PHPTextHoverDescriptor> hovers;

	/**
	 * Returns the registered PHP text hovers descriptors.<br>
	 * The descriptors can later be instantiated to create an instance of the text hover.
	 * 
	 * @return A list of registered PHP text hover descriptors.
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<PHPTextHoverDescriptor> getTextHoversDescriptors()
	{
		if (hovers == null)
		{
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] elements = registry.getConfigurationElementsFor(PHPEditorPlugin.PLUGIN_ID,
					EXTENSION_POINT_NAME);
			hovers = new ArrayList<PHPTextHoverDescriptor>(3);
			for (IConfigurationElement element : elements)
			{
				if (element.getName().equals(EXTENSION_NAME))
				{
					hovers.add(new PHPTextHoverDescriptor(element));
				}
			}
		}
		return (List<PHPTextHoverDescriptor>) ((ArrayList<PHPTextHoverDescriptor>) hovers).clone();
	}

}
