/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.aptana.editor.php.core.model;

/**
 * Common protocol for model elements that have associated source code.
 */
public interface ISourceReference {
	/**
	 * Returns whether this element exists in the model.
	 *
	 * @return <code>true</code> if this element exists in the script model
	 *
	 */
	boolean exists();
			
	/**
	 * Returns the source range associated with this element.
	 * <p>
	 * For class files, this returns the range of the entire compilation unit 
	 * associated with the class file (if there is one).
	 * </p>
	 *
	 * @return the source range, or <code>null</code> if this element has no 
	 *   associated source code
	 */
	ISourceRange getSourceRange();
//	
//	/**
//	 * Returns the source code associated with this element.
//	 * This extracts the substring from the source buffer containing this source
//	 * element. This corresponds to the source range that would be returned by
//	 * <code>getSourceRange</code>.
//	 * <p>
//	 * For class files, this returns the source of the entire compilation unit 
//	 * associated with the class file (if there is one).
//	 * </p>
//	 *
//	 * @return the source code, or <code>null</code> if this element has no 
//	 *   associated source code
//	 * @exception ModelException if an exception occurs while accessing its corresponding resource
//	 */
//	String getSource() throws ModelException;
}
