package com.aptana.editor.php.indexer;

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
		PHPGlobalIndexer.getInstance().cleanLibraries();
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
