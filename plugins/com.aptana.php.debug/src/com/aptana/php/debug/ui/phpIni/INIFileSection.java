package com.aptana.php.debug.ui.phpIni;

import java.util.ArrayList;
import java.util.Collections;
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
