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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * A descriptor for a PHP text hover. A caller can use this descriptor to instantiate a text hover instance.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPTextHoverDescriptor
{

	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_ACTIVATE_PLUGIN = "activatePlugin"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private IConfigurationElement element;
	private String id;
	private boolean canActivatePlugin;

	/**
	 * @param element
	 */
	public PHPTextHoverDescriptor(IConfigurationElement element)
	{
		this.element = element;
		this.id = element.getAttribute(ATTR_ID);
		this.canActivatePlugin = Boolean.parseBoolean(element.getAttribute(ATTR_ACTIVATE_PLUGIN));
	}

	/**
	 * Returns the descriptor's ID.
	 * 
	 * @return The ID
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * Returns true if this hover is allowed to activate the plugin that contributed it.
	 */
	public boolean canActivatePlugin()
	{
		return canActivatePlugin;
	}

	/**
	 * Instantiate a text hover. In case the hover is on a plugin that is not loaded yet, we look into the
	 * {@link #canActivatePlugin()} to determine if we can instantiate it anyway.
	 * 
	 * @return An instance of AbstractPHPTextHover; Null, in case an un-loaded plugin is not allowed to be loaded.
	 * @throws CoreException
	 */
	public AbstractPHPTextHover createTextHover() throws CoreException
	{
		String contributor = element.getContributor().getName();
		boolean isPluginActivated = Platform.getBundle(contributor).getState() == Bundle.ACTIVE;
		if (isPluginActivated || canActivatePlugin())
		{
			return (AbstractPHPTextHover) element.createExecutableExtension(ATTR_CLASS);
		}
		return null;
	}
}
