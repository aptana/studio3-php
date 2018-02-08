/**
 * 
 */
package com.aptana.editor.php.indexer;

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;

import com.aptana.core.CorePlugin;
import com.aptana.core.IDebugScopes;
import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.internal.contentAssist.ContentAssistUtils;

/**
 * An incremental project builder for PHP projects. This builder is here for clean operations.
 * 
 * @author Shalom Gibly
 * @since Aptana PHP 1.1
 */
public class IncrementalPHPProjectBuilder extends IncrementalProjectBuilder
{

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void clean(IProgressMonitor monitor)
	{
		IProject project = getProject();
		PHPGlobalIndexer.getInstance().clean(project, monitor);
		PHPGlobalIndexer.getInstance().cleanLibraries(monitor);
		ContentAssistUtils.cleanIndex();
		if (IdeLog.isTraceEnabled(CorePlugin.getDefault(), IDebugScopes.BUILDER))
		{
			// @formatter:off
			String message = MessageFormat.format("Cleaning the PHP project {0}", //$NON-NLS-1$
					(project != null) ? project.getName() : "null" //$NON-NLS-1$
			);
			// @formatter:on
			IdeLog.logTrace(CorePlugin.getDefault(), message, IDebugScopes.BUILDER);
		}
	}

	/**
	 * Constructor
	 */
	public IncrementalPHPProjectBuilder()
	{
	}

	/**
	 * Returns null, as the PHP plugin still does not use the builders as it should.
	 * 
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int, java.util.Map,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("rawtypes")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
	{
		// FIXME - SG: Convert from Indexer timer to the builder system.
		IProject project = getProject();
		if ((kind == CLEAN_BUILD || kind == FULL_BUILD) && project != null)
		{
			PHPGlobalIndexer.getInstance().clean(project, monitor);
			PHPGlobalIndexer.getInstance().build(project, monitor);
			if (IdeLog.isTraceEnabled(CorePlugin.getDefault(), IDebugScopes.BUILDER))
			{
				// @formatter:off
				String message = MessageFormat.format("Building the PHP project {0}", //$NON-NLS-1$
						project.getName());
				// @formatter:on
				IdeLog.logTrace(CorePlugin.getDefault(), message, IDebugScopes.BUILDER);
			}
		}
		return null;
	}

}
