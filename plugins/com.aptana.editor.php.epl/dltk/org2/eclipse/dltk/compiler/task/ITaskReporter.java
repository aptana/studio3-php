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
package org2.eclipse.dltk.compiler.task;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * The interface to create <i>tasks</i> for the attached resource. At any given
 * moment this object operates on single {@link IResource}.
 */
public interface ITaskReporter extends IAdaptable {

	/**
	 * Creates new task for the attached resource.
	 * 
	 * @param message
	 * @param lineNumber
	 * @param priority
	 * @param charStart
	 * @param charEnd
	 */
	void reportTask(String message, int lineNumber, int priority,
			int charStart, int charEnd);

}
