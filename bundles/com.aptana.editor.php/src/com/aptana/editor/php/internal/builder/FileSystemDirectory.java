/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import java.io.File;

import com.aptana.editor.php.internal.core.builder.IBuildPath;

/**
 * FileSystemDirectory
 * 
 * @author Denis Denisenko
 */
public class FileSystemDirectory extends AbstractDirectory
{

	/**
	 * File.
	 */
	private File directory;

	/**
	 * FileSystemDirectory constructor.
	 * 
	 * @param directory
	 *            - directory.
	 * @param buildPath
	 *            - build path.
	 */
	public FileSystemDirectory(File directory, IBuildPath buildPath)
	{
		super(buildPath, directory.getAbsolutePath());
		this.directory = directory;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getShortName()
	{
		return directory.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((directory == null) ? 0 : directory.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FileSystemDirectory other = (FileSystemDirectory) obj;
		if (directory == null)
		{
			if (other.directory != null)
				return false;
		}
		else if (!directory.equals(other.directory))
			return false;
		return true;
	}

	/**
	 * Gets directory.
	 * 
	 * @return directory file.
	 */
	protected File getDirectory()
	{
		return directory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return directory.toString();
	}
}
