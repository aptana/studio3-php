/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Preferences;

import com.aptana.editor.php.PHPEditorPlugin;

/**
 * @author Pavel Petrochenko
 */
@SuppressWarnings("deprecation")
public final class LibraryManager
{

	private static final String USERLIBRARIES = "com.aptana.php.interpreters.libraries"; //$NON-NLS-1$

	private static final String LIBRARIES_TURNED_OFF = "com.aptana.php.interpreters.libraries.turnedOff"; //$NON-NLS-1$

	private LibraryManager()
	{
		// super("com.aptana.ide.php.interpreters.libraries");
		String string = PHPEditorPlugin.getDefault().getPluginPreferences().getString(LIBRARIES_TURNED_OFF);
		if (string != null && string.length() > 0)
		{
			String[] split = string.split(","); //$NON-NLS-1$
			for (int a = 0; a < split.length; a++)
			{
				turnedOff.add(split[a].trim());
			}
		}
		String str = PHPEditorPlugin.getDefault().getPluginPreferences().getString(USERLIBRARIES);
		if (str != null && str.length() != 0)
		{
			String[] split = str.split("\r"); //$NON-NLS-1$
			for (String s : split)
			{
				userLibraries.add(new UserLibrary(s));
			}
		}
	}

	public void setUserLibraries(UserLibrary[] libraries)
	{
		StringBuilder bld = new StringBuilder();
		for (UserLibrary l : libraries)
		{
			bld.append(l.toString());
			bld.append('\r');
		}
		if (bld.length() > 0)
		{
			bld.deleteCharAt(bld.length() - 1);
		}
		PHPEditorPlugin.getDefault().getPluginPreferences().setValue(USERLIBRARIES, bld.toString());
		this.userLibraries = new HashSet<UserLibrary>(Arrays.asList(libraries));
		PHPEditorPlugin.getDefault().savePluginPreferences();
		for (ILibraryListener l : listeners)
		{
			l.userLibrariesChanged(libraries);
		}

	}

	private HashSet<ILibraryListener> listeners = new HashSet<ILibraryListener>();

	private static LibraryManager instance;

	private HashSet<String> turnedOff = new HashSet<String>();

	private HashSet<UserLibrary> userLibraries = new HashSet<UserLibrary>();

	public void addLibraryListener(ILibraryListener libraryListener)
	{
		listeners.add(libraryListener);
	}

	public void removeLibraryListener(ILibraryListener libraryListener)
	{
		listeners.remove(libraryListener);
	}

	public static synchronized LibraryManager getInstance()
	{
		if (instance == null)
		{
			instance = new LibraryManager();
		}
		return instance;
	}

	public boolean isTurnedOn(IPHPLibrary lib)
	{
		return !turnedOff.contains(lib.getId().trim());
	}

	public PHPLibrary[] getAll()
	{
		/*
		 * TODO - Shalom: Hook the PHP libraries RegistryLazyObject[] all = super.getAll(); PHPLibrary[] result = new
		 * PHPLibrary[all.length]; for (int a = 0; a < all.length; a++) { result[a] = (PHPLibrary) all[a]; }
		 */
		return new PHPLibrary[0];
	}

	public IPHPLibrary[] getAllLibraries()
	{
		/*
		 * TODO - Shalom: Hook the PHP libraries RegistryLazyObject[] all = super.getAll(); IPHPLibrary[] result = new
		 * IPHPLibrary[all.length+userLibraries.size()]; int a; for (a = 0; a < all.length; a++) { result[a] =
		 * (PHPLibrary) all[a]; } for (UserLibrary l:userLibraries) { result[a++]=l; }
		 */
		return new PHPLibrary[0];
	}

	public PHPLibrary getObject(String id)
	{
		// TODO - Shalom: Hook the PHP libraries
		// return (PHPLibrary) super.getObject(id);
		return (PHPLibrary) null;
	}

	/*
	 * TODO - Shalom: Hook the PHP libraries
	 * @Override protected RegistryLazyObject createObject( IConfigurationElement configurationElement) { return new
	 * PHPLibrary(configurationElement); }
	 */
	public void setTurnedOff(Set<IPHPLibrary> turnedOff)
	{
		StringBuilder bld = new StringBuilder();
		HashSet<IPHPLibrary> currentLibraries = new HashSet<IPHPLibrary>();
		for (IPHPLibrary l : getAllLibraries())
		{
			if (l.isTurnedOn())
			{
				currentLibraries.add(l);
			}
		}
		HashSet<String> tn = new HashSet<String>();
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
		Preferences pluginPreferences = PHPEditorPlugin.getDefault().getPluginPreferences();
		pluginPreferences.setValue(LIBRARIES_TURNED_OFF, bld.toString());
		this.turnedOff = tn;
		HashSet<PHPLibrary> newLibraries = new HashSet<PHPLibrary>();
		for (PHPLibrary l : getAll())
		{
			if (l.isTurnedOn())
			{
				newLibraries.add(l);
			}
		}
		HashSet<IPHPLibrary> added = new HashSet<IPHPLibrary>();
		HashSet<IPHPLibrary> removed = new HashSet<IPHPLibrary>();
		for (PHPLibrary l : newLibraries)
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
	 * @param id
	 * @return library with a given id
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

}
