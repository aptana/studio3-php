/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.filesystem.EFS;

import com.aptana.core.resources.FileStoreUniformResource;
import com.aptana.core.resources.IUniformResource;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * File system - based module.
 * 
 * @author Denis Denisenko
 */
public class FileSystemModule extends AbstractBuildPathResource implements IModule
{
	/**
	 * File.
	 */
	private File file;
	private IUniformResource uniformResource;
	private boolean isInWorkspace;

	/**
	 * FileSystemModule constructor.
	 * 
	 * @param file
	 *            - file.
	 * @param buildPath
	 *            - build path.
	 * @param isInWorkspace
	 *            - mark this file-system module as one that also exists in the workspace, but probably not in a PHP
	 *            project.
	 */
	public FileSystemModule(File file, IBuildPath buildPath, boolean isInWorkspace)
	{
		super(buildPath, file.getAbsolutePath());
		this.file = file;
		this.isInWorkspace = isInWorkspace;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws FileNotFoundException
	 */
	public InputStream getContents() throws FileNotFoundException
	{
		return new FileInputStream(file); // $codepro.audit.disable closeWhereCreated
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((file == null) ? 0 : file.hashCode());
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
		final FileSystemModule other = (FileSystemModule) obj;
		if (file == null)
		{
			if (other.file != null)
				return false;
		}
		else if (!file.equals(other.file))
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getShortName()
	{
		return file.getName();
	}

	/**
	 * Gets file.
	 * 
	 * @return file.
	 */
	protected File getFile()
	{
		return file;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return file.toString();
	}

	public long getTimeStamp()
	{
		return file.lastModified();
	}

	public IUniformResource getExternalFile()
	{
		if (uniformResource == null)
		{
			uniformResource = new FileStoreUniformResource(EFS.getLocalFileSystem().fromLocalFile(file));
		}
		return uniformResource;
	}

	/**
	 * @return the isInWorkspace
	 */
	public boolean isInWorkspace()
	{
		return isInWorkspace;
	}
}
