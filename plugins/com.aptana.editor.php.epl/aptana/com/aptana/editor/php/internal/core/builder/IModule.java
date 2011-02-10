/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.core.builder;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract module.
 * 
 * @author Denis Denisenko
 */
public interface IModule extends IBuildPathResource
{

	/**
	 * Gets module contents.
	 * 
	 * @return module contents.
	 */
	InputStream getContents() throws IOException;

	long getTimeStamp();
}
