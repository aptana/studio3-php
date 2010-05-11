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
package com.aptana.editor.php.internal.contentAssist.preferences;

/**
 * IPreferenceConstants
 * 
 * @author Denis Denisenko
 */
public interface IContentAssistPreferencesConstants
{
	/**
	 * Filter by key.
	 * 
	 * @see #CONTENT_ASSIST_EXPLICIT_INCLUDE
	 * @see #CONTENT_ASSIST_INCLUDE_ALL
	 */
	String CONTENT_ASSIST_FILTER_TYPE = "com.aptana.ide.editor.php.contentassist.CONTENT_ASSIST_FILTER_TYPE"; //$NON-NLS-1$

	/**
	 * Filter by looking at the explicit includes.
	 * 
	 * @see CONTENT_ASSIST_FILTER_BY_INCLUDES
	 */
	String CONTENT_ASSIST_EXPLICIT_INCLUDE = "com.aptana.ide.editor.php.contentassist.CONTENT_ASSIST_EXPLICIT_INCLUDE"; //$NON-NLS-1$

	/**
	 * Include everything in the content assist (e.g. Do not filter)
	 * 
	 * @see CONTENT_ASSIST_FILTER_BY_INCLUDES
	 */
	String CONTENT_ASSIST_INCLUDE_ALL = "com.aptana.ide.editor.php.contentassist.CONTENT_ASSIST_INCLUDE_ALL"; //$NON-NLS-1$

	/**
	 * Auto activate in identifiers.
	 */
	String AUTO_ACTIVATE_ON_IDENTIFIERS = "com.aptana.ide.editor.php.contentassist.AUTO_ACTIVATE_ON_IDENTIFIERS"; //$NON-NLS-1$

	/**
	 * Parse unsaved module on identifiers completion.
	 */
	String PARSE_UNSAVED_MODULE_ON_IDENTIFIERS_COMPLETION = "com.aptana.ide.editor.php.contentassist.PARSE_UNSAVED_MODULE_ON_IDENTIFIERS_COMPLETION"; //$NON-NLS-1$

	/**
	 * Whether to insert parentheses after method calls during auto-completion.
	 */
	String INSERT_PARENTHESES_AFTER_METHOD_CALLS = "com.aptana.ide.editor.php.contentassist.INSERT_PARENTHESES_AFTER_METHOD_CALLS"; //$NON-NLS-1$

	/**
	 * Whether to insert parentheses after new instance creation statement during auto-completion.
	 */
	String INSERT_PARENTHESES_AFTER_NEW_INSTANCE = "com.aptana.ide.editor.php.contentassist.INSERT_PARENTHESES_AFTER_NEW_INSTANCE"; //$NON-NLS-1$

	/**
	 * Insert semicolon after method calls during auto-completion.
	 */
	String INSERT_SEMICOLON_AFTER_METHOD_CALLS = "com.aptana.ide.editor.php.contentassist.INSERT_SEMICOLON_AFTER_METHOD_CALLS"; //$NON-NLS-1$

	/**
	 * Insert semicolon after new instance creation during auto-completion.
	 */
	String INSERT_SEMICOLON_AFTER_NEW_INSTANCE = "com.aptana.ide.editor.php.contentassist.INSERT_SEMICOLON_AFTER_NEW_INSTANCE"; //$NON-NLS-1$

	/**
	 * Insert function parameters.
	 */
	String INSERT_FUNCTION_PARAMETERS = "com.aptana.ide.editor.php.contentassist.INSERT_FUNCTION_PARAMETERS"; //$NON-NLS-1$

	/**
	 * Insert 'optional' function parameters.
	 */
	String INSERT_OPTIONAL_FUNCTION_PARAMETERS = "com.aptana.ide.editor.php.contentassist.INSERT_OPTIONAL_FUNCTION_PARAMETERS"; //$NON-NLS-1$

	/**
	 * Whether tab-jump is enabled for function parameters.
	 */
	String PARAMETRS_TAB_JUMP = "com.aptana.ide.editor.php.contentassist.PARAMETRS_TAB_JUMP"; //$NON-NLS-1$

	/**
	 * Insert mode. Can be either "insert" or "overwrite".
	 */
	String INSERT_MODE = "com.aptana.ide.editor.php.contentassist.INSERT_MODE"; //$NON-NLS-1$

	/**
	 * "insert" mode.
	 */
	String INSERT_MODE_INSERT = "insert"; //$NON-NLS-1$

	/**
	 * "overwrite" mode.
	 */
	String INSERT_MODE_OVERWRITE = "overwrite"; //$NON-NLS-1$

	/**
	 * Whether to link type hierarchy to the editor.
	 */
	String LINK_TYPEHIERARCHY_TO_EDITOR = "LINK_TYPEHIERARCHY_TO_EDITOR"; //$NON-NLS-1$
}
