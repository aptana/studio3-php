/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
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
