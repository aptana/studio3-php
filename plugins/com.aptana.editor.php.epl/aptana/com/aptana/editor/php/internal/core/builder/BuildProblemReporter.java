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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org2.eclipse.dltk.compiler.problem.CategorizedProblem;
import org2.eclipse.dltk.compiler.problem.DefaultProblem;
import org2.eclipse.dltk.compiler.problem.IProblem;
import org2.eclipse.dltk.compiler.problem.ProblemCollector;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.resources.IUniformResource;
import com.aptana.core.resources.MarkerUtils;
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
	}

	public void flush()
	{
		try
		{
			IResource workspaceResource = null;
			IUniformResource externalResource = null;
			if (isExternal)
			{
				externalResource = (IUniformResource) resource;
			}
			else
			{
				workspaceResource = (IResource) resource;
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
				// if (problem.getID() != 0) {
				// m.setAttribute(IScriptModelMarker.ID, problem.getID());
				// }
				// final String[] arguments = problem.getArguments();
				// if (arguments != null && arguments.length != 0) {
				// m.setAttribute(IScriptModelMarker.ARGUMENTS, Util
				// .getProblemArgumentsForMarker(arguments));
				// }
			}
			problems.clear();
		}
		catch (CoreException e)
		{
			IdeLog.logError(PHPEplPlugin.getDefault(), "Error updating PHP error markers", e); //$NON-NLS-1$
		}
	}
}
