/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;

import com.aptana.php.debug.ui.phpIni.IPhpIniFileModifier;

/**
 * PHP debug launch support interface.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public interface IPHPDebugLaunchSupport
{
	/**
	 * Creates and returns a PHP ini configuration file that will be configured to include the required paths and
	 * debugger settings.
	 * 
	 * @param phpIniFile
	 *            The base PHP configuration file
	 * @param phpExePath
	 *            A path to the PHP Interpreter that should be used
	 * @param project
	 *            The current project
	 * @return A temporary PHP configuration file
	 */
	File generatePhpIni(File phpIniFile, String phpExePath, IProject project, String debuggerID);

	/**
	 * Returns an instance of an {@link IPhpIniFileModifier}.
	 * 
	 * @param configurationFile
	 *            A file reference for the PHP INI to configure.
	 * @return an {@link IPhpIniFileModifier}
	 * @throws IOException
	 */
	IPhpIniFileModifier getPhpIniFileModifier(File configurationFile) throws IOException;
}
