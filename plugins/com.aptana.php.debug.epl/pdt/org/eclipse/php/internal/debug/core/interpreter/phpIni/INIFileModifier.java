/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org.eclipse.php.internal.debug.core.interpreter.phpIni;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aptana.php.debug.ui.phpini.INIFileSection;
import com.aptana.php.debug.ui.phpini.IPhpIniFileModifier;
import com.aptana.php.debug.ui.phpini.PHPIniEntry;

/**
 * This class is used for modifying INI file.
 * At the end {@link #close()} must be called, otherwise the file will be leaved unmodified.
 *
 * @author michael
 */
public class INIFileModifier implements IPhpIniFileModifier {
	
	private static final String GLOBAL_SECTION = "__global__"; //$NON-NLS-1$
	private static final Pattern SECTION_PATTERN = Pattern.compile("\\[([^\\]]+)\\]"); //$NON-NLS-1$
	//private static final Pattern NAME_VAL_PATTERN = Pattern.compile("([\\w]+)\\s*=\\s*(.*)"); //$NON-NLS-1$
	private static final Pattern NAME_VAL_PATTERN = Pattern.compile("^;?\\s*((?:\\w|_|\\.)+)\\s*=\\s*(.*)"); //$NON-NLS-1$
	private File configFile;
	private List<INIFileSection> sections;
	private boolean isDirty;

	/**
	 * Create new INI file modifier class instance.
	 * If provided INI file doesn't exist - it will be created.
	 *
	 * @param configFile INI file path
	 * @throws IOException
	 */
	public INIFileModifier(String configFile) throws IOException {
		this(new File(configFile));
	}

	/**
	 * Create new INI file modifier class instance.
	 * If provided INI file doesn't exist - it will be created.
	 *
	 * @param configFile INI file object
	 * @throws IOException
	 */
	public INIFileModifier(File configFile) throws IOException {
		this.configFile = configFile;
		this.sections = new LinkedList<INIFileSection>();
		read();
	}

	/**
	 * Returns whether this INIModifier had some changes since the last flush or read. The returned value reflects ONLY
	 * changes that were made directly to this class, by invoking the functions for adding, removing and commenting
	 * entries. Any other change that was made directly on the instances that are held by this instance will not reflect
	 * the 'dirty' state.
	 * 
	 * @return True, if a change was made to the elements through this class API; False, otherwise.
	 */
	public boolean isDirty() {
		return isDirty;
	}
	
	/**
	 * Adds new entry to the INI file.
	 * New entry will be added to the default (unnamed) section
	 *
	 * @param name Entry name
	 * @param value Value name
	 * @param replace Whether to replace the old entry
	 */
	public void addEntry(String name, String value, boolean replace) {
		addEntry(GLOBAL_SECTION, name, value, replace, null);
	}
	
	/**
	 * Adds new entry to the INI file.
	 * New entry will be added to the default (unnamed) section, no old entries will be replaced
	 *
	 * @param name Entry name
	 * @param value Value name
	 */
	public void addEntry(String name, String value) {
		addEntry(GLOBAL_SECTION, name, value, false, null);
	}

	/**
	 * Adds new entry to the INI file.
	 * New entry will be added to the given section, no old entries will be replaced
	 *
	 * @param sectionName Section name
	 * @param name Entry name
	 * @param value Value name
	 */
	public void addEntry(String sectionName, String name, String value) {
		addEntry(sectionName, name, value, false, null);
	}
	
	/**
	 * Inserts new entry to the INI file before the entry specified.
	 *
	 * @param before - entry to insert the entry after. 
	 * @param name  - new entry name.
	 * @param value - new entry value.
	 */
	public void insertEntry(PHPIniEntry before, String name, String value) {
		if (before == null || name == null || value == null) {
			throw new NullPointerException();
		}
		//trimming the value
		value = value.trim();

		INIFileSection section = before.getSection();

		int lineIndex = getEntryLineIndex(before);
		if (lineIndex == -1)
		{
			return;
		}
		int entryIndex = getEntryIndex(before);
		if (entryIndex == -1)
		{
			return;
		}
		
		section.getLines().add(lineIndex, name + '=' + value);
		section.getEntries().add(entryIndex,
				new PHPIniEntry(name, value, section));
		isDirty = true;
		
	}
	
	/**
	 * Inserts entry to the beginning of a section.
	 * @param section - section.
	 * @param name - entry name.
	 * @param value - entry value.
	 */
	public void insertEntryToSectionBeginning(INIFileSection section, String name, String value)
	{
		if (section == null || name == null || value == null) {
			throw new NullPointerException();
		}
		
		section.getLines().add(0, name + '=' + value);
		section.getEntries().add(0,
				new PHPIniEntry(name, value, section));
		isDirty = true;
	}

