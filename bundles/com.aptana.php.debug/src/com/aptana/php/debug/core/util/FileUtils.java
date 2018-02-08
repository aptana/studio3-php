/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.util;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

import com.aptana.core.logging.IdeLog;
import com.aptana.php.debug.IDebugScopes;
import com.aptana.php.debug.PHPDebugPlugin;

/**
 * File utilities for the PHP debugger.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FileUtils
{

	/**
	 * Set a given file with executable permissions.<br>
	 * Note that this method will only function on a non-Windows OS systems.
	 * 
	 * @param file
	 *            The file to set with executable permissions.
	 */
	public static void setExecutablePermissions(File file)
	{
		if (!Platform.getOS().equals(Platform.OS_WIN32))
		{
			IFileStore fileStore = EFS.getLocalFileSystem().fromLocalFile(file);
			if (fileStore != null)
			{
				IFileInfo fileInfo = fileStore.fetchInfo();
				if (!fileInfo.getAttribute(EFS.ATTRIBUTE_EXECUTABLE))
				{
					fileInfo.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, true);
					try
					{
						fileStore.putInfo(fileInfo, EFS.SET_ATTRIBUTES, new NullProgressMonitor());
					}
					catch (CoreException e)
					{
						IdeLog.logError(PHPDebugPlugin.getDefault(), "Error while changing execution permissions", e, //$NON-NLS-1$
								IDebugScopes.DEBUG);
					}
				}
			}
		}
	}
}
