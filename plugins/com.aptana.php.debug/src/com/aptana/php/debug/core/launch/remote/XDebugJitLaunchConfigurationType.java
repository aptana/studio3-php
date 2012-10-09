/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.launch.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;

import com.aptana.editor.php.indexer.PHPGlobalIndexer;

/**
 * Remote launch delegate for PHP debug sessions that were triggered remotely by an XDebug server.
 * 
 * @author Shalom Gibly
 * @since Aptana PHP 1.1
 */
public class XDebugJitLaunchConfigurationType implements ILaunchConfigurationDelegate2
{
	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration,
	 * java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
	{
	}

	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
	{
		PHPGlobalIndexer.getInstance();
		return false;
	}

	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
	{
		return true;
	}

	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
	{
		return null;
	}

	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
	{
		return true;
	}
}
