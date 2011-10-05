/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.EclipseUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.php.PHPEditorPlugin;

/**
 * Libraries manager.
 * 
 * @author Pavel Petrochenko, Shalom Gibly
 */
public final class LibraryManager
{

	private static final String USERLIBRARIES = "com.aptana.editor.php.userLibraries"; //$NON-NLS-1$
	private static final String LIBRARIES_TURNED_OFF = "com.aptana.editor.php.turnedOffLibraries"; //$NON-NLS-1$

	private Set<ILibraryListener> listeners = new HashSet<ILibraryListener>();

	private static LibraryManager instance;

	private Set<String> turnedOff = new HashSet<String>();

	private Set<UserLibrary> userLibraries = new HashSet<UserLibrary>();

	/**
	 * Constructor
	 */
	private LibraryManager()
	{
		String string = readFromPreferences(LIBRARIES_TURNED_OFF);
		if (string != null && string.length() > 0)
		{
			String[] split = string.split(","); //$NON-NLS-1$
			for (int a = 0; a < split.length; a++)
			{
				turnedOff.add(split[a].trim());
			}
		}
		String str = readFromPreferences(USERLIBRARIES);
		if (str != null && str.length() != 0)
		{
			String[] split = str.split("\r"); //$NON-NLS-1$ // $codepro.audit.disable platformSpecificLineSeparator
			for (String s : split)
			{
				userLibraries.add(new UserLibrary(s));
			}
		}
	}

	/**
	 * Returns an instance of the LibraryManager
	 * 
	 * @return {@link LibraryManager}
	 */
	public static synchronized LibraryManager getInstance()
	{
		if (instance == null)
		{
			instance = new LibraryManager();
		}
		return instance;
	}

	/**
	 * Set user libraries.
	 * 
	 * @param libraries
	 */
	public void setUserLibraries(UserLibrary[] libraries)
	{
		StringBuilder bld = new StringBuilder();
		for (UserLibrary l : libraries)
		{
			bld.append(l.toString());
			bld.append('\r'); // $codepro.audit.disable platformSpecificLineSeparator
		}
		if (bld.length() > 0)
		{
			bld.deleteCharAt(bld.length() - 1);
		}
		saveToPreferences(USERLIBRARIES, bld.toString());
		this.userLibraries = new HashSet<UserLibrary>(Arrays.asList(libraries));
		for (ILibraryListener l : listeners)
		{
			l.userLibrariesChanged(libraries);
		}

	}

	public void addLibraryListener(ILibraryListener libraryListener)
	{
		listeners.add(libraryListener);
	}

	public void removeLibraryListener(ILibraryListener libraryListener)
	{
		listeners.remove(libraryListener);
	}

	public boolean isTurnedOn(IPHPLibrary lib)
	{
		return !turnedOff.contains(lib.getId().trim());
	}

	public IPHPLibrary[] getAllLibraries()
	{
		// TODO - Shalom: get pre-registered libraries that were contributed through an extesion point
		return userLibraries.toArray(new IPHPLibrary[userLibraries.size()]);
	}

	public void setTurnedOff(Set<IPHPLibrary> turnedOff)
	{
		StringBuilder bld = new StringBuilder();
		// Collect all the libraries that are currently turned on.
		Set<IPHPLibrary> currentLibraries = new HashSet<IPHPLibrary>();
		IPHPLibrary[] libraries = getAllLibraries();
		for (IPHPLibrary l : libraries)
		{
			if (l.isTurnedOn())
			{
				currentLibraries.add(l);
			}
		}
		Set<String> tn = new HashSet<String>();
		for (IPHPLibrary l : turnedOff)
		{
			bld.append(l.getId());
			tn.add(l.getId().trim());
			bld.append(',');
		}
		if (bld.length() > 0)
		{
			bld = bld.deleteCharAt(bld.length() - 1);
		}
		this.turnedOff = tn;
		// Save the 'off' libraries to the preferences.
		saveToPreferences(LIBRARIES_TURNED_OFF, bld.toString());

		// Collect the changes and notify the listeners
		Set<IPHPLibrary> newLibraries = new HashSet<IPHPLibrary>();
		for (IPHPLibrary l : libraries)
		{
			if (l.isTurnedOn())
			{
				newLibraries.add(l);
			}
		}
		Set<IPHPLibrary> added = new HashSet<IPHPLibrary>();
		Set<IPHPLibrary> removed = new HashSet<IPHPLibrary>();
		for (IPHPLibrary l : newLibraries)
		{
			if (!currentLibraries.contains(l))
			{
				added.add(l);
			}
		}
		for (IPHPLibrary l : currentLibraries)
		{
			if (!newLibraries.contains(l))
			{
				removed.add(l);
			}
		}
		if (!added.isEmpty() || !removed.isEmpty())
		{
			for (ILibraryListener l : listeners)
			{
				l.librariesChanged(added, removed);
			}
		}
	}

	/**
	 * Returns an {@link IPHPLibrary} with a given ID.
	 * 
	 * @param id
	 * @return Library with a given id; Null, if none is found.
	 */
	public IPHPLibrary getLibrary(String id)
	{
		IPHPLibrary[] allLibraries = getAllLibraries();
		for (IPHPLibrary l : allLibraries)
		{
			if (l.getId().equals(id))
			{
				return l;
			}
		}
		return null;
	}

	/**
	 * Save the libraries to the preferences.
	 * 
	 * @param key
	 *            the preferences key
	 * @param value
	 *            The string value to save
	 */
	private void saveToPreferences(String key, String value)
	{
		IEclipsePreferences prefs = EclipseUtil.instanceScope().getNode(PHPEditorPlugin.PLUGIN_ID);
		prefs.put(key, value);
		try
		{
			prefs.flush();
		}
		catch (BackingStoreException e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error saving to the preferences", e); //$NON-NLS-1$
		}
	}

	private String readFromPreferences(String key)
	{
		IEclipsePreferences prefs = EclipseUtil.instanceScope().getNode(PHPEditorPlugin.PLUGIN_ID);
		return prefs.get(key, StringUtil.EMPTY);
	}
}
