/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
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
