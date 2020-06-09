/*******************************************************************************
 * Copyright (c) 2008 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.zend.testConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * A singleton controller to be the central point to notify of Debug Server tests results.
 * @author yaronm
 */
public class DebugServerTestController {

	private static DebugServerTestController instance = null;
	private List<IDebugServerTestListener> listeners = new ArrayList<IDebugServerTestListener>(1);

	private DebugServerTestController() {
	}

	public static DebugServerTestController getInstance() {
		if (instance == null) {
			instance = new DebugServerTestController();
		}
		return instance;
	}

	public synchronized void notifyTestListener(DebugServerTestEvent event) {
		for (IDebugServerTestListener listener : listeners) {
			listener.testEventReceived(event);
		}
	}

	public synchronized void addListener(IDebugServerTestListener listener) {
		listeners.add(listener);
	}

	public synchronized void removeListener(IDebugServerTestListener listener) {
		listeners.remove(listener);
	}
}
