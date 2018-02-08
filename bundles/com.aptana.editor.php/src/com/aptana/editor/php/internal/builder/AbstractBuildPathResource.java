/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import org.eclipse.core.runtime.IPath;

import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IBuildPathResource;

/**
 * Abstract build-path resource.
 * 
 * @author Denis Denisenko
 */
public abstract class AbstractBuildPathResource implements IBuildPathResource
{
	/**
	 * Module build path.
	 */
	private IBuildPath buildPath;

	/**
	 * Module full path.
	 */
	private String fullPath;

	/**
	 * AbstractModule constructor.
	 * 
	 * @param buildPath
	 *            - module build path.
	 * @param fullPath
	 *            - module full path.
	 */
	protected AbstractBuildPathResource(IBuildPath buildPath, String fullPath)
	{
		super();
		this.buildPath = buildPath;
		this.fullPath = fullPath;
	}

	/**
	 * {@inheritDoc}
	 */
	public IBuildPath getBuildPath()
	{
		return buildPath;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFullPath()
	{
		return fullPath;
	}

	/**
	 * {@inheritDoc}
	 */
	public IPath getPath()
	{
		return getBuildPath().getResourcePath(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		/*
		 * result = prime * result + ((buildPath == null) ? 0 : buildPath.hashCode());
		 */
		result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		// A generic comparison using the class
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final AbstractBuildPathResource other = (AbstractBuildPathResource) obj;
		/*
		 * if (buildPath == null) { if (other.buildPath != null) { return false; } } else if
		 * (!buildPath.equals(other.buildPath)) { return false; }
		 */
		if (fullPath == null)
		{
			if (other.fullPath != null)
			{
				return false;
			}
		}
		else if (!fullPath.equals(other.fullPath))
		{
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString()
	{
		return getFullPath().toString();
	}
}
