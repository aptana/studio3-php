/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.core.builder;

import org.eclipse.core.runtime.IPath;

/**
 * Directory.
 * 
 * @author Denis Denisenko
 */
public interface IDirectory extends IBuildPathResource
{
	/**
	 * Gets path of a module inside the directory from the root of build-path.
	 * 
	 * @param moduleName
	 *            - module name.
	 * @return module path.
	 */
	IPath getModulePath(String moduleName);
}
