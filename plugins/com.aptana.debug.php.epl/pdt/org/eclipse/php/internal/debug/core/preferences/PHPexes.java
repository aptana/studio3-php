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
package org.eclipse.php.internal.debug.core.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.php.internal.debug.core.Logger;
import org.eclipse.php.internal.debug.core.interpreter.preferences.PHPexeItem;
import org.eclipse.php.internal.debug.core.xdebug.communication.XDebugCommunicationDaemon;
import org.eclipse.php.internal.debug.core.zend.communication.DebuggerCommunicationDaemon;

import com.aptana.debug.php.core.IPHPDebugCorePreferenceKeys;
import com.aptana.debug.php.core.interpreter.IInterpreter;
import com.aptana.debug.php.core.interpreter.Interpreters;
import com.aptana.debug.php.epl.PHPDebugEPLPlugin;

/**
 * A managing class for all the registered PHP Interpreters. As of PDT 1.0 this class can handle multiple debuggers.
 * 
 * @author Shalom Gibly
 */
public class PHPexes
{

	public static final String SEPARATOR = ";";

	public static final String ZEND_DEBUGGER_ID = DebuggerCommunicationDaemon.ZEND_DEBUGGER_ID;
	public static final String XDEBUG_DEBUGGER_ID = XDebugCommunicationDaemon.XDEBUG_DEBUGGER_ID;

	private static final String EXTENSIONS = "extensions";

	/**
	 * PHP Language name.
	 */
	public static final String PHP_LANGUAGE_NAME = "php";

	private static Object lock = new Object();
	// A singleton instance
	private static PHPexes instance;

	// Hold a mapping from the debugger ID to a map of installed
	private HashMap<String, HashMap<String, PHPexeItem>> items = new HashMap<String, HashMap<String, PHPexeItem>>();
	private ArrayList<PHPexeItem> allItems = new ArrayList<PHPexeItem>();
	// Hold a mapping to each debugger default PHPExeItem.
	private PHPexeItem defaultItem;

	private final LinkedList<IPHPExesListener> listeners = new LinkedList<IPHPExesListener>();

	/**
	 * Returns a single instance of this PHPexes class.
	 * 
	 * @return A singleton PHPexes instance.
	 */
	public static PHPexes getInstance()
	{
		synchronized (lock)
		{
			if (instance == null)
			{
				instance = new PHPexes();
			}
			return instance;
		}
	}

	// Private constructor
	private PHPexes()
	{
		load();
	}

