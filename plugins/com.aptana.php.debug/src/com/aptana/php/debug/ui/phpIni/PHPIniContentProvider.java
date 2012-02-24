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
package com.aptana.php.debug.ui.phpIni;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.aptana.php.debug.core.PHPDebugSupportManager;

/**
 * Content provider for PHP.ini data.
 * 
 * @author Denis Denisenko
 */
public class PHPIniContentProvider implements ITreeContentProvider
{

	/**
	 * Ini file modifier.
	 */
	private IPhpIniFileModifier modifier;

	/**
	 * File path and name.
	 */
	private String fileName;

	/**
	 * PHPIniContentProvider constructor.
	 * 
	 * @param fileName
	 *            - file name.
	 * @throws IOException
	 *             IF IO error occurs.
	 */
	public PHPIniContentProvider(String fileName) throws IOException
	{
		modifier = PHPDebugSupportManager.getLaunchSupport().getPhpIniFileModifier(new File(fileName));
		this.fileName = fileName;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getElements(Object inputElement)
	{

		List<INIFileSection> sections = modifier.getSections();
		return sections.toArray();
	}

	/**
	 * Return file name.
	 * 
	 * @return file name.
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose()
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

	public Object[] getChildren(Object parentElement)
	{
		if (parentElement instanceof INIFileSection)
		{
			List<PHPIniEntry> entries = ((INIFileSection) parentElement).getEntries();
			return entries.toArray();
		}
		else
		{
			return new Object[0];
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getParent(Object element)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasChildren(Object element)
	{
		if (element == null)
		{
			return true;
		}

		if (element instanceof INIFileSection)
		{
			return !((INIFileSection) element).getEntries().isEmpty();
		}

		return false;
	}

	/**
	 * Saves file.
	 * 
	 * @throws IOException
	 *             IF IO error occurs.
	 */
	public void save() throws IOException
	{
		if (modifier != null)
		{
			modifier.flush();
		}
	}

	/**
	 * Returns true if a change was made to the INI and was not saved yet.
	 * 
	 * @return True, if a change was made to the INI and was not saved yet; False, otherwise.
	 * @see INIFileModifier#isDirty()
	 */
	public boolean isDirtyINI()
	{
		if (modifier != null)
		{
			return modifier.isDirty();
		}
		return false;
	}

	/**
	 * Modifies entry.
	 * 
	 * @param entry
	 *            - entry to modify.
	 * @param value
	 *            - new entry value to set.
	 */
	public void modifyEntry(PHPIniEntry entry, String value)
	{
		modifier.addEntry(entry.getSection().getName(), entry.getKey(), value, true, Pattern.quote(entry.getValue()));
	}

	/**
	 * Adds entry.
	 * 
	 * @param section
	 *            - section to add entry to.
	 * @param name
	 *            - entry name.
	 * @param value
	 *            - new entry value to set.
	 */
	public void addEntry(INIFileSection section, String name, String value)
	{
		modifier.addEntry(section.getName(), name, value);
	}

	/**
	 * Inserts new entry to the INI file after the entry specified.
	 * 
	 * @param before
	 *            - entry to insert the entry before.
	 * @param name
	 *            - new entry name.
	 * @param value
	 *            - new entry value.
	 */
	public void insertEntry(PHPIniEntry before, String name, String value)
	{
		modifier.insertEntry(before, name, value);
	}

	/**
	 * Inserts entry to the beginning of a section.
	 * 
	 * @param section
	 *            - section.
	 * @param name
	 *            - entry name.
	 * @param value
	 *            - entry value.
	 */
	public void insertEntryToSectionBeginning(INIFileSection section, String name, String value)
	{
		modifier.insertEntryToSectionBeginning(section, name, value);
	}

	/**
	 * Gets section by name.
	 * 
	 * @param name
	 *            - section name.
	 * @return section or null.
	 */
	public INIFileSection getSectionByName(String name)
	{
		return modifier.getSectionByName(name);
	}

	/**
	 * Gets global section.
	 * 
	 * @return global section.
	 */
	public INIFileSection getGlobalSection()
	{
		return modifier.getGlobalSection();
	}

	/**
	 * Removes entry.
	 * 
	 * @param entry
	 *            - entry to remove.
	 * @param value
	 *            - new entry value to set.
	 */
	public void removeEntry(PHPIniEntry entry)
	{
		modifier.removeEntry(entry.getSection().getName(), entry.getKey(), Pattern.quote(entry.getValue()));
	}

	/**
	 * Removes section.
	 * 
	 * @param section
	 *            - section to remove.
	 */
	public void removeSection(INIFileSection section)
	{
		modifier.removeSection(section);
	}

	/**
	 * Adds new section.
	 * 
	 * @param sectionName
	 *            - section name.
	 * @return added section.
	 */
	public INIFileSection addSection(String sectionName)
	{
		return modifier.addSection(sectionName);
	}

	/**
	 * Gets all sections.
	 * 
	 * @return sections.
	 */
	public List<INIFileSection> getSections()
	{
		return modifier.getSections();
	}

	/**
	 * Comments entry.
	 * 
	 * @param entry
	 *            - entry to comment.
	 */
	public void commentEntry(PHPIniEntry entry)
	{
		modifier.commentEntry(entry.getSection().getName(), entry.getKey(), Pattern.quote(entry.getValue()));
	}

	/**
	 * Uncomments entry.
	 * 
	 * @param entry
	 *            - entry to comment.
	 */
	public void uncommentEntry(PHPIniEntry entry)
	{
		modifier.uncommentEntry(entry.getSection().getName(), entry.getKey(), Pattern.quote(entry.getValue()));
	}
}
