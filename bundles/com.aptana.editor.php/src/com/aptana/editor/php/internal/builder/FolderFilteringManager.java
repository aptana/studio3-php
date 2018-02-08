/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import java.io.File;

import org.eclipse.core.resources.IFolder;

/**
 * Manager of folders filtering.
 * 
 * @author Denis Denisenko
 */
public class FolderFilteringManager
{
	/**
	 * Folder names to skip.
	 */
	private static String[] namesToSkip = new String[] { ".svn", //$NON-NLS-1$
			"_svn", //$NON-NLS-1$
			"cvs", //$NON-NLS-1$
			".git" }; //$NON-NLS-1$

	/**
	 * Gets whether the folder is acceptable.
	 * 
	 * @param folder
	 *            - folder to check.
	 */
	public static boolean acceptFolder(IFolder folder)
	{
		String folderName = folder.getName();
		for (String toSkip : namesToSkip)
		{
			if (folderName.toLowerCase().equals(toSkip))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Gets whether the folder is acceptable.
	 * 
	 * @param folder
	 *            - folder to check.
	 */
	public static boolean acceptFolder(File folder)
	{
		if (!folder.isDirectory())
		{
			return false;
		}

		String folderName = folder.getName();
		for (String toSkip : namesToSkip)
		{
			if (folderName.toLowerCase().equals(toSkip))
			{
				return false;
			}
		}

		return true;
	}

	private FolderFilteringManager()
	{
	}
}
