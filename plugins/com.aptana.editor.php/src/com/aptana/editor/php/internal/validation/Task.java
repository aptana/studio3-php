/**
 * Copyright (c) 2005-2008 Aptana, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code, this entire header must remain intact.
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
