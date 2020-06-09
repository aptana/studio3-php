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

import org2.eclipse.php.debug.core.debugger.messages.IDebugRequestMessage;

public class SetProtocolRequest extends DebugMessageRequestImpl implements IDebugRequestMessage {

	private int fProtocolID;

	public void setProtocolID (int protocolID) {
		fProtocolID = protocolID;
	}

	public int getProtocolID() {
		return fProtocolID;
	}

	/* (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.messages.IDebugMessage#deserialize(java.io.DataInputStream)
	 */
	public void deserialize(DataInputStream in) throws IOException {
		setID(in.readInt());
		setProtocolID(in.readInt());
	}

	/* (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.messages.IDebugMessage#getType()
	 */
	public int getType() {
		return 10000;
	}

	/* (non-Javadoc)
	 * @see org2.eclipse.php.internal.debug.core.debugger.messages.IDebugMessage#serialize(java.io.DataOutputStream)
	 */
	public void serialize(DataOutputStream out) throws IOException {
		out.writeShort(getType());
		out.writeInt(getID());
		out.writeInt(getProtocolID());
	}
}
