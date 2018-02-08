/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.php.internal.indexer;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Element entry for unpacked index.
 * 
 * @author Denis Denisenko
 */
public class UnpackedEntry implements IElementEntry
{
	/**
	 * Element category.
	 */
	private int category;

	/**
	 * Entry path.
	 */
	private String entryPath;

	/**
	 * Entry path.
	 */
	private String lowerCaseEntryPath;

	/**
	 * Entry value.
	 */
	private Object value;

	/**
	 * Entry module.
	 */
	private IModule module;

	/**
	 * Entry constructor.
	 * 
	 * @param category
	 *            - category.
	 * @param entryPath
	 *            - entry path.
	 * @param value
	 *            - entry value.
	 * @param module
	 *            - entry module.
	 */
	public UnpackedEntry(int category, String entryPath, Object value, IModule module)
	{
		super();
		this.category = category;
		this.entryPath = entryPath;
		if (entryPath != null)
		{
			this.lowerCaseEntryPath = entryPath.toLowerCase();
		}
		this.value = value;
		this.module = module;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getCategory()
	{
		return category;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntryPath()
	{
		return entryPath;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getValue()
	{
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public IModule getModule()
	{
		return module;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return entryPath + " | " + getValue(); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + category;
		result = prime * result + ((entryPath == null) ? 0 : entryPath.hashCode());
		result = prime * result + ((module == null) ? 0 : module.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final UnpackedEntry other = (UnpackedEntry) obj;
		if (category != other.category)
		{
			return false;
		}
		if (entryPath == null)
		{
			if (other.entryPath != null)
			{
				return false;
			}
		}
		else if (!entryPath.equals(other.entryPath))
		{
			return false;
		}
		if (module == null)
		{
			if (other.module != null)
			{
				return false;
			}
		}
		else if (!module.equals(other.module))
		{
			return false;
		}
		if (value == null)
		{
			if (other.value != null)
			{
				return false;
			}
		}
		else if (!value.equals(other.value))
		{
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLowerCaseEntryPath()
	{
		return lowerCaseEntryPath;
	}
}