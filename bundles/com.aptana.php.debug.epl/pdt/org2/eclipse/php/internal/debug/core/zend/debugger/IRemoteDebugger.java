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
package org2.eclipse.php.internal.debug.core.zend.debugger;

import org2.eclipse.php.debug.core.debugger.IDebugHandler;
import org2.eclipse.php.debug.core.debugger.messages.IDebugRequestMessage;
import org2.eclipse.php.debug.core.debugger.messages.IDebugResponseMessage;
import org2.eclipse.php.internal.debug.core.zend.communication.CommunicationAdministrator;
import org2.eclipse.php.internal.debug.core.zend.communication.CommunicationClient;
import org2.eclipse.php.internal.debug.core.zend.communication.DebugConnectionThread;

/**
 * @author michael
 *
 */
public interface IRemoteDebugger extends Debugger, CommunicationClient, CommunicationAdministrator, IDebugFeatures {

	public DebugConnectionThread getConnectionThread();

	public IDebugHandler getDebugHandler ();

	public boolean go(GoResponseHandler responseHandler);

	public boolean isActive();

    public void closeConnection();

	public void closeDebugSession();

	public boolean stepOver(StepOverResponseHandler responseHandler);

	public boolean stepInto(StepIntoResponseHandler responseHandler);

	public IDebugResponseMessage sendCustomRequest (IDebugRequestMessage request);

	/**
	 * @return current protocol ID that is used in this debug session
	 */
	public int getCurrentProtocolID();

}