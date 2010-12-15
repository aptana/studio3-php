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
package org.eclipse.php.internal.debug.ui.views.variables;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.elements.adapters.DefaultVariableCellModifier;
import org.eclipse.debug.internal.ui.elements.adapters.VariableColumnPresentation;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.php.internal.debug.core.zend.model.PHPValue;
import org.eclipse.php.internal.debug.core.zend.model.PHPVariable;

import com.aptana.debug.php.epl.PHPDebugEPLPlugin;

/**
 * PHPVariableCellModifier is the cell modifier for the PHPVariables.
 * @author shalom
 *
 */
public class PHPVariableCellModifier extends DefaultVariableCellModifier implements ICellModifier {

	/**
	 * Overrides the default cell modifier to support PHPVariables. 
	 * (The method returns the PHPValue.getValue() instead of getValueString()).
	 */
	public Object getValue(Object element, String property) {
		if (VariableColumnPresentation.COLUMN_VARIABLE_VALUE.equals(property)) {
			if (element instanceof PHPVariable) {
				PHPVariable variable = (PHPVariable) element;
				try {
					if (variable.getValue() instanceof PHPValue) {
						return ((PHPValue) variable.getValue()).getValue();
					}
					return variable.getValue().getValueString();
				} catch (DebugException e) {
					PHPDebugEPLPlugin.logError(e);
				}
			}
		}
		return null;
	}
}
