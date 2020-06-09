/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.xdebug;

import org2.eclipse.php.internal.debug.core.xdebug.breakpoints.PdtLayer;

public class IDELayerFactory {
	private static PdtLayer layer = new PdtLayer();

	public static IDELayer getIDELayer() {
		return layer;
	}

}
