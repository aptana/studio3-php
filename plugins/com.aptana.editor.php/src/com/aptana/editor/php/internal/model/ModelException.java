/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.model;

/**
 * Model exception
 * 
 * @author Denis Denisenko
 */
public class ModelException extends Exception
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 510707111126366344L;

	/**
	 * ModelException constructor.
	 * 
	 * @param message
	 * @param cause
	 */
	public ModelException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * ModelException constructor.
	 * 
	 * @param message
	 */
	public ModelException(String message)
	{
		super(message);
	}

	/**
	 * ModelException constructor.
	 * 
	 * @param cause
	 */
	public ModelException(Throwable cause)
	{
		super(cause);
	}
}
