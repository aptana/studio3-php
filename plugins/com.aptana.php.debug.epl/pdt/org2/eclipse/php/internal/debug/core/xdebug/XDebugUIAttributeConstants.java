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

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;


public interface XDebugUIAttributeConstants {
	// preference page constants
	public static final String XDEBUG_PREF_PORT = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_port";
	public static final String XDEBUG_PREF_SHOWSUPERGLOBALS = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_showSuperGlobals";
	public static final String XDEBUG_PREF_ARRAYDEPTH = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_arrayDepth";
	public static final String XDEBUG_PREF_MULTISESSION = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_multisession";


	// launch id constants
	/*
	public static final String LAUNCH_CONFIG_TYPE_EXE = "org2.eclipse.php.xdebug.core.XdebugExelaunchConfigurationType";
	public static final String LAUNCH_CONFIG_TYPE_WEB = "org2.eclipse.php.xdebug.core.XdebugWeblaunchConfigurationType";
	*/
}
