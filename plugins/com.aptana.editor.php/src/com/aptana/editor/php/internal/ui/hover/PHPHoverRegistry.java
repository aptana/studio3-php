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

	private ArrayList<PHPTextHoverDescriptor> hovers;

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
		return (List<PHPTextHoverDescriptor>) hovers.clone();
	}

}
