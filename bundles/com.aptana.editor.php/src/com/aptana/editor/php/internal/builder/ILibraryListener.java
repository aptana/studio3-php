/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import java.util.Set;

/**
 * @author Pavel Petrochenko
 */
public interface ILibraryListener
{

	public void librariesChanged(Set<IPHPLibrary> turnedOn, Set<IPHPLibrary> turnedOf);

	public void userLibrariesChanged(UserLibrary[] newLibraries);

}
