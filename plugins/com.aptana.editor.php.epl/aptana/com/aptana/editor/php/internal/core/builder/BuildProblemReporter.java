/*******************************************************************************
 * Copyright (c) 2008 xored software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package com.aptana.editor.php.internal.core.builder;

import java.net.URI;
import java.text.MessageFormat;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org2.eclipse.dltk.compiler.problem.CategorizedProblem;
import org2.eclipse.dltk.compiler.problem.DefaultProblem;
import org2.eclipse.dltk.compiler.problem.IProblem;
import org2.eclipse.dltk.compiler.problem.ProblemCollector;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.resources.IUniformResource;
import com.aptana.core.resources.MarkerUtils;
import com.aptana.core.util.EclipseUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.php.epl.PHPEplPlugin;

/**
 * A problem reporter that can work on a workspace resource or on an external resource, {@link IUniformResource}.
 */
public class BuildProblemReporter extends ProblemCollector
{

	/*
	 * This can hold an IResource or an IUniformResource, in case the resource is out of workspace.
	 */
	private final Object resource;
	private boolean oldMarkersDeleted = false;
	private boolean isExternal;
	private String resourceLocation;

	/**
	 * Constructs a new BuildProblemReporter for a given resource.
	 * 
	 * @param resource
	 *            Can be {@link IResource} or an {@link IUniformResource}
	 */
	public BuildProblemReporter(Object resource)
	{
		this.isExternal = (resource instanceof IUniformResource);
		if (isExternal || resource instanceof IResource)
		{
			this.resource = resource;
		}
		else
		{
			throw new IllegalArgumentException(
					"The given resource is expected to be an IResource or an IUniformResource"); //$NON-NLS-1$
		}
		resourceLocation = StringUtil.EMPTY;
	}

	private static ISchedulingRule getMarkerRule(Object resource)
	{
		if (resource instanceof IResource)
		{
			return ResourcesPlugin.getWorkspace().getRuleFactory().markerRule((IResource) resource);
		}
		return null;
	}

	public void flush()
	{
		// Performance fix: schedules the error handling as a single workspace update so that we don't trigger a
		// bunch of resource updated events while problem markers are being added to the file.
		IWorkspaceRunnable runnable = new IWorkspaceRunnable()
		{
			public void run(IProgressMonitor monitor)
			{
				updateMarkers();
			}
		};
		try
		{
			ResourcesPlugin.getWorkspace().run(runnable, getMarkerRule(resource), IWorkspace.AVOID_UPDATE,
					new NullProgressMonitor());
		}
		catch (CoreException e)
		{
			IdeLog.logWarning(PHPEplPlugin.getDefault(),
					MessageFormat.format(Messages.BuildProblemReporter_UpdateMarkersError, resourceLocation), e,
					PHPEplPlugin.DEBUG_SCOPE);
		}

	}

	private void updateMarkers()
	{
		try
		{
			if (EclipseUtil.isTesting())
			{
				return;
			}
			IResource workspaceResource = null;
			IUniformResource externalResource = null;
			if (isExternal)
			{
				externalResource = (IUniformResource) resource;
				URI uri = externalResource.getURI();
				if (uri != null)
				{
					resourceLocation = uri.getPath();
				}
			}
			else
			{
				workspaceResource = (IResource) resource;
				if (workspaceResource == null || !workspaceResource.isAccessible())
				{
					IdeLog.logWarning(PHPEplPlugin.getDefault(),
							"BuildProblemReporter::flush -> Unexpected null or non-accessible resource"); //$NON-NLS-1$
					return;
				}
				resourceLocation = workspaceResource.getLocation().toString();
			}
			if (!oldMarkersDeleted)
			{
				oldMarkersDeleted = true;
				if (isExternal)
				{
					MarkerUtils.deleteMarkers(externalResource, DefaultProblem.MARKER_TYPE_PROBLEM, true);
					MarkerUtils.deleteMarkers(externalResource, DefaultProblem.MARKER_TYPE_TASK, true);
				}
				else
				{
					workspaceResource.deleteMarkers(DefaultProblem.MARKER_TYPE_PROBLEM, true, IResource.DEPTH_INFINITE);
					workspaceResource.deleteMarkers(DefaultProblem.MARKER_TYPE_TASK, true, IResource.DEPTH_INFINITE);
				}
			}
			for (final IProblem problem : problems)
			{
				final String markerType;
				if (problem instanceof CategorizedProblem)
				{
					markerType = ((CategorizedProblem) problem).getMarkerType();
				}
				else
				{
					markerType = DefaultProblem.MARKER_TYPE_PROBLEM;
				}
				IMarker m = null;
				if (isExternal)
				{
					m = MarkerUtils.createMarker(externalResource, null, markerType);
					// Make sure we don't persist this marker on an external file.
					m.setAttribute(IMarker.TRANSIENT, true);
				}
				else
				{
					m = workspaceResource.createMarker(markerType);
				}
				if (m == null || !m.exists())
				{
					IdeLog.logError(PHPEplPlugin.getDefault(), "Error creating a PHP marker", PHPEplPlugin.DEBUG_SCOPE); //$NON-NLS-1$
				}
				if (problem.getSourceLineNumber() >= 0)
				{
					m.setAttribute(IMarker.LINE_NUMBER, problem.getSourceLineNumber() + 1);
				}
				m.setAttribute(IMarker.MESSAGE, problem.getMessage());
				if (problem.getSourceStart() >= 0)
				{
					m.setAttribute(IMarker.CHAR_START, problem.getSourceStart());
				}
				if (problem.getSourceEnd() >= 0)
				{
					m.setAttribute(IMarker.CHAR_END, problem.getSourceEnd());
				}
				if (DefaultProblem.MARKER_TYPE_PROBLEM.equals(markerType))
				{
					int severity = IMarker.SEVERITY_INFO;
					if (problem.isError())
					{
						severity = IMarker.SEVERITY_ERROR;
					}
					else if (problem.isWarning())
					{
						severity = IMarker.SEVERITY_WARNING;
					}
					m.setAttribute(IMarker.SEVERITY, severity);
				}
				else
				{
					m.setAttribute(IMarker.USER_EDITABLE, Boolean.FALSE);
					if (problem instanceof TaskInfo)
					{
						m.setAttribute(IMarker.PRIORITY, ((TaskInfo) problem).getPriority());
					}
				}
			}
		}
		catch (CoreException e)
		{
			IdeLog.logWarning(PHPEplPlugin.getDefault(),
					MessageFormat.format(Messages.BuildProblemReporter_UpdateMarkersError, resourceLocation), e,
					PHPEplPlugin.DEBUG_SCOPE);
		}
		finally
		{
			// in any case, clear the problems
			problems.clear();
		}
	}
}
