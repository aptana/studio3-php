/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.parser2;

import org.eclipse.core.resources.IProject;

import com.aptana.parsing.ParseState;

/**
 * @author Kevin Lindsey
 */
public class PHPParseState extends ParseState
{
	private IProject project;
	private Object owner;

	public Object getOwner()
	{
		return owner;
	}

	public void setOwner(Object owner)
	{
		this.owner = owner;
	}

	/**
	 * PHPParseState
	 */
	public PHPParseState()
	{
	}

	// public PHPParseState(IParseState parent)
	// {
	// super(PHPMimeType.MimeType, parent);
	// }
	//	

	/**
	 * @return project or null
	 */
	public IProject getProject()
	{
		return project;
	}

	/**
	 * sets project
	 * 
	 * @param project
	 */
	public void setProject(IProject project)
	{
		this.project = project;
	}
}
