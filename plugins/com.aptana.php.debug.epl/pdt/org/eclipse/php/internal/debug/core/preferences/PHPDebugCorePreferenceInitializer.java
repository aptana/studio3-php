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
package org.eclipse.php.internal.debug.core.preferences;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org.eclipse.php.internal.debug.core.launching.PHPExecutableLaunchDelegate;
import org.eclipse.php.internal.debug.core.xdebug.communication.XDebugCommunicationDaemon;

import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * Sets default values for PHP Debug preferences
 */
public class PHPDebugCorePreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		//		IEclipsePreferences node = new DefaultScope().getNode(Activator.getDefault().getBundle().getSymbolicName());
		IEclipsePreferences preferences = new DefaultScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID);
		// formatting preferences
		preferences.putBoolean(PHPDebugCorePreferenceNames.STOP_AT_FIRST_LINE, true);
		preferences.putBoolean(PHPDebugCorePreferenceNames.RUN_WITH_DEBUG_INFO, true);
		preferences.putBoolean(PHPDebugCorePreferenceNames.OPEN_IN_BROWSER, true);
		preferences.putBoolean(PHPDebugCorePreferenceNames.OPEN_DEBUG_VIEWS, true);
		preferences.putInt(PHPDebugCorePreferenceNames.ZEND_DEBUG_PORT, 10000);
		preferences.put(PHPDebugCorePreferenceNames.TRANSFER_ENCODING, "UTF-8"); //$NON-NLS-1$
		preferences.put(PHPDebugCorePreferenceNames.OUTPUT_ENCODING, "UTF-8"); //$NON-NLS-1$
		preferences.put(IPHPDebugCorePreferenceKeys.CONFIGURATION_DELEGATE_CLASS, PHPExecutableLaunchDelegate.class.getName());
		preferences.put(IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID, XDebugCommunicationDaemon.XDEBUG_DEBUGGER_ID); // The default is Zend's debugger
		preferences.put(IPHPDebugConstants.PHP_DEBUG_PARAMETERS_INITIALIZER, "org.eclipse.php.debug.core.defaultInitializer"); //$NON-NLS-1$
		preferences.put(IPHPDebugCorePreferenceKeys.NOTIFY_NON_STANDARD_PORT, MessageDialogWithToggle.ALWAYS);
		preferences.put(IPHPDebugCorePreferenceKeys.BREAK_ON_FIRST_LINE_FOR_UNKNOWN_JIT, MessageDialogWithToggle.PROMPT);

		try {
			StringBuilder b = new StringBuilder();
			Enumeration<NetworkInterface> ii = NetworkInterface.getNetworkInterfaces();
			while (ii.hasMoreElements()) {
				NetworkInterface i = ii.nextElement();
				Enumeration<InetAddress> aa = i.getInetAddresses();
				while (aa.hasMoreElements()) {
					InetAddress a = aa.nextElement();
					if (a instanceof Inet4Address && !a.isLoopbackAddress()) {
						b.append(a.getHostAddress()).append(","); //$NON-NLS-1$
					}
				}
			}
			b.append("127.0.0.1"); //$NON-NLS-1$
			preferences.put(PHPDebugCorePreferenceNames.CLIENT_IP, b.toString());
		} catch (Exception e) {
		}
	}
}
