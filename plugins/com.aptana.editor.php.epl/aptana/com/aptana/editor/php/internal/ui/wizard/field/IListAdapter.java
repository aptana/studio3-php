/**
 * Copyright (c) 2005-2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.ui.wizard.field;

import org.eclipse.php.internal.ui.wizard.field.ListDialogField;


/**
 * Change listener used by <code>ListDialogField</code> and <code>CheckedListDialogField</code>
 */
@SuppressWarnings("unchecked")
public interface IListAdapter {
	
	/**
	 * A button from the button bar has been pressed.
	 * @param field 
	 * @param index 
	 */
	void customButtonPressed(ListDialogField field, int index);
	
	/**
	 * The selection of the list has changed.
	 * @param field 
	 */	
	void selectionChanged(ListDialogField field);
	
	/**
	 * En entry in the list has been double clicked
	 * @param field 
	 */
	void doubleClicked(ListDialogField field);	

}
