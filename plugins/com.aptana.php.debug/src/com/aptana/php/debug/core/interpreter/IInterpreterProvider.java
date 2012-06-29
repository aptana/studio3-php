/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.interpreter;

import java.util.List;

/**
 * Provider of interpreters.
 * 
 * @author Denis Denisenko
 */
public interface IInterpreterProvider
{
	/**
	 * Gets interpreters.
	 * 
	 * @return interpreters.
	 */
	List<IInterpreter> getInterpreters();

	/**
	 * Gets default interpreter.
	 * 
	 * @return default interpreter, might be null if no default interpreter is configured.
	 */
	IInterpreter getDefaultInterpreter();
}
