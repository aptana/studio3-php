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
package org2.eclipse.php.internal.debug.core.xdebug.dbgp;

import org.eclipse.swt.widgets.Shell;
import org2.eclipse.php.internal.debug.core.debugger.AbstractDebuggerConfiguration;
import org2.eclipse.php.internal.debug.core.launching.XDebugExeLaunchConfigurationDelegate;
import org2.eclipse.php.internal.debug.core.launching.XDebugWebLaunchConfigurationDelegate;
import org2.eclipse.php.internal.debug.core.xdebug.XDebugPreferenceMgr;

/**
 * XDebug's debugger configuration class.
 * 
 * @author Shalom Gibly
 *	@since PDT 1.0
 */
public class XDebugDebuggerConfiguration extends AbstractDebuggerConfiguration {

	/**
	 * Constructs a new XDebugDebuggerConfiguration.
	 */
	public XDebugDebuggerConfiguration() {
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.IDebuggerConfiguration#openConfigurationDialog(org.eclipse.swt.widgets.Shell)
	 */
	public void openConfigurationDialog(final Shell parentShell) {
		new XDebugConfigurationDialog(this, parentShell).open();
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.AbstractDebuggerConfiguration#getPort()
	 */
	public int getPort() {
		return XDebugPreferenceMgr.getPort();
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.AbstractDebuggerConfiguration#setPort(int)
	 */
	public void setPort(int port) {
		XDebugPreferenceMgr.setPort(port);
	}

	/* (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.IDebuggerConfiguration#getScriptLaunchDelegateClass()
	 */
	public String getScriptLaunchDelegateClass() {
		return XDebugExeLaunchConfigurationDelegate.class.getName();
	}

	/* (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.IDebuggerConfiguration#getWebLaunchDelegateClass()
	 */
	public String getWebLaunchDelegateClass() {
		return XDebugWebLaunchConfigurationDelegate.class.getName();
	}

	/* (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.AbstractDebuggerConfiguration#applyDefaults()
	 */
	public void applyDefaults() {
		XDebugPreferenceMgr.applyDefaults(preferences);
		save();
	}
}