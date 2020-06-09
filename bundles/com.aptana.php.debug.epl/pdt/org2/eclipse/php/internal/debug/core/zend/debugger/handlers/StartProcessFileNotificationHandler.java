/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.zend.debugger.handlers;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org2.eclipse.php.debug.core.debugger.handlers.IDebugMessageHandler;
import org2.eclipse.php.debug.core.debugger.messages.IDebugMessage;
import org2.eclipse.php.debug.core.debugger.parameters.IDebugParametersKeys;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.model.PHPConditionalBreakpoint;
import org2.eclipse.php.internal.debug.core.pathmapper.PathEntry;
import org2.eclipse.php.internal.debug.core.pathmapper.VirtualPath;
import org2.eclipse.php.internal.debug.core.zend.debugger.Breakpoint;
import org2.eclipse.php.internal.debug.core.zend.debugger.RemoteDebugger;
import org2.eclipse.php.internal.debug.core.zend.debugger.messages.ContinueProcessFileNotification;
import org2.eclipse.php.internal.debug.core.zend.debugger.messages.StartProcessFileNotification;
import org2.eclipse.php.internal.debug.core.zend.model.PHPDebugTarget;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

public class StartProcessFileNotificationHandler implements IDebugMessageHandler {

	protected boolean isFirstFileToDebug;

	public StartProcessFileNotificationHandler() {
		isFirstFileToDebug = true;
	}

	public void handle(IDebugMessage message, PHPDebugTarget debugTarget) {

		// do everything we need in order to prepare for processing current file
		StartProcessFileNotification notification = (StartProcessFileNotification) message;
		String remoteFileName = notification.getFileName();

		prepareForProcessing(remoteFileName, debugTarget);

		// send notification to tell debugger to continue processing file
		RemoteDebugger remoteDebugger = (RemoteDebugger) debugTarget.getRemoteDebugger();
		remoteDebugger.sendCustomNotification(new ContinueProcessFileNotification());
	}

	protected void prepareForProcessing(String remoteFileName, PHPDebugTarget debugTarget) {

		RemoteDebugger remoteDebugger = (RemoteDebugger) debugTarget.getRemoteDebugger();
		ILaunchConfiguration launchConfiguration = debugTarget.getLaunch().getLaunchConfiguration();

		remoteDebugger.removeCWDCache();
		debugTarget.setLastFileName(remoteFileName);

		boolean isWebServerDebugger = Boolean.toString(true).equals(debugTarget.getLaunch().getAttribute(IDebugParametersKeys.WEB_SERVER_DEBUGGER));
		String debugType = ""; //$NON-NLS-1$
		try {
			debugType = launchConfiguration.getAttribute(IDebugParametersKeys.PHP_DEBUG_TYPE, ""); //$NON-NLS-1$
		} catch (CoreException e) {
			PHPDebugEPLPlugin.logError(e);
		}

		String localPath = null;
		if (isFirstFileToDebug) { // we suppose that we always get full path
									// here
			if (isWebServerDebugger) {
				PathEntry pathEntry = debugTarget.mapFirstDebugFile(remoteFileName);
				if (pathEntry != null) {
					localPath = pathEntry.getResolvedPath();
				} else {
					localPath = remoteFileName;
				}

				// set current working directory to the current script directory
				// on debugger side
				if (debugType.equals(IDebugParametersKeys.PHP_WEB_SCRIPT_DEBUG)) {
					VirtualPath remotePath = new VirtualPath(remoteFileName);
					remotePath.removeLastSegment();
					remoteDebugger.setCurrentWorkingDirectory(remotePath.toString());
				}
			} else {
				try {
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(Path.fromOSString(remoteFileName));
					if (file != null) {
						localPath = file.getFullPath().toString();
					}
				} catch (Exception e) {
				}
				if (localPath == null) {
					localPath = remoteFileName;
				}
			}
		} else {
			localPath = remoteDebugger.convertToLocalFilename(remoteFileName);
		}

		// send found breakpoints with remote file name
		if (ILaunchManager.DEBUG_MODE.equals(debugTarget.getLaunch().getLaunchMode())) {
			if (isFirstFileToDebug) {
				try {
					boolean stopAtFirstLine = launchConfiguration.getAttribute(IDebugParametersKeys.FIRST_LINE_BREAKPOINT, false);
					if (stopAtFirstLine) {
						Breakpoint bpToSend = new Breakpoint(remoteFileName, -1);
						bpToSend.setType(Breakpoint.ZEND_STATIC_BREAKPOINT);
						bpToSend.setLifeTime(Breakpoint.ZEND_ONETIME_BREAKPOINT);
						debugTarget.getRemoteDebugger().addBreakpoint(bpToSend);
					}
				} catch (CoreException e) {
					PHPDebugEPLPlugin.logError(e);
				}
			}
			if (localPath != null) {
				IBreakpoint[] breakPoints = findBreakpoints(localPath, debugTarget);

				for (IBreakpoint bp : breakPoints) {
					try {
						if (bp.isEnabled()) {

							PHPConditionalBreakpoint phpBP = (PHPConditionalBreakpoint) bp;
							Breakpoint runtimeBreakpoint = phpBP.getRuntimeBreakpoint();

							int lineNumber = (Integer) bp.getMarker().getAttribute(IMarker.LINE_NUMBER);
							int bpID = runtimeBreakpoint.getID();
							int bpType = runtimeBreakpoint.getType();
							int bpLifeTime = runtimeBreakpoint.getLifeTime();
							Breakpoint bpToSend = new Breakpoint(remoteFileName, lineNumber);
							bpToSend.setID(bpID);
							bpToSend.setType(bpType);
							bpToSend.setLifeTime(bpLifeTime);
							bpToSend.setConditionalFlag(runtimeBreakpoint.getConditionalFlag());
							bpToSend.setExpression(runtimeBreakpoint.getExpression());

							debugTarget.getRemoteDebugger().addBreakpoint(bpToSend);
							runtimeBreakpoint.setID(bpToSend.getID());
						}
					} catch (CoreException e) {
						PHPDebugEPLPlugin.logError(e);
					}
				}
			}
		}

		isFirstFileToDebug = false;
	}

	protected IBreakpoint[] findBreakpoints(String localPath, PHPDebugTarget debugTarget) {
		IBreakpointManager breakpointManager = debugTarget.getBreakpointManager();
		if (!breakpointManager.isEnabled()) {
			return new IBreakpoint[0];
		}
		IBreakpoint[] breakpoints = breakpointManager.getBreakpoints(IPHPDebugConstants.ID_PHP_DEBUG_CORE);
		List<IBreakpoint> l = new LinkedList<IBreakpoint>();
		for (IBreakpoint bp : breakpoints) {
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(localPath);
			if (bp.getMarker().getResource().equals(resource)) {
				l.add(bp);
			}
			try {
				Object secondaryId = bp.getMarker().getAttribute(IPHPDebugConstants.SECONDARY_ID_KEY);
				if (secondaryId != null) {
					if (new VirtualPath(localPath).equals(new VirtualPath((String) secondaryId))) {
						l.add(bp);
					}
				}
			} catch (CoreException e) {
				PHPDebugEPLPlugin.logError(e);
			}
		}
		return l.toArray(new IBreakpoint[l.size()]);
	}
}
