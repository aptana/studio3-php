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
package com.aptana.editor.php.internal.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Preferences;

import com.aptana.editor.php.PHPEditorPlugin;

/**
 * 
 * @author Pavel Petrochenko
 * 
 */
public final class LibraryManager {

	private static final String USERLIBRARIES = "com.aptana.php.interpreters.libraries";
	
	private static final String LIBRARIES_TURNED_OFF = "com.aptana.php.interpreters.libraries.turnedOff";

	private LibraryManager() {
//		super("com.aptana.ide.php.interpreters.libraries");
		String string = PHPEditorPlugin.getDefault().getPluginPreferences()
				.getString(LIBRARIES_TURNED_OFF);
		if (string != null && string.length() > 0) {
			String[] split = string.split(",");
			for (int a = 0; a < split.length; a++) {
				turnedOff.add(split[a].trim());
			}
		}
		String str=PHPEditorPlugin.getDefault().getPluginPreferences().getString(USERLIBRARIES);
		if (str!=null&&str.length()!=0) {
			String[] split = str.split("\r");
			for (String s:split) {
				userLibraries.add(new UserLibrary(s));
			}
		}
	}
	
	public void setUserLibraries(UserLibrary[] libraries) {
		StringBuilder bld=new StringBuilder();
		for (UserLibrary l:libraries) {
			bld.append(l.toString());
			bld.append('\r');
		}
		if (bld.length()>0) {
		bld.deleteCharAt(bld.length()-1);
		}
		PHPEditorPlugin.getDefault().getPluginPreferences().setValue(USERLIBRARIES, bld.toString());
		this.userLibraries=new HashSet<UserLibrary>(Arrays.asList(libraries));
		PHPEditorPlugin.getDefault().savePluginPreferences();
		for (ILibraryListener l : listeners) {
			l.userLibrariesChanged(libraries);
		}
		
	}

	private HashSet<ILibraryListener> listeners = new HashSet<ILibraryListener>();

	private static LibraryManager instance;

	private HashSet<String> turnedOff = new HashSet<String>();
	
	private HashSet<UserLibrary> userLibraries=new HashSet<UserLibrary>();

	public void addLibraryListener(ILibraryListener libraryListener) {
		listeners.add(libraryListener);
	}

	public void removeLibraryListener(ILibraryListener libraryListener) {
		listeners.remove(libraryListener);
	}

	public static synchronized LibraryManager getInstance() {
		if (instance == null) {
			instance = new LibraryManager();
		}
		return instance;
	}

	public boolean isTurnedOn(IPHPLibrary lib) {
		return !turnedOff.contains(lib.getId().trim());
	}

	public PHPLibrary[] getAll() {
		/* TODO - Shalom: Hook the PHP libraries
		RegistryLazyObject[] all = super.getAll();
		PHPLibrary[] result = new PHPLibrary[all.length];
		for (int a = 0; a < all.length; a++) {
			result[a] = (PHPLibrary) all[a];
		}
	    */
		return new PHPLibrary[0];
	}
	
	public IPHPLibrary[] getAllLibraries() {
		/* TODO - Shalom: Hook the PHP libraries
		RegistryLazyObject[] all = super.getAll();
		IPHPLibrary[] result = new IPHPLibrary[all.length+userLibraries.size()];
		int a;
		for (a = 0; a < all.length; a++) {
			result[a] = (PHPLibrary) all[a];
		}
		for (UserLibrary l:userLibraries) {
			result[a++]=l;
		}
		*/
		return new PHPLibrary[0];
	}

	public PHPLibrary getObject(String id) {
		// TODO - Shalom: Hook the PHP libraries
        //		return (PHPLibrary) super.getObject(id);
		return (PHPLibrary)null;
	}

	/* TODO - Shalom: Hook the PHP libraries
	@Override
	protected RegistryLazyObject createObject(
			IConfigurationElement configurationElement) {
		return new PHPLibrary(configurationElement);
	}
    */ 
	public void setTurnedOff(Set<IPHPLibrary> turnedOff) {
		StringBuilder bld = new StringBuilder();
		HashSet<IPHPLibrary> currentLibraries = new HashSet<IPHPLibrary>();
		for (IPHPLibrary l : getAllLibraries()) {
			if (l.isTurnedOn()) {
				currentLibraries.add(l);
			}
		}
		HashSet<String> tn = new HashSet<String>();
		for (IPHPLibrary l : turnedOff) {
			bld.append(l.getId());
			tn.add(l.getId().trim());
			bld.append(',');
		}
		if (bld.length()>0) {
		bld = bld.deleteCharAt(bld.length() - 1);
		}
		Preferences pluginPreferences = PHPEditorPlugin.getDefault()
				.getPluginPreferences();
		pluginPreferences.setValue(LIBRARIES_TURNED_OFF, bld.toString());
		this.turnedOff = tn;
		HashSet<PHPLibrary> newLibraries = new HashSet<PHPLibrary>();
		for (PHPLibrary l : getAll()) {
			if (l.isTurnedOn()) {
				newLibraries.add(l);
			}
		}
		HashSet<IPHPLibrary> added = new HashSet<IPHPLibrary>();
		HashSet<IPHPLibrary> removed = new HashSet<IPHPLibrary>();
		for (PHPLibrary l : newLibraries) {
			if (!currentLibraries.contains(l)) {
				added.add(l);
			}
		}
		for (IPHPLibrary l : currentLibraries) {
			if (!newLibraries.contains(l)) {
				removed.add(l);
			}
		}
		if (!added.isEmpty() || !removed.isEmpty()) {
			for (ILibraryListener l : listeners) {
				l.librariesChanged(added, removed);
			}
		}
	}

	/**
	 * 
	 * @param id
	 * @return library with a given id 
	 */
	public IPHPLibrary getLibrary(String id) {
		IPHPLibrary[] allLibraries = getAllLibraries();
		for (IPHPLibrary l:allLibraries) {
			if (l.getId().equals(id)) {
				return l;
			}
		}
		return null;
	}

	
}
