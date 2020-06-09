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
package org2.eclipse.php.internal.debug.core.preferences;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;


/**
 * Common preference keys used by PHP Debug
 */
public class PHPDebugCorePreferenceNames {
	private PHPDebugCorePreferenceNames() {
		// empty private constructor so users cannot instantiate class
	}

	private static final String Debug_ID = PHPDebugEPLPlugin.PLUGIN_ID;

	public static final String STOP_AT_FIRST_LINE = Debug_ID + "stop_at_first_line_string";//$NON-NLS-1$
	public static final String ZEND_DEBUG_PORT = Debug_ID + "zend_debug_port";//$NON-NLS-1$
	public static final String CLIENT_IP = Debug_ID + "client_ip";//$NON-NLS-1$

	// Workspace defaults PHP executables keys
	public static final String INSTALLED_PHP_NAMES = Debug_ID + "installedPHPNames";//$NON-NLS-1$
	public static final String INSTALLED_PHP_LOCATIONS = Debug_ID + "installedPHPLocations";//$NON-NLS-1$
	public static final String INSTALLED_PHP_INIS = Debug_ID + "installedPHPInis";//$NON-NLS-1$
	public static final String INSTALLED_PHP_DEBUGGERS = Debug_ID + "installedPHPDebuggers";//$NON-NLS-1$
	public static final String INSTALLED_PHP_DEFAULTS = Debug_ID + "installedPHPDefaults";//$NON-NLS-1$

	// Project default PHP executables keys
	public static final String DEFAULT_PHP = Debug_ID + "defaultPHP";//$NON-NLS-1$

	public static final String DIALOG_COLUMN_WIDTH = Debug_ID + "phpdebug.dialog.columnwidth";//$NON-NLS-1$
	public static final String RUN_WITH_DEBUG_INFO = Debug_ID + "run_with_debug_info";//$NON-NLS-1$
	public static final String OPEN_IN_BROWSER = Debug_ID + "open_in_browser";//$NON-NLS-1$
	public static final String AUTO_SAVE_DIRTY = Debug_ID + "auto_save_dirty";//$NON-NLS-1$
	public static final String OPEN_DEBUG_VIEWS = Debug_ID + "open_debug_views";//$NON-NLS-1$
	public static final String TRANSFER_ENCODING = Debug_ID + "transfer_encoding";//$NON-NLS-1$
	public static final String OUTPUT_ENCODING = Debug_ID + "output_encoding";//$NON-NLS-1$
}
