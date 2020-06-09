/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core;

import org.eclipse.ui.IURIEditorInput;




/**
 * Constants for the PHP debugger.
 */
@SuppressWarnings("nls")
public interface IPHPDebugConstants {

	/**PHP Wep Page launch type ID*/
	public static String PHP_WEB_LAUNCH_TYPE_ID = "com.aptana.php.debug.core.launching.webPageLaunch";

	public static String URL_ENCODING = "UTF-8"; //$NON-NLS-1$

	public static final String ATTR_WORKING_DIRECTORY = "ATTR_WORKING_DIRECTORY"; //$NON-NLS-1$
	public static final String ATTR_EXECUTABLE_LOCATION = "ATTR_LOCATION"; //$NON-NLS-1$
	public static final String ATTR_INI_LOCATION = "ATTR_INI_LOCATION"; //$NON-NLS-1$
	public static final String ATTR_FILE_FULL_PATH = "ATTR_FILE_FULL_PATH"; //$NON-NLS-1$

	/**
	 * Unique identifier for the PHP debug model
	 */
	public static final String ID_PHP_DEBUG_CORE = "org2.eclipse.php.debug.core";

	public static final String PHP_Port = ID_PHP_DEBUG_CORE + ".PHP_Port";
	public static final String PHP_URL = ID_PHP_DEBUG_CORE + ".PHP_URL";
	public static final String PHP_Project = ID_PHP_DEBUG_CORE + ".PHP_Project";
	public static final String PHP_File = ID_PHP_DEBUG_CORE + ".PHP_File";
	public static final String PHP_Exe = ID_PHP_DEBUG_CORE + ".PHP_EXE";
	public static final String ConditionEnabled = ID_PHP_DEBUG_CORE + ".ConditionEnabled";
	public static final String Condition = ID_PHP_DEBUG_CORE + ".Condition";
	public static final String PHPProcessType = ID_PHP_DEBUG_CORE + ".launching.PHPProcess";
	public static final String RUN_WITH_DEBUG_INFO = ID_PHP_DEBUG_CORE + ".RunWithDebugInfo";
	public static final String OPEN_IN_BROWSER = ID_PHP_DEBUG_CORE + ".OpenInBrowser";
	public static final String USE_INTERNAL_BROWSER = ID_PHP_DEBUG_CORE + ".UseExternalBrowser";
	public static final String USE_SSH_TUNNEL = ID_PHP_DEBUG_CORE + ".UseSSHTunnel";
	public static final String SSH_TUNNEL_USER_NAME = ID_PHP_DEBUG_CORE + ".SSHTunnelUserName";
	public static final String SSH_TUNNEL_PASSWORD = ID_PHP_DEBUG_CORE + ".SSHTunnelPassword";
	public static final String SSH_TUNNEL_SECURE_PREF_NODE = "/com/aptana/ide/php.debug"; //$NON-NLS-1$

	public static final String STORAGE_TYPE = ID_PHP_DEBUG_CORE + ".Storage_Type";

	/** File from include path ({@link LocalFileStorageEditorInput}) */
	public static final String STORAGE_TYPE_INCLUDE = ID_PHP_DEBUG_CORE + ".Include";

	/** Remote file */
	public static final String STORAGE_TYPE_REMOTE = ID_PHP_DEBUG_CORE + ".Remote";

	/** External file ({@link IURIEditorInput}) */
	public static final String STORAGE_TYPE_EXTERNAL = ID_PHP_DEBUG_CORE + ".External";

	/** File resource for storage (if available) */
	public static final String STORAGE_FILE = ID_PHP_DEBUG_CORE + ".Storage_File";

	/** Project resource for storage (if available) */
	public static final String STORAGE_PROJECT = ID_PHP_DEBUG_CORE + ".Project";

	/** Include path base directory for storage (if available) */
	public static final String STORAGE_INC_BASEDIR = ID_PHP_DEBUG_CORE + ".Include_Path_Basedir";

	public static final String Default_Server_Name = "Default PHP Web Server";
	public static final String PHPEXELaunchType = "com.aptana.php.debug.core.launching.PHPExeLaunchConfigurationType";
	public static final String PHPServerLaunchType = "com.aptana.php.debug.core.launching.webPageLaunch";

	/**
	 * Status code indicating an unexpected internal error (value <code>150</code>).
	 */
	public static final int INTERNAL_ERROR = 150;

	/**
	 * Status code indicating an error while connecting to the debug server (valuse <code>200</code>), usually, as a result
	 * of a debug session that is initialized on a file that does not exist on the server side.
	 */
	public static final int DEBUG_CONNECTION_ERROR = 200;

	/**
	 * Debug parameters initializer preferences key
	 */
	public static final String PHP_DEBUG_PARAMETERS_INITIALIZER = ID_PHP_DEBUG_CORE + ".debugParametersInitializer";

	public static final String DEBUG_PER_PROJECT = ID_PHP_DEBUG_CORE + ".use-project-settings"; //$NON-NLS-1$
	public static final String DEBUG_QUALIFIER = ID_PHP_DEBUG_CORE + ".Debug_Process_Preferences"; //$NON-NLS-1$
	public static final String PREFERENCE_PAGE_ID = "org2.eclipse.php.debug.ui.preferences.PhpDebugPreferencePage";
	public static final String PROJECT_PAGE_ID = "org2.eclipse.php.debug.ui.property.PhpDebugPreferencePage"; //$NON-NLS-1$

	public static final String DEBUGGING_PAGES = "debugPages"; //$NON-NLS-1$
	public static final String DEBUGGING_ALL_PAGES = "debugAllPages"; //$NON-NLS-1$
	public static final String DEBUGGING_FIRST_PAGE = "debugFirstPage"; //$NON-NLS-1$
	public static final String DEBUGGING_START_FROM = "debugFrom"; //$NON-NLS-1$
	public static final String DEBUGGING_SHOULD_CONTINUE = "debugFromURL"; //$NON-NLS-1$
	public static final String DEBUGGING_START_FROM_URL = "debugContinue"; //$NON-NLS-1$

	public static final String PREF_STEP_FILTERS_LIST = ID_PHP_DEBUG_CORE + ".pref_step_filters_list"; //$NON-NLS-1$

	/**
	 * Secondary ID of breakpoint 
	 */
	public static final String SECONDARY_ID_KEY = "secondary_id"; //$NON-NLS-1$
}
