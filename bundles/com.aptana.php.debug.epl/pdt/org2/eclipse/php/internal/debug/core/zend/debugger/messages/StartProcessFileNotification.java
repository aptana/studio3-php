/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.zend.debugger.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org2.eclipse.php.debug.core.debugger.messages.IDebugNotificationMessage;
import org2.eclipse.php.internal.debug.core.zend.communication.CommunicationUtilities;

/**
 * This message is sent by Debugger when it starts processing a new file
 *
 * @author michael
 */
public class StartProcessFileNotification extends DebugMessageNotificationImpl implements IDebugNotificationMessage {

	private String fileName;

	public void deserialize(DataInputStream in) throws IOException {
		setFileName(CommunicationUtilities.readString(in));
	}

	public int getType() {
		return 2009;
	}

	public void serialize(DataOutputStream out) throws IOException {
		out.writeShort(getType());
		CommunicationUtilities.writeString(out, getFileName());
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
}
