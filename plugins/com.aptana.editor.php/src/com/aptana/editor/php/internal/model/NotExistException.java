/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.model;

import com.aptana.editor.php.core.model.IModelElement;

/**
 * Exception indicating model element does not exist any more.
 * 
 * @author Denis Denisenko
 */
public class NotExistException extends ModelException
{
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = -1267587426345890835L;

	public NotExistException(IModelElement element)
	{
		super("Model element " + element + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
