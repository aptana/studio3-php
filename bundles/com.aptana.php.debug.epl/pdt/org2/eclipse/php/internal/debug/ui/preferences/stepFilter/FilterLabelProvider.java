/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.ui.preferences.stepFilter;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org2.eclipse.php.internal.debug.core.preferences.stepFilters.DebugStepFilter;
import org2.eclipse.php.internal.debug.core.preferences.stepFilters.IStepFilterTypes;
import org2.eclipse.php.internal.debug.ui.PHPDebugUIMessages;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.ui.PHPPluginImages;
import com.aptana.ui.util.SWTUtils;

/**
 * Label provider for Debug Step Filter objects
 * @author yaronm
 */
public class FilterLabelProvider extends LabelProvider implements ITableLabelProvider {

	/**
	 * @see ITableLabelProvider#getColumnText(Object, int)
	 */
	public String getColumnText(Object object, int column) {
		String text = ""; //$NON-NLS-1$
		if (column == 0) {
			DebugStepFilter filter = (DebugStepFilter) object;
			text = filter.getPath();
			if (filter.isReadOnly()) {
				text += PHPDebugUIMessages.FilterLabelProvider_readOnly;
			}
			return text;
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		String text = ""; //$NON-NLS-1$
		DebugStepFilter filter = (DebugStepFilter) element;
		text = filter.getPath();
		if (filter.isReadOnly()) {
			text += PHPDebugUIMessages.FilterLabelProvider_readOnly;
		}
		return text;
	}

	/**
	 * @see ITableLabelProvider#getColumnImage(Object, int)
	 */
	public Image getColumnImage(Object object, int column) {
		DebugStepFilter filter = (DebugStepFilter) object;
		switch (filter.getType()) {
			case IStepFilterTypes.PHP_PROJECT:
				return PHPPluginImages.get(PHPPluginImages.IMG_OBJS_PHP_PROJECT);
			case IStepFilterTypes.PHP_PROJECT_FOLDER:
				return PHPPluginImages.get(PHPPluginImages.IMG_OBJS_PHP_FOLDER);
			case IStepFilterTypes.PHP_PROJECT_FILE:
				return SWTUtils.getImage(PHPEditorPlugin.getDefault(), "/icons/full/obj16/php.png"); //$NON-NLS-1$

			case IStepFilterTypes.PHP_INCLUDE_PATH_LIBRARY:
			case IStepFilterTypes.PHP_INCLUDE_PATH_LIBRARY_FILE:
			case IStepFilterTypes.PHP_INCLUDE_PATH_LIBRARY_FOLDER:
				return PHPPluginImages.get(PHPPluginImages.IMG_OBJS_LIBRARY);

			case IStepFilterTypes.PHP_INCLUDE_PATH_VAR:
			case IStepFilterTypes.PHP_INCLUDE_PATH_VAR_FILE:
			case IStepFilterTypes.PHP_INCLUDE_PATH_VAR_FOLDER:
				return PHPPluginImages.get(PHPPluginImages.IMG_OBJS_ENV_VAR);
		}
		return null;
	}
}
