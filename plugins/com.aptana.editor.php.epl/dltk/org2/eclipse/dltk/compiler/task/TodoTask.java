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

public class TodoTask {
	public static final String PRIORITY_NORMAL = "NORMAL"; //$NON-NLS-1$
	public static final String PRIORITY_LOW = "LOW"; //$NON-NLS-1$
	public static final String PRIORITY_HIGH = "HIGH"; //$NON-NLS-1$

	public TodoTask() {
		// empty
	}

	public TodoTask(String name, String priority) {
		this.name = name;
		this.priority = priority;
	}

	public String name;
	public String priority;

}
