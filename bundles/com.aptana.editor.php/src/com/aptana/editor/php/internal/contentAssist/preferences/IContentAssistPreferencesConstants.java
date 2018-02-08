/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.contentAssist.preferences;

/**
 * IPhpPreferenceConstants
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
	String CONTENT_ASSIST_FILTER_TYPE = "com.aptana.editor.php.contentassist.CONTENT_ASSIST_FILTER_TYPE"; //$NON-NLS-1$

	/**
	 * Filter by looking at the explicit includes.
	 * 
	 * @see CONTENT_ASSIST_FILTER_BY_INCLUDES
	 */
	String CONTENT_ASSIST_EXPLICIT_INCLUDE = "com.aptana.editor.php.contentassist.CONTENT_ASSIST_EXPLICIT_INCLUDE"; //$NON-NLS-1$

	/**
	 * Include everything in the content assist (e.g. Do not filter)
	 * 
	 * @see CONTENT_ASSIST_FILTER_BY_INCLUDES
	 */
	String CONTENT_ASSIST_INCLUDE_ALL = "com.aptana.editor.php.contentassist.CONTENT_ASSIST_INCLUDE_ALL"; //$NON-NLS-1$

	/**
	 * Auto activate in identifiers.
	 */
	String AUTO_ACTIVATE_ON_IDENTIFIERS = "com.aptana.editor.php.contentassist.AUTO_ACTIVATE_ON_IDENTIFIERS"; //$NON-NLS-1$

	/**
	 * Parse unsaved module on identifiers completion.
	 */
	String PARSE_UNSAVED_MODULE_ON_IDENTIFIERS_COMPLETION = "com.aptana.editor.php.contentassist.PARSE_UNSAVED_MODULE_ON_IDENTIFIERS_COMPLETION"; //$NON-NLS-1$

	/**
	 * Whether to insert parentheses after method calls during auto-completion.
	 */
	String INSERT_PARENTHESES_AFTER_METHOD_CALLS = "com.aptana.editor.php.contentassist.INSERT_PARENTHESES_AFTER_METHOD_CALLS"; //$NON-NLS-1$

	/**
	 * Whether to insert parentheses after new instance creation statement during auto-completion.
	 */
	String INSERT_PARENTHESES_AFTER_NEW_INSTANCE = "com.aptana.editor.php.contentassist.INSERT_PARENTHESES_AFTER_NEW_INSTANCE"; //$NON-NLS-1$

	/**
	 * Insert semicolon after method calls during auto-completion.
	 */
	String INSERT_SEMICOLON_AFTER_METHOD_CALLS = "com.aptana.editor.php.contentassist.INSERT_SEMICOLON_AFTER_METHOD_CALLS"; //$NON-NLS-1$

	/**
	 * Insert semicolon after new instance creation during auto-completion.
	 */
	String INSERT_SEMICOLON_AFTER_NEW_INSTANCE = "com.aptana.editor.php.contentassist.INSERT_SEMICOLON_AFTER_NEW_INSTANCE"; //$NON-NLS-1$

	/**
	 * Insert function parameters.
	 */
	String INSERT_FUNCTION_PARAMETERS = "com.aptana.editor.php.contentassist.INSERT_FUNCTION_PARAMETERS"; //$NON-NLS-1$

	/**
	 * Insert 'optional' function parameters.
	 */
	String INSERT_OPTIONAL_FUNCTION_PARAMETERS = "com.aptana.editor.php.contentassist.INSERT_OPTIONAL_FUNCTION_PARAMETERS"; //$NON-NLS-1$

	/**
	 * Whether tab-jump is enabled for function parameters.
	 */
	String PARAMETRS_TAB_JUMP = "com.aptana.editor.php.contentassist.PARAMETRS_TAB_JUMP"; //$NON-NLS-1$

	/**
	 * Insert mode. Can be either "insert" or "overwrite".
	 */
	String INSERT_MODE = "com.aptana.editor.php.contentassist.INSERT_MODE"; //$NON-NLS-1$

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
