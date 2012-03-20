/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
/**
 * 
 */
package org2.eclipse.php.internal.debug.core.zend.debugger;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.widgets.Shell;
import org2.eclipse.php.internal.debug.core.debugger.AbstractDebuggerConfiguration;
import org2.eclipse.php.internal.debug.core.launching.PHPExecutableLaunchDelegate;
import org2.eclipse.php.internal.debug.core.launching.PHPWebPageLaunchDelegate;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebugCorePreferenceNames;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * Zend's debugger configuration class.
 * 
 * @author Shalom Gibly
 *	@since PDT 1.0
 */
public class ZendDebuggerConfiguration extends AbstractDebuggerConfiguration {

	/**
	 * Constructs a new ZendDebuggerConfiguration.
	 */
	public ZendDebuggerConfiguration() {
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.IDebuggerConfiguration#openConfigurationDialog(org.eclipse.swt.widgets.Shell)
	 */
	public void openConfigurationDialog(Shell parentShell) {
		new ZendDebuggerConfigurationDialog(this, parentShell).open();
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.AbstractDebuggerConfiguration#getPort()
	 */
	public int getPort() {
		return preferences.getInt(PHPDebugCorePreferenceNames.ZEND_DEBUG_PORT, 10000);
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.AbstractDebuggerConfiguration#setPort(int)
	 */
	public void setPort(int port) {
		preferences.putInt(PHPDebugCorePreferenceNames.ZEND_DEBUG_PORT, port);
	}

	/* (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.IDebuggerConfiguration#getScriptLaunchDelegateClass()
	 */
	public String getScriptLaunchDelegateClass() {
		return PHPExecutableLaunchDelegate.class.getName();
	}

	/* (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.IDebuggerConfiguration#getWebLaunchDelegateClass()
	 */
	public String getWebLaunchDelegateClass() {
		return PHPWebPageLaunchDelegate.class.getName();
	}
	
	/* (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.AbstractDebuggerConfiguration#applyDefaults()
	 */
	public void applyDefaults() {
		IEclipsePreferences defaults = new DefaultScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID);
		
		setPort(defaults.getInt(PHPDebugCorePreferenceNames.ZEND_DEBUG_PORT, 10000));
		preferences.putBoolean(PHPDebugCorePreferenceNames.RUN_WITH_DEBUG_INFO, defaults.getBoolean(PHPDebugCorePreferenceNames.RUN_WITH_DEBUG_INFO, true));
		save();
	}
}
