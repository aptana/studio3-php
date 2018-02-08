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
package org2.eclipse.php.core.compiler;

import org2.eclipse.dltk.ast.Modifiers;

/**
 * This interface extends DLTK's {@link Modifiers} with PDT-related flags.
 * 
 */
public interface IPHPModifiers extends Modifiers {

	/**
	 * Represents non-PHP language element
	 */
	public static final int NonPhp = 1 << Modifiers.USER_MODIFIER;

	/**
	 * Element that has "@internal" in its PHPDoc
	 * 
	 * @deprecated
	 */
	public static final int Internal = 1 << (Modifiers.USER_MODIFIER + 1);

	/**
	 * Constructor method
	 */
	public static final int Constructor = 1 << (Modifiers.USER_MODIFIER + 2);

	public static final int USER_MODIFIER = Modifiers.USER_MODIFIER + 3;
	
	/**
	 * The <code>int</code> value representing a <code>named constant</code> (created by a 'define') modifier.
	 */
	public static final int NAMED_CONSTANT = Modifiers.USER_MODIFIER + 4;
}
