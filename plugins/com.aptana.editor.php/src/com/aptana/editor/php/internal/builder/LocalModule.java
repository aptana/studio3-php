/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Local module.
 * 
 * @author Denis Denisenko
 */
public class LocalModule extends AbstractBuildPathResource implements IModule
{
	/**
	 * Local file.
	 */
	private IFile file;

	/**
	 * Module constructor.
	 * 
	 * @param file
	 *            - local file.
	 */
	public LocalModule(IFile file, IBuildPath buildPath)
	{
		super(buildPath, (file.getFullPath() == null || file.getLocation() == null) ? null : file.getLocation().toOSString());
		this.file = file;
	}

	/**
	 * {@inheritDoc}
	 */
	public InputStream getContents() throws IOException
	{
		try
		{

			return file.getContents();
		}
		catch (CoreException e)
		{
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Gets file.
	 * 
	 * @return file.
	 */
	public IFile getFile()
	{
		return file;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		if (file == null)
		{
			return "null"; //$NON-NLS-1$
		}

		return file.getFullPath().toPortableString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getShortName()
	{
		return file.getName();
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
		LocalModule other = (LocalModule) obj;
		if (file == null)
		{
			if (other.file != null)
				return false;
		}
		else if (!file.equals(other.file))
			return false;
		return true;
	}

	public long getTimeStamp()
	{
		return file.getModificationStamp();
	}
}
