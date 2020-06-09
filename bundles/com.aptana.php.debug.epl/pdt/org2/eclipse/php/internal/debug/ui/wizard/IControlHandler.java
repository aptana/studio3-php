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
package org2.eclipse.php.internal.debug.ui.wizard;

import org.eclipse.jface.resource.ImageDescriptor;

public interface IControlHandler {

	/**
	 * Updates the control error messages and buttons.
	 */
	public void update();

	/**
	 * Sets the title of this control (if exists).
	 *  
	 * @param title the title of the control
	 */
	public void setTitle(String title);

	/**
	 * Sets the control's description (if exists).
	 * 
	 * @param desc the control's description
	 */
	public void setDescription(String desc);

	/**
	 * The control's image descriptor.
	 * 
	 * @param image the control's image descriptor
	 */
	public void setImageDescriptor(ImageDescriptor image);

	/**
	 * Set an error or warning message.
	 * 
	 * @param newMessage the new message
	 * @param newType the new type, from IStatus
	 */
	public void setMessage(String newMessage, int newType);
}
