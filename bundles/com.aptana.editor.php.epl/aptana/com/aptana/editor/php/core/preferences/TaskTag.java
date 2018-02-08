package com.aptana.editor.php.core.preferences;

import org.eclipse.core.resources.IMarker;

/**
 * Simple representation of the values that make up a Task Tag
 */
public class TaskTag {

	public static final int PRIORITY_HIGH = IMarker.PRIORITY_HIGH;
	public static final int PRIORITY_LOW = IMarker.PRIORITY_LOW;
	public static final int PRIORITY_NORMAL = IMarker.PRIORITY_NORMAL;

	/**
	 * this task tag's priority
	 */
	private int fPriority = PRIORITY_NORMAL;
	
	/**
	 * this task tag's "tagging" text
	 */
	private String fTag = null;

	public TaskTag(String tag, int priority) {
		super();
		fTag = tag;
		fPriority = priority;
	}

	public int getPriority() {
		return fPriority;
	}

	public String getTag() {
		return fTag;
	}

	public String toString() {
		return getTag() + ":" + getPriority(); //$NON-NLS-1$
	}
}
