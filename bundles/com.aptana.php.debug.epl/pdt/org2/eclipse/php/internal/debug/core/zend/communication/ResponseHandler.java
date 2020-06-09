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
/*
 * ResponseHandler.java
 *
 * Created on January 4, 2001, 11:28 AM
 */

package org2.eclipse.php.internal.debug.core.zend.communication;

/**
 *
 * @author  eran
 * @version
 */
public interface ResponseHandler {

    public void handleResponse(Object request, Object response);

}
