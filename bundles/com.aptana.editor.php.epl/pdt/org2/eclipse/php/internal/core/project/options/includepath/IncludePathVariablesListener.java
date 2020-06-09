/**
 * Copyright (c) 2006 Zend Technologies
 * 
 */
package org2.eclipse.php.internal.core.project.options.includepath;

import org.eclipse.core.runtime.IPath;

public interface IncludePathVariablesListener
{

	void includePathVariablesChanged(String[] names, IPath[] paths);

}