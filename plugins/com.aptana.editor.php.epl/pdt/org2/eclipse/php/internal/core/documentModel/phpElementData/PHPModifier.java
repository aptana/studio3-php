/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.core.documentModel.phpElementData;

import org2.eclipse.php.core.compiler.PHPFlags;

/**
 * @deprecated Use {@link PHPFlags}
 */
public class PHPModifier {

	/**
	 * The <code>int</code> value representing the <code>public</code> modifier.
	 */
	public static final int PUBLIC = 0x00000001;

	/**
	 * The <code>int</code> value representing the <code>private</code> modifier.
	 */
	public static final int PRIVATE = 0x00000002;

	/**
	 * The <code>int</code> value representing the <code>protected</code> modifier.
	 */
	public static final int PROTECTED = 0x00000004;

	/**
	 * The <code>int</code> value representing the <code>static</code> modifier.
	 */
	public static final int STATIC = 0x00000008;

	/**
	 * The <code>int</code> value representing the <code>final</code> modifier.
	 */
	public static final int FINAL = 0x00000010;

	/**
	 * The <code>int</code> value representing the <code>interface</code> modifier.
	 */
	public static final int INTERFACE = 0x00000200;

	/**
	 * The <code>int</code> value representing the <code>abstract</code> modifier.
	 */
	public static final int ABSTRACT = 0x00000400;

	/**
	 * The <code>int</code> value representing a <code>named constant</code> (created by a 'define') modifier.
	 */
	public static final int NAMED_CONSTANT = 0x00000800;

	/**
	 * Return <tt>true</tt> if the integer argument includes the
	 * <tt>public</tt> modifer, <tt>false</tt> otherwise.
	 *
	 * @param 	mod a set of modifers
	 * @return <tt>true</tt> if <code>mod</code> includes the
	 * <tt>public</tt> modifier; <tt>false</tt> otherwise.
	 */
	public static boolean isPublic(int mod) {
		return (mod & PUBLIC) != 0;
	}

	/**
	 * Return <tt>true</tt> if the integer argument includes the
	 * <tt>private</tt> modifer, <tt>false</tt> otherwise.
	 *
	 * @param 	mod a set of modifers
	 * @return <tt>true</tt> if <code>mod</code> includes the
	 * <tt>private</tt> modifier; <tt>false</tt> otherwise.
	 */
	public static boolean isPrivate(int mod) {
		return (mod & PRIVATE) != 0;
	}

	/**
	 * Return <tt>true</tt> if the integer argument includes the
	 * <tt>protected</tt> modifer, <tt>false</tt> otherwise.
	 *
	 * @param 	mod a set of modifers
	 * @return <tt>true</tt> if <code>mod</code> includes the
	 * <tt>protected</tt> modifier; <tt>false</tt> otherwise.
	 */
	public static boolean isProtected(int mod) {
		return (mod & PROTECTED) != 0;
	}

	/**
	 * Return <tt>true</tt> if the integer argument includes the
	 * <tt>static</tt> modifer, <tt>false</tt> otherwise.
	 *
	 * @param 	mod a set of modifers
	 * @return <tt>true</tt> if <code>mod</code> includes the
	 * <tt>static</tt> modifier; <tt>false</tt> otherwise.
	 */
	public static boolean isStatic(int mod) {
		return (mod & STATIC) != 0;
	}

	/**
	 * Return <tt>true</tt> if the integer argument includes the
	 * <tt>final</tt> modifer, <tt>false</tt> otherwise.
	 *
	 * @param 	mod a set of modifers
	 * @return <tt>true</tt> if <code>mod</code> includes the
	 * <tt>final</tt> modifier; <tt>false</tt> otherwise.
	 */
	public static boolean isFinal(int mod) {
		return (mod & FINAL) != 0;
	}
	
	/**
	 * Return <tt>true</tt> if the integer argument includes the
	 * <tt>constant</tt> modifer, <tt>false</tt> otherwise.
	 * A named constant in PHP is defined by a 'define' call in the code.
	 * 
	 * @param 	mod a set of modifers
	 * @return <tt>true</tt> if <code>mod</code> includes the
	 * <tt>const</tt> modifier; <tt>false</tt> otherwise.
	 */
	public static boolean isNamedConstant(int mod) {
		return (mod & NAMED_CONSTANT) != 0;
	}

	/**
	 * Return <tt>true</tt> if the integer argument includes the
	 * <tt>interface</tt> modifer, <tt>false</tt> otherwise.
	 *
	 * @param 	mod a set of modifers
	 * @return <tt>true</tt> if <code>mod</code> includes the
	 * <tt>interface</tt> modifier; <tt>false</tt> otherwise.
	 */
	public static boolean isInterface(int mod) {
		return (mod & INTERFACE) != 0;
	}

	/**
	 * Return <tt>true</tt> if the integer argument includes the
	 * <tt>abstract</tt> modifer, <tt>false</tt> otherwise.
	 *
	 * @param 	mod a set of modifers
	 * @return <tt>true</tt> if <code>mod</code> includes the
	 * <tt>abstract</tt> modifier; <tt>false</tt> otherwise.
	 */
	public static boolean isAbstract(int mod) {
		return (mod & ABSTRACT) != 0;
	}

	@SuppressWarnings("nls")
	public static String toString(int mod) {
		StringBuffer sb = new StringBuffer();

		if ((mod & PUBLIC) != 0) {
			sb.append("public ");
		}
		if ((mod & PROTECTED) != 0) {
			sb.append("protected ");
		}
		if ((mod & PRIVATE) != 0) {
			sb.append("private ");
		}

		//Canonical order
		if ((mod & ABSTRACT) != 0) {
			sb.append("abstract ");
		}
		if ((mod & STATIC) != 0) {
			sb.append("static ");
		}
		if ((mod & FINAL) != 0) {
			sb.append("final ");
		}
		if ((mod & INTERFACE) != 0) {
			sb.append("interface ");
		}

		int len;
		if ((len = sb.length()) > 0) { /* trim trailing space */
			return sb.toString().substring(0, len - 1);
		}
		return "";
	}

}