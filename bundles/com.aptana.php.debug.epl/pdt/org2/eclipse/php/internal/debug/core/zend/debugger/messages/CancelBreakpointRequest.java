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
/*
 * CancelBreakpointRequest.java
 *
 */

package org2.eclipse.php.internal.debug.core.zend.debugger.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org2.eclipse.php.debug.core.debugger.messages.IDebugRequestMessage;

/**
 * Request to remove a break point.
 *
 * @author guy
 */
public class CancelBreakpointRequest extends DebugMessageRequestImpl implements IDebugRequestMessage {

	private int breakpointId;

	/**
	 * Sets the Break Point id.
	 */
	public void setBreakpointID(int id) {
		this.breakpointId = id;
	}

	/**
	 * Returns the Break Point id.
	 */
	public int getBreakpointID() {
		return this.breakpointId;
	}

	public void deserialize(DataInputStream in) throws IOException {
		setID(in.readInt());
		setBreakpointID(in.readInt());
	}

	public int getType() {
		return 22;
	}

	public void serialize(DataOutputStream out) throws IOException {
		out.writeShort(getType());
		out.writeInt(getID());
		out.writeInt(getBreakpointID());
	}

}