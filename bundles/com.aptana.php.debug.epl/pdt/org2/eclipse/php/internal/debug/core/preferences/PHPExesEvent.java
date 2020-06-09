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
 * An Add/Remove PHP Executables event
 * @author yaronm
 */
public class PHPExesEvent {

	private PHPexeItem phpExeItem;

	public PHPExesEvent(PHPexeItem phpExeItem){
		this.phpExeItem = phpExeItem;
	}

	/**
	 * Returns the PHPExeItem that this event holds
	 * @return
	 */
	public PHPexeItem getPHPExeItem() {
		return phpExeItem;
	}
}
