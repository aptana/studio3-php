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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used for modifying INI file.
 * At the end {@link #close()} must be called, otherwise the file will be leaved unmodified.
 *
 * @author michael
 */
public class INIFileModifier {
	
	public static final int PHP_EXTENSION_VALIDATION_UNKNOWN = 0;
	public static final int PHP_EXTENSION_VALIDATION_OK = 1;
	public static final int PHP_EXTENSION_VALIDATION_ERROR = 2;
	public static final int PHP_EXTENSION_VALIDATION_WARNING = 3;
	
	/*
	 * Extensions indicators in the ini
	 */
	private static final String[] EXTENSION_ENTRIES = {"extension", "zend_extension", "zend_extension_ts"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * PHP ini entry.
	 * @author Denis Denisenko, Shalom Gibly
	 */
	public static class PHPIniEntry implements Map.Entry<String, String>
	{
		/**
		 * Key.
		 */
		private String key;
		
		/**
		 * Value.
		 */
		private String value;
		
		/**
		 * Whether entry is commented.
		 */
		private boolean commented;
		
		/**
		 * Holds the validity state of this element.
		 * 
		 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_OK
		 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_ERROR
		 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_WARNING
		 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_UNKNOWN
		 */
		private int validity;

		/**
		 * Holds the validation note (if exists). A validation note is either an error note or a warning note that was
		 * generated while executing the PHP interpreter.
		 */
		private String validationNote;
		
		/**
		 * Parent section.
		 */
		private final INIFileSection parent;
		
		/**
		 * PHPIniEntry constructor.
		 * @param key - entry key.
		 * @param value - entry value.
		 * @param parent - parent section.
		 */
		public PHPIniEntry(String key, String value, INIFileSection parent)
		{
			this.key = key;
			this.value = value;
			this.parent = parent;
			this.validity = PHP_EXTENSION_VALIDATION_UNKNOWN;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getKey() {
			return key;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getValue() {
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		public String setValue(String val) {
			value = val;
			return val;
		}
		
		/**
		 * Returns section.
		 * @return section.
		 */
		public INIFileSection getSection()
		{
			return parent;
		}
		
		/**
		 * Returns the validation state of this entry. Note that the state is not set until the PHP ini validator is
		 * invoked.
		 * 
		 * @return the validation state of this entry.
		 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_OK
		 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_ERROR
		 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_WARNING
		 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_UNKNOWN
		 */
		public int getValidationState()
		{
			return validity;
		}

		/**
		 * Sets the validation state and note for this entry.
		 * 
		 * @param state
		 * @param validationNote
		 */
		public void setValidationState(int state, String validationNote)
		{
			this.validity = state;
			this.validationNote = validationNote;
		}

		/**
		 * Returns the validation note that was set for this element when the PHP ini validator was invoked.
		 * 
		 * @return the validation note; or null.
		 */
		public String getValidationNote()
		{
			return this.validationNote;
		}

		/**
		 * {@inheritDoc}
		 */
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		/**
		 * Sets commented state.
		 * 
		 * @param commented - commented state to set.
		 */
		void setCommented(boolean commented)
		{
			this.commented = commented;
		}

		/**
		 * Gets whether this entry is commented.
		 * 
		 * @return true if commented, false otherwise.
		 */
		public boolean getCommented()
		{
			return commented;
		}

		/**
		 * Returns whether this entry marks a PHP extension entry.
		 * Note: Commented entries will always return false.
		 * @return True, if it's an extension entry; False, otherwise.
		 */
		public boolean isExtensionEntry()
		{
			if (getCommented()) {
				return false;
			}
			for (String ext : EXTENSION_ENTRIES)
			{
				if (ext.equalsIgnoreCase(getKey()))
				{
					return true;
				}
			}
			return false;
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
			final PHPIniEntry other = (PHPIniEntry) obj;
			if (key == null)
			{
				if (other.key != null)
				{
					return false;
				}
			} 
			else if (!key.equals(other.key))
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
	}
	
	/**
	 * INI file section.
	 * @author Denis Denisenko
	 *
	 */
	public class INIFileSection {
		
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
		 * @param name - section name.
		 */
		public INIFileSection(String name) {
			this.name = name;
			this.lines = new LinkedList<String>();
			this.entries = new ArrayList<PHPIniEntry>();
		}
		
		/**
		 * Gets section name.
		 * @return section name.
		 */
		public String getName()
		{
			return name;
		}
		
		/**
		 * Gets read-only entries list.
		 * @return read-only entries list.
		 */
		public List<PHPIniEntry> getEntries()
		{
			return Collections.unmodifiableList(entries);
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
		
		section.lines.add(lineIndex, name + '=' + value);
		section.entries.add(entryIndex,
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
		
		section.lines.add(0, name + '=' + value);
		section.entries.add(0,
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
			if (section.name.equals(sectionName)) {
				if (replace) {
					//replacing values in the lines list
					for (int i = 0; i < section.lines.size(); ++i) {
						Matcher m = NAME_VAL_PATTERN.matcher(section.lines.get(i));
						if (m.matches()) {
							String oldName = m.group(1);
							String oldValue = Pattern.quote(m.group(2));
							if (oldName.equals(name) && (replacePattern == null || oldValue.equals(replacePattern))) {
								section.lines.set(i, name + '=' + value);
							}
						}
					}
					
					/**
					 * Replacing values in the entries list.
					 */
					String val = value;
					for (PHPIniEntry entry : section.entries)
					{
						if (entry.getKey().equals(name)
								&& Pattern.quote(entry.getValue()).equals(replacePattern))
						{
							entry.setValue(val);
						}
					}
					
				} else {
					section.entries.add(
							new PHPIniEntry(name, value, section));
					section.lines.add(name + '=' + value);
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
			if (sectionName == null || section.name.equals(sectionName)) {
				for (int i = 0; i < section.lines.size(); ++i) {
					Matcher m = NAME_VAL_PATTERN.matcher(section.lines.get(i));
					if (m.matches()) {
						String oldName = m.group(1);
						String oldValue = Pattern.quote(m.group(2));
						if (oldName.equals(name) && (removePattern == null || oldValue.equals(removePattern))) {
							section.lines.remove(i--);
							removed = true;
						}
					}
				}
				
				List<PHPIniEntry> toRemove = new ArrayList<PHPIniEntry>();
				for (PHPIniEntry entry : section.entries)
				{
					if (entry.getKey().equals(name)
							&& removePattern != null && Pattern.quote(entry.getValue()).equals(removePattern))
					{
						toRemove.add(entry);
					}
				}
				section.entries.removeAll(toRemove);
				
				if (sectionName != null) {
					break;
				}
			}
		}
		isDirty = true;
		return removed;
	}
	
	/**
	 * Gets read-only sections list.
	 * @return read-only sections list.
	 */
	public List<INIFileSection> getSections()
	{
		return Collections.unmodifiableList(sections);
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
			if (sectionName == null || section.name.equals(sectionName)) {
				for (int i = 0; i < section.lines.size(); ++i) {
					String line = section.lines.get(i);
					Matcher m = NAME_VAL_PATTERN.matcher(line);
					if (m.matches()) {
						String oldName = m.group(1);
						String oldValue = Pattern.quote(m.group(2));
						if (!line.startsWith(";") && oldName.equals(name) && (commentPattern == null || oldValue.equals(commentPattern))) { //$NON-NLS-1$
							section.lines.set(i, ';' + line);
						}
					}
				}
				
				for (PHPIniEntry entry : section.entries)
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
			if (sectionName == null || section.name.equals(sectionName)) {
				for (int i = 0; i < section.lines.size(); ++i) {
					String line = section.lines.get(i);
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
							section.lines.set(i, line.substring(index));
						}
					}
				}
				
				for (PHPIniEntry entry : section.entries)
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
	 * Writes all changes to the INI configuration file
	 * @throws IOException
	 */
	public void close() throws IOException {
		flush();
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
					currentSection.entries.add(entry);
				}
				currentSection.lines.add(line);
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
			if (section.name != GLOBAL_SECTION) {
				w.println('[' + section.name + ']');
			}
			for (String line : section.lines) {
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
		for (int i = 0; i < section.lines.size(); ++i) {
			String line = section.lines.get(i);
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
		return section.entries.indexOf(entry);
	}
}
