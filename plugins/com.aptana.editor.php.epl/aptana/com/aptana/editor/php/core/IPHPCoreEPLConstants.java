/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.core;

import com.aptana.editor.php.epl.PHPEplPlugin;

/**
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public interface IPHPCoreEPLConstants
{
	public static final String PLUGIN_ID = PHPEplPlugin.PLUGIN_ID;

	public static final String IP_VARIABLE_INITIALIZER_EXTPOINT_ID = "includePathVariables"; //$NON-NLS-1$

	// Project Option names
	public static final String PHPOPTION_DEFAULT_ENCODING = PLUGIN_ID + ".defaultEncoding"; //$NON-NLS-1$
	public static final String PHPOPTION_CONTEXT_ROOT = PLUGIN_ID + ".contextRoot"; //$NON-NLS-1$
	public static final String PHPOPTION_INCLUDE_PATH = PLUGIN_ID + ".includePath"; //$NON-NLS-1$

	public static final String INCLUDE_PATH_VARIABLE_NAMES = PLUGIN_ID + ".includePathVariableNames"; //$NON-NLS-1$
	public static final String INCLUDE_PATH_VARIABLE_PATHS = PLUGIN_ID + ".includePathVariablePaths"; //$NON-NLS-1$

	public static final String RESERVED_INCLUDE_PATH_VARIABLE_NAMES = PLUGIN_ID + ".includePathReservedVariableNames"; //$NON-NLS-1$
	public static final String RESERVED_INCLUDE_PATH_VARIABLE_PATHS = PLUGIN_ID + ".includePathReservedVariablePaths"; //$NON-NLS-1$
}
