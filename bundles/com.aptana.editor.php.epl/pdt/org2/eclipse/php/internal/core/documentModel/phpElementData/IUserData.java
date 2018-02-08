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

interface IUserData {

	/**
	 * Returns the name of the file.
	 */
	String getFileName();

	/**
	 * Returns the start position of the user data in the file.
	 */
	int getStartPosition();

	/**
	 * Returns the end position of the user data in the file.
	 */
	int getEndPosition();

	/**
	 * Returns the stop position of the user data in the file.
	 */
	int getStopPosition();

	/**
	 * Returns the stop line of the user data in the file.
	 */
	int getStopLine();

}