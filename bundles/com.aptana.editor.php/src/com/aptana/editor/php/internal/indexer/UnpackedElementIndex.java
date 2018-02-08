/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.internal.contentAssist.ContentAssistUtils;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Unpacked element index.
 * 
 * @author Denis Denisenko
 */
public class UnpackedElementIndex implements IModifiableElementsIndex
{
	/**
	 * Entries.
	 */
	protected THashMap<IModule, List<UnpackedEntry>> entries = new THashMap<IModule, List<UnpackedEntry>>();

	private TObjectLongHashMap<IModule> timeStamps = new TObjectLongHashMap<IModule>();

	/**
	 * Category->Path->Entries map. Value might be represented by the single entry or by the entries list.
	 */
	private TIntObjectHashMap<THashMap<String, Object>> pathToEntries = new TIntObjectHashMap<THashMap<String, Object>>();

	/**
	 * Encoded first character of the path->Path->Entries map.
	 */
	private TIntObjectHashMap<TShortObjectHashMap<Object>> firstCharToEntries = new TIntObjectHashMap<TShortObjectHashMap<Object>>();

	/**
	 * Encoded first two characters of the path->Path->Entries map.
	 */
	private TIntObjectHashMap<TIntObjectHashMap<Object>> firstTwoCharsToEntries = new TIntObjectHashMap<TIntObjectHashMap<Object>>();

	/**
	 * Buffer used for conversions.
	 */
	private ByteBuffer converter = ByteBuffer.allocate(4);

	public void recordTimeStamp(IModule m, long timeStamp)
	{
		try
		{
			timeStamps.put(m, timeStamp);
		}
		catch (Exception e)
		{
			IdeLog.logWarning(PHPEditorPlugin.getDefault(),
					"Error recording timestamp for " + m.getFullPath(), e, PHPEditorPlugin.INDEXER_SCOPE); //$NON-NLS-1$
		}
	}

