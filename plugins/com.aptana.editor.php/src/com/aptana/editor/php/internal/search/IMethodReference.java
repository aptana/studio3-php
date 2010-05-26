/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.search;


/**
 * 
 * @author Pavel petrochenko
 *
 */
public interface IMethodReference {

	/**
	 * name
	 * @return
	 */
	String name();

	/**
	 * qualified name
	 * @return
	 */
	String getQualifiedName();
	
	/**
	 * 
	 * @return external reference suitable for opening editor or null
	 */
	ExternalReference toExternalReference();

	/**
	 * 
	 * @return true if is abstract
	 */
	boolean isAbstract();

	
}

