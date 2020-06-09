/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.xdebug;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpPreferences;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

public class XDebugPreferenceInit {

	public static void setDefaults() {
		IEclipsePreferences prefs = new DefaultScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID);
		prefs.putInt(XDebugUIAttributeConstants.XDEBUG_PREF_PORT, getPortDefault());
		prefs.putBoolean(XDebugUIAttributeConstants.XDEBUG_PREF_SHOWSUPERGLOBALS, showSuperGlobalsDefault());
		prefs.putInt(XDebugUIAttributeConstants.XDEBUG_PREF_ARRAYDEPTH, getDepthDefault());
		prefs.putBoolean(XDebugUIAttributeConstants.XDEBUG_PREF_MULTISESSION, useMultiSessionDefault());
	}

	public static int getDepthDefault() {
		return 3;
	}

	public static int getPortDefault() {
		return DBGpPreferences.DBGP_PORT_DEFAULT;
	}

	public static int getTimeoutDefault() {
		return DBGpPreferences.DBGP_TIMEOUT_DEFAULT;
	}

	public static boolean showSuperGlobalsDefault() {
		return true;
	}

	public static boolean useMultiSessionDefault() {
		return false;
	}

	/*
	public static String getDefaultServerURL() {
		return "http://localhost";
	}
	*/
}