	public long getTimeStamp(IModule m)
	{
		return timeStamps.get(m);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized IElementEntry addEntry(int category, String entryPath, Object value, IModule module)
	{
		UnpackedEntry entry = new UnpackedEntry(category, entryPath, value, module);

		// adding entry to the list of a module's entries
		addEntryToModuleList(module, entry);

		// adding entry to path->entry map
		addEntryToPathToEntriesMap(entry);

		// adding entry to first path character->entry map
		addEntryToFirstCharToEntriesMap(entry);

		// adding entry to first 2 path characters->entry map
		addEntryToFirstTwoCharsToEntriesMap(entry);

		return entry;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void removeModuleEntries(IModule module)
	{
		List<UnpackedEntry> entriesToRemove = entries.get(module);
		if (entriesToRemove != null && !entriesToRemove.isEmpty())
		{
			for (UnpackedEntry entryToRemove : entriesToRemove)
			{
				// System.out.println("Removing entry: " + entryToRemove);
				removeEntriesFromPathToEntries(entryToRemove);
				removeEntriesFromFirstCharToEntries(entryToRemove);
				removeEntriesFromFirstTwoCharcToEntries(entryToRemove);
			}
		}
		timeStamps.remove(module);
		entries.remove(module);

		// List<IElementEntry> curr = this.getEntriesStartingWith(-1, "C");
		// for (IElementEntry e : curr)
		// {
		// System.out.println("After removing: " + e);
		// }
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<IElementEntry> getEntriesStartingWith(int category, String path)
	{
		int indexOf = path.lastIndexOf('\\');
		String namespace = null;
		boolean filterByNamespace = ContentAssistUtils.isFilterByNamespace();
		if (indexOf != -1)
		{
			namespace = path.substring(0, indexOf);
			path = path.substring(indexOf + 1);
		}
		final String lowerCasepath = path.toLowerCase();
		final List<IElementEntry> toReturn = new ArrayList<IElementEntry>();

		// handling empty path
		if (lowerCasepath.length() == 0)
		{
			if (category == IElementsIndex.ANY_CETEGORY)
			{
				entries.forEachValue(new TObjectProcedure<List<UnpackedEntry>>()
				{
					public boolean execute(List<UnpackedEntry> list)
					{
						toReturn.addAll(list);
						return true;
					}
				});
			}
			else
			{
				TShortObjectHashMap<Object> map = firstCharToEntries.get(category);
				if (map != null)
				{
					map.forEachValue(new TObjectProcedure<Object>()
					{
						public boolean execute(Object obj)
						{
							if (obj instanceof IElementEntry)
							{
								toReturn.add((IElementEntry) obj);
							}
							else if (obj instanceof Collection)
							{
								toReturn.addAll((Collection<IElementEntry>) obj);
							}
							return true;
						}
					});
				}
			}
		}
		// handling single character path
		else if (lowerCasepath.length() == 1)
		{
			final short firstCharacter = (short) lowerCasepath.charAt(0);
			if (category == IElementsIndex.ANY_CETEGORY)
			{
				firstCharToEntries.forEachValue(new TObjectProcedure<TShortObjectHashMap<Object>>()
				{
					public boolean execute(TShortObjectHashMap<Object> map)
					{
						Object objResult = map.get(firstCharacter);
						if (objResult != null)
						{
							addObjRefToList(toReturn, objResult);
						}
						return true;
					}
				});
			}
			else
			{
				TShortObjectHashMap<Object> map = firstCharToEntries.get(category);
				if (map != null)
				{
					Object objResult = map.get(firstCharacter);
					if (objResult != null)
					{
						addObjRefToList(toReturn, objResult);
					}
				}
			}

		}
		// handling two characters path
		else if (lowerCasepath.length() == 2)
		{
			final int key = getIntKey(lowerCasepath);
			if (category == IElementsIndex.ANY_CETEGORY)
			{
				firstTwoCharsToEntries.forEachValue(new TObjectProcedure<TIntObjectHashMap<Object>>()
				{
					public boolean execute(TIntObjectHashMap<Object> map)
					{
						Object objResult = map.get(key);
						if (objResult != null)
						{
							addObjRefToList(toReturn, objResult);
						}
						return true;
					}
				});
			}
			else
			{
				TIntObjectHashMap<Object> map = firstTwoCharsToEntries.get(category);
				if (map != null)
				{
					Object objResult = map.get(key);
					if (objResult != null)
					{
						addObjRefToList(toReturn, objResult);
					}
				}
			}

		}
		// handling more then two characters path
		else if (lowerCasepath.length() > 2)
		{
			// handling more then two characters path
			final int key = getIntKey(lowerCasepath);
			if (category == IElementsIndex.ANY_CETEGORY)
			{
				firstTwoCharsToEntries.forEachValue(new TObjectProcedure<TIntObjectHashMap<Object>>()
				{
					public boolean execute(TIntObjectHashMap<Object> map)
					{
						Object objResult = map.get(key);
						if (objResult != null)
						{
							addObjRefToListWithPathCheck(toReturn, objResult, lowerCasepath);
						}
						return true;
					}
				});
			}
			else
			{
				TIntObjectHashMap<Object> map = firstTwoCharsToEntries.get(category);
				if (map != null)
				{
					Object objResult = map.get(key);
					if (objResult != null)
					{
						addObjRefToListWithPathCheck(toReturn, objResult, lowerCasepath);
					}
				}
			}
		}
		if (filterByNamespace)
		{
			if (namespace != null)
			{
				List<IElementEntry> filter = new ArrayList<IElementEntry>();
				for (IElementEntry e : toReturn)
				{
					Object value = e.getValue();
					if (value instanceof AbstractPHPEntryValue)
					{
						AbstractPHPEntryValue m = (AbstractPHPEntryValue) value;
						String nameSpace2 = m.getNameSpace();
						if (nameSpace2 != null && nameSpace2.equals(namespace))
						{
							filter.add(e);
						}
					}
				}
				return filter;
			}
			else
			{
				List<IElementEntry> filter = new ArrayList<IElementEntry>();
				for (IElementEntry e : toReturn)
				{
					Object value = e.getValue();
					if (value instanceof AbstractPHPEntryValue)
					{
						AbstractPHPEntryValue m = (AbstractPHPEntryValue) value;
						if (m instanceof NamespacePHPEntryValue)
						{
							filter.add(e);
						}
						else
						{
							String nameSpace2 = m.getNameSpace();
							if (nameSpace2 == null || nameSpace2.length() == 0)
							{
								filter.add(e);
							}
						}
					}
				}
				return filter;
			}
		}
		return toReturn;
	}

	/**
	 * Adds an object reference (that might be an entry or a list of entries) to the specified list.
	 * 
	 * @param list
	 *            - list to add entries to.
	 * @param reference
	 *            - reference.
	 */
	@SuppressWarnings("unchecked")
	private void addObjRefToList(List<IElementEntry> list, Object reference)
	{
		if (reference instanceof IElementEntry)
		{
			list.add((IElementEntry) reference);
		}
		else if (reference instanceof Collection)
		{
			list.addAll((Collection<IElementEntry>) reference);
		}
	}

	/**
	 * Adds an object reference (that might be an entry or a list of entries) to the specified list if its lower-case
	 * path starts with the path specified.
	 * 
	 * @param list
	 *            - list to add entries to.
	 * @param reference
	 *            - reference.
	 * @param lowerCasePath
	 *            - lower-case path to compare to
	 */
	@SuppressWarnings("unchecked")
	private void addObjRefToListWithPathCheck(List<IElementEntry> list, Object reference, String lowerCasePath)
	{
		if (reference instanceof IElementEntry)
		{
			if (((IElementEntry) reference).getLowerCaseEntryPath().startsWith(lowerCasePath))
			{
				list.add((IElementEntry) reference);
			}
		}
		else if (reference instanceof Collection<?>)
		{
			for (IElementEntry currentEntry : (Collection<IElementEntry>) reference)
			{
				if (currentEntry.getLowerCaseEntryPath().startsWith(lowerCasePath))
				{
					list.add(currentEntry);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<IElementEntry> getEntries(int category, String path)
	{
		int indexOf = path.lastIndexOf('\\');
		boolean filterByNamespace = ContentAssistUtils.isFilterByNamespace();
		String namespace = null;
		if (indexOf != -1)
		{
			namespace = path.substring(0, indexOf);
			path = path.substring(indexOf + 1);

		}
		final String lowerCasePath = path.toLowerCase();

		if (category == IElementsIndex.ANY_CETEGORY)
		{
			final List<IElementEntry> result = new ArrayList<IElementEntry>();

			pathToEntries.forEachValue(new TObjectProcedure<THashMap<String, Object>>()
			{
				public boolean execute(THashMap<String, Object> map)
				{
					Object resultObject = map.get(lowerCasePath);
					if (resultObject == null)
					{
						return true;
					}

					if (resultObject instanceof Collection)
					{
						result.addAll((Collection<IElementEntry>) resultObject);
					}
					else if (resultObject instanceof IElementEntry)
					{
						result.add((IElementEntry) resultObject);
					}

					return true;
				}
			});
			if (filterByNamespace && namespace != null)
			{
				List<IElementEntry> filter = new ArrayList<IElementEntry>();
				for (IElementEntry e : result)
				{
					Object value = e.getValue();
					if (value instanceof AbstractPHPEntryValue)
					{
						AbstractPHPEntryValue m = (AbstractPHPEntryValue) value;
						String nameSpace2 = m.getNameSpace();
						if (nameSpace2 != null && nameSpace2.equals(namespace))
						{
							filter.add(e);
						}
					}
				}
				return filter;
			}
			return result;
		}
		else
		{
			Map<String, Object> map = pathToEntries.get(category);
			if (map == null)
			{
				return Collections.emptyList();
			}

			final List<IElementEntry> result = new ArrayList<IElementEntry>();

			Object resultObject = map.get(lowerCasePath);
			if (resultObject == null)
			{
				return Collections.emptyList();
			}

			if (resultObject instanceof Collection)
			{
				result.addAll((Collection<IElementEntry>) resultObject);
			}
			else if (resultObject instanceof IElementEntry)
			{
				result.add((IElementEntry) resultObject);
			}
			if (filterByNamespace && namespace != null)
			{
				List<IElementEntry> filter = new ArrayList<IElementEntry>();
				for (IElementEntry e : result)
				{
					Object value = e.getValue();
					if (value instanceof AbstractPHPEntryValue)
					{
						AbstractPHPEntryValue m = (AbstractPHPEntryValue) value;
						String nameSpace2 = m.getNameSpace();
						if (nameSpace2 != null && nameSpace2.equals(namespace))
						{
							filter.add(e);
						}
					}
				}
				return filter;
			}
			return result;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized int size()
	{
		int size = 0;
		for (List<UnpackedEntry> moduleEntries : entries.values())
		{
			size += moduleEntries.size();
		}

		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized List<IElementEntry> getModuleEntries(IModule module)
	{
		List<IElementEntry> result = new ArrayList<IElementEntry>();
		List<UnpackedEntry> moduleEntries = entries.get(module);
		if (moduleEntries != null)
		{
			result.addAll(moduleEntries);
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized Set<IModule> getModules()
	{
		return Collections.unmodifiableSet(entries.keySet());
	}

	public IModule[] getAllModules()
	{
		return timeStamps.keys(new IModule[timeStamps.size()]);
	}

	/**
	 * Removes all the entries that have the same path and module as the entry specified from the
	 * firstTowCharsToEntries.
	 * 
	 * @param entryToRemove
	 *            - entry to remove.
	 */
	@SuppressWarnings("unchecked")
	private void removeEntriesFromFirstTwoCharcToEntries(UnpackedEntry entryToRemove)
	{
		TIntObjectHashMap<Object> map = firstTwoCharsToEntries.get(entryToRemove.getCategory());
		if (map == null)
		{
			return;
		}

		// skipping those entries having not enough characters in path
		String lowerCasePath = entryToRemove.getLowerCaseEntryPath();
		if (lowerCasePath.length() < 2)
		{
			return;
		}

		int key = getIntKey(lowerCasePath);

		Object pathToEntriesValue = map.get(key);
		if (pathToEntriesValue == null)
		{
			return;
		}

		if (pathToEntriesValue instanceof UnpackedEntry)
		{
			if (entryToRemove.equals(pathToEntriesValue))
			{
				map.remove(key);
			}
		}
		else if (pathToEntriesValue instanceof Collection)
		{
			// removing all entries that have the same module as the entry
			// specified
			Iterator<IElementEntry> it = ((Collection<IElementEntry>) pathToEntriesValue).iterator();
			while (it.hasNext())
			{
				IElementEntry currentEntry = it.next();
				if (lowerCasePath.equals(currentEntry.getLowerCaseEntryPath())
						&& entryToRemove.getModule().equals(currentEntry.getModule()))
				{
					it.remove();
				}
			}

			if (((Collection<IElementEntry>) pathToEntriesValue).size() == 0)
			{
				map.remove(key);
			}
		}
	}

	/**
	 * Gets integter key that encodes first two characters of the path.
	 * 
	 * @param lowerCasePath
	 *            - lower-case path
	 * @return integer key
	 */
	private int getIntKey(String lowerCasePath)
	{
		short firstCharacter = (short) lowerCasePath.charAt(0);
		short secondCharacter = (short) lowerCasePath.charAt(1);
		converter.clear();
		converter.putShort(firstCharacter);
		converter.putShort(secondCharacter);
		converter.flip();

		int key = converter.getInt();
		return key;
	}

	/**
	 * Removes all the entries that have the same path and module as the entry specified from the firstCharToEntries.
	 * 
	 * @param entryToRemove
	 *            - entry to remove.
	 */
	@SuppressWarnings("unchecked")
	private void removeEntriesFromFirstCharToEntries(UnpackedEntry entryToRemove)
	{
		TShortObjectHashMap<Object> map = firstCharToEntries.get(entryToRemove.getCategory());
		if (map == null)
		{
			return;
		}

		// skipping those entries having not enough characters in path
		String lowerCasePath = entryToRemove.getLowerCaseEntryPath();
		if (lowerCasePath.length() == 0)
		{
			return;
		}

		short firstCharacter = (short) lowerCasePath.charAt(0);

		Object pathToEntriesValue = map.get(firstCharacter);
		if (pathToEntriesValue == null)
		{
			return;
		}

		if (pathToEntriesValue instanceof UnpackedEntry)
		{
			if (entryToRemove.equals(pathToEntriesValue))
			{
				map.remove(firstCharacter);
			}
		}
		else if (pathToEntriesValue instanceof Collection)
		{
			Iterator<IElementEntry> it = ((Collection<IElementEntry>) pathToEntriesValue).iterator();
			// removing all entries that have the same module as the entry
			// specified
			while (it.hasNext())
			{
				IElementEntry currentEntry = it.next();
				if (lowerCasePath.equals(currentEntry.getLowerCaseEntryPath())
						&& entryToRemove.getModule().equals(currentEntry.getModule()))
				{
					it.remove();
				}
			}

			if (((Collection<IElementEntry>) pathToEntriesValue).size() == 0)
			{
				map.remove(firstCharacter);
			}
		}
	}

	/**
	 * Removes all the entries that have the same path and module as the entry specified from the pathToEntries.
	 * 
	 * @param entryToRemove
	 *            - entry to remove.
	 */
	@SuppressWarnings("unchecked")
	private void removeEntriesFromPathToEntries(UnpackedEntry entryToRemove)
	{
		Map<String, Object> map = pathToEntries.get(entryToRemove.getCategory());
		if (map == null)
		{
			return;
		}

		Object pathToEntriesValue = map.get(entryToRemove.getLowerCaseEntryPath());
		if (pathToEntriesValue == null)
		{
			return;
		}

		if (pathToEntriesValue instanceof UnpackedEntry)
		{
			if (entryToRemove.equals(pathToEntriesValue))
			{
				map.remove(entryToRemove.getLowerCaseEntryPath());
			}
		}
		else if (pathToEntriesValue instanceof Collection)
		{
			// removing all entries that have the same module as the entry
			// specified
			Iterator<IElementEntry> it = ((Collection<IElementEntry>) pathToEntriesValue).iterator();
			while (it.hasNext())
			{
				IElementEntry currentEntry = it.next();
				if (entryToRemove.getModule().equals(currentEntry.getModule()))
				{
					it.remove();
				}
			}

			if (((Collection<IElementEntry>) pathToEntriesValue).size() == 0)
			{
				map.remove(entryToRemove.getLowerCaseEntryPath());
			}
		}
	}

	/**
	 * Adds entry to first character of the path -> entries map.
	 * 
	 * @param entry
	 *            - entry.
	 */
	@SuppressWarnings("unchecked")
	private void addEntryToFirstTwoCharsToEntriesMap(UnpackedEntry entry)
	{
		TIntObjectHashMap<Object> map = firstTwoCharsToEntries.get(entry.getCategory());
		if (map == null)
		{
			map = new TIntObjectHashMap<Object>();
			firstTwoCharsToEntries.put(entry.getCategory(), map);
		}

		// skipping those entries having not enough characters in path
		String lowerCasePath = entry.getLowerCaseEntryPath();
		if (lowerCasePath.length() < 2)
		{
			return;
		}

		int key = getIntKey(lowerCasePath);

		Object objectResult = map.get(key);
		if (objectResult == null)
		{
			map.put(key, entry);
		}
		else
		{
			if (objectResult instanceof IElementEntry)
			{
				if (!objectResult.equals(entry))
				{
					IElementEntry oldEntry = (IElementEntry) objectResult;
					Set<IElementEntry> list = new HashSet<IElementEntry>(2);
					list.add(oldEntry);
					list.add(entry);
					map.put(key, list);
				}
			}
			else if (objectResult instanceof Collection<?>)
			{
				if (!((HashSet<IElementEntry>) objectResult).contains(entry))
				{
					((HashSet<IElementEntry>) objectResult).add(entry);
				}
			}
		}
	}

	/**
	 * Adds entry to first character of the path -> entries map.
	 * 
	 * @param entry
	 *            - entry.
	 */
	@SuppressWarnings("unchecked")
	private void addEntryToFirstCharToEntriesMap(UnpackedEntry entry)
	{
		TShortObjectHashMap<Object> map = firstCharToEntries.get(entry.getCategory());
		if (map == null)
		{
			map = new TShortObjectHashMap<Object>();
			firstCharToEntries.put(entry.getCategory(), map);
		}

		// skipping those entries having not enough characters in path
		String lowerCasePath = entry.getLowerCaseEntryPath();
		if (lowerCasePath.length() == 0)
		{
			return;
		}

		short firstCharacter = (short) lowerCasePath.charAt(0);

		Object objectResult = map.get(firstCharacter);
		if (objectResult == null)
		{
			map.put(firstCharacter, entry);
		}
		else
		{
			if (objectResult instanceof IElementEntry)
			{
				if (!objectResult.equals(entry))
				{
					IElementEntry oldEntry = (IElementEntry) objectResult;
					Set<IElementEntry> list = new HashSet<IElementEntry>(2);
					list.add(oldEntry);
					list.add(entry);
					map.put(firstCharacter, list);
				}
			}
			else if (objectResult instanceof Collection<?>)
			{
				if (!((Collection<IElementEntry>) objectResult).contains(entry))
				{
					((Collection<IElementEntry>) objectResult).add(entry);
				}
			}
		}
	}

	/**
	 * Adds entry to path->entries map.
	 * 
	 * @param entry
	 *            - entry.
	 */
	@SuppressWarnings("unchecked")
	private void addEntryToPathToEntriesMap(UnpackedEntry entry)
	{
		int category = entry.getCategory();
		String entryPathLowerCase = entry.getLowerCaseEntryPath();

		// getting path->entries map, creating the new one if needed
		THashMap<String, Object> map = pathToEntries.get(category);
		if (map == null)
		{
			map = new THashMap<String, Object>();
			pathToEntries.put(category, map);
		}

		Object pathToEntriesValue = map.get(entryPathLowerCase);
		if (pathToEntriesValue == null)
		{
			map.put(entryPathLowerCase, entry);
		}
		else
		{
			if (pathToEntriesValue instanceof UnpackedEntry)
			{
				if (entry.equals(pathToEntriesValue))
				{
					return;
				}
				Set<UnpackedEntry> val = new HashSet<UnpackedEntry>(2);
				val.add((UnpackedEntry) pathToEntriesValue);
				val.add(entry);
				map.put(entryPathLowerCase, val);
			}
			else if (pathToEntriesValue instanceof Collection)
			{
				Collection<UnpackedEntry> val = (Collection<UnpackedEntry>) pathToEntriesValue;
				if (val.contains(entry))
				{
					return;
				}
				val.add(entry);
			}
		}
	}

	/**
	 * Adds entry to the list of module entries.
	 * 
	 * @param module
	 *            - module.
	 * @param entry
	 *            - entry.
	 */
	private void addEntryToModuleList(IModule module, UnpackedEntry entry)
	{
		List<UnpackedEntry> moduleEntries = entries.get(module);
		if (moduleEntries == null)
		{
			moduleEntries = new ArrayList<UnpackedEntry>();
			entries.put(module, moduleEntries);
		}
		moduleEntries.add(entry);
	}

	public void removeTimeStamp(IModule module)
	{
		timeStamps.remove(module);
	}
}
