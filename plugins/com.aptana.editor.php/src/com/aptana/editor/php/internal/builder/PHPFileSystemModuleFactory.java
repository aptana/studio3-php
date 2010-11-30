/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
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
	private static final String PHP_SOURCE_CONTENT_TYPE = "com.aptana.contenttype.html.php"; //$NON-NLS-1$
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
