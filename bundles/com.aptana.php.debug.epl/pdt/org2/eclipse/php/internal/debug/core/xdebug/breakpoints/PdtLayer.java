/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.xdebug.breakpoints;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.model.PHPLineBreakpoint;
import org2.eclipse.php.internal.debug.core.model.PHPRunToLineBreakpoint;
import org2.eclipse.php.internal.debug.core.sourcelookup.PHPSourceNotFoundInput;
import org2.eclipse.php.internal.debug.core.sourcelookup.containers.PHPCompositeSourceContainer;
import org2.eclipse.php.internal.debug.core.xdebug.IDELayer;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpBreakpoint;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpBreakpointFacade;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpLogger;
import org2.eclipse.php.internal.debug.core.zend.debugger.Breakpoint;

public class PdtLayer implements IDELayer, DBGpBreakpointFacade {

	public String getBreakpointModelID() {
		return IPHPDebugConstants.ID_PHP_DEBUG_CORE;
	}

	public Object sourceNotFound(Object debugElement) {
		Object obj = null;
		if (debugElement instanceof IStackFrame) {
			obj = new PHPSourceNotFoundInput((IStackFrame) debugElement);
		}
		return obj;
	}

	public ISourceContainer getSourceContainer(IProject resource, ILaunchConfiguration launchConfig) {
		return new PHPCompositeSourceContainer(resource, launchConfig);
	}

	public DBGpBreakpoint createDBGpBreakpoint(IBreakpoint breakpoint) {
		return new PdtBreakpoint((PHPLineBreakpoint) breakpoint);
	}

	public IBreakpoint findBreakpointHit(String filename, int lineno) {
		IBreakpoint bpFound = null;
		filename = normalizeFileName(filename);
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(getBreakpointModelID());
		for (int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint = breakpoints[i];
			if (supportsBreakpoint(breakpoint)) {
				if (breakpoint instanceof PHPLineBreakpoint) {
					PHPLineBreakpoint lineBreakpoint = (PHPLineBreakpoint) breakpoint;
					Breakpoint zBP = lineBreakpoint.getRuntimeBreakpoint();
					String bFileName = zBP.getFileName();
					bFileName = normalizeFileName(bFileName);
					int bLineNumber = zBP.getLineNumber();
					if (bLineNumber == lineno && bFileName.equals(filename)) {
						bpFound = breakpoint;
						if (DBGpLogger.debugBP()) {
							DBGpLogger.debug("breakpoint at " + filename + "(" + lineno + ") found"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}

					}

					// remove all RunToLine breakpoints while we search through the
					// list of all our breakpoints looking for the one that was hit
					if (breakpoint instanceof PHPRunToLineBreakpoint) {
						IBreakpointManager bmgr = DebugPlugin.getDefault().getBreakpointManager();
						try {
							if (DBGpLogger.debugBP()) {
								DBGpLogger.debug("removing runtoline breakpoint"); //$NON-NLS-1$
							}
							bmgr.removeBreakpoint(breakpoint, true);
						} catch (CoreException e) {
							DBGpLogger.logException("Exception trying to remove a runtoline breakpoint", this, e); //$NON-NLS-1$
						}
					}
				}
			}
		}
		return bpFound;
	}

	/*
	 * Returns the file name with a forward slashes only
	 */
	private static String normalizeFileName(String fileName)
	{
		if (fileName == null)
		{
			return null;
		}
		return fileName.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean supportsBreakpoint(IBreakpoint bp) {
		if (bp.getModelIdentifier().equals(getBreakpointModelID())) {
			//TODO: Improvement: Breakpoint: better support for breakpoint rejection
			//ok it is a PHP breakpoint, but are there any other restrictions we could impose ?
			//look at BreakpointSet for more info on what PHPIDE does
			return true;
		}
		return false;
	}

	public IBreakpoint createRunToLineBreakpoint(IFile fileName, int lineNumber) throws DebugException {
		return new PHPRunToLineBreakpoint(fileName, lineNumber);
	}

	public String getSystemDebugProperty() {
		return "org2.eclipse.php.debug.ui.activeDebugging"; //$NON-NLS-1$
	}

}
