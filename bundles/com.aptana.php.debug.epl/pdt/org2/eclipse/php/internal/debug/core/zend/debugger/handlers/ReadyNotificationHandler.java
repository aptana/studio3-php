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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org2.eclipse.php.debug.core.debugger.IDebugHandler;
import org2.eclipse.php.debug.core.debugger.handlers.IDebugMessageHandler;
import org2.eclipse.php.debug.core.debugger.messages.IDebugMessage;
import org2.eclipse.php.internal.core.util.FileUtils;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.model.PHPConditionalBreakpoint;
import org2.eclipse.php.internal.debug.core.preferences.stepFilters.DebugStepFilterController;
import org2.eclipse.php.internal.debug.core.zend.debugger.RemoteDebugger;
import org2.eclipse.php.internal.debug.core.zend.debugger.messages.ReadyNotification;
import org2.eclipse.php.internal.debug.core.zend.model.PHPDebugTarget;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

public class ReadyNotificationHandler implements IDebugMessageHandler {

	public void handle(IDebugMessage message, PHPDebugTarget debugTarget) {
		ReadyNotification readyNotification = (ReadyNotification) message;

		String currentFile = readyNotification.getFileName();
		try {
			if (debugTarget.isStepFiltersEnabled()) {
				String localPath = ((RemoteDebugger) debugTarget.getRemoteDebugger()).convertToLocalFilename(currentFile);
				if (DebugStepFilterController.getInstance().isFiltered(localPath)) {// file is filtered
					try {
						if (!isBreakPointExistInFile(debugTarget, currentFile)) {//file has a B.P
							// skip this step and continue the next 'Step Into'
							debugTarget.getRemoteDebugger().stepInto();
							return;
						}
					} catch (CoreException ce) {
						PHPDebugEPLPlugin.logError(ce);
					}
				}
			}
		} finally {
			int currentLine = readyNotification.getLineNumber();
			IDebugHandler debugHandler = debugTarget.getRemoteDebugger().getDebugHandler();
			debugHandler.ready(currentFile, currentLine);
		}
	}

	// check if the currentFile has a breakpoint
	private boolean isBreakPointExistInFile(PHPDebugTarget debugTarget, String currentFile) throws CoreException {
		IBreakpoint[] bPoints = debugTarget.getBreakpointManager().getBreakpoints(IPHPDebugConstants.ID_PHP_DEBUG_CORE);
		for (IBreakpoint bp : bPoints) {
			if (bp.isEnabled() && FileUtils.checkIfEqualFilePaths(((PHPConditionalBreakpoint) bp).getRuntimeBreakpoint().getFileName(), currentFile)) {
				return true;
			}
		}
		return false;
	}
}
