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
 * DebuggerErrorNotification.java
 *
 */

package org2.eclipse.php.internal.debug.core.zend.debugger.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org2.eclipse.php.debug.core.debugger.messages.IDebugNotificationMessage;
import org2.eclipse.php.internal.debug.core.zend.communication.CommunicationUtilities;

/**
 * @author erez
 */
public class DebuggerErrorNotification extends DebugMessageNotificationImpl implements IDebugNotificationMessage {

	private int errorLevel = 0;
	private String errorText;

	public int getErrorLevel() {
		return this.errorLevel;
	}

	public void setErrorLevel(int errorLevel) {
		this.errorLevel = errorLevel;
	}

	public String getErrorText() {
		return this.errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}

	public void deserialize(DataInputStream in) throws IOException {
		setErrorLevel(in.readInt());
		setErrorText(CommunicationUtilities.readString(in));
	}

	public int getType() {
		return 2007;
	}

	public void serialize(DataOutputStream out) throws IOException {
		out.writeShort(getType());
		out.writeInt(getErrorLevel());
		CommunicationUtilities.writeString(out, getErrorText());
	}
}