	/**
	 * Adds new entry to the INI file.
	 * If <code>replace</code> is <code>true</code> the old entry will be replaced, otherwise - add a new one.
	 *
	 * @param sectionName Section name
	 * @param name Entry name
	 * @param value Value name
	 * @param replace Whether to replace the old entry or add a new one
	 * @param replacePattern Pattern to check against existing entry value, before replacing it.
	 * 	If <code>replacePattern</code> is <code>null</code> - every entry that matches the given name will be replaced
	 */
	public void addEntry(String sectionName, String name, String value, boolean replace, String replacePattern) {
		if (sectionName == null || name == null || value == null) {
			throw new NullPointerException();
		}
		//trimming the value
		value = value.trim();
		for (INIFileSection section : sections) {
			if (section.getName().equals(sectionName)) {
				if (replace) {
					//replacing values in the lines list
					for (int i = 0; i < section.getLines().size(); ++i) {
						Matcher m = NAME_VAL_PATTERN.matcher(section.getLines().get(i));
						if (m.matches()) {
							String oldName = m.group(1);
							String oldValue = Pattern.quote(m.group(2));
							if (oldName.equals(name) && (replacePattern == null || oldValue.equals(replacePattern))) {
								section.getLines().set(i, name + '=' + value);
							}
						}
					}
					
					/**
					 * Replacing values in the entries list.
					 */
					String val = value;
					for (PHPIniEntry entry : section.getEntries())
					{
						if (entry.getKey().equals(name)
								&& Pattern.quote(entry.getValue()).equals(replacePattern))
						{
							entry.setValue(val);
						}
					}
					
				} else {
					section.getEntries().add(
							new PHPIniEntry(name, value, section));
					section.getLines().add(name + '=' + value);
				}
				break;
			}
		}
		isDirty = true;
	}

	/**
	 * Removes entry from the INI file from the global section.
	 *
	 * @param name Entry name
	 * @param removePattern Pattern to check against existing entry value, before removing it.
	 * 	If <code>removePattern</code> is <code>null</code> every entry that matches the given name will be removed.
	 * @return <code>true</code> if some entry was removed, otherwise - false
	 */
	public boolean removeEntry(String name, String removePattern) {
		return removeEntry(GLOBAL_SECTION, name, removePattern);
	}

	/**
	 * Removes all entries from the INI file from all sections.
	 * Same as <code>removeEntry(null, name, null)</code>.
	 *
	 * @param name Entry name
	 * @return <code>true</code> if some entry was removed, otherwise - false
	 * @see #removeEntry(String, String, String)
	 */
	public boolean removeAllEntries(String name) {
		return removeEntry(null, name, null);
	}

	/**
	 * Removes all entries from the INI file from all sections.
	 * Same as <code>removeEntry(null, name, removePattern)</code>.
	 *
	 * @param name Entry name
	 * @param removePattern Pattern to check against existing entry value, before removing it.
	 * 	If <code>removePattern</code> is <code>null</code> every entry that matches the given name will be removed.
	 *
	 * @return <code>true</code> if some entry was removed, otherwise - false
	 * @see #removeEntry(String, String, String)
	 */
	public boolean removeAllEntries(String name, String removePattern) {
		return removeEntry(null, name, removePattern);
	}

	/**
	 * Removes entry from the INI file from the given section.
	 *
	 * @param sectionName Section name.
	 * 	If <code>sectionName</code> is <code>null</code>, matching entries from all sections will be removed.
	 *
	 * @param name Entry name
	 * @param removePattern Pattern to check against existing entry value, before removing it.
	 * 	If <code>removePattern</code> is <code>null</code> every entry that matches the given name will be removed.
	 * @return <code>true</code> if some entry was removed, otherwise - false
	 */
	public boolean removeEntry(String sectionName, String name, String removePattern) {
		if (name == null) {
			throw new NullPointerException();
		}
		boolean removed = false;
		for (INIFileSection section : sections) {
			if (sectionName == null || section.getName().equals(sectionName)) {
				for (int i = 0; i < section.getLines().size(); ++i) {
					Matcher m = NAME_VAL_PATTERN.matcher(section.getLines().get(i));
					if (m.matches()) {
						String oldName = m.group(1);
						String oldValue = Pattern.quote(m.group(2));
						if (oldName.equals(name) && (removePattern == null || oldValue.equals(removePattern))) {
							section.getLines().remove(i--);
							removed = true;
						}
					}
				}
				
				List<PHPIniEntry> toRemove = new ArrayList<PHPIniEntry>();
				for (PHPIniEntry entry : section.getEntries())
				{
					if (entry.getKey().equals(name)
							&& removePattern != null && Pattern.quote(entry.getValue()).equals(removePattern))
					{
						toRemove.add(entry);
					}
				}
				section.getEntries().removeAll(toRemove);
				
				if (sectionName != null) {
					break;
				}
			}
		}
		isDirty = true;
		return removed;
	}
	
