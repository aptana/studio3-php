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
package org2.eclipse.php.internal.debug.ui.wizard.exe;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org2.eclipse.php.debug.core.debugger.pathmapper.PathMapper;
import org2.eclipse.php.internal.debug.core.interpreter.preferences.PHPexeItem;
import org2.eclipse.php.internal.debug.core.pathmapper.PathMapperRegistry;
import org2.eclipse.php.internal.debug.ui.pathmapper.PathMappingComposite;
import org2.eclipse.php.internal.debug.ui.wizard.CompositeFragment;
import org2.eclipse.php.internal.debug.ui.wizard.IControlHandler;

import com.aptana.editor.php.epl.PHPEplPlugin;

/**
 * @author michael
 */
public class PathMapperCompositeFragment extends CompositeFragment {

	private PathMappingComposite pathMapperComposite;

	public PathMapperCompositeFragment(Composite parent, IControlHandler handler, boolean isForEditing) {
		super(parent, handler, isForEditing);
		controlHandler.setTitle("PHP Executable Path Mapping");
		controlHandler.setDescription("Specify mapping between PHP executable relative and local paths");
		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(PHPEplPlugin.PLUGIN_ID, "/icons/full/wizban/phpexe_wiz.gif"); //$NON-NLS-1$
		controlHandler.setImageDescriptor(imageDescriptor);
		setDisplayName("Path Mapping");
		setTitle("Edit PHP Executable Path Mapping");
		setDescription("Configure PHP Executable Path Mapping");
		if (isForEditing) {
			setData(((PHPExeEditDialog) controlHandler).getPHPExeItem());
		}
		createControl(isForEditing);
	}

	/**
	 * Create the page
	 */
	protected void createControl(boolean isForEditing) {
		//set layout for this composite (whole page)
		GridLayout pageLayout = new GridLayout();
		setLayout(pageLayout);

		Composite composite = new Composite(this, SWT.NONE);
		pageLayout.numColumns = 1;
		composite.setLayout(pageLayout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(data);

		pathMapperComposite = new PathMappingComposite(composite, SWT.NONE);
		data = new GridData(GridData.FILL_BOTH);
		pathMapperComposite.setLayoutData(data);

		Dialog.applyDialogFont(this);

		init();
		validate();
	}

	protected void init() {
		if (pathMapperComposite == null || pathMapperComposite.isDisposed()) {
			return;
		}
		PHPexeItem phpExeItem = getPHPExeItem();
		if (phpExeItem != null) {
			PathMapper pathMapper = PathMapperRegistry.getByPHPExe(phpExeItem);
			if (pathMapper != null) {
				pathMapperComposite.setData(pathMapper.getMapping());
			}
		}
	}

	protected void validate() {
		setMessage(getDescription(), IMessageProvider.NONE);
		setComplete(true);
		controlHandler.update();
	}

	protected void setMessage(String message, int type) {
		controlHandler.setMessage(message, type);
		setComplete(type != IMessageProvider.ERROR);
		controlHandler.update();
	}

	public boolean performOk() {
		PHPexeItem phpExeItem = getPHPExeItem();
		if (phpExeItem != null) {
			PathMapper pathMapper = PathMapperRegistry.getByPHPExe(phpExeItem);
			pathMapper.setMapping(pathMapperComposite.getMappings());
			PathMapperRegistry.storeToPreferences();
		}
		return true;
	}

	public void setData(Object phpExeItem) {
		if (phpExeItem != null && !(phpExeItem instanceof PHPexeItem)) {
			throw new IllegalArgumentException("The given object is not a PHPExeItem");
		}
		super.setData(phpExeItem);
		init();
		validate();
	}

	public PHPexeItem getPHPExeItem() {
		return (PHPexeItem) getData();
	}
}
