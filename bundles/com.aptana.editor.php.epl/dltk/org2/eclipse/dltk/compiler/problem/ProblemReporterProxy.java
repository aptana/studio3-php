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
package org2.eclipse.dltk.compiler.problem;

/**
 * The {@link IProblemReporter} implementation which forwards all methods call
 * to the instance passed in the constructor.
 */
@SuppressWarnings("rawtypes")
public class ProblemReporterProxy implements IProblemReporter {

	private final IProblemReporter original;

	/**
	 * @param original
	 */
	protected ProblemReporterProxy(IProblemReporter original) {
		this.original = original;
	}

	public void reportProblem(IProblem problem) {
		if (original != null) {
			original.reportProblem(problem);
		}
	}

	public Object getAdapter(Class adapter) {
		if (original != null) {
			return original.getAdapter(adapter);
		}
		return null;
	}

}
