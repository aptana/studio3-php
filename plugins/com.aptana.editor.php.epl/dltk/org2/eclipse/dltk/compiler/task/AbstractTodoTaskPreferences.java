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

import java.util.List;

public abstract class AbstractTodoTaskPreferences implements
		ITodoTaskPreferences {

	protected abstract String getRawTaskTags();

	public final List<TodoTask> getTaskTags() {
		return TaskTagUtils.decodeTaskTags(getRawTaskTags());
	}

	public final String[] getTagNames() {
		final List<TodoTask> taskTags = getTaskTags();
		final int size = taskTags.size();
		final String[] result = new String[size];
		for (int i = 0; i < size; ++i) {
			result[i] = taskTags.get(i).name;
		}
		return result;
	}

}
