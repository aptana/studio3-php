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
package org.eclipse.dltk.internal.core.builder;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.compiler.problem.CategorizedProblem;
import org.eclipse.dltk.compiler.problem.DefaultProblem;
import org.eclipse.dltk.compiler.problem.IProblem;
import org.eclipse.dltk.compiler.problem.ProblemCollector;

import com.aptana.editor.php.epl.PHPEplPlugin;

public class BuildProblemReporter extends ProblemCollector {

	private final IResource resource;
	private boolean oldMarkersDeleted = false;

	/**
	 * @param resource
	 */
	public BuildProblemReporter(IResource resource) {
		this.resource = resource;
	}

	public void flush() {
		try {
			if (!oldMarkersDeleted) {
				oldMarkersDeleted = true;
				resource.deleteMarkers(DefaultProblem.MARKER_TYPE_PROBLEM,
						true, IResource.DEPTH_INFINITE);
				resource.deleteMarkers(DefaultProblem.MARKER_TYPE_TASK, true,
						IResource.DEPTH_INFINITE);
			}
			for (final IProblem problem : problems) {
				final String markerType;
				if (problem instanceof CategorizedProblem) {
					markerType = ((CategorizedProblem) problem).getMarkerType();
				} else {
					markerType = DefaultProblem.MARKER_TYPE_PROBLEM;
				}
				final IMarker m = resource.createMarker(markerType);
				if (problem.getSourceLineNumber() >= 0) {
					m.setAttribute(IMarker.LINE_NUMBER, problem
							.getSourceLineNumber() + 1);
				}
				m.setAttribute(IMarker.MESSAGE, problem.getMessage());
				if (problem.getSourceStart() >= 0) {
					m
							.setAttribute(IMarker.CHAR_START, problem
									.getSourceStart());
				}
				if (problem.getSourceEnd() >= 0) {
					m.setAttribute(IMarker.CHAR_END, problem.getSourceEnd());
				}
				if (DefaultProblem.MARKER_TYPE_PROBLEM.equals(markerType)) {
					int severity = IMarker.SEVERITY_INFO;
					if (problem.isError()) {
						severity = IMarker.SEVERITY_ERROR;
					} else if (problem.isWarning()) {
						severity = IMarker.SEVERITY_WARNING;
					}
					m.setAttribute(IMarker.SEVERITY, severity);
				} else {
					m.setAttribute(IMarker.USER_EDITABLE, Boolean.FALSE);
					if (problem instanceof TaskInfo) {
						m.setAttribute(IMarker.PRIORITY, ((TaskInfo) problem)
								.getPriority());
					}
				}
//				if (problem.getID() != 0) {
//					m.setAttribute(IScriptModelMarker.ID, problem.getID());
//				}
//				final String[] arguments = problem.getArguments();
//				if (arguments != null && arguments.length != 0) {
//					m.setAttribute(IScriptModelMarker.ARGUMENTS, Util
//							.getProblemArgumentsForMarker(arguments));
//				}
			}
			problems.clear();
		} catch (CoreException e) {
			PHPEplPlugin.logError("Error updating markers", e); //$NON-NLS-1$
		}
	}
}
