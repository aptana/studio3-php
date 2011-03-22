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

import com.aptana.editor.php.epl.PHPEplPlugin;

public interface ITodoTaskPreferences {

	public static final String CASE_SENSITIVE = PHPEplPlugin.PLUGIN_ID
			+ "tasks.case_sensitive"; //$NON-NLS-1$
	public static final String TAGS = PHPEplPlugin.PLUGIN_ID + "tasks.tags"; //$NON-NLS-1$
	public static final String ENABLED = PHPEplPlugin.PLUGIN_ID + "tasks.enabled"; //$NON-NLS-1$

	/**
	 * Checks if the tags are enabled
	 * 
	 * @return
	 */
	boolean isEnabled();

	/**
	 * Checks if the tags are case sensitive
	 * 
	 * @return
	 */
	boolean isCaseSensitive();

	/**
	 * returns task tags
	 * 
	 * @return list of {@link TodoTask}
	 */
	List<TodoTask> getTaskTags();

	/**
	 * returns just the names of the tags
	 * 
	 * @return
	 */
	String[] getTagNames();

}
