/**
 * Copyright (c) 2005-2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.search;


/**
 * @author Pavel Petrochenko
 *
 */
public interface IElementNode
{
	
	/**
	 * CLASS
	 */
	final int CLASS=1;
	
	/**
	 * FUNCTION
	 */
	final int FUNCTION=2;
	
	/**
	 * CONSTANT
	 */
	final int CONSTANT=3;

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
