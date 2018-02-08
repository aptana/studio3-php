/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPNature;

/**
 * Workspace folder selection dialog
 * 
 * @author Denis Denisenko
 */
public class WorkspaceFolderSelectionDialog extends ElementTreeSelectionDialog
{

	public WorkspaceFolderSelectionDialog(Shell parent)
	{
		super(parent, new WorkbenchLabelProvider(), new WorkbenchContentProvider());

		addFilter(new ViewerFilter()
		{

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element)
			{
				if (!(element instanceof IResource))
				{
					return false;
				}

				if (((IResource) element).getType() == IResource.FILE)
				{
					return false;
				}
				try
				{
					IProject project = ((IResource) element).getProject();
					if (!project.isAccessible() || !project.hasNature(PHPNature.NATURE_ID))
					{
						return false;
					}
				}
				catch (CoreException e)
				{
					IdeLog.logError(PHPEditorPlugin.getDefault(), "Workspace folder selection dialog error", e); //$NON-NLS-1$
					return false;
				}

				return true;
			}
		});
	}

}
