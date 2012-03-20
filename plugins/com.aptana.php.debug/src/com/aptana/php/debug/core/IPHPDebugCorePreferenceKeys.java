/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core;

import com.aptana.php.debug.PHPDebugPlugin;

/**
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public interface IPHPDebugCorePreferenceKeys
{
	public static final String DEBUGGER_ID = PHPDebugPlugin.PLUGIN_ID;

	public static final String PHP_DEBUG_MODEL_PRESENTATION_ID = "org2.eclipse.php.debug.core"; //$NON-NLS-1$

	public static final String PHP_DEBUGGER_ID = DEBUGGER_ID + ".php_debugger_id";//$NON-NLS-1$
	public static final String NOTIFY_NON_STANDARD_PORT = DEBUGGER_ID + ".notifyNonStandardPort"; //$NON-NLS-1$
	public static final String BREAK_ON_FIRST_LINE_FOR_UNKNOWN_JIT = DEBUGGER_ID + ".breakOnFirstLineForUnknownJIT"; //$NON-NLS-1$
	public static final String ALLOW_MULTIPLE_LAUNCHES = DEBUGGER_ID + ".allowMultipleLaunches"; //$NON-NLS-1$
	public static final String SWITCH_BACK_TO_PREVIOUS_PERSPECTIVE = DEBUGGER_ID + ".switchBackToPreviousPerspective"; //$NON-NLS-1$
	public static final String CONFIGURATION_DELEGATE_CLASS = DEBUGGER_ID + ".configurationDelegateClass"; //$NON-NLS-1$

	public static final String ATTR_USE_SPECIFIC_FILE = "ATTR_USE_SPECIFIC_FILE"; //$NON-NLS-1$
	public static final String ATTR_FILE = "ATTR_FILE"; //$NON-NLS-1$
	public static final String ATTR_AUTO_GENERATED_URL = "ATTR_AUTO_GENERATED_URL"; //$NON-NLS-1$

	public static final String ATTR_SERVER_NAME = "ATTR_SERVER_NAME"; //$NON-NLS-1$
	public static final String ATTR_SERVER_FILE_NAME = "ATTR_SERVER_FILE_NAME"; //$NON-NLS-1$
	public static final String ATTR_SERVER_BASE_URL = "ATTR_SERVER_BASE_URL"; //$NON-NLS-1$

	public static final String ATTR_HTTP_POST = "ATTR_HTTP_POST"; //$NON-NLS-1$
	public static final String ATTR_HTTP_GET = "ATTR_HTTP_GET"; //$NON-NLS-1$

}
