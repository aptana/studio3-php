/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.aptana.core.build.RequiredBuildParticipant;
import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.text.reconciler.PHPReconcileContext;
import com.aptana.index.core.build.BuildContext;

/**
 * PHP build participant
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class PHPBuildParticipant extends RequiredBuildParticipant
{

	public void buildFile(BuildContext context, IProgressMonitor monitor)
	{
		if (context instanceof PHPReconcileContext)
		{
			try
			{
				context.getAST();
			}
			catch (CoreException e)
			{
				IdeLog.logWarning(PHPEditorPlugin.getDefault(), "Error creating a PHP AST for " //$NON-NLS-1$
						+ context.getURI(), com.aptana.parsing.IDebugScopes.PARSING);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.core.build.IBuildParticipant#deleteFile(com.aptana.core.build.BuildContext,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void deleteFile(BuildContext context, IProgressMonitor monitor)
	{
		// No-Op.
	}
}
