package com.aptana.editor.php.indexer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Clean and rebuild the attached PHP libraries after a Project->Rebuild PHP Libraries... action.
 * 
 * @author Shalom Gibly
 * @since Aptana PHP 1.1
 */
public class CleanLibrariesAction implements IWorkbenchWindowActionDelegate
{

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window)
	{
	}

	/**
	 * Run the clean and rebuild.
	 * 
	 * @param action
	 */
	public void run(IAction action)
	{
		Job cleanJob = new Job(Messages.CleanLibrariesAction_rebuildingLibraries)
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				PHPGlobalIndexer.getInstance().cleanLibraries(monitor);
				return Status.OK_STATUS;
			}
		};
		cleanJob.setPriority(Job.BUILD);
		cleanJob.schedule();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection)
	{
	}
}
