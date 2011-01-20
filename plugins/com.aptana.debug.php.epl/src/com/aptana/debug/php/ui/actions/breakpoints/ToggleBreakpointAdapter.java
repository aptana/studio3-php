package com.aptana.debug.php.ui.actions.breakpoints;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.php.internal.debug.ui.breakpoint.provider.PHPBreakpointProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;

import com.aptana.editor.common.AbstractThemeableEditor;

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
		Job job = new Job("Toggle Breakpoint")
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
						new PHPBreakpointProvider().addBreakpoint(unifiedEditor, editorInput, rs.getStartLine(), rs.getOffset());
					}
					catch (CoreException e)
					{
						e.printStackTrace();
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

	private boolean canToggleLineBreakpoint(IWorkbenchPart part, ISelection selection)
	{
		if (selection instanceof ITextSelection)
		{
			IEditorInput editorInput = ((IEditorPart) part).getEditorInput();
			IFile rl = (IFile) editorInput.getAdapter(IFile.class);

			return true;
		}
		return false;
	}

	/**
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException
	{
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException
	{
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