	/**
	 * Change to executable permissions for non-windows machines.
	 */
	public static void changePermissions(File file)
	{
		if (!Platform.getOS().equals(Platform.OS_WIN32))
		{
			LocalFile localFile = new LocalFile(file);
			IFileInfo info = localFile.fetchInfo();
			if (!info.getAttribute(EFS.ATTRIBUTE_EXECUTABLE))
			{
				info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, true);
				try
				{
					localFile.putInfo(info, EFS.SET_ATTRIBUTES, null);
				}
				catch (CoreException e)
				{
					Logger.logException(e);
				}
			}
		}
	}

	/**
	 * Adds a {@link PHPexeItem} to the list of installed items that are assigned to its debugger id. Note that the
	 * first inserted item will set to be the default one until a call to {@link #setDefaultItem(PHPexeItem)} is made.
	 * 
	 * @param item
	 * @see #setDefaultItem(PHPexeItem)
	 * @see #getItem(String, String)
	 */
	public synchronized void addItem(PHPexeItem item)
	{
		allItems.add(item);
		String debuggerId = item.getDebuggerID();
		HashMap<String, PHPexeItem> map = items.get(debuggerId);
		if (map == null)
		{
			map = new HashMap<String, PHPexeItem>();
			items.put(debuggerId, map);
		}
		// Set the default item.
		if (map.isEmpty() && item.isDefault())
		{
			setDefaultItem(item);
		}
		map.put(item.getName(), item);
		synchronized (listeners)
		{
			IPHPExesListener[] allListeners = new IPHPExesListener[listeners.size()];
			listeners.toArray(allListeners);
			for (IPHPExesListener listener : allListeners)
			{
				PHPExesEvent phpExesEvent = new PHPExesEvent(item);
				listener.phpExeAdded(phpExesEvent);
			}
		}
	}

	/**
	 * Returns the default item for the specified debugger.
	 * 
	 * @return The default PHPexeItem for the given debugger, or null if no such debugger exists.
	 */
	public PHPexeItem getDefaultItem(String debuggerId)
	{
		if (defaultItem != null)
		{
			if (defaultItem.getDebuggerID().equals(debuggerId))
			{
				return defaultItem;
			}
		}
		return null;
	}

	/**
	 * Returns true if there are PHP Interpreters registered to the given debugger.
	 * 
	 * @param debuggerId The debugger id.
	 * @return True, if there are executables for this debugger; False, otherwise.
	 * @see #hasItems()
	 */
	public boolean hasItems(String debuggerId)
	{
		HashMap<String, PHPexeItem> map = items.get(debuggerId);
		return map != null && map.size() > 0;
	}

	/**
	 * Returns true if there are any registered PHP Interpreters.
	 * 
	 * @return True, if there is at least one registered PHP Interpreter; False, otherwise.
	 * @see #hasItems(String)
	 */
	public boolean hasItems()
	{
		return getAllItems().length > 0;
	}

	/**
	 * Returns all the editable items.
	 * 
	 * @return An array of editable PHPExeItems.
	 */
	public PHPexeItem[] getEditableItems()
	{
		Set<String> installedDebuggers = PHPDebuggersRegistry.getDebuggersIds();
		ArrayList<PHPexeItem> list = new ArrayList<PHPexeItem>();
		for (String debuggerId : installedDebuggers)
		{
			HashMap<String, PHPexeItem> installedExes = items.get(debuggerId);
			if (installedExes != null)
			{
				Set<String> exeNames = installedExes.keySet();
				for (String name : exeNames)
				{
					PHPexeItem exeItem = installedExes.get(name);
					if (exeItem.isEditable())
					{
						list.add(exeItem);
					}
				}
			}
		}
		return list.toArray(new PHPexeItem[list.size()]);
	}

	/**
	 * Returns the {@link PHPexeItem} for the given debuggerId that has the given name.
	 * 
	 * @param debuggerId
	 * @param name
	 * @return A {@link PHPexeItem} or null if none is installed.
	 */
	public PHPexeItem getItem(String debuggerId, String name)
	{
		HashMap<String, PHPexeItem> map = items.get(debuggerId);
		if (map == null)
		{
			return null;
		}
		return map.get(name);
	}

	/**
	 * Search for the executable file name in all of the registered {@link PHPexeItem}s and return a reference to the
	 * item that refer to the same file. This method invokes the {@link #getItemForFile(String, String)} and returns the
	 * first item in the array.
	 * 
	 * @param exeFilePath The executable file name.
	 * @param iniFilePath The php ini file path (can be null).
	 * @return The corresponding {@link PHPexeItem}, or null if none was found.
	 */
	public PHPexeItem getItemForFile(String exeFilePath, String iniFilePath)
	{
		PHPexeItem[] items = getItemsForFile(exeFilePath, iniFilePath);
		if (items != null && items.length > 0)
		{
			return items[0];
		}
		return null;
	}

	/**
	 * Search for the executable file name in all of the registered {@link PHPexeItem}s and return a reference to the
	 * items that refer to the same file.
	 * 
	 * @param exeFilePath The executable file name.
	 * @param iniFilePath The php ini file path (can be null).
	 * @return The corresponding {@link PHPexeItem}, or null if none was found.
	 */
	public PHPexeItem[] getItemsForFile(String exeFilePath, String iniFilePath)
	{
		ArrayList<PHPexeItem> result = new ArrayList<PHPexeItem>(5);
		Set<String> installedDebuggers = PHPDebuggersRegistry.getDebuggersIds();
		for (String debuggerId : installedDebuggers)
		{
			HashMap<String, PHPexeItem> installedExes = items.get(debuggerId);
			if (installedExes != null)
			{
				Set<String> exeNames = installedExes.keySet();
				for (String name : exeNames)
				{
					PHPexeItem exeItem = installedExes.get(name);
					// Check for ini equality
					boolean iniEquals = true;
					if (iniFilePath != null)
					{
						iniEquals = exeItem.getINILocation() == null ? iniFilePath == null || iniFilePath.equals("")
								: iniFilePath.equals(exeItem.getINILocation().toString());
					}
					if (iniEquals && exeFilePath != null && exeFilePath.equals(exeItem.getExecutable().toString()))
					{
						result.add(exeItem);
					}
				}
			}
		}
		if (result.isEmpty())
		{
			return null;
		}
		return result.toArray(new PHPexeItem[result.size()]);
	}

	/**
	 * Returns the PHPExeItems registered for the given debugger id.
	 * 
	 * @param debuggerId
	 * @return An array of installed exe items for the given debugger; null if no such debugger is registered, or the
	 *         debugger does not have any executables.
	 */
	public PHPexeItem[] getItems(String debuggerId)
	{
		HashMap<String, PHPexeItem> installedExes = items.get(debuggerId);
		if (installedExes == null)
		{
			return null;
		}
		PHPexeItem[] retItems = new PHPexeItem[installedExes.size()];
		return installedExes.values().toArray(retItems);
	}

	/**
	 * Returns an array of all the installed {@link PHPexeItem}s for all the installed debuggers.
	 * 
	 * @return An array of all the installed debuggers.
	 */
	public PHPexeItem[] getAllItems()
	{
		ArrayList<PHPexeItem> allItems = new ArrayList<PHPexeItem>();
		Set<String> debuggers = items.keySet();
		for (String debugger : debuggers)
		{
			HashMap<String, PHPexeItem> debuggerItems = items.get(debugger);
			if (debuggerItems != null)
			{
				Collection<PHPexeItem> exeItems = debuggerItems.values();
				for (PHPexeItem item : exeItems)
				{
					allItems.add(item);
				}
			}
		}
		return allItems.toArray(new PHPexeItem[allItems.size()]);
	}

	// Load executables from the preferences and validate the default interpreter.
	private void load()
	{

		items = new HashMap<String, HashMap<String, PHPexeItem>>();

		List<IInterpreter> interpreters = Interpreters.getDefault().getInterpreters(PHP_LANGUAGE_NAME);
		for (IInterpreter interpreter : interpreters)
		{
			// TODO add some more general handling
			if (interpreter instanceof PHPexeItem)
			{
				addItem((PHPexeItem) interpreter);
			}
		}

		// Make sure we load the right default.
		// In case this is not the first run, we should have a default set into the INSTALLED_PHP_DEFAULTS key.
		// Once we get it out, we can set the default.
		Preferences prefs = PHPProjectPreferences.getModelPreferences();
		String defaultDebuggerSetting = prefs.getString(PHPDebugCorePreferenceNames.INSTALLED_PHP_DEFAULTS);
		if (defaultDebuggerSetting != null && defaultDebuggerSetting.length() != 0)
		{
			String[] debuggerDefault = defaultDebuggerSetting.split("=");
			if (debuggerDefault.length == 2)
			{
				PHPexeItem item = getItem(debuggerDefault[0], debuggerDefault[1]);
				if (item != null)
				{
					setDefaultItem(item);
					return;
				}
			}
		}

		// In this case we have no setting for the INSTALLED_PHP_DEFAULTS, or the previous setting is invalid.
		// Since the code was taken from the PDT, there is a possibility for several default debuggers (one for each of
		// the
		// debugger types
		// Our plug-in supports only one default debugger, so we should search and set the first CGI interpreter to be
		// used as default.
		String debuggerId = PHPDebugEPLPlugin.getCurrentDebuggerId();
		PHPexeItem defaultItem = getDefaultItem(debuggerId);
		if (defaultItem != null)
		{
			setDefaultItem((PHPexeItem) defaultItem);
		}
		else
		{
			// Set the default item for the current debugger id
			PHPexeItem[] itemsForDebugger = getItems(debuggerId);
			if (itemsForDebugger == null)
			{
				// the default debugger was probably removed from the system.
				// Try to set a different debugger.
				if (PHPexes.ZEND_DEBUGGER_ID.equals(debuggerId))
				{
					debuggerId = PHPexes.XDEBUG_DEBUGGER_ID;
				}
				else 
				{
					debuggerId = PHPexes.ZEND_DEBUGGER_ID;
				}
				itemsForDebugger = getItems(debuggerId);
				if (itemsForDebugger == null)
				{
					// both debuggers have been removed.
					return;
				}
				// Set the new default
				prefs.setValue(IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID, debuggerId);
				PHPDebugEPLPlugin.getDefault().savePluginPreferences();
			}
			PHPexeItem first = null;
			PHPexeItem firstCGI = null;
			for (PHPexeItem item : itemsForDebugger)
			{
				if (item.isDefault())
				{
					setDefaultItem(item);
					first = null;
					break;
				}
				if (first == null)
				{
					first = item;
				}
				if (firstCGI == null)
				{
					if (PHPexeItem.SAPI_CGI.equals(item.getSapiType()))
					{
						firstCGI = item;
					}
				}
			}
			// In case there was no defined default interpreter, make sure that the first CGI that we located is set to
			// be the default.
			// In case we could not locate a CGI, set it to the first CLI.
			if (firstCGI != null)
			{
				setDefaultItem(firstCGI);
			}
			else if (first != null)
			{
				setDefaultItem(first);
			}
		}
	}

	/**
	 * Removes an item. In case the removed item was the default one, a different random item will be picked to be the
	 * new default one for the specific debugger.
	 * 
	 * @param debuggerID
	 * @param item
	 */
	public synchronized void removeItem(PHPexeItem item)
	{
		allItems.remove(item);
		String debuggerID = item.getDebuggerID();
		HashMap<String, PHPexeItem> exes = items.get(debuggerID);
		PHPexeItem removedItem = null;
		if (exes != null)
		{
			removedItem = exes.remove(item.getName());
		}
		if (removedItem != null && removedItem.isDefault())
		{
			defaultItem = null;
		}

		// Notify
		IPHPExesListener[] allListeners = new IPHPExesListener[listeners.size()];
		listeners.toArray(allListeners);
		for (IPHPExesListener listener : allListeners)
		{
			PHPExesEvent phpExesEvent = new PHPExesEvent(item);
			listener.phpExeRemoved(phpExesEvent);
		}
	}

	/**
	 * Sets a default exe item for its debugger id.
	 * 
	 * @param defaultItem
	 */
	public synchronized void setDefaultItem(PHPexeItem defaultItem)
	{
		if (defaultItem == this.defaultItem)
		{
			return;
		}
		// String debuggerID = defaultItem.getDebuggerID();
		// Remove any item that was previously set as default.
		for (PHPexeItem e : allItems)
		{
			if (e.equals(defaultItem))
			{
				e.setDefault(true);
			}
			else
			{
				e.setDefault(false);
			}
		}
		defaultItem.setDefault(true);
		PHPexeItem oldDefault = this.defaultItem;
		this.defaultItem = defaultItem;

		// Notify
		IPHPExesListener[] allListeners = new IPHPExesListener[listeners.size()];
		listeners.toArray(allListeners);
		for (IPHPExesListener listener : allListeners)
		{
			listener.phpExeDefaultChanged(oldDefault, this.defaultItem);
		}
	}

	/**
	 * Sets a default exe item for the given debugger.
	 * 
	 * @param debuggerID
	 * @param defaultItem
	 */
	public void setDefaultItem(String debuggerID, String defaultItemName)
	{
		PHPexeItem item = getItem(debuggerID, defaultItemName);
		if (item != null)
		{
			setDefaultItem(item);
		}
	}

	/**
	 * Save the edited PHP Interpreter items to the plug-in preferences.
	 */
	@SuppressWarnings("unchecked")
	public void save()
	{
		Preferences prefs = PHPProjectPreferences.getModelPreferences();
		final PHPexeItem[] phpItems = getEditableItems();
		final StringBuffer locationsString = new StringBuffer();
		final StringBuffer inisString = new StringBuffer();
		final StringBuffer namesString = new StringBuffer();
		final StringBuffer debuggersString = new StringBuffer();
		final StringBuffer extensionsString = new StringBuffer();
		for (int i = 0; i < phpItems.length; i++)
		{
			final PHPexeItem item = phpItems[i];
			if (i > 0)
			{
				locationsString.append(SEPARATOR);
				inisString.append(SEPARATOR);
				namesString.append(SEPARATOR);
				debuggersString.append(SEPARATOR);
				extensionsString.append(SEPARATOR);
			}
			locationsString.append(item.getExecutable().toString());
			inisString.append(item.getINILocation() != null ? item.getINILocation().toString() : "null"); //$NON-NLS-1$
			namesString.append(item.getName());
			debuggersString.append(item.getDebuggerID());
			String[] extensions2 = item.getExtensions();
			for (String s : extensions2)
			{
				extensionsString.append(s);
				extensionsString.append((char) 5);
			}
		}
		prefs.setValue(PHPDebugCorePreferenceNames.INSTALLED_PHP_NAMES, namesString.toString());
		prefs.setValue(PHPDebugCorePreferenceNames.INSTALLED_PHP_LOCATIONS, locationsString.toString());
		prefs.setValue(PHPDebugCorePreferenceNames.INSTALLED_PHP_INIS, inisString.toString());
		prefs.setValue(PHPDebugCorePreferenceNames.INSTALLED_PHP_DEBUGGERS, debuggersString.toString());
		prefs.setValue(EXTENSIONS, extensionsString.toString());

		// save the default executables per debugger id
		final StringBuffer defaultsString = new StringBuffer();
		Iterator<PHPexeItem> iterator = (Iterator<PHPexeItem>) (defaultItem != null ? Collections
				.singleton(defaultItem).iterator() : Collections.emptySet().iterator());
		while (iterator.hasNext())
		{
			PHPexeItem exeItem = iterator.next();
			defaultsString.append(exeItem.getDebuggerID());
			defaultsString.append('=');
			defaultsString.append(exeItem.getName());
			if (iterator.hasNext())
			{
				defaultsString.append(SEPARATOR);
			}
		}
		prefs.setValue(PHPDebugCorePreferenceNames.INSTALLED_PHP_DEFAULTS, defaultsString.toString());
		if (defaultItem != null)
		{
			prefs.setValue(IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID, defaultItem.getDebuggerID());
		}
		PHPDebugEPLPlugin.getDefault().savePluginPreferences();
	}

	public void addPHPExesListener(IPHPExesListener listener)
	{
		synchronized (listeners)
		{
			listeners.add(listener);
		}
	}

	public void removePHPExesListener(IPHPExesListener listener)
	{
		synchronized (listeners)
		{
			listeners.remove(listener);
		}
	}

	/**
	 * Locate and return the first CGI interpreter for the give debugger id. May return null if non is found.
	 * 
	 * @param debuggerId The debugger Id string
	 * @param sapiType Should be {@link PHPexeItem#SAPI_CGI} or {@link PHPexeItem#SAPI_CLI}
	 */
	public static PHPexeItem locateCGI(String debuggerId, String sapiType)
	{
		PHPexeItem[] items = PHPexes.getInstance().getItems(debuggerId);
		for (PHPexeItem item : items)
		{
			if (PHPexeItem.SAPI_CGI.equals(item.getSapiType()))
			{
				return item;
			}
		}
		return null;
	}
}
