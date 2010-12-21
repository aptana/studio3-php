/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.php.internal.debug.core.xdebug;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.php.internal.debug.core.PHPDebugCoreMessages;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpPreferences;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpProxyHandler;
import org.osgi.service.prefs.BackingStoreException;

import com.aptana.debug.php.core.preferences.PHPDebugPreferencesUtil;
import com.aptana.debug.php.epl.PHPDebugEPLPlugin;
@SuppressWarnings("nls")
public class XDebugPreferenceMgr {
	
	// general
	public static final String XDEBUG_PREF_PORT = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_port";
	public static final String XDEBUG_PREF_SHOWSUPERGLOBALS = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_showSuperGlobals";
	public static final String XDEBUG_PREF_ARRAYDEPTH = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_arrayDepth";
	public static final String XDEBUG_PREF_CHILDREN = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_children";
	public static final String XDEBUG_PREF_MULTISESSION = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_multisession";
	public static final String XDEBUG_PREF_REMOTESESSION = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_remotesession";
	//capture output
	public static final String XDEBUG_PREF_CAPTURESTDOUT = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_capturestdout";
	public static final String XDEBUG_PREF_CAPTURESTDERR = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_capturestderr";
	//proxy
	public static final String XDEBUG_PREF_USEPROXY = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_useproxy";
	public static final String XDEBUG_PREF_IDEKEY = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_idekey";
	public static final String XDEBUG_PREF_PROXY = PHPDebugEPLPlugin.PLUGIN_ID + ".xdebug_proxy";

	public static enum AcceptRemoteSession {
		off, localhost, any, prompt
	}
	
	public static final String[] remoteSessionOptions = {PHPDebugCoreMessages.XDebugConfigurationDialog_remoteSessionOption_off, 
		PHPDebugCoreMessages.XDebugConfigurationDialog_remoteSessionOption_localhost, 
		PHPDebugCoreMessages.XDebugConfigurationDialog_remoteSessionOption_any,
		PHPDebugCoreMessages.XDebugConfigurationDialog_remoteSessionOption_prompt};		

	
	
	//just happens to match DBGp values so no conversion to DBGp Preferences required.
	public static enum CaptureOutput {
		off, copy, redirect
	}
	
	public static final String[] captureOutputOptions =  {PHPDebugCoreMessages.XDebugConfigurationDialog_capture_off,
		PHPDebugCoreMessages.XDebugConfigurationDialog_capture_copy, 
		PHPDebugCoreMessages.XDebugConfigurationDialog_capture_redirect};
	

	public static void setDefaults() {
		IEclipsePreferences prefs = new DefaultScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID);
		prefs.putInt(XDebugPreferenceMgr.XDEBUG_PREF_PORT, getPortDefault());
		prefs.putBoolean(XDebugPreferenceMgr.XDEBUG_PREF_SHOWSUPERGLOBALS, showSuperGlobalsDefault());
		prefs.putInt(XDebugPreferenceMgr.XDEBUG_PREF_ARRAYDEPTH, getDepthDefault());
		prefs.putBoolean(XDebugPreferenceMgr.XDEBUG_PREF_MULTISESSION, useMultiSessionDefault());	
		prefs.putInt(XDebugPreferenceMgr.XDEBUG_PREF_CHILDREN, getChildrenDefault());
		prefs.putInt(XDebugPreferenceMgr.XDEBUG_PREF_REMOTESESSION, getAcceptRemoteSessionDefault());
		
		prefs.putInt(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDOUT, getCaptureDefault());
		prefs.putInt(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDERR, getCaptureDefault());

