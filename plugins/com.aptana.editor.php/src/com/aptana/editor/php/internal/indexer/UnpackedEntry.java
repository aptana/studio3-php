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