/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
/*
 * Created on 14.11.2004
 * 
 * TODO To change the template for this generated file go to Window - Preferences - Script - Code
 * Style - Code Templates
 */
package org2.eclipse.dltk.ast.references;


public abstract class Reference {
	protected int sourceStart;
	protected int sourceEnd;

	protected Reference() {
		super();
	}

	protected Reference(int sourceStart, int sourceEnd) {
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}

	public abstract String getStringRepresentation();
}
