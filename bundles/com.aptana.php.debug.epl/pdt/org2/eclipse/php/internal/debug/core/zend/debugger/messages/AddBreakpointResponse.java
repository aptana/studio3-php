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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org2.eclipse.php.debug.core.debugger.messages.IDebugResponseMessage;

/**
 * Response to a new break point.
 *
 * @author guy
 */
public class AddBreakpointResponse extends DebugMessageResponseImpl implements IDebugResponseMessage {

	private int breakPointID;

	/**
	 * Sets the Break Point id.
	 */
	public void setBreakpointID(int breakPointID) {
		this.breakPointID = breakPointID;
	}

	/**
	 * Returns the Break Point id.
	 */
	public int getBreakpointID() {
		return breakPointID;
	}

	public int getType() {
		return 1021;
	}

	public void deserialize(DataInputStream in) throws IOException {
		setID(in.readInt());
		setStatus(in.readInt());
		setBreakpointID(in.readInt());
	}

	public void serialize(DataOutputStream out) throws IOException {
		out.writeShort(getType());
		out.writeInt(getID());
		out.writeInt(getStatus());
		out.writeInt(getBreakpointID());
	}
}