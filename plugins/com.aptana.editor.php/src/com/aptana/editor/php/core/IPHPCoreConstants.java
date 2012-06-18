package com.aptana.editor.php.core;

import org2.eclipse.php.internal.core.PHPVersion;

import com.aptana.editor.php.PHPEditorPlugin;

public interface IPHPCoreConstants
{
	public static final String PLUGIN_ID = PHPEditorPlugin.PLUGIN_ID;

	//
	// Project Option values
	//
	public static final String PHP4 = PHPVersion.PHP4.getAlias();
	public static final String PHP5 = PHPVersion.PHP5.getAlias();
	public static final String PHP53 = PHPVersion.PHP5_3.getAlias();

	public static final String ATTR_TOOL_ARGUMENTS = "ATTR_TOOL_ARGUMENTS"; //$NON-NLS-1$

	public static final String DEFAULT_INDENTATION_SIZE = "1"; //$NON-NLS-1$

	public static final String PHP_OPTIONS_PHP_VERSION = "phpVersion"; //$NON-NLS-1$
	public static final String PHP_OPTIONS_PHP_ROOT_CONTEXT = "phpRootContext"; //$NON-NLS-1$

	public static final String FORMATTER_USE_TABS = PLUGIN_ID + ".phpForamtterUseTabs"; //$NON-NLS-1$
	public static final String FORMATTER_INDENTATION_SIZE = PLUGIN_ID + ".phpForamtterIndentationSize"; //$NON-NLS-1$

	// workspace locale and default local preferences identifiers
	public final static String WORKSPACE_LOCALE = PLUGIN_ID + ".workspaceLocale"; //$NON-NLS-1$
	public final static String WORKSPACE_DEFAULT_LOCALE = PLUGIN_ID + ".workspaceDefaultLocale"; //$NON-NLS-1$

	public static final String RSE_TEMP_PROJECT_NATURE_ID = "org.eclipse.rse.ui.remoteSystemsTempNature"; //$NON-NLS-1$
}
