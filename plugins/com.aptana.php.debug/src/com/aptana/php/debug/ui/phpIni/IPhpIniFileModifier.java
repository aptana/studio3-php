/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.phpIni;

import java.io.IOException;
import java.util.List;

/**
 * A PHP INI file modifier interface.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public interface IPhpIniFileModifier
{

	/**
	 * The INI sections.<br>
	 * The sections are defined with a string in square brackets.
	 * 
	 * @return The sections in the INI.
	 */
	List<INIFileSection> getSections();

	/**
	 * Flush the INI and save it to its file.
	 * 
	 * @throws IOException
	 */
	void flush() throws IOException;

	/**
	 * Returns true if the INI has changes.
	 * 
	 * @return True, if the INI was changed and is 'dirty'.
	 */
	boolean isDirty();

	/**
	 * Adds new entry to the INI. In case the 'replace' flag is true, an old entry will be replaced. Otherwise, a new
	 * entry will be created.
	 * 
	 * @param sectionName
	 *            The section name
	 * @param entryName
	 *            The entry name
	 * @param value
	 *            The value name
	 * @param replace
	 *            Indicate to replace an old entry or add a new one.
	 * @param replacePattern
	 *            The pattern to check for existing entry value before replacing it. When null, all entries that matches
	 *            the given name will be replaced.
	 */
	void addEntry(String sectionName, String entryName, String value, boolean replace, String replacePattern);

	/**
	 * Adds an entry to the INI file.<br>
	 * This call will add a new entry to the given section.
	 * 
	 * @param sectionName
	 *            The section name to insert into.
	 * @param entryName
	 *            The inserted entry's name.
	 * @param entryValue
	 *            The inserted entry's value.
	 */
	void addEntry(String sectionName, String entryName, String entryValue);

	/**
	 * Adds an entry to the INI file.<br>
	 * This call will add a new entry to the global (default) section.
	 * 
	 * @param entryName
	 *            The inserted entry's name.
	 * @param entryValue
	 *            The inserted entry's value.
	 */
	void addEntry(String entryName, String entryValue);

	/**
	 * Inserts an entry to before the given entry.
	 * 
	 * @param beforeEntryName
	 *            - The entry that appears right before the place we wish this entry to be inserted.
	 * @param entryName
	 *            - The inserted entry name.
	 * @param value
	 *            - The inserted entry value.
	 */
	void insertEntry(PHPIniEntry beforeEntryName, String entryName, String value);

	/**
	 * Inserts an entry into the beginning of a section.
	 * 
	 * @param section
	 *            - The section.
	 * @param entryName
	 *            - The entry name.
	 * @param value
	 *            - The inserted entry value.
	 */
	void insertEntryToSectionBeginning(INIFileSection section, String entryName, String value);

	/**
	 * Returns an {@link INIFileSection} by its name.
	 * 
	 * @param name
	 *            The name of the section to look for.
	 * @return The section found; Null, if none was located.
	 */
	INIFileSection getSectionByName(String name);

	/**
	 * Returns the global section (e.g. anything that is not under an INI section)
	 * 
	 * @return The global {@link INIFileSection}.
	 */
	INIFileSection getGlobalSection();

	/**
	 * Removes an entry from the INI.
	 * 
	 * @param sectionName
	 *            The section name. in case it's null, all matching entries from all sections will be removed.
	 * @param entryName
	 *            The entry name
	 * @param removePattern
	 *            The pattern to check for before removing an item. If the pattern is null, all entries that matches the
	 *            name will be removed.
	 * @return True, if at least one entry was removed.
	 */
	boolean removeEntry(String sectionName, String entryName, String removePattern);

	/**
	 * Removes all the entries with the given name from all the sections in the INI.
	 * 
	 * @param entryName
	 *            The entry name to remove.
	 * @param removePattern
	 *            The pattern to check for before removing an item. If the pattern is null, all entries that matches the
	 *            name will be removed.
	 * @return True, if at least one entry was removed.
	 * @see #removeEntry(String, String, String)
	 */
	boolean removeAllEntries(String entryName, String removePattern);

	/**
	 * Removes a section.
	 * 
	 * @param section
	 *            - The {@link INIFileSection} to remove.
	 */
	void removeSection(INIFileSection section);

	/**
	 * Adds a section.
	 * 
	 * @param sectionName
	 *            - The section name to add.
	 */
	INIFileSection addSection(String sectionName);

	/**
	 * Comment an entry in the INI.
	 * 
	 * @param sectionName
	 *            The section name. The section name. in case it's null, all matching entries from all sections will be
	 *            commented.
	 * @param entryName
	 *            The entry name
	 * @param commentPattern
	 *            The pattern to check for before commenting. If the pattern is null, all entries that matches the name
	 *            will be commented.
	 * @see #uncommentEntry(String, String, String)
	 */
	void commentEntry(String sectionName, String entryName, String commentPattern);

	/**
	 * Un-comment an entry in the INI.
	 * 
	 * @param sectionName
	 *            The section name. The section name. in case it's null, all matching entries from all sections will be
	 *            un-commented.
	 * @param entryName
	 *            The entry name
	 * @param commentPattern
	 *            The pattern to check for before un-commenting. If the pattern is null, all entries that matches the
	 *            name will be un-commented.
	 * @see #commentEntry(String, String, String)
	 */
	void uncommentEntry(String sectionName, String entryName, String commentPattern);
}
