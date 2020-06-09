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
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.model.IBreakpoint;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.model.PHPConditionalBreakpoint;
import org2.eclipse.php.internal.debug.core.model.PHPLineBreakpoint;
import org2.eclipse.php.internal.debug.core.model.PHPRunToLineBreakpoint;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpBreakpoint;

/**
 * 
 *
 */
public class PdtBreakpoint implements DBGpBreakpoint {

	private PHPLineBreakpoint bp;
	private IFile workspaceFile;

	/**
	 * 
	 * @param breakpoint
	 */
	public PdtBreakpoint(PHPLineBreakpoint breakpoint) {
		bp = breakpoint;
		IMarker marker = bp.getMarker();

		IResource resource;
		int lineNumber = 0;
		if (breakpoint instanceof PHPRunToLineBreakpoint) {
			resource = ((PHPRunToLineBreakpoint) breakpoint).getSourceFile();
			lineNumber = bp.getRuntimeBreakpoint().getLineNumber();
		} else {
			resource = marker.getResource();
			lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, 0);
		}

		String fileName = "";
		if (resource instanceof IWorkspaceRoot) {
			//TODO: Improvement: Breakpoint: we need to handle this because PHPBreakpoint can be setup with this situation
			try {
				//String storageType = (String)marker.getAttribute(IPHPConstants.Include_Storage_type);
				// storage types include
				// IPHPConstants.Include_Storage_zip   - don't know how to handle this one
				// IPHPConstants.Include_Storage_LFile - should be able to support
				// IPHPConstants.Include_Storage_RFile - doubt if this one is ever used in current PHP IDE.
				//
				// Include_Storage_Project contains the project it is found in.
				fileName = (String) marker.getAttribute(IPHPDebugConstants.STORAGE_TYPE_INCLUDE);
				fileName = marker.getAttribute(IPHPDebugConstants.SECONDARY_ID_KEY, fileName); // gets the full path.

				// adding bps to these include files has strange affects. If one fails to add the first time it is because it removes one
				// from elsewhere, then you find you can add it. Multiple breakpoints in multiple files is possible but you experience this
				// problem. Will need to raise a defect on this at some point.

				IResource res = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));
				if (res instanceof IFile) {
					workspaceFile = (IFile) res;
				}

			} catch (CoreException e) {
			}

		} else {

			// a file in the workspace, handles included projects
			fileName = (resource.getRawLocation()).toOSString();
			if (resource instanceof IFile) {
				workspaceFile = (IFile) resource;
			}
		}

		// do we need to update the runtimeBreakpoint information ?
		bp.getRuntimeBreakpoint().setFileName(fileName);
		bp.getRuntimeBreakpoint().setLineNumber(lineNumber);
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.xdebug.core.dbgp.DBGpBreakpoint#getBreakpoint()
	 */
	public IBreakpoint getBreakpoint() {
		return bp;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.xdebug.core.dbgp.DBGpBreakpoint#setBreakpoint(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void setBreakpoint(IBreakpoint breakpoint) {
		bp = (PHPLineBreakpoint) breakpoint;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.xdebug.core.dbgp.DBGpBreakpoint#getID()
	 */
	public int getID() {
		return bp.getRuntimeBreakpoint().getID();
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.xdebug.core.dbgp.DBGpBreakpoint#setID(int)
	 */
	public void setID(int id) {
		bp.getRuntimeBreakpoint().setID(id);
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.xdebug.core.dbgp.DBGpBreakpoint#isConditionChanged()
	 */
	public boolean hasConditionChanged() {
		return bp.isConditionChanged();
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.xdebug.core.dbgp.DBGpBreakpoint#setConditionChanged(boolean)
	 */
	public void resetConditionChanged() {
		bp.setConditionChanged(false);
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.xdebug.core.dbgp.DBGpBreakpoint#getIFile()
	 */
	public IFile getIFile() {
		return workspaceFile;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.xdebug.core.IDEBreakpoint#getFileName()
	 */
	public String getFileName() {
		return bp.getRuntimeBreakpoint().getFileName();
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.xdebug.core.IDEBreakpoint#getLineNumber()
	 */
	public int getLineNumber() {
		return bp.getRuntimeBreakpoint().getLineNumber();
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.xdebug.core.IDEBreakpoint#getExpression()
	 */
	public String getExpression() {
		return bp.getRuntimeBreakpoint().getExpression();
	}

	public boolean isConditional() {
		return (bp instanceof PHPConditionalBreakpoint);
	}

	public boolean isConditionEnabled() {
		if (isConditional()) {
			return ((PHPConditionalBreakpoint) bp).isConditionEnabled();
		}
		return false;
	}
}
