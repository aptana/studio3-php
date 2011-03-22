/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
/*
 * (c) 2002, 2005 xored software and others all rights reserved. http://www.xored.com
 */

package org2.eclipse.dltk.ast.references;

public class SimpleReference extends Reference {

	protected String fName;

	public SimpleReference(int start, int end, String name) {
		super(start, end);
		this.fName = name;
	}

	public String getName() {
		return fName;
	}

	public void setName(String name) {
		this.fName = name;
	}

	public String getStringRepresentation() {
		return fName;
	}

	public String toString() {
		return this.fName;
	}
}
