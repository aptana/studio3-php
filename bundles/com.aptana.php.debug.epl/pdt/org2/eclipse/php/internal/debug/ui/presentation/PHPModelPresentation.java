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

import java.io.File;
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
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.model.PHPConditionalBreakpoint;
import org2.eclipse.php.internal.debug.core.model.PHPLineBreakpoint;
import org2.eclipse.php.internal.debug.core.sourcelookup.PHPSourceNotFoundInput;
import org2.eclipse.php.internal.debug.core.zend.communication.DebuggerCommunicationDaemon;
import org2.eclipse.php.internal.debug.core.zend.model.PHPDebugTarget;
import org2.eclipse.php.internal.debug.core.zend.model.PHPStackFrame;
import org2.eclipse.php.internal.debug.core.zend.model.PHPThread;
import org2.eclipse.php.internal.debug.ui.PHPDebugUIMessages;
import org2.eclipse.php.internal.debug.ui.breakpoint.PHPBreakpointImageDescriptor;
import org2.eclipse.php.internal.debug.ui.console.SourceDisplayUtil;
import org2.eclipse.php.internal.debug.ui.sourcelookup.PHPSourceNotFoundEditorInput;
import org2.eclipse.php.internal.debug.ui.util.ImageDescriptorRegistry;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;


/**
 * Renders PHP debug elements
 */
public class PHPModelPresentation extends LabelProvider implements IDebugModelPresentation {
	protected final static String UNTITLED_FOLDER_PATH = "Untitled_Documents"; //$NON-NLS-1$

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
		if (element instanceof PHPDebugTarget) {
			return getTargetText((PHPDebugTarget) element);
		} else if (element instanceof PHPThread) {
			return getThreadText((PHPThread) element);
		} else if (element instanceof PHPStackFrame) {
			return getStackFrameText((PHPStackFrame) element);
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
					return filename + " [line: " + lineNumber.toString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
				} catch (CoreException e) {
					PHPDebugEPLPlugin.logError("Unexpected error in PHPModelPresentation", e); //$NON-NLS-1$
				}
			}
		}
		return null;

	}

	private String getTargetText(PHPDebugTarget target) {
		String label = ""; //$NON-NLS-1$
		if (target.isTerminated()) {
			label = MessageFormat.format(PHPDebugUIMessages.MPresentation_Terminated_1, new Object[] {});
		}
		label += PHPDebugUIMessages.MPresentation_PHP_APP_1;
		int debugPort = PHPDebugEPLPlugin.getDebugPort(DebuggerCommunicationDaemon.ZEND_DEBUGGER_ID);
		return  label + " (port " + debugPort + ')'; //$NON-NLS-1$
	}

	private String getThreadText(PHPThread thread) {
		PHPDebugTarget target = (PHPDebugTarget) thread.getDebugTarget();
		String label = ""; //$NON-NLS-1$
		try {
			label = target.getName();
		} catch (DebugException e1) {
			// Just log should never happen
			PHPDebugEPLPlugin.logError("PHPModelPresentation error getting target name", e1); //$NON-NLS-1$
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
			label = PHPDebugUIMessages.MPresentation_Terminated_1 + label;
		}
		return label;
	}

	private String getStackFrameText(PHPStackFrame frame) {
		try {
			StringBuffer buffer = new StringBuffer();
			String frameName = frame.getName();
			if (frameName != null && frameName.length() > 0) {
				buffer.append(frame.getName());
				buffer.append("(): "); //$NON-NLS-1$
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
		if (element instanceof PHPSourceNotFoundInput) {
			 return new PHPSourceNotFoundEditorInput((PHPSourceNotFoundInput) element);
		}
		if (element instanceof PHPLineBreakpoint) {
			PHPLineBreakpoint breakpoint = (PHPLineBreakpoint) element;
			IMarker marker = breakpoint.getMarker();
			IResource resource = marker.getResource();

			if (resource instanceof IFile) {
				return new FileEditorInput((IFile) resource);
			}

			if (element instanceof PHPSourceNotFoundInput) {
				return new PHPSourceNotFoundEditorInput((PHPSourceNotFoundInput) element);
			}

			// Breakpoints for external files are stored in workspace root:
//			else if (resource instanceof IWorkspaceRoot) {
//				try {
//					String filename = (String) marker.getAttribute(IPHPConstants.STORAGE_FILE);
//					String type = (String) marker.getAttribute(IPHPConstants.STORAGE_TYPE);
//
//					if (IPHPConstants.STORAGE_TYPE_INCLUDE.equals(type)) {
//						String projectName = (String) marker.getAttribute(IPHPConstants.STORAGE_PROJECT, "");
//						IProject project = PHPDebugUIPlugin.getProject(projectName);
//						String includeBaseDir = (String) marker.getAttribute(IPHPConstants.STORAGE_INC_BASEDIR, "");
//						filename = marker.getAttribute(Constants.SECONDARY_ID_KEY, filename);
//
//						File file = new File(filename);
//						LocalFileStorage lfs = new LocalFileStorage(file);
//						lfs.setProject(project);
//						lfs.setIncBaseDirName(includeBaseDir);
//						return new LocalFileStorageEditorInput(lfs);
//					} else if (IPHPConstants.STORAGE_TYPE_EXTERNAL.equals(type) || IPHPConstants.STORAGE_TYPE_REMOTE.equals(type)) {
//						File file = new File(filename);
//						return new FileStoreEditorInput(FileStoreFactory.createFileStore(file));
//					}
//				} catch (CoreException e) {
//					PHPDebugEPLPlugin.logError("Unexpected error in PHPModelPresentation", e);
//				}
//			}
		}
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
//			if (isUntitled(element)) {
//				String path = ((IFileStore)element).toString();
//				return new NonExistingPHPFileEditorInput(new Path(path));
//			} 
//			return new FileStoreEditorInput((IFileStore)element);
//		}
		if (element instanceof IFileStore) // IFileStore
		{
			IFileStore localFile = (IFileStore)element;
			File file = new File(localFile.toString());
			if (file.exists()) {
				return new FileStoreEditorInput(localFile);
			} 
			// TODO: SG - Return a source-not-found editor input in this case.
		}
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

	protected boolean isUntitled(Object element) {
		if (element instanceof IFileStore) {
			final IFileStore localFile = (IFileStore)element;
			IFileStore parentDir = localFile.getParent();
			if (parentDir != null && UNTITLED_FOLDER_PATH.equals(parentDir.getName())) {
				return true;
			}
		}
		return false;
	}
}
