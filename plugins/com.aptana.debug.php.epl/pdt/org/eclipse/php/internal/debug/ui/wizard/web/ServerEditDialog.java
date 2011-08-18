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
package org.eclipse.php.internal.debug.ui.wizard.web;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.php.internal.debug.ui.wizard.CompositeFragment;
import org.eclipse.php.internal.debug.ui.wizard.ICompositeFragmentFactory;
import org.eclipse.php.internal.debug.ui.wizard.IControlHandler;
import org.eclipse.php.internal.debug.ui.wizard.WizardFragmentsFactoryRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org2.eclipse.php.util.SWTUtil;

import com.aptana.debug.php.epl.PHPDebugEPLPlugin;
import com.aptana.ui.util.SWTUtils;
import com.aptana.webserver.core.AbstractWebServerConfiguration;

public class ServerEditDialog extends TitleAreaDialog implements IControlHandler {

	protected static final String FRAGMENT_GROUP_ID = "org.eclipse.php.server.ui.serverWizardAndComposite";
	private AbstractWebServerConfiguration server;
	private ArrayList runtimeComposites;
	private SelectionListener tabsListener;

	/**
	 * Instantiate a new server edit dialog.
	 *
	 * @param parentShell the parent SWT shell
	 * @param server An assigned IServer
	 */
	public ServerEditDialog(Shell parentShell, AbstractWebServerConfiguration server) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.server = server;
		runtimeComposites = new ArrayList(3);
	}

	protected Control createDialogArea(Composite parent) {
		// Create a tabbed container that will hold all the fragments
		CTabFolder tabs = SWTUtil.createTabFolder(parent);
		ICompositeFragmentFactory[] factories = WizardFragmentsFactoryRegistry.getFragmentsFactories(FRAGMENT_GROUP_ID);
		for (ICompositeFragmentFactory element : factories) {
			CTabItem tabItem = new CTabItem(tabs, SWT.BORDER);
			CompositeFragment fragment = element.createComposite(tabs, this);
			fragment.setData(server);
			tabItem.setText(fragment.getDisplayName());
			tabItem.setControl(fragment);
			runtimeComposites.add(fragment);
		}

		getShell().setText("Edit Server");
		getShell().setImage(SWTUtils.getImage(PHPDebugEPLPlugin.getDefault(), "/icons/full/obj16/server_run.gif")); //$NON-NLS-1$

		tabsListener = new TabsSelectionListener();
		tabs.addSelectionListener(tabsListener);
		return tabs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		Iterator composites = runtimeComposites.iterator();
		while (composites.hasNext()) {
			((CompositeFragment) composites.next()).performCancel();
		}
		super.cancelPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		Iterator composites = runtimeComposites.iterator();
		while (composites.hasNext()) {
			((CompositeFragment) composites.next()).performOk();
		}
		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.php.internal.server.apache.ui.IControlHandler#setDescription(java.lang.String)
	 */
	public void setDescription(String desc) {
		super.setMessage(desc);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.php.internal.server.apache.ui.IControlHandler#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public void setImageDescriptor(ImageDescriptor image) {
		super.setTitleImage(image.createImage());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.php.internal.server.apache.ui.IControlHandler#update()
	 */
	public void update() {
		Button button = getButton(IDialogConstants.OK_ID);
		if (button != null) {
			Iterator composites = runtimeComposites.iterator();
			while (composites.hasNext()) {
				if (!((CompositeFragment) composites.next()).isComplete()) {
					button.setEnabled(false);
					return;
				}
			}
			button.setEnabled(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#setMessage(java.lang.String, int)
	 */
	public void setMessage(String newMessage, int newType) {
		// Override the WARNING with an INFORMATION.
		// We have a bug that cause the warning to be displayed in all the tabs and not
		// only in the selected one. (TODO - Fix this)
		if (newType == IMessageProvider.WARNING) {
			newType = IMessageProvider.INFORMATION;
		}
		super.setMessage(newMessage, newType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.php.internal.server.apache.ui.IControlHandler#getServer()
	 */
	public AbstractWebServerConfiguration getServer() {
		return server;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.php.internal.server.apache.ui.IControlHandler#setServer(org.eclipse.wst.server.core.IServer)
	 */
	public void setServer(AbstractWebServerConfiguration server) {
		this.server = server;
	}

	private class TabsSelectionListener implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent e) {
			// Do nothing
		}

		public void widgetSelected(SelectionEvent e) {
			CTabItem item = (CTabItem)e.item;
			CompositeFragment fragment = (CompositeFragment)item.getControl();
			setTitle(fragment.getTitle());
			setDescription(fragment.getDescription());
		}

	}
}
