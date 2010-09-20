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
	private static final String PHP_CONTENT_TYPE = "com.aptana.contenttype.html.php"; //$NON-NLS-1$

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