	/**
	 * Gets the sections list.
	 * @return the sections list.
	 */
	public List<INIFileSection> getSections()
	{
		return sections;
	}

	/**
	 * Removes entry from the INI file from the global section.
	 *
	 * @param name Entry name
	 * @param commentPattern Pattern to check against existing entry value, before removing it.
	 * 	If <code>commentPattern</code> is <code>null</code> every entry that matches the given name will be commented.
	 */
	public void commentEntry(String name, String commentPattern) {
		commentEntry(GLOBAL_SECTION, name, commentPattern);
		isDirty = true;
	}

	/**
	 * Removes entry from the INI file from all sections.
	 * Same as <code>commentEntry(null, name, commentPattern)</code>.
	 *
	 * @param name Entry name
	 * @param commentPattern Pattern to check against existing entry value, before removing it.
	 * 	If <code>commentPattern</code> is <code>null</code> every entry that matches the given name will be commented.
	 *
	 * @see #commentEntry(String, String, String)
	 */
	public void commentAllEntries(String name, String commentPattern) {
		commentEntry(null, name, commentPattern);
		isDirty = true;
	}

	/**
	 * Comments entry.
	 *
	 * @param sectionName Section name.
	 * 	If <code>sectionName</code> is <code>null</code>, matching entries from all sections will be commented.
	 *
	 * @param name Entry name
	 * @param commentPattern Pattern to check against existing entry value, before removing it.
	 * 	If <code>commentPattern</code> is <code>null</code> every entry that matches the given name will be commented.
	 */
	public void commentEntry(String sectionName, String name, String commentPattern) {
		if (name == null) {
			throw new NullPointerException();
		}
		for (INIFileSection section : sections) {
			if (sectionName == null || section.getName().equals(sectionName)) {
				List<String> lines = section.getLines();
				for (int i = 0; i < lines.size(); ++i) {
					String line = lines.get(i);
					Matcher m = NAME_VAL_PATTERN.matcher(line);
					if (m.matches()) {
						String oldName = m.group(1);
						String oldValue = Pattern.quote(m.group(2));
						if (!line.startsWith(";") && oldName.equals(name) && (commentPattern == null || oldValue.equals(commentPattern))) { //$NON-NLS-1$
							lines.set(i, ';' + line);
						}
					}
				}
				
				for (PHPIniEntry entry : section.getEntries())
				{
					if (entry.getKey().equals(name)
							&& !entry.getCommented()
							&& (commentPattern == null || Pattern.quote(entry.getValue()).equals(commentPattern)))
					{
						entry.setCommented(true);
					}
				}
				
				if (sectionName != null) {
					break;
				}
			}
		}
		isDirty = true;
	}
	
	/**
	 * Uncomments entry.
	 *
	 * @param sectionName Section name.
	 * 	If <code>sectionName</code> is <code>null</code>, matching entries from all sections will be commented.
	 *
	 * @param name Entry name
	 * @param commentPattern Pattern to check against existing entry value, before removing it.
	 * 	If <code>commentPattern</code> is <code>null</code> every entry that matches the given name will be commented.
	 */
	public void uncommentEntry(String sectionName, String name, String commentPattern) {
		if (name == null) {
			throw new NullPointerException();
		}
		for (INIFileSection section : sections) {
			if (sectionName == null || section.getName().equals(sectionName)) {
				for (int i = 0; i < section.getLines().size(); ++i) {
					String line = section.getLines().get(i);
					Matcher m = NAME_VAL_PATTERN.matcher(line);
					if (m.matches()) {
						String oldName = m.group(1);
						String oldValue = Pattern.quote(m.group(2));
						if (line.startsWith(";") && oldName.equals(name) && (commentPattern == null || oldValue.equals(commentPattern))) { //$NON-NLS-1$
							int index = 1;
							for (; index < line.length(); index++)
							{
								if (!Character.isWhitespace(line.charAt(index)))
								{
									break;
								}
							}
							section.getLines().set(i, line.substring(index));
						}
					}
				}
				
				for (PHPIniEntry entry : section.getEntries())
				{
					if (entry.getKey().equals(name)
							&& entry.getCommented()
							&& (commentPattern == null || Pattern.quote(entry.getValue()).equals(commentPattern)))
					{
						entry.setCommented(false);
					}
				}
				
				if (sectionName != null) {
					break;
				}
			}
		}
		isDirty = true;
	}

