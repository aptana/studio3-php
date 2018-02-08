/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.validation;

import com.aptana.editor.php.core.preferences.TaskTag;

public class Task extends TaskTag
{

	private final int start;
	private final int end;
	private final int lineNumber;
	private final String description;

	public Task(String tag,String description, int priority,int start,int end,int lineNumber)
	{
		super(tag, priority);
		this.start=start;
		this.end=end;
		this.description=description;
		this.lineNumber=lineNumber;
	}

	public String getDescription()
	{
		return description;
	}

	public int getStart()
	{
		return start;
	}

	public int getEnd()
	{
		return end;
	}

	public int getLineNumber()
	{
		return lineNumber;
	}

}
