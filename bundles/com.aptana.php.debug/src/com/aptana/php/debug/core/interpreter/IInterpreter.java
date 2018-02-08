/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.interpreter;

import java.io.File;

/**
 * Abstract interpreter.
 * 
 * @author Denis Denisenko
 */
public interface IInterpreter
{
	/**
	 * Gets interpreter executable.
	 * 
	 * @return interpreter executable.
	 */
	File getExecutable();

	/**
	 * Gets interpreter debugger ID.
	 * 
	 * @return interpreter debugger ID.
	 */
	String getDebuggerID();

	/**
	 * Gets INI file location.
	 * 
	 * @return INI file location.
	 */
	File getINILocation();
}
