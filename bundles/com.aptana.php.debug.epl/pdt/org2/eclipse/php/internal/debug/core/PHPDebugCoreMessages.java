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

import org.eclipse.osgi.util.NLS;

/**
 * Strings used by PHP Debugger Core
 *
 */
public class PHPDebugCoreMessages extends NLS {
    private static final String BUNDLE_NAME = "org2.eclipse.php.internal.debug.core.PHPDebugCoreMessages";//$NON-NLS-1$

    public static String LineBreakPointMessage_1;
    public static String ConditionalBreakPointMessage_1;
    public static String ConditionalBreakPointMessage_2;
    public static String DebuggerFileNotFound_1;
    public static String DebuggerDebugPortInUse_1;
    public static String DebuggerConnection_Problem_1;
    public static String DebuggerConnection_Problem_2;
    public static String DebuggerConnection_Problem_3;
    public static String DebuggerConnection_Failed_1;
    public static String Debugger_Unexpected_Error_1;
    public static String Debugger_ResourceNotFound;
    public static String Debugger_LaunchError_title;
    public static String Debugger_InvalidDebugResource;
    public static String Debugger_General_Error;
    public static String Debugger_Launch_Error;
    public static String Debugger_Error_Message;
    public static String Debugger_Error_Message_2;
    public static String Debugger_Error_Message_3;
    public static String Debugger_Error_Crash_Message;
    public static String Debugger_Incompatible_Protocol;
    public static String Port_Error_Message_Message;
    public static String Port_Error_Message_Title;

	public static String PHPLaunchUtilities_activeLaunchDetected;
	public static String PHPLaunchUtilities_confirmation;
	public static String PHPLaunchUtilities_multipleLaunchesPrompt;
	public static String PHPLaunchUtilities_phpLaunchTitle;
	public static String PHPLaunchUtilities_rememberDecision;
	public static String PHPLaunchUtilities_PHPPerspectiveSwitchTitle;
	public static String PHPLaunchUtilities_PHPPerspectiveSwitchMessage;
	public static String PHPLaunchUtilities_terminate;
	public static String PHPLaunchUtilities_waitingForDebugger;
	public static String PHPLaunchUtilities_nonStandardPort;
	public static String PHPLaunchUtilities_xdebugNonStandardPort;
	public static String PHPLaunchUtilities_zendDebugNonStandardPort;
	public static String PHPLaunchUtilities_portSettingsPreferencesPage;
	public static String PHPLaunchUtilities_doNotShowThisAgain;

	public static String PHPWebPageLaunchDelegate_serverNotFound;

	public static String DebuggerConfigurationDialog_debugPort;
	public static String DebuggerConfigurationDialog_invalidPort;
	public static String DebuggerConfigurationDialog_invalidPortRange;
	public static String DebuggerConfigurationDialog_portInUse;

	public static String ZendDebuggerConfigurationDialog_runWithDebugInfo;
	public static String ZendDebuggerConfigurationDialog_zendDebugger;
	public static String ZendDebuggerConfigurationDialog_zendDebuggerSettings;

	//title and groups
	public static String XDebugConfigurationDialog_mainTitle;
	public static String XDebugConfigurationDialog_generalGroup;
	public static String XDebugConfigurationDialog_captureGroup;
	public static String XDebugConfigurationDialog_proxyGroup;

	//general
	public static String XDebugConfigurationDialog_invalidTimeout;
	public static String XDebugConfigurationDialog_invalidTimeoutValue;
	public static String XDebugConfigurationDialog_maxArrayDepth;
	public static String XDebugConfigurationDialog_maxChildren;
	public static String XDebugConfigurationDialog_showSuperGlobals;
	public static String XDebugConfigurationDialog_invalidPortRange;
	public static String XDebugConfigurationDialog_useMultisession;
	public static String XDebugConfigurationDialog_remoteSession;
	public static String XDebugConfigurationDialog_remoteSessionOption_off;
	public static String XDebugConfigurationDialog_remoteSessionOption_localhost;
	public static String XDebugConfigurationDialog_remoteSessionOption_any;
	public static String XDebugConfigurationDialog_remoteSessionOption_prompt;

	//capture output
	public static String XDebugConfigurationDialog_captureStdout;
	public static String XDebugConfigurationDialog_captureStderr;
	public static String XDebugConfigurationDialog_capture_off;
	public static String XDebugConfigurationDialog_capture_copy;
	public static String XDebugConfigurationDialog_capture_redirect;

	//proxy
	public static String XDebugConfigurationDialog_useProxy;
	public static String XDebugConfigurationDialog_idekey;
	public static String XDebugConfigurationDialog_proxy;

	//General XDebug messages

	//DBGpTarget messages
	public static String XDebugMessage_debugError;
	public static String XDebugMessage_unexpectedTermination;

	//XDebug Communication Demon msgs
	public static String XDebugMessage_remoteSessionTitle;
	public static String XDebugMessage_remoteSessionPrompt;

	//proxy Handler
	public static String XDebug_DBGpProxyHandler_0;
	public static String XDebug_DBGpProxyHandler_1;
	public static String XDebug_DBGpProxyHandler_2;
	public static String XDebug_DBGpProxyHandler_3;

	public static String XDebug_ExeLaunchConfigurationDelegate_0;
	public static String XDebug_ExeLaunchConfigurationDelegate_1;
	public static String XDebug_ExeLaunchConfigurationDelegate_2;
	public static String XDebug_ExeLaunchConfigurationDelegate_3;
	public static String XDebug_ExeLaunchConfigurationDelegate_4;

	public static String XDebug_WebLaunchConfigurationDelegate_0;
	public static String XDebug_WebLaunchConfigurationDelegate_1;
	public static String XDebug_WebLaunchConfigurationDelegate_2;
	public static String XDebug_WebLaunchConfigurationDelegate_3;
	public static String XDebug_WebLaunchConfigurationDelegate_4;

	public static String XDebug_DBGpTarget_0;
	public static String XDebug_DBGpTarget_1;
	public static String XDebug_DBGpTarget_2;
	public static String XDebug_DBGpContainerValue_0;
	public static String XDebug_DBGpMultiSessionTarget_0;
	public static String XDebug_DBGpStackFrame_0;
	public static String XDebug_DBGpStringValue_0;
	public static String XDebug_DBGpThread_0;
	public static String XDebug_DBGpVariable_0;
	public static String XDebug_DBGpVariable_1;
	public static String XDebug_IDBGpModelConstants_0;
	public static String XDebug_IDBGpModelConstants_1;

	public static String Debugger_incomingDebuggerJitRequestTitle;
	public static String Debugger_incomingDebuggerJitRequest;

    static {
        // load message values from bundle file
        NLS.initializeMessages(BUNDLE_NAME, PHPDebugCoreMessages.class);
    }

    private PHPDebugCoreMessages() {
        // cannot create new instance
    }
}