		//Proxy config doesn't need its default values set here.
	}
	
	public static void applyDefaults(IEclipsePreferences preferences) {
		IPreferencesService service = Platform.getPreferencesService();
		IScopeContext[] defaultContext = new IScopeContext[] { new DefaultScope() };
		
		preferences.putInt(XDebugPreferenceMgr.XDEBUG_PREF_PORT, service.getInt(PHPDebugEPLPlugin.PLUGIN_ID,
				XDebugPreferenceMgr.XDEBUG_PREF_PORT, getPortDefault(), defaultContext));
		preferences.putBoolean(XDebugPreferenceMgr.XDEBUG_PREF_SHOWSUPERGLOBALS, service.getBoolean(
				PHPDebugEPLPlugin.PLUGIN_ID, XDebugPreferenceMgr.XDEBUG_PREF_SHOWSUPERGLOBALS,
				showSuperGlobalsDefault(), defaultContext));
		preferences.putInt(XDebugPreferenceMgr.XDEBUG_PREF_ARRAYDEPTH, service.getInt(PHPDebugEPLPlugin.PLUGIN_ID,
				XDebugPreferenceMgr.XDEBUG_PREF_ARRAYDEPTH, getDepthDefault(), defaultContext));
		preferences.putInt(XDebugPreferenceMgr.XDEBUG_PREF_CHILDREN, service.getInt(PHPDebugEPLPlugin.PLUGIN_ID,
				XDebugPreferenceMgr.XDEBUG_PREF_CHILDREN, getChildrenDefault(), defaultContext));
		preferences.putBoolean(XDebugPreferenceMgr.XDEBUG_PREF_MULTISESSION, service.getBoolean(
				PHPDebugEPLPlugin.PLUGIN_ID, XDebugPreferenceMgr.XDEBUG_PREF_MULTISESSION, useMultiSessionDefault(),
				defaultContext));
		preferences.putInt(XDebugPreferenceMgr.XDEBUG_PREF_REMOTESESSION, service.getInt(PHPDebugEPLPlugin.PLUGIN_ID,
				XDebugPreferenceMgr.XDEBUG_PREF_REMOTESESSION, getAcceptRemoteSessionDefault(), defaultContext));

		preferences.putInt(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDOUT, service.getInt(PHPDebugEPLPlugin.PLUGIN_ID,
				XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDOUT, getCaptureDefault(), defaultContext));
		preferences.putInt(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDERR, service.getInt(PHPDebugEPLPlugin.PLUGIN_ID,
				XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDERR, getCaptureDefault(), defaultContext));

		preferences.putBoolean(XDebugPreferenceMgr.XDEBUG_PREF_USEPROXY, service.getBoolean(
				PHPDebugEPLPlugin.PLUGIN_ID, XDebugPreferenceMgr.XDEBUG_PREF_USEPROXY, false, defaultContext));
		preferences.put(XDebugPreferenceMgr.XDEBUG_PREF_IDEKEY, service.getString(PHPDebugEPLPlugin.PLUGIN_ID,
				XDebugPreferenceMgr.XDEBUG_PREF_IDEKEY, DBGpProxyHandler.instance.generateIDEKey(), defaultContext));
		preferences.putBoolean(XDebugPreferenceMgr.XDEBUG_PREF_PROXY, service.getBoolean(PHPDebugEPLPlugin.PLUGIN_ID,
				XDebugPreferenceMgr.XDEBUG_PREF_PROXY, false, defaultContext));
	}
	
	/**
	 * create the DBGp preferences from the UI preferences.
	 * <ul>
	 * <li>Array Depth
	 * <li>Children
	 * <li>show super globals
	 * @return
	 */
	public static DBGpPreferences createSessionPreferences() {
		DBGpPreferences sessionPrefs = new DBGpPreferences();
		int maxDepth = PHPDebugPreferencesUtil.getInt(XDebugPreferenceMgr.XDEBUG_PREF_ARRAYDEPTH, getDepthDefault());
		if (1 == maxDepth) {
			XDebugPreferenceMgr.setDefaults();
			maxDepth = XDebugPreferenceMgr.getDepthDefault();
		}
		sessionPrefs.setValue(DBGpPreferences.DBGP_MAX_DEPTH_PROPERTY, maxDepth);

		int maxChildren = PHPDebugPreferencesUtil.getInt(XDebugPreferenceMgr.XDEBUG_PREF_CHILDREN, getChildrenDefault());		
		sessionPrefs.setValue(DBGpPreferences.DBGP_MAX_CHILDREN_PROPERTY, maxChildren);
		
		boolean getSuperGlobals = PHPDebugPreferencesUtil.getBoolean(XDebugPreferenceMgr.XDEBUG_PREF_SHOWSUPERGLOBALS, showSuperGlobalsDefault());
		sessionPrefs.setValue(DBGpPreferences.DBGP_SHOW_GLOBALS_PROPERTY, getSuperGlobals);
		
		//ui stored values are identical to DBGp expected values so no need to convert
		int captureStdout = PHPDebugPreferencesUtil.getInt(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDOUT, getCaptureDefault());
		sessionPrefs.setValue(DBGpPreferences.DBGP_CAPTURE_STDOUT_PROPERTY, captureStdout);
		
		int captureStderr = PHPDebugPreferencesUtil.getInt(XDebugPreferenceMgr.XDEBUG_PREF_CAPTURESTDERR, getCaptureDefault());		
		sessionPrefs.setValue(DBGpPreferences.DBGP_CAPTURE_STDERR_PROPERTY, captureStderr);
		
		return sessionPrefs;
		
		
	}
	
	// provide easy access to the preferences which are not DBGp Session preferences.
	public static int getPort() {
		return PHPDebugPreferencesUtil.getInt(XDebugPreferenceMgr.XDEBUG_PREF_PORT, getDepthDefault());
	}

	public static void setPort(int port) {
		IEclipsePreferences prefs = new InstanceScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID);
		prefs.putInt(XDebugPreferenceMgr.XDEBUG_PREF_PORT, port);
		try
		{
			prefs.flush();
		}
		catch (BackingStoreException e)
		{
			PHPDebugEPLPlugin.logError(e);
		}
	}	
	
	public static boolean useMultiSession() {
		return PHPDebugPreferencesUtil.getBoolean(XDebugPreferenceMgr.XDEBUG_PREF_MULTISESSION, useMultiSessionDefault());
	}
	
	public static boolean useProxy() {
		return PHPDebugPreferencesUtil.getBoolean(XDebugPreferenceMgr.XDEBUG_PREF_USEPROXY, false);
	}
	
	public static void setUseProxy(boolean newState) {
		IEclipsePreferences prefs = new InstanceScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID);
		prefs.putBoolean(XDebugPreferenceMgr.XDEBUG_PREF_USEPROXY, newState);
		try
		{
			prefs.flush();
		}
		catch (BackingStoreException e)
		{
			PHPDebugEPLPlugin.logError(e);
		}
	}
	
	public static AcceptRemoteSession getAcceptRemoteSession() {
		int rSess = PHPDebugPreferencesUtil.getInt(XDebugPreferenceMgr.XDEBUG_PREF_REMOTESESSION,
				getAcceptRemoteSessionDefault());
		return AcceptRemoteSession.values()[rSess];
	}
	
	//the defaults for the UI preferences
	private static int getDepthDefault() {
		return DBGpPreferences.DBGP_MAX_DEPTH_DEFAULT;
	}
	
	private static int getChildrenDefault() {
		return DBGpPreferences.DBGP_MAX_CHILDREN_DEFAULT;
	}

	private static int getPortDefault() {
		return DBGpPreferences.DBGP_PORT_DEFAULT;
	}

	private static boolean showSuperGlobalsDefault() {
		return DBGpPreferences.DBGP_SHOW_GLOBALS_DEFAULT;
	}
	
	private static int getCaptureDefault() {
		// we use the UI definition here as it would be mapped
		// if required to the appropriate DBGp Value.
		return CaptureOutput.copy.ordinal();
	}
	
	
	private static boolean useMultiSessionDefault() {
		// this is not a DBGp property.
		return false;
	}
	
	private static int getAcceptRemoteSessionDefault() {
		// this is not a DBGp property
		return AcceptRemoteSession.localhost.ordinal();
	}
	


}
