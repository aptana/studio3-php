/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package com.aptana.editor.php.core.model;

import java.util.Set;

import com.aptana.editor.php.core.model.IType;


/**
 * A listener which gets notified when a particular type hierarchy object
 * changes.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface ITypeHierarchyChangedListener {
	
	/**
	 * Notifies that type hierarchy changed.
	 * @param changedTypes - types changed.
	 */
	void typeHierarchyChanged(Set<IType> changedTypes);
}
