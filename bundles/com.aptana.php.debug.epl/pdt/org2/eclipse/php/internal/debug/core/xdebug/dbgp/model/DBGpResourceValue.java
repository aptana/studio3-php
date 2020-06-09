/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import org.w3c.dom.Node;

public class DBGpResourceValue extends DBGpValue {

	public DBGpResourceValue(DBGpVariable owningVariable, Node property) {
		super(owningVariable);
		setModifiable(false);
		simpleParseNode(property);
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.xdebug.core.dbgp.model.DBGpValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return DBGpVariable.PHP_RESOURCE;
	}

	void genValueString(String data) {
		if (data != null && data.trim().length() > 0) {
			setValueString(data);
		} else {
			setValueString("");
		}
	}

}
