/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.core;

import org.eclipse.core.resources.IProjectNature;
import org2.eclipse.php.internal.core.project.options.PHPProjectOptions;

/**
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public interface IPHPNature extends IProjectNature
{
	/**
	 * Returns the {@link PHPProjectOptions} for the project of this nature.
	 * 
	 * @return {@link PHPProjectOptions}
	 */
	PHPProjectOptions getOptions();
}
