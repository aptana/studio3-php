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
package org.eclipse.php.internal.debug.ui.breakpoint.adapter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.debug.ui.actions.RunToLineHandler;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org.eclipse.php.internal.debug.core.model.PHPDebugElement;
import org.eclipse.php.internal.debug.core.model.PHPRunToLineBreakpoint;
import org.eclipse.php.internal.debug.core.resources.ExternalFilesRegistry;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpElement;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpTarget;
import org.eclipse.php.internal.debug.core.zend.model.PHPDebugTarget;
import org.eclipse.php.internal.debug.ui.PHPDebugUIMessages;
import org.eclipse.php.internal.debug.ui.breakpoint.provider.PHPBreakpointProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.editors.text.NonExistingFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.aptana.debug.php.epl.PHPDebugEPLPlugin;
import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.ui.util.StatusLineMessageTimerManager;

/**
 * Run to line target for the PHP debugger
 */
public class PHPRunToLineAdapter implements IRunToLineTarget {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#runToLine(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection,
	 *      org.eclipse.debug.core.model.ISuspendResume)
	 */
	public void runToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) throws CoreException {
		IEditorPart editorPart = (IEditorPart) part;
		IEditorInput input = editorPart.getEditorInput();
		String errorMessage = null;
		if (input == null) {
			errorMessage = PHPDebugUIMessages.PHPRunToLineAdapter_0;
		} else {
			ITextEditor textEditor = (ITextEditor) editorPart;
			IDocument document = textEditor.getDocumentProvider().getDocument(input);
			if (document == null) {
				errorMessage = PHPDebugUIMessages.PHPRunToLineAdapter_1;
			} else {
				ITextSelection textSelection = (ITextSelection) selection;

				int lineNumber = 0;
				try {
					lineNumber = document.getLineOfOffset(textSelection.getOffset());
				} catch (BadLocationException e) {
				}
				// Figure out if the selected line is a valid line to place a temporary breakpoint for the run-to-line
				AbstractThemeableEditor commonEditor = null;
				if (textEditor instanceof AbstractThemeableEditor)
				{
					commonEditor = (AbstractThemeableEditor) textEditor;
				}
				if (commonEditor == null)
				{
					StatusLineMessageTimerManager.setErrorMessage(PHPDebugUIMessages.CannotRunToLine, 1000, true); // hide message after 1 second
					return;
				}
				int validLinePosition = PHPBreakpointProvider.getValidPosition(commonEditor, lineNumber);
				if (validLinePosition < 0) {
					StatusLineMessageTimerManager.setErrorMessage(PHPDebugUIMessages.CannotRunToLine, 1000, true); // hide message after 1 second
					return;
				} else {
					int validLineNumber = 0;
					try {
						validLineNumber = document.getLineOfOffset(validLinePosition);
						if (validLineNumber != lineNumber) {
							StatusLineMessageTimerManager.setErrorMessage(PHPDebugUIMessages.CannotRunToLine, 1000, true); // hide message after 1 second
							return;
						}
					} catch (BadLocationException ble) {
						StatusLineMessageTimerManager.setErrorMessage(PHPDebugUIMessages.CannotRunToLine, 1000, true); // hide message after 1 second
						return;
					}
				}
				lineNumber++;
				if (lineNumber > 0) {
					if (getValidPosition(commonEditor, lineNumber) != -1) {
						if (target instanceof IAdaptable) {
							IDebugTarget debugTarget = (IDebugTarget) ((IAdaptable) target).getAdapter(IDebugTarget.class);
							if (debugTarget != null) {
								IFile file = getFile(textEditor);
								//TODO: we need a to call a debugger specific api, so an extension point is 
								//required here for different debuggers to plug into.
								if (debugTarget instanceof PHPDebugTarget) {
									IBreakpoint breakpoint = new PHPRunToLineBreakpoint(file, lineNumber);
									RunToLineHandler handler = new RunToLineHandler(debugTarget, target, breakpoint);
									handler.run(new NullProgressMonitor());
								}
								else if (debugTarget instanceof DBGpTarget) { 
									DBGpTarget t = (DBGpTarget) debugTarget;
									t.runToLine(file, lineNumber);									
								}
								return;
							}
						}
					} else {
						errorMessage = PHPDebugUIMessages.PHPRunToLineAdapter_2;
					}
				} else {
					errorMessage = PHPDebugUIMessages.PHPRunToLineAdapter_2;
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IPHPDebugConstants.INTERNAL_ERROR, errorMessage, null));
	}

	public boolean canRunToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) {
		//TODO: PHP Debug elements should have a shared marker and test for here
		//This will be an enhancement to the generic debug API.
		if (target instanceof PHPDebugElement || target instanceof DBGpElement) {
			// allow running to the line only when the target is suspended.
			return target.isSuspended();
		}
		return false;
	}

	/**
	 * The file input for the TextEditor
	 * 
	 * @return the IFile that this strategy is operating on
	 */
	protected IFile getFile(ITextEditor textEditor) {
		if (textEditor == null)
			return null;
		IEditorInput input = textEditor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput) input).getFile();
		}
		if (input instanceof IPathEditorInput) {
			String filePath = ((IPathEditorInput)input).getPath().toOSString();
			IFile result = ExternalFilesRegistry.getInstance().getFileEntry(filePath);
			if (result == null && filePath.length() > 0 && filePath.charAt(0) == '/') {
				return ExternalFilesRegistry.getInstance().getFileEntry(filePath.substring(1));
			}
			return result;
		}
		if (input instanceof NonExistingFileEditorInput) {
			return (IFile)((NonExistingFileEditorInput)input).getAdapter(IResource.class);
		}
		return null;
		
	}

	/**
	 * Finds a valid position somewhere on lineNumber in document, idoc, where a
	 * breakpoint can be set and returns that position. -1 is returned if a
	 * position could not be found.
	 * 
	 * @param editorLineNumber
	 * @return position to set breakpoint or -1 if no position could be found
	 */
	private int getValidPosition(AbstractThemeableEditor ed, int editorLineNumber) {
		return PHPBreakpointProvider.getValidPosition(ed, editorLineNumber);
	}
}
