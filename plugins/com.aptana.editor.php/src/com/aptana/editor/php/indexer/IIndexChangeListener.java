/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.indexer;

/**
 * @author Pavel Petrochenko
 *
 */
public interface IIndexChangeListener
{
	/**
	 * @param done
	 * @param message
	 */
	void stateChanged(boolean done, String message);
	
	/**
	 * Notifies that a global change is processed.
	 */
	void changeProcessed();
}
