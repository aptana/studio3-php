/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package com.aptana.editor.php.core.model.env;

public interface IGenericType {
	int getModifiers();

	/**
	 * Answer whether the receiver contains the resolved binary form or the
	 * unresolved source form of the type.
	 */
	boolean isBinaryType();
}
