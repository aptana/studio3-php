/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Sebastian Davids - bug 128529
 * Semion Chichelnitsky (semion@il.ibm.com) - bug 278064
 *******************************************************************************/
package com.aptana.editor.php.internal.ui.dialog;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.php.internal.ui.dialog.messages"; //$NON-NLS-1$

	public static String FilteredItemsSelectionDialog_Cache;
	public static String FilteredItemsSelectionDialog_Error;
	public static String FilteredItemsSelectionDialog_Filter;
	public static String FilteredItemsSelectionDialog_FilteringJob0;
	public static String FilteredItemsSelectionDialog_FilteringJob1;
	public static String FilteredItemsSelectionDialog_GetFilteredElements;
	public static String FilteredItemsSelectionDialog_Menu;
	public static String FilteredItemsSelectionDialog_RefreshCache;
	public static String FilteredItemsSelectionDialog_RefreshingCache;
	public static String FilteredItemsSelectionDialog_RefreshItems;
	public static String FilteredItemsSelectionDialog_RefreshJob;
	public static String FilteredItemsSelectionDialog_RemoveHistoryItem;
	public static String FilteredItemsSelectionDialog_RemoveItems;
	public static String FilteredItemsSelectionDialog_Searching;
	public static String FilteredItemsSelectionDialog_SelectedItems;
	public static String FilteredItemsSelectionDialog_SelectItem;
	public static String FilteredItemsSelectionDialog_Temp0;
	public static String FilteredItemsSelectionDialog_Temp1;
	public static String FilteredItemsSelectionDialog_ToggleStatus;

	public static String ElementSelectionDialog_classes;
	public static String ElementSelectionDialog_constants;
	public static String ElementSelectionDialog_extraBarText;
	public static String ElementSelectionDialog_functions;

	public static String ElementSelectionDialog_traits;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
