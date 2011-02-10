/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Factory for PHP modules.
 * 
 * @author Denis Denisenko
 */
public final class PHPLocalModuleFactory
{
	private static final String COMPILED_SMARTY_REGEX = "(%%).+(%%).+"; //$NON-NLS-1$
	private static final String COMPILED_SMARTY_PREFIX = "%%"; //$NON-NLS-1$

	/**
	 * PHP extension.
	 */
	private static final String PHP_EXTENSION = "php"; //$NON-NLS-1$

	/**
	 * PHP content type.
	 */
	private static final String PHP_CONTENT_TYPE = "com.aptana.contenttype.phtml.php"; //$NON-NLS-1$

	/**
	 * Gets module by local resource.
	 * 
	 * @param resource
	 *            - resource.
	 * @param buildPath
	 *            - build path.
	 * @return module
	 */
	public static IModule getModule(IResource resource, IBuildPath buildPath)
	{
		// igonring inaccesible resources
		if (!resource.isAccessible())
		{
			return null;
		}

		if (!(resource instanceof IFile) /* || !resource.exists() */)
		{
			return null;
		}

		// skipping unsynchronized resources.
		if (!resource.isSynchronized(1))
		{
			// FIXME SG: Use resource.refreshLocal(depth, monitor) in these cases (need testing)
			return null;
		}

		IFile file = (IFile) resource;
		String fileName = file.getName();
		if (hasValidExtension(fileName))
		{
			if (fileName.startsWith(COMPILED_SMARTY_PREFIX))
			{
				if (fileName.matches(COMPILED_SMARTY_REGEX))
				{
					return null;
				}
			}
			return new LocalModule(file, buildPath);
		}

		return null;
	}

	/**
	 * Gets module by local resource. This method does not check for module existence and whether the module resource is
	 * synchronized.
	 * 
	 * @param resource
	 *            - resource.
	 * @param buildPath
	 *            - build path.
	 * @return module
	 */
	public static IModule getModuleUnsafe(IResource resource, IBuildPath buildPath)
	{
		IFile file = (IFile) resource;

		if (hasValidExtension(file.getName()))
		{
			return new LocalModule(file, buildPath);
		}

		return null;
	}

	/**
	 * Whether file has the valid PHP extension.
	 * 
	 * @param fileName
	 *            - file name.
	 * @return true if file extension is a valid PHP extension, false otherwise.
	 */
	private static boolean hasValidExtension(String fileName)
	{
		IContentTypeManager manager = Platform.getContentTypeManager();
		IContentType type = manager.getContentType(PHP_CONTENT_TYPE);
		if (type == null)
		{
			return fileName.endsWith(PHP_EXTENSION);
		}

		return type.isAssociatedWith(fileName);
		// String[] validTypes = type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
		// if (validTypes == null || validTypes.length == 0)
		// {
		// return fileName.endsWith(PHP_EXTENSION);
		// }
		//		
		// for (String validType : validTypes)
		// {
		// if (fileName.endsWith(validType))
		// {
		// return true;
		// }
		// }
		//		
		// return false;
	}

	/**
	 * PHPModuleFactory private constructor.
	 */
	private PHPLocalModuleFactory()
	{
	}
}
