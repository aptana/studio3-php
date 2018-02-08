/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import org.eclipse.core.resources.IContainer;

import com.aptana.editor.php.internal.core.builder.IBuildPath;

/**
 * Local directory implementation.
 * 
 * @author Denis Denisenko
 */
public class LocalDirectory extends AbstractDirectory
{

	/**
	 * Folder.
	 */
	private IContainer resource;

	/**
	 * LocalDirectory constructor.
	 * 
	 * @param container
	 *            - container.
	 * @param buildPath
	 *            - build path.
	 */
	public LocalDirectory(IContainer container, IBuildPath buildPath)
	{
		super(buildPath, container.getFullPath().toOSString());
		this.resource = container;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getShortName()
	{
		return resource.getName();
	}

	/**
	 * Gets folder resource.
	 * 
	 * @return folder resource.
	 */
	public IContainer getContainer()
	{
		return resource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		if (resource == null)
		{
			return "null"; //$NON-NLS-1$
		}

		return resource.getFullPath().toPortableString();
	}
}
