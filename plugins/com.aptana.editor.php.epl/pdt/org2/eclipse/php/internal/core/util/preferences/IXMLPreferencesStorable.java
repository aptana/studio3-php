/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.core.util.preferences;

import java.util.Map;

/**
 * The IXMLPreferencesStorable should be implemented by any class that should save or load its properties as XML. This
 * class works with the XMLPreferencesWriter and XMLPreferencesReader.
 */
public interface IXMLPreferencesStorable
{

	/**
	 * Returns hash map, that represent this object.
	 * 
	 * @return HashMap
	 */
	public Map storeToMap();

	/**
	 * Restores the object from the map.
	 * 
	 * @param HashMap
	 *            map
	 */
	public void restoreFromMap(Map map);
}
