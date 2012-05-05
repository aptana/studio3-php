/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.actions.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;
import org2.eclipse.php.internal.debug.ui.breakpoint.provider.PHPBreakpointProvider;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * @author Pavel Petrochenko
 */
public class ToggleBreakpointAdapter implements IToggleBreakpointsTarget
{
	/**
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection) throws CoreException
	{
		Job job = new Job(Messages.ToggleBreakpointAdapter_toggleBreakpointJobName)
		{
			protected IStatus run(IProgressMonitor monitor)
			{

				if (selection instanceof ITextSelection)
				{
					if (monitor.isCanceled())
					{
						return Status.CANCEL_STATUS;
					}
					EditorPart editorPart = (EditorPart) part;
					IEditorInput editorInput = editorPart.getEditorInput();
					// IResource resource = (IResource) editorInput.getAdapter(IFile.class);
					TextSelection rs = (TextSelection) selection;

					try
					{
						AbstractThemeableEditor unifiedEditor = (AbstractThemeableEditor) editorPart;
						new PHPBreakpointProvider().addBreakpoint(unifiedEditor, editorInput, rs.getStartLine(),
								rs.getOffset());
					}
					catch (CoreException e)
					{
						IdeLog.logError(PHPDebugEPLPlugin.getDefault(), e);
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(true);
		job.schedule();
	}

	/**
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection)
	{
		return (selection instanceof ITextSelection);
	}

	/**
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException
	{
	}

	/**
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection)
	{
		return false;
	}

	/**
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException
	{
	}

	/**
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection)
	{
		return false;
	}
}
