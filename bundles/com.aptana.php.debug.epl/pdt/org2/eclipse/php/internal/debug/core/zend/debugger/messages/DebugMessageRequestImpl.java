/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.zend.debugger.messages;

import org2.eclipse.php.debug.core.debugger.messages.IDebugRequestMessage;

public abstract class DebugMessageRequestImpl extends DebugMessageImpl implements IDebugRequestMessage {

	private int id;

	/**
	 * Set the request id.
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * Return the request id.
	 */
	public int getID() {
		return this.id;
	}
}
