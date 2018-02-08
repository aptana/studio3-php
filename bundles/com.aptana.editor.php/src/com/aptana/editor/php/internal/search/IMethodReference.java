/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
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

