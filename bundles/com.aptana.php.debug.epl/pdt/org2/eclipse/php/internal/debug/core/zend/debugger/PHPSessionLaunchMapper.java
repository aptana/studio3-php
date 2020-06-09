/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.zend.debugger;


import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.swt.browser.Browser;
import org2.eclipse.php.internal.core.util.collections.IntHashtable;
import org2.eclipse.php.internal.core.util.collections.IntMap;
/**
 * This class is responsible for mapping debug session id's to the ILaunch that is responsible for
 * the session.
 *
 * @author Shalom Gibly
 */
public class PHPSessionLaunchMapper implements ILaunchesListener {

	private static final String SYSTEM_DEBUG_PROPERTY = "org2.eclipse.php.debug.ui.activeDebugging";
	private static PHPSessionLaunchMapper instance;
	private IntHashtable map;

	private PHPSessionLaunchMapper() {
		map = new IntHashtable(10);
	}

	private static PHPSessionLaunchMapper getInstance() {
		if (instance == null) {
			instance = new PHPSessionLaunchMapper();
			DebugPlugin.getDefault().getLaunchManager().addLaunchListener(instance);
		}
		return instance;
	}

	/**
	 * Put a session Id mapping to an ILaunch.
	 *
	 * @param sessionID
	 * @param launch
	 */
	public static void put(int sessionID, ILaunch launch) {
		getInstance().map.put(sessionID, launch);
	}

	/**
	 * Returns the ILaunch mapped to the given session ID.
	 * In case there is no such map, null is returned.
	 *
	 * @param sessionID
	 * @return	The mapped ILaunch, or null if non exists.
	 */
	public static ILaunch get(int sessionID) {
		return (ILaunch) getInstance().map.get(sessionID);
	}

	/**
	 * Removes the ILaunch mapped to the given sessionID and returns it.
	 * Returns null if no such session ID was found in the mapper.
	 *
	 * @param sessionID
	 * @return	The removed ILaunch, or null if non exists.
	 */
	public static ILaunch remove(int sessionID) {
		return (ILaunch) getInstance().map.remove(sessionID);
	}

	public void launchesAdded(ILaunch[] launches) {
		updateSystemProperty(launches);
	}

	public void launchesChanged(ILaunch[] launches) {
		updateSystemProperty(launches);
	}

	public void launchesRemoved(ILaunch[] launches) {
		// Remove any launch mapping if the launch was removed and we are still mapping it.
		IntMap.Entry[] entries = new IntMap.Entry[map.size()];
		map.entrySet().toArray(entries);
		for (IntMap.Entry entry : entries) {
			for (ILaunch element : launches) {
				if (entry.getValue() == element) {
					map.remove(entry.getKey());
				}
			}
		}
		updateSystemProperty(launches);
		if (DebugPlugin.getDefault().getLaunchManager().getLaunches().length == 0) {
			// In case we have no more launches, clear the browser's cache (cookies) to avoid any debug session trigger as a result
			// of a remaining cookie.
			Browser.clearSessions();
		}
	}

	/**
	 * Update the "org2.eclipse.php.debug.ui.activeDebugging" system property.
	 * This method is important for any action that is defined to be visible when a debug session is
	 * active (such as the Run to Line action).
	 *
	 * @param launches
	 */
	public static void updateSystemProperty(ILaunch[] launches) {
		boolean hasActiveLaunch = false;
		for (ILaunch launch : launches) {
			hasActiveLaunch |= !launch.isTerminated();
		}
		System.setProperty(SYSTEM_DEBUG_PROPERTY, hasActiveLaunch ? "true" : "false");
	}
}
