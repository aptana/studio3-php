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
package org2.eclipse.php.internal.debug.ui.presentation;

import java.text.MessageFormat;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.model.PHPConditionalBreakpoint;
import org2.eclipse.php.internal.debug.core.model.PHPLineBreakpoint;
import org2.eclipse.php.internal.debug.core.xdebug.communication.XDebugCommunicationDaemon;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpStackFrame;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpTarget;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpThread;
import org2.eclipse.php.internal.debug.ui.PHPDebugUIMessages;
import org2.eclipse.php.internal.debug.ui.breakpoint.PHPBreakpointImageDescriptor;
import org2.eclipse.php.internal.debug.ui.console.SourceDisplayUtil;
import org2.eclipse.php.internal.debug.ui.util.ImageDescriptorRegistry;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * Renders PHP debug elements
 */
public class XDebugModelPresentation extends LabelProvider implements IDebugModelPresentation {

	private ImageDescriptorRegistry fDebugImageRegistry;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 */

	public void setAttribute(String attribute, Object value) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (element instanceof PHPConditionalBreakpoint) {
			return getBreakpointImage((PHPConditionalBreakpoint) element);
		}
		return null;
	}

	// Returns the conditional breakpoint icon (enabled / disabled).
	// In case the breakpoint is not conditional, return null and let the default breakpoint
	// icon.
	private Image getBreakpointImage(PHPConditionalBreakpoint breakpoint) {
		try {
			if (breakpoint.isConditionEnabled()) {
				PHPBreakpointImageDescriptor descriptor;
				if (breakpoint.isEnabled()) {
					descriptor = new PHPBreakpointImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT), PHPBreakpointImageDescriptor.CONDITIONAL | PHPBreakpointImageDescriptor.ENABLED);
				} else {
					descriptor = new PHPBreakpointImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED), PHPBreakpointImageDescriptor.CONDITIONAL);
				}
				return getDebugImageRegistry().get(descriptor);
			}
		} catch (CoreException e) {
			return null;
		}
		return null;
	}

	protected ImageDescriptorRegistry getDebugImageRegistry() {
		if (fDebugImageRegistry == null) {
			fDebugImageRegistry = PHPDebugEPLPlugin.getImageDescriptorRegistry();
		}
		return fDebugImageRegistry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof DBGpTarget) {
			return getTargetText((DBGpTarget) element);
		} else if (element instanceof DBGpThread) {
			return getThreadText((DBGpThread) element);
		} else if (element instanceof DBGpStackFrame) {
			//return getStackFrameText((DBGpStackFrame) element);
			return null;
		} else if (element instanceof PHPLineBreakpoint) {
			PHPLineBreakpoint breakpoint = (PHPLineBreakpoint) element;
			IMarker marker = breakpoint.getMarker();
			IResource resource = marker.getResource();
			if (resource instanceof IFile) {
				return null;
			} else if (resource instanceof IWorkspaceRoot) {
				try {
					String filename = (String) marker.getAttribute(IPHPDebugConstants.STORAGE_FILE);
					Integer lineNumber = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
					return filename + " [" + PHPDebugUIMessages.XDebugPresentation_line + ": " + lineNumber.toString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} catch (CoreException e) {
					PHPDebugEPLPlugin.logError("Unexpected error in XDebugModelPresentation", e); //$NON-NLS-1$
				}
			}
		}
		return null;

	}

	private String getTargetText(DBGpTarget target) {
		String label = ""; //$NON-NLS-1$
		if (target.isTerminated()) {
			label = MessageFormat.format(PHPDebugUIMessages.MPresentation_Terminated_1, new Object[] {});
		}
		label += PHPDebugUIMessages.MPresentation_PHP_APP_1;
		int debugPort = PHPDebugEPLPlugin.getDebugPort(XDebugCommunicationDaemon.XDEBUG_DEBUGGER_ID);
		return  label + " (port " + debugPort + ')'; //$NON-NLS-1$
	}

	private String getThreadText(DBGpThread thread) {
		DBGpTarget target = (DBGpTarget) thread.getDebugTarget();
		String label = ""; //$NON-NLS-1$
		try {
			label = target.getName();
		} catch (DebugException e1) {
			// Just log should never happen
			PHPDebugEPLPlugin.logError("XDebugModelPresentation error getting target name", e1); //$NON-NLS-1$
		}
		if (thread.isStepping()) {
			label += PHPDebugUIMessages.MPresentation_Stepping_1;
		} else if (thread.isSuspended()) {
			IBreakpoint[] breakpoints = thread.getBreakpoints();
			if (breakpoints.length == 0) {
				label += PHPDebugUIMessages.MPresentation_Suspended_1;
			} else {
				IBreakpoint breakpoint = breakpoints[0]; // there can only be
				// one in PHP
				if (breakpoint instanceof PHPLineBreakpoint) {
					label += PHPDebugUIMessages.MPresentation_SLineBreakpoint_1;
				}
			}
		} else if (thread.isTerminated()) {
			label = PHPDebugUIMessages.MPresentation_Terminated_1 + label; //$NON-NLS-1$
		}
		return label;
	}

	private String getStackFrameText(DBGpStackFrame frame) {
		try {
			// Fix bug #160443 (Stack frames line numbers update).
			// Synchronize the top frame with the given values.
			DBGpThread thread = (DBGpThread) frame.getThread();
			DBGpStackFrame topFrame = (DBGpStackFrame) thread.getTopStackFrame();
			if (topFrame != null && topFrame.equals(frame)) {
				frame = topFrame;
			} // end fix

			StringBuffer buffer = new StringBuffer();
			String frameName = frame.getName();
			if (frameName != null && frameName.length() > 0) {
				buffer.append(frame.getName());
				buffer.append("() "); //$NON-NLS-1$
			}
			buffer.append(frame.getSourceName());
			buffer.append(PHPDebugUIMessages.MPresentation_ATLine_1 + (frame.getLineNumber()));
			return buffer.toString();

		} catch (DebugException e) {
			PHPDebugEPLPlugin.logError("Unexpected error in PHPModelPresentation", e); //$NON-NLS-1$
		} catch (NullPointerException npe) {
			// This is here for debug purpose. Figure out why do we get nulls.
			StringBuffer errorMessage = new StringBuffer("NPE in getStackFrameText(). Frame = "); //$NON-NLS-1$
			errorMessage.append(frame);
			if (frame != null) {
				errorMessage.append(", Thread = "); //$NON-NLS-1$
				errorMessage.append(frame.getThread());
			}
			PHPDebugEPLPlugin.logError(errorMessage.toString(), npe);
		}
		return ""; //$NON-NLS-1$

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(org.eclipse.debug.core.model.IValue,
	 *      org.eclipse.debug.ui.IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
		String detail = ""; //$NON-NLS-1$
		try {
			detail = value.getValueString();
		} catch (DebugException e) {
		}
		listener.detailComputed(value, detail);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(java.lang.Object)
	 */
	public IEditorInput getEditorInput(Object element) {
		if (element instanceof IFile) {
			return new FileEditorInput((IFile) element);
		}
		if (element instanceof IFileStore)
		{
			try
			{
				return SourceDisplayUtil.getEditorInput(((IFileStore)element).toLocalFile(0, null));
			} catch (CoreException e)
			{
				// TODO: SG - return a valid PHPSourceNotFoundEditorInput for this case.
				// return new PHPSourceNotFoundEditorInput((PHPSourceNotFoundInput) element);
			}
		}
		if (element instanceof PHPLineBreakpoint) {
			PHPLineBreakpoint breakpoint = (PHPLineBreakpoint) element;
			IMarker marker = breakpoint.getMarker();
			IResource resource = marker.getResource();

			if (resource instanceof IFile) {
				return new FileEditorInput((IFile) resource);
			}
			// Breakpoints for external files are stored in workspace root:
			else if (resource instanceof IWorkspaceRoot) {
				try {
					String filename = (String) marker.getAttribute(IPHPDebugConstants.STORAGE_FILE);
					String type = (String) marker.getAttribute(IPHPDebugConstants.STORAGE_TYPE);

					// TODO: SG - Fix the editor input to support other input sources.
					//					if (IPHPDebugConstants.STORAGE_TYPE_INCLUDE.equals(type)) {
					//						String projectName = (String) marker.getAttribute(IPHPDebugConstants.STORAGE_PROJECT, ""); //$NON-NLS-1$
					//						IProject project = PHPDebugUIPlugin.getProject(projectName);
					//						String includeBaseDir = (String) marker.getAttribute(IPHPDebugConstants.STORAGE_INC_BASEDIR, ""); //$NON-NLS-1$
					//						filename = marker.getAttribute(StructuredResourceMarkerAnnotationModel.SECONDARY_ID_KEY, filename);
					//
					//						File file = new File(filename);
					//						LocalFileStorage lfs = new LocalFileStorage(file);
					//						lfs.setProject(project);
					//						lfs.setIncBaseDirName(includeBaseDir);
					//						return new LocalFileStorageEditorInput(lfs);
					//					} else if (IPHPDebugConstants.STORAGE_TYPE_EXTERNAL.equals(type)) {
					//						File file = new File(filename);
					//						LocalFile store = new LocalFile(file);
					//						return new FileStoreEditorInput(store);
					//					}
				} catch (CoreException e) {
					PHPDebugEPLPlugin.logError("Unexpected error in PHPModelPresentation", e); //$NON-NLS-1$
				}
			}
		}
		// TODO: SG - Fix the editor input to support other input sources.
		//		if (element instanceof ZipEntryStorage) {
		//			return new ZipEntryStorageEditorInput((ZipEntryStorage) element);
		//		}
		//		if (element instanceof LocalFileStorage) {
		//			return new LocalFileStorageEditorInput((LocalFileStorage) element);
		//		}
		//		if (element instanceof PHPSourceNotFoundInput) {
		//			return new PHPSourceNotFoundEditorInput((PHPSourceNotFoundInput) element);
		//		}
		//		if (element instanceof IFileStore) {
		//			return new FileStoreEditorInput((IFileStore) element);
		//		}
		PHPDebugEPLPlugin.logError("Unknown editor input type: " + element.getClass().getName()); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(org.eclipse.ui.IEditorInput,
	 *      java.lang.Object)
	 */
	public String getEditorId(IEditorInput input, Object inputObject) {
		try {
			IEditorDescriptor descriptor= IDE.getEditorDescriptor(input.getName());
			return descriptor.getId();
		} catch (PartInitException e) {
			return null;
		}
	}
}
