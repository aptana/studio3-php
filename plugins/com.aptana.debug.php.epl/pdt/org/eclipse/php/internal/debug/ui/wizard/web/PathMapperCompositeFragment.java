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
package org.eclipse.php.internal.debug.ui.wizard.web;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.php.debug.core.debugger.pathmapper.PathMapper;
import org.eclipse.php.debug.core.debugger.pathmapper.PathMapper.Mapping;
import org.eclipse.php.internal.core.util.ScrolledCompositeImpl;
import org.eclipse.php.internal.debug.core.pathmapper.PathMapperRegistry;
import org.eclipse.php.internal.debug.ui.pathmapper.PathMappingComposite;
import org.eclipse.php.internal.debug.ui.wizard.CompositeFragment;
import org.eclipse.php.internal.debug.ui.wizard.IControlHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.webserver.core.AbstractWebServerConfiguration;

/**
 * @author michael
 */
public class PathMapperCompositeFragment extends CompositeFragment {

	private PathMappingComposite pathMapperComposite;

	public PathMapperCompositeFragment(Composite parent, IControlHandler handler, boolean isForEditing) {
		super(parent, handler, isForEditing);
		controlHandler.setTitle("Server Path Mapping");
		controlHandler.setDescription("Specify mapping between server and local paths");
		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(PHPEplPlugin.PLUGIN_ID, "/icons/full/wizban/server_wiz.gif"); //$NON-NLS-1$
		controlHandler.setImageDescriptor(imageDescriptor);
		setDisplayName("Path Mapping");
		setTitle("Edit Server Path Mapping");
		setDescription("Configure Server Path Mapping");
		createControl(isForEditing);
	}

	/**
	 * Create the page
	 */
	protected void createControl(boolean isForEditing) {
		//set layout for this composite (whole page)
		GridLayout pageLayout = new GridLayout();
		setLayout(pageLayout);
		Label label = new Label(this, SWT.WRAP);
		label.setText("Specify mapping between the server and the workspace paths. \nChanges made here affect all the launch configurations that use the selected server.");
		// SG: Aptana mod - Use scrolled composite
		ScrolledCompositeImpl scrolledComposite = new ScrolledCompositeImpl(this, SWT.H_SCROLL | SWT.V_SCROLL);
		GridData data = new GridData(GridData.FILL_BOTH);
		scrolledComposite.setLayoutData(data);
		
		pathMapperComposite = new PathMappingComposite(scrolledComposite, SWT.NONE);
		data = new GridData(GridData.FILL_BOTH);
		pathMapperComposite.setLayoutData(data);
		
		scrolledComposite.setContent(pathMapperComposite);
		scrolledComposite.setLayout(new GridLayout());
		scrolledComposite.setFont(this.getFont());
		
		Point size = pathMapperComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolledComposite.setMinSize(size.x, size.y);
		data = new GridData(GridData.FILL_BOTH);
		scrolledComposite.setLayoutData(data);
		
		Dialog.applyDialogFont(this);
		init();
		validate();
	}
	/**
	 * Add a property change listener that will be notified when mappings were added, modified or delete.
	 * The returned id for the properyChange event can be: {@value PathMappingComposite#IDX_ADD}, {@value PathMappingComposite#IDX_EDIT} or {@value PathMappingComposite#IDX_REMOVE}.
	 * 
	 * @param listener
	 */
	public void addPropertyChangeListener(IPropertyListener listener) {
		if (pathMapperComposite != null) {
			pathMapperComposite.addPropertyChangeListener(listener);
		}
	}
	
	/**
	 * Remove a property change listener.
	 * @param listener
	 */
	public void removePropertyChangeListener(IPropertyListener listener) {
		if (pathMapperComposite != null) {
			pathMapperComposite.removePropertyChangeListener(listener);
		}
	}
	
	protected void init() {
		if (pathMapperComposite == null || pathMapperComposite.isDisposed()) {
			return;
		}
		AbstractWebServerConfiguration server = getServer();
		pathMapperComposite.setServerIsValid(server != null);
		if (server != null) {
			PathMapper pathMapper = PathMapperRegistry.getByServer(server);
			pathMapperComposite.setData(pathMapper.getMapping());
		} else {
			pathMapperComposite.setData(null);
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
		AbstractWebServerConfiguration server = getServer();
		if (server != null) {
			PathMapper pathMapper = PathMapperRegistry.getByServer(server);
			pathMapper.setMapping(pathMapperComposite.getMappings());
			PathMapperRegistry.storeToPreferences();
		}
		return true;
	}

	/**
	 * Override the super setData to handle only Server types.
	 *
	 * @throws IllegalArgumentException if the given object is not a {@link AbstractWebServerConfiguration}
	 */
	public void setData(Object server) {
		if (server != null && !(server instanceof AbstractWebServerConfiguration)) {
			throw new IllegalArgumentException("The given object is not a Server");
		}
		super.setData(server);
		init();
		validate();
	}

	public Mapping[] getMappings() {
		return pathMapperComposite.getMappings();
	}

	/**
	 * Returns the Server that is attached to this fragment.
	 *
	 * @return The attached Server.
	 */
	public AbstractWebServerConfiguration getServer() {
		return (AbstractWebServerConfiguration) getData();
	}
}
