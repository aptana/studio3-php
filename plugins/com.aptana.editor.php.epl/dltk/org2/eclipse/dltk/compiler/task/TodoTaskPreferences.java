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

import org.eclipse.core.runtime.Preferences;

/**
 * Implementation of the {@link ITodoTaskPreferences} backed by
 * {@link Preferences}
 */
@SuppressWarnings("deprecation")
public class TodoTaskPreferences extends AbstractTodoTaskPreferences implements
		ITodoTaskPreferences {

	private Preferences store;

	public TodoTaskPreferences(Preferences store) {
		this.store = store;
	}

	public boolean isEnabled() {
		return store.getBoolean(ENABLED);
	}

	public boolean isCaseSensitive() {
		return store.getBoolean(CASE_SENSITIVE);
	}

	protected String getRawTaskTags() {
		return store.getString(TAGS);
	}

	/**
	 * @deprecated
	 */
	public void setTaskTags(List<TodoTask> elements) {
		store.setValue(TAGS, TaskTagUtils.encodeTaskTags(elements));
	}

	/**
	 * @deprecated use {@link TaskTagUtils#initializeDefaultValues(Preferences)}
	 */
	public static void initializeDefaultValues(Preferences store) {
		TaskTagUtils.initializeDefaultValues(store);
	}
}
