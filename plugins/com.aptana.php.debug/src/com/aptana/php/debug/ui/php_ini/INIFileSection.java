/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.php_ini;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * INI file section.
 * 
 * @author Denis Denisenko
 */
public class INIFileSection
{

	/**
	 * Section name.
	 */
	String name;

	/**
	 * Section lines list.
	 */
	List<String> lines;

	/**
	 * Section entries.
	 */
	List<PHPIniEntry> entries;

	/**
	 * INIFileSection constructor.
	 * 
	 * @param name
	 *            - section name.
	 */
	public INIFileSection(String name)
	{
		this.name = name;
		this.lines = new LinkedList<String>();
		this.entries = new ArrayList<PHPIniEntry>();
	}

	/**
	 * Gets section name.
	 * 
	 * @return section name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the entries.
	 * 
	 * @return A direct access to the entries reference.
	 */
	public List<PHPIniEntry> getEntries()
	{
		return entries;
	}

	/**
	 * Returns the string lines.
	 * 
	 * @return A direct access to the string lines reference.
	 */
	public List<String> getLines()
	{
		return lines;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	// $codepro.audit.disable
	// com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.obeyEqualsContract.obeyGeneralContractOfEquals
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
		if (getClass() != obj.getClass()) // $codepro.audit.disable useEquals
		{
			return false;
		}
		final INIFileSection other = (INIFileSection) obj;
		if (name == null)
		{
			if (other.name != null)
			{
				return false;
			}
		}
		else if (!name.equals(other.name))
		{
			return false;
		}
		return true;
	}
}
