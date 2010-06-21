package org.eclipse.php.internal.core;

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
}
