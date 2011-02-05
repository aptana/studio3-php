/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.core.builder;

import java.util.List;

/**
 * Listener to the build paths changes.
 * 
 * @author Denis Denisenko
 */
public interface IBuildPathsListener
{
	/**
	 * Notifies build path modules structure or contents changed.
	 * 
	 * @param added
	 *            - added build paths.
	 * @param removed
	 *            - removed build paths.
	 */
	void changed(List<IBuildPath> added, List<IBuildPath> removed);
}
