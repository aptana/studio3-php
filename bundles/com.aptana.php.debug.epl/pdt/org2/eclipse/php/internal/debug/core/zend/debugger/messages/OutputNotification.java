/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
/*
 * OutputNotification.java
 *
 * Created on June 15, 2000, 12:55 PM
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
public class OutputNotification extends DebugMessageNotificationImpl implements IDebugNotificationMessage {

	private String output = null;

	public String getOutput() {
		return output;
	}

	public void setOutput(String outputText) {
		this.output = outputText;
	}

	public void deserialize(DataInputStream in) throws IOException {
		setOutput(CommunicationUtilities.readEncodedString(in, getTransferEncoding()));
	}

	public int getType() {
		return 2004;
	}

	public void serialize(DataOutputStream out) throws IOException {
		out.writeShort(getType());
		CommunicationUtilities.writeEncodedString(out, getOutput(), getTransferEncoding());
	}
}