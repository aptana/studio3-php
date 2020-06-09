/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.xdebug.dbgp.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;


public class SimpleIntValue extends DBGpElement implements IValue {
	private int currentValue;
	private int wantedValue;
	private IVariable[] vars = new IVariable[0];

	public SimpleIntValue(int currentValue, int wantedValue, IDebugTarget debugTarget) {
		super(debugTarget);
		this.currentValue = currentValue;
		this.wantedValue = wantedValue;
	}

	public String getReferenceTypeName() throws DebugException {
		return "int";
	}

	public String getValueString() throws DebugException {
		//TODO: cache
		if (currentValue == wantedValue) {
			return Integer.toString(currentValue);
		}
		else {
			return Integer.toString(currentValue) + " (" + Integer.toString(wantedValue)+ ")";
		}
	}

	public IVariable[] getVariables() throws DebugException {
		return vars;
	}

	public boolean hasVariables() throws DebugException {
		return false;
	}

	public boolean isAllocated() throws DebugException {
		return false;
	}

	public ILaunch getLaunch() {
		return getDebugTarget().getLaunch();
	}
}
