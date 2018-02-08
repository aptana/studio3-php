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
package org2.eclipse.php.internal.ui.wizard.field;


/**
 * Change listener used by <code>StringButtonDialogField</code>
 */
public interface IStringButtonAdapter
{

	/**
	 * @param field
	 */
	void changeControlPressed(DialogField field);

}
