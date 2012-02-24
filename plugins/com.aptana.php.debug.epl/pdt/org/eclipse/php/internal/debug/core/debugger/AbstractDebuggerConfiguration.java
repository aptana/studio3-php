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
package org.eclipse.php.internal.debug.core.debugger;

import java.util.HashMap;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import com.aptana.core.util.StringUtil;
import com.aptana.php.debug.core.daemon.ICommunicationDaemon;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * An abstract implementation of the IDebuggerConfiguration.
 * 
 * @author Shalom Gibly
 * @since PDT 1.0
 */
public abstract class AbstractDebuggerConfiguration implements IDebuggerConfiguration {

	protected IEclipsePreferences preferences;
	private HashMap<String, String> attributes;
	private ICommunicationDaemon communicationDaemon;

	/**
	 * AbstractDebuggerConfiguration constructor.
	 */
	public AbstractDebuggerConfiguration() {
		preferences = PHPDebugEPLPlugin.getInstancePreferences();
		attributes = new HashMap<String, String>();
	}

	/**
	 * Sets an attribute for this debugger.
	 * 
	 * @param id The ID.
	 * @param value The value.
	 * @see #save()
	 * @see #getAttribute(String)
	 */
	public void setAttribute(String id, String value)
	{
		String defaultValue = new DefaultScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID).get(id, StringUtil.EMPTY);
		if (StringUtil.EMPTY.equals(defaultValue))
		{
			attributes.put(id, value);
		}
		else
		{
			preferences.put(id, value);
			try
			{
				preferences.flush();
			}
			catch (BackingStoreException e)
			{
				PHPDebugEPLPlugin.logError(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.php.internal.debug.core.debugger.IDebuggerConfiguration#getAttribute(java.lang.String)
	 */
	public String getAttribute(String id) {
		String attribute = attributes.get(id);
		if (attribute == null) {
			attribute = preferences.get(id, StringUtil.EMPTY);
		}
		return attribute;
	}

	/**
	 * Sets the debugger's id.
	 * 
	 * @param id
	 */
	public void setDebuggerId(String id) {
		attributes.put(DEBUGGER_ID, id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.php.internal.debug.core.debugger.IDebuggerConfiguration#getDebuggerId()
	 */
	public String getDebuggerId() {
		return getAttribute(DEBUGGER_ID);
	}

	/**
	 * Sets the debugger's name.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		attributes.put(DEBUGGER_NAME, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.php.internal.debug.core.debugger.IDebuggerConfiguration#getName()
	 */
	public String getName() {
		return getAttribute(DEBUGGER_NAME);
	}

	/**
	 * Sets the debugger's port.
	 * 
	 * @param port
	 */
	public abstract void setPort(int port);

	/**
	 * Returns the debugger's port number.
	 * 
	 * @return The debugger's port number. -1, if none is defined.
	 */
	public abstract int getPort();

	/**
	 * Returns the {@link ICommunicationDaemon} that is related to this debugger configuration.
	 * 
	 * @return the communicationDaemon (can be null)
	 */
	public ICommunicationDaemon getCommunicationDaemon() {
		return communicationDaemon;
	}

	/**
	 * Sets the {@link ICommunicationDaemon} that is related to this debugger configuration.
	 * 
	 * @param communicationDaemon the communicationDaemon to set
	 */
	public void setCommunicationDaemon(ICommunicationDaemon communicationDaemon) {
		this.communicationDaemon = communicationDaemon;
	}

	/**
	 * Save any plug-in preferences that needs to be saved. 
	 */
	public void save() {
		PHPDebugEPLPlugin.getDefault().savePluginPreferences();
	}
	
	/**
	 * Apply the default values for this debugger configuration and save them.
	 * Note that the changes affecting the PDT immediately.
	 */
	public abstract void applyDefaults();
}
