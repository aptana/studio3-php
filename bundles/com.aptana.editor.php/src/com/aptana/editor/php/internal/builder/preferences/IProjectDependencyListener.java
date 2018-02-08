/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder.preferences;

import org.eclipse.core.resources.IProject;

/**
 * IProjectDependencyListener
 * 
 * @author Denis Denisenko
 */
public interface IProjectDependencyListener
{
	/**
	 * Notifies project dependencies changed.
	 * 
	 * @param project
	 *            - project, which dependencies are changed.
	 * @param dependencies
	 *            - dependencies.
	 */
	void dependenciesChanged(IProject project, ProjectDependencies dependencies);
}
