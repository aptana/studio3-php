/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.core.documentModel.phpElementData;


import java.io.Serializable;

import org.eclipse.core.runtime.IAdaptable;

/**
 * The top level interface for all langauges code.
 */
@SuppressWarnings("rawtypes")
public interface CodeData extends Comparable, Serializable, IAdaptable {

	/**
	 * Returns the name of the CodeData.
	 * @return The name of the CodeData.
	 */
	String getName();

	/**
	 * Returns a description of the CodeData.
	 * @return Description of the CodeData.
	 */
	String getDescription();

	/**
	 * return true if this CodeData is user code
	 */
	boolean isUserCode();

	/**
	 * Returns the user data
	 * @return the user data
	 */
	IUserData getUserData();


}