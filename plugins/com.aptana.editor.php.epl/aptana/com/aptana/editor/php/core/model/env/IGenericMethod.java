/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package com.aptana.editor.php.core.model.env;

public interface IGenericMethod {
	int getModifiers();
	
	boolean isConstructor();

	/**
	 * Answer the names of the argument or null if the argument names are not
	 * available.
	 */
	String[] getArgumentNames();
}
