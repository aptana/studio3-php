/**
 * Copyright (c) 2005-2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
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
