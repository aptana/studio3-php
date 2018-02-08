/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeManager.ContentTypeChangeEvent;

import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Factory for fyle system - based PHP modules.
 * 
 * @author Denis Denisenko
 */
public final class PHPFileSystemModuleFactory
{
	private static final String COMPILED_SMARTY_REGEX = "(%%).+(%%).+"; //$NON-NLS-1$
	private static final String COMPILED_SMARTY_PREFIX = "%%"; //$NON-NLS-1$
	private static final String PHP_SOURCE_CONTENT_TYPE = "com.aptana.contenttype.phtml.php"; //$NON-NLS-1$
	private static IContentType contentType = Platform.getContentTypeManager().getContentType(PHP_SOURCE_CONTENT_TYPE);
	static
	{
		Platform.getContentTypeManager().addContentTypeChangeListener(
				new IContentTypeManager.IContentTypeChangeListener()
				{

					public void contentTypeChanged(ContentTypeChangeEvent event)
					{
						if (PHP_SOURCE_CONTENT_TYPE.equals(event.getContentType().getId()))
						{
							contentType = Platform.getContentTypeManager().getContentType(PHP_SOURCE_CONTENT_TYPE);
						}
					}
				});
	}

	/**
	 * Gets module by local resource.
	 * 
	 * @param resource
	 *            - resource.
	 * @param buildPath
	 *            - build path.
	 * @return module
	 */
	public static IModule getModule(File resource, IBuildPath buildPath)
	{

		if (resource.exists() && resource.isFile())
		{
			String name = resource.getName();
			if (contentType.isAssociatedWith(name))
			{
				// Ignore compiled Smarty files
				if (name.startsWith(COMPILED_SMARTY_PREFIX))
				{
					if (name.matches(COMPILED_SMARTY_REGEX))
					{
						return null;
					}
				}
				// This should catch all the PHP associated file-types
				return new FileSystemModule(resource, buildPath, false);
			}
			// int pointIndex = name.indexOf('.');
			//			
			// if (pointIndex == -1 || pointIndex == name.length() - 1)
			// {
			// return null;
			// }
			//			
			// String extension = resource.getName().substring(pointIndex + 1);
			// if (extension.equals(PHP_EXTENSION))
			// {
			// return new FileSystemModule(resource, buildPath);
			// }
			// if (resource.getName().endsWith(PHP_EXTENSION)) {
			// return new FileSystemModule(resource, buildPath);
			// }
		}

		return null;
	}

	/**
	 * PHPModuleFactory private constructor.
	 */
	private PHPFileSystemModuleFactory()
	{
	}
}