	/**
	 * Reads INI file contents
	 * @throws IOException
	 */
	protected void read() throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(configFile));
		String line;
		INIFileSection currentSection = new INIFileSection(GLOBAL_SECTION);
		sections.add(currentSection);
		while ((line = r.readLine()) != null) {
			line = line.trim();
			Matcher m = SECTION_PATTERN.matcher(line);
			if (m.matches()) {
				String sectionName = m.group(1);
				currentSection = new INIFileSection(sectionName);
				sections.add(currentSection);
			} else {
				Matcher entryMatcher = NAME_VAL_PATTERN.matcher(line);
				if (entryMatcher.matches())
				{
					String val = entryMatcher.group(2).trim();
					PHPIniEntry entry = new PHPIniEntry(entryMatcher.group(1), val,
							currentSection);
					if (line.startsWith(";")) //$NON-NLS-1$
					{
						entry.setCommented(true);
					}
					currentSection.getEntries().add(entry);
				}
				currentSection.getLines().add(line);
			}
		}
		r.close();
		isDirty = false;
	}

	/**
	 * Writes INI file contents back to the file
	 * @throws IOException
	 */
	public void flush() throws IOException {
		PrintWriter w = new PrintWriter(new FileWriter(configFile));
		for (INIFileSection section : sections) {
			if (section.getName() != GLOBAL_SECTION) {
				w.println('[' + section.getName() + ']');
			}
			for (String line : section.getLines()) {
				w.println(line);
			}
		}
		w.close();
		isDirty = false;
	}
	
	/**
	 * Gets section by name. 
	 * @param name - section name.
	 * @return section or null.
	 */
	public INIFileSection getSectionByName(String name)
	{
		for (INIFileSection section : sections)
		{
			if (section.getName().equals(name))
			{
				return section;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets global section. 
	 * @return global section.
	 */
	public INIFileSection getGlobalSection()
	{
		return getSectionByName(GLOBAL_SECTION);
	}
	
	/**
	 * Adds section.
	 * @param sectionName - section name.
	 * @return added section.
	 */
	public INIFileSection addSection(String sectionName)
	{
		INIFileSection currentSection = new INIFileSection(sectionName);
		sections.add(currentSection);
		isDirty = true;
		return currentSection;
	}
	
	/**
	 * Removes section.
	 * @param section - section to remove.
	 */
	public void removeSection(INIFileSection section)
	{
		sections.remove(section);
		isDirty = true;
	}
	
	/** 
	 * Performs a lookup in the ini and returns a non-commented entry with the given name or null if none is found.
	 * 
	 * @param name An entry name
	 * @return The entry; or null if non was found
	 */
	public PHPIniEntry getEntryByName(String name)
	{
		if (name == null)
		{
			return null;
		}
		List<INIFileSection> allSections = getSections();
		for (INIFileSection section : allSections)
		{
			List<PHPIniEntry> entries = section.getEntries();
			for (PHPIniEntry entry : entries)
			{
				if (!entry.getCommented() && name.equals(entry.getKey()))
				{
					return entry;
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets entry line index.
	 * @param entry - entry.
	 * @return entry line index or -1 if not found.
	 */
	private int getEntryLineIndex(PHPIniEntry entry)
	{
		INIFileSection section = entry.getSection();
		for (int i = 0; i < section.getLines().size(); ++i) {
			String line = section.getLines().get(i);
			Matcher m = NAME_VAL_PATTERN.matcher(line);
			if (m.matches()) {
				String name = m.group(1);
				String value = m.group(2);
				if (value == null)
				{
					continue;
				}
				value = Pattern.quote(value);
				if (name.equals(entry.getKey()) 
						&& value.equals(Pattern.quote(entry.getValue()))) {
					return i;
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * Gets entry index.
	 * @param entry - entry.
	 * @return entry index or -1 if not found.
	 */
	private int getEntryIndex(PHPIniEntry entry)
	{
		INIFileSection section = entry.getSection();
		return section.getEntries().indexOf(entry);
	}
}
