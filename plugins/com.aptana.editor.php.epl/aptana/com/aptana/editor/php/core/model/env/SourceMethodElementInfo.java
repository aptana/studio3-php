/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package com.aptana.editor.php.core.model.env;



public class SourceMethodElementInfo extends MemberElementInfo implements
		ISourceMethod {

	/**
	 * For a source method (that is, a method contained in a source module) this
	 * is a collection of the names of the parameters for this method, in the
	 * order the parameters are declared.
	 */
	private String[] argumentNames;
	private String[] argumentInitializers;
	private boolean isConstructor;

	public void setArgumentNames(String[] names) {
		this.argumentNames = names;
	}

	public String[] getArgumentNames() {
		return this.argumentNames;
	}

	public void setArgumentInializers(String[] initializers) {
		this.argumentInitializers = initializers;
	}

	public String[] getArgumentInitializers() {
		return this.argumentInitializers;
	}

	public void setIsConstructor(boolean isConstructor) {
		this.isConstructor = isConstructor;
	}

	public boolean isConstructor() {
		return isConstructor;
	}

	public char[][] getExceptionTypeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public char[] getReturnTypeName() {
		// TODO Auto-generated method stub
		return null;
	}

	public char[][][] getTypeParameterBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	public char[][] getTypeParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}
}
