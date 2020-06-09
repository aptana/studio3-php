/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.zend.debugger.handlers;

import org2.eclipse.php.debug.core.debugger.IDebugHandler;
import org2.eclipse.php.debug.core.debugger.handlers.IDebugMessageHandler;
import org2.eclipse.php.debug.core.debugger.messages.IDebugMessage;
import org2.eclipse.php.internal.debug.core.zend.model.PHPDebugTarget;

public class ScriptEndedNotificationHandler implements IDebugMessageHandler {

	public void handle(IDebugMessage message, PHPDebugTarget debugTarget) {
		IDebugHandler debugHandler = debugTarget.getRemoteDebugger().getDebugHandler();
		debugHandler.handleScriptEnded();
	}
}
