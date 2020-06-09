/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.ui.launching.server;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.ui.launching.LaunchUtil;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * PHP server tab that is displayed in the Run/Debug launch configuration tabs.
 * 
 * @author Robert G., Shalom G.
 */
public class PHPWebPageLaunchConfigurationTab extends ServerLaunchConfigurationTab
{
	/**
	 * PHP extension.
	 */
	private static final String PHP_EXTENSION = "php";

	public PHPWebPageLaunchConfigurationTab()
	{
		super();
	}

	public void createExtensionControls(Composite parent)
	{
	}

	protected void applyExtension(ILaunchConfigurationWorkingCopy configuration)
	{
		configuration.setAttribute(IPHPDebugConstants.RUN_WITH_DEBUG_INFO, true); // Always run with debug info
	}

	protected boolean isValidExtension(ILaunchConfiguration launchConfig)
	{
		return true;
	}

	protected void createServerSelectionControl(Composite parent)
	{
		PHPDebugEPLPlugin.createDefaultPHPServer();
		super.createServerSelectionControl(parent);
	}

	public String[] getRequiredNatures()
	{
		return LaunchUtil.getRequiredNatures();
	}

	public String[] getFileExtensions()
	{
		return LaunchUtil.getFileExtensions();
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getDefaultExtension()
	{
		return PHP_EXTENSION;
	}
}
