/*******************************************************************************
 * Copyright (c) 2008 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.zend.testConnection;

/**
 * This represents an event which is created when a Debug Server Test completes.
 * Note : The result of event can be Success,Timeout OR Failure.
 * @author yaronm
 */
public class DebugServerTestEvent {
	/**
	 * A Success type event
	 */
	public final static int TEST_SUCCEEDED = 0;

	/**
	 * A Timeout type event which simply caused by a time out
	 */
	public final static int TEST_TIMEOUT = 1;

	/**
	 * A Failure Test event due to unsupported debuuger version
	 */
	public final static int TEST_FAILED_DEBUGER_VERSION = 2;

	private int fEventType;
	private String fSourceHost;
	private String fFailureMessage = ""; //$NON-NLS-1$

	public DebugServerTestEvent(String sourceHost, int eventType) {
		fSourceHost = sourceHost;
		fEventType = eventType;
	}

	/**
	 * Returns the event type.
	 * See DebugServerTestEvent constants types
	 * @return
	 */
	public int getEventType() {
		return fEventType;
	}

	/**
	 * The URL string representation of the source of event
	 * @return
	 */
	public String getSourceURL() {
		return fSourceHost;
	}
}
