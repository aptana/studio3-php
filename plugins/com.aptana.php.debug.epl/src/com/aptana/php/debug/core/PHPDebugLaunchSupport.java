/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.php.internal.debug.core.interpreter.phpIni.INIFileModifier;
import org.eclipse.php.internal.debug.core.interpreter.phpIni.PHPINIDebuggerUtil;

import com.aptana.php.debug.ui.php_ini.IPhpIniFileModifier;

/**
 * An implementation of the {@link IPHPDebugLaunchSupport} that provides EPL classes support to the non-EPL debugger
 * contributions.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPDebugLaunchSupport implements IPHPDebugLaunchSupport
{

	public PHPDebugLaunchSupport()
	{
		// TODO Auto-generated constructor stub
	}

	public File generatePhpIni(File phpIniFile, String phpExePath, IProject project, String debuggerID)
	{
		return PHPINIDebuggerUtil.prepareBeforeDebug(phpIniFile, phpExePath, project, debuggerID);
	}

	public IPhpIniFileModifier getPhpIniFileModifier(File configurationFile) throws IOException
	{
		return new INIFileModifier(configurationFile);
	}

}
