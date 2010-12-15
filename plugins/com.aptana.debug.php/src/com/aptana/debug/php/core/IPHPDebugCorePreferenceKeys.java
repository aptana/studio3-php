/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
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
package com.aptana.debug.php.core;

import com.aptana.debug.php.PHPDebugPlugin;

/**
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public interface IPHPDebugCorePreferenceKeys
{
	public static final String DEBUGGER_ID = PHPDebugPlugin.PLUGIN_ID;

	public static final String PHP_DEBUG_MODEL_PRESENTATION_ID = DEBUGGER_ID + ".presentation.phpModelPresentation"; //$NON-NLS-1$

	public static final String PHP_DEBUGGER_ID = DEBUGGER_ID + ".php_debugger_id";//$NON-NLS-1$
	public static final String NOTIFY_NON_STANDARD_PORT = DEBUGGER_ID + ".notifyNonStandardPort"; //$NON-NLS-1$
	public static final String BREAK_ON_FIRST_LINE_FOR_UNKNOWN_JIT = DEBUGGER_ID + ".breakOnFirstLineForUnknownJIT"; //$NON-NLS-1$
	public static final String ALLOW_MULTIPLE_LAUNCHES = DEBUGGER_ID + ".allowMultipleLaunches"; //$NON-NLS-1$
	public static final String SWITCH_BACK_TO_PREVIOUS_PERSPECTIVE = DEBUGGER_ID + ".switchBackToPreviousPerspective"; //$NON-NLS-1$

	public static final String ATTR_USE_SPECIFIC_FILE = "ATTR_USE_SPECIFIC_FILE"; //$NON-NLS-1$
	public static final String ATTR_FILE = "ATTR_FILE"; //$NON-NLS-1$
	public static final String ATTR_AUTO_GENERATED_URL = "ATTR_AUTO_GENERATED_URL"; //$NON-NLS-1$

	public static final String ATTR_SERVER_NAME = "ATTR_SERVER_NAME"; //$NON-NLS-1$
	public static final String ATTR_SERVER_FILE_NAME = "ATTR_SERVER_FILE_NAME"; //$NON-NLS-1$
	public static final String ATTR_SERVER_BASE_URL = "ATTR_SERVER_BASE_URL"; //$NON-NLS-1$

	public static final String ATTR_HTTP_POST = "ATTR_HTTP_POST"; //$NON-NLS-1$
	public static final String ATTR_HTTP_GET = "ATTR_HTTP_GET"; //$NON-NLS-1$
}
