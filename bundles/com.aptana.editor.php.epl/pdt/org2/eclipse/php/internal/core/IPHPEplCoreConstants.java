/*******************************************************************************
 * Copyright (c) 2009-2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *     Appcelerator Inc.
 *******************************************************************************/
package org2.eclipse.php.internal.core;

import com.aptana.editor.php.epl.PHPEplPlugin;

public interface IPHPEplCoreConstants
{
	public static final String PLUGIN_ID = PHPEplPlugin.PLUGIN_ID;

	// Task Tags
	public static final String TASK_PRIORITIES = PLUGIN_ID + ".taskPriorities"; //$NON-NLS-1$
	public static final String TASK_PRIORITY_HIGH = "HIGH"; //$NON-NLS-1$
	public static final String TASK_PRIORITY_LOW = "LOW"; //$NON-NLS-1$
	public static final String TASK_PRIORITY_NORMAL = "NORMAL"; //$NON-NLS-1$
	public static final String TASK_TAGS = PLUGIN_ID + ".taskTags"; //$NON-NLS-1$
	public static final String TASK_CASE_SENSITIVE = PLUGIN_ID + ".taskCaseSensitive"; //$NON-NLS-1$
	public static final String DEFAULT_TASK_TAGS = "TODO,FIXME,XXX,@todo"; //$NON-NLS-1$
	public static final String DEFAULT_TASK_PRIORITIES = "NORMAL,HIGH,NORMAL,NORMAL"; //$NON-NLS-1$
	public static final String ENABLED = "enabled"; //$NON-NLS-1$
	public static final String DISABLED = "disabled"; //$NON-NLS-1$

	// Code formatter
	public static final int DEFAULT_INDENTATION_SIZE = 1;
	public static final String FORMATTER_INDENTATION_SIZE = PLUGIN_ID + ".phpForamtterIndentationSize"; //$NON-NLS-1$
	public static final String FORMATTER_USE_TABS = PLUGIN_ID + ".phpForamtterUseTabs"; //$NON-NLS-1$

	// Code Assist
	/**
	 * Whether the code assist look at the PHP namespace to filter out results from other namespaces. This is useful
	 * when dealing with PHP frameworks that do their magic during runtime.
	 */
	public static final String STRICT_NS_CODE_ASSIST = PLUGIN_ID + ".strictNamespaceCodeAssist"; //$NON-NLS-1$
}
