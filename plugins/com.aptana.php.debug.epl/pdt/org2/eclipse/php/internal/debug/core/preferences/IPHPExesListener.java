/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.preferences;

import org2.eclipse.php.internal.debug.core.interpreter.preferences.PHPexeItem;


/**
 * A listener to events when adding/removing PHP Executables
 * @author yaronm, Shalom G.
 */
public interface IPHPExesListener {
	public void phpExeAdded(PHPExesEvent event);

	public void phpExeRemoved(PHPExesEvent event);

	public void phpExeDefaultChanged(PHPexeItem oldDefault, PHPexeItem newDefault);
}
