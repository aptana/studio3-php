/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.WorkbenchEncoding;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.builder.LocalModule;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Encoding utilities library.
 * 
 * @author Shalom Gibly
 */
public class EncodingUtils
{
	/**
	 * Returns the defined encoding for the given module.
	 * 
	 * <pre>
	 * The search for the encoding is done in this order:
	 * 1. Check the encoding that is set specifically to a LocalModule.
	 * 2. Check the workspace default charset.
	 * 3. If all the above fails, get ResourcesPlugin.getEncoding(), which actually gets the encoding from the system.
	 * </pre>
	 * 
	 * @param module
	 *            an {@link IModule}.
	 * @return The module's encoding.
	 */
	public static String getModuleEncoding(IModule module)
	{
		String charset = null;
		try
		{
			if (module instanceof LocalModule)
			{
				IFile file = ((LocalModule) module).getFile();
				if (file != null)
				{
					String fileCharset = file.getCharset(true);
					if (fileCharset != null)
					{
						charset = fileCharset;
					}
				}
			}
		}
		catch (Throwable e)
		{
			// If there is any error, return the default
			IdeLog.logInfo(PHPEditorPlugin.getDefault(),
					"PHP encoding utils - Returning the default encoding due to an error (getModuleEncoding)", //$NON-NLS-1$
					e, PHPEditorPlugin.DEBUG_SCOPE);
		}
		if (charset == null)
		{
			try
			{
				IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
				charset = workspaceRoot.getDefaultCharset(true);
			}
			catch (CoreException ce)
			{
				charset = WorkbenchEncoding.getWorkbenchDefaultEncoding();
			}
		}
		if (charset == null)
		{
			// Use the system's encoding
			charset = ResourcesPlugin.getEncoding();
		}
		return charset;
	}
}
