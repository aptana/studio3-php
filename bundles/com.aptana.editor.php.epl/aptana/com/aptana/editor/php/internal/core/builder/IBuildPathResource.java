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
 * Build-path resource.
 * 
 * @author Denis Denisenko
 */
public interface IBuildPathResource
{
	/**
	 * Gets module full system path.
	 * 
	 * @return module full system path.
	 */
	String getFullPath();

	/**
	 * Gets module build path.
	 * 
	 * @return module build path.
	 */
	IBuildPath getBuildPath();

	/**
	 * Gets module short name.
	 * 
	 * @return module short name.
	 */
	String getShortName();

	/**
	 * Gets path inside the resource build-path, starting from the build-path root.
	 * 
	 * @return path.
	 */
	IPath getPath();
}
