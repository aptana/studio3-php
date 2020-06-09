/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org2.eclipse.php.internal.core.corext;

/**
 * A source range defines an element's source coordinates relative to its source
 * buffer.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface ISourceRange {

	/**
	 * Returns the number of characters of the source code for this element,
	 * relative to the source buffer in which this element is contained.
	 * 
	 * @return the number of characters of the source code for this element,
	 *         relative to the source buffer in which this element is contained
	 */
	int getLength();

	/**
	 * Returns the 0-based index of the first character of the source code for
	 * this element, relative to the source buffer in which this element is
	 * contained.
	 * 
	 * @return the 0-based index of the first character of the source code for
	 *         this element, relative to the source buffer in which this element
	 *         is contained
	 */
	int getOffset();
}
