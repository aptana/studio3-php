/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.search;

/**
 * @author Pavel Petrochenko
 */
public interface IElementNode
{

	/**
	 * CLASS
	 */
	final int CLASS = 1;

	/**
	 * FUNCTION
	 */
	final int FUNCTION = 2;

	/**
	 * CONSTANT
	 */
	final int CONSTANT = 3;

	/**
	 * TRAIT
	 */
	final int TRAIT = 4;

	/**
	 * @return name
	 */
	String getName();

	/**
	 * @return modifiers
	 */
	int getModifiers();

	/**
	 * @return path
	 */
	String getPath();

	/**
	 * @return reference suitable for opening in editor
	 */
	ExternalReference toExternalReference();

	/**
	 * @return kind of the node
	 */
	int getKind();
}
