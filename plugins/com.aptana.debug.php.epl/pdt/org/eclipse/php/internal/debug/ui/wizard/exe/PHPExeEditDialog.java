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
package org.eclipse.php.internal.debug.ui.wizard.exe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.php.internal.debug.core.interpreter.preferences.PHPexeItem;
import org.eclipse.php.internal.debug.ui.wizard.CompositeFragment;
import org.eclipse.php.internal.debug.ui.wizard.ICompositeFragmentFactory;
import org.eclipse.php.internal.debug.ui.wizard.IControlHandler;
import org.eclipse.php.internal.debug.ui.wizard.WizardFragmentsFactoryRegistry;
import org.eclipse.php.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class PHPExeEditDialog extends TitleAreaDialog implements IControlHandler
{

	protected static final String FRAGMENT_GROUP_ID = "org.eclipse.php.debug.ui.phpExeWizardCompositeFragment";
	private List<CompositeFragment> runtimeComposites;
	private PHPexeItem phpExeItem;
	private PHPexeItem[] existingItems;
	private final boolean shouldValidate;

	/**
	 * Constructs a new PHP executables edit dialog.
	 * 
	 * @param shell
	 * @param phpExeItem
	 * @param existingItems
	 */
	public PHPExeEditDialog(Shell shell, PHPexeItem phpExeItem, PHPexeItem[] existingItems)
	{
		this(shell, phpExeItem, existingItems, false);
	}

	/**
	 * Constructs a new PHP executables edit dialog.
	 * 
	 * @param shell
	 * @param phpExeItem
	 * @param existingItems
	 * @param shouldValidate Indicate whether to validate the PHP ini when the dialog is displayed.
	 */
	public PHPExeEditDialog(Shell shell, PHPexeItem phpExeItem, PHPexeItem[] existingItems, boolean shouldValidate)
	{
		super(shell);
		this.setShellStyle(this.getShellStyle() | SWT.RESIZE);
		this.existingItems = existingItems;
		this.phpExeItem = phpExeItem;
		this.runtimeComposites = new ArrayList<CompositeFragment>(3);
		this.shouldValidate = shouldValidate;
	}

	/**
	 * Returns true if the PHP ini should validate on initialization.
	 * 
	 * @return True if the PHP ini should validate on initialization; False otherwise.
	 */
	public boolean shouldValidate()
	{
		return shouldValidate;
	}

	public void setDescription(String desc)
	{
		super.setMessage(desc);
	}

	public PHPexeItem[] getExistingItems()
	{
		return this.existingItems;
	}

	public void setPHPExeItem(PHPexeItem phpExeItem)
	{
		this.phpExeItem = phpExeItem;
	}

	public PHPexeItem getPHPExeItem()
	{
		return this.phpExeItem;
	}

	public void setImageDescriptor(ImageDescriptor image)
	{
		super.setTitleImage(image.createImage());
	}

	protected Control createDialogArea(Composite parent)
	{
		// Create a tabbed container that will hold all the fragments
		CTabFolder tabs = SWTUtil.createTabFolder(parent);
		ICompositeFragmentFactory[] factories = WizardFragmentsFactoryRegistry
				.getFragmentsFactories(PHPExeEditDialog.FRAGMENT_GROUP_ID);
		for (ICompositeFragmentFactory element : factories)
		{
			CTabItem tabItem = new CTabItem(tabs, SWT.BORDER);
			CompositeFragment fragment = element.createComposite(tabs, this);
			fragment.setData(this.phpExeItem);
			if (fragment instanceof IPHPExeCompositeFragment)
			{
				((IPHPExeCompositeFragment) fragment).setExistingItems(this.existingItems);
			}
			tabItem.setText(fragment.getDisplayName());
			tabItem.setControl(fragment);
			this.runtimeComposites.add(fragment);
		}
		this.getShell().setText("Edit PHP Executable");
		tabs.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				CTabItem item = (CTabItem) e.item;
				CompositeFragment fragment = (CompositeFragment) item.getControl();
				PHPExeEditDialog.this.setTitle(fragment.getTitle());
				PHPExeEditDialog.this.setDescription(fragment.getDescription());
			}
		});
		return tabs;
	}

	protected void cancelPressed()
	{
		Iterator<CompositeFragment> composites = this.runtimeComposites.iterator();
		while (composites.hasNext())
		{
			composites.next().performCancel();
		}
		super.cancelPressed();
	}

	protected void okPressed()
	{
		Iterator<CompositeFragment> composites = this.runtimeComposites.iterator();
		while (composites.hasNext())
		{
			composites.next().performOk();
		}
		super.okPressed();
	}

	public void update()
	{
		Button button = this.getButton(IDialogConstants.OK_ID);
		if (button != null)
		{
			Iterator<CompositeFragment> composites = this.runtimeComposites.iterator();
			while (composites.hasNext())
			{
				if (!composites.next().isComplete())
				{
					button.setEnabled(false);
					return;
				}
			}
			button.setEnabled(true);
		}
	}

	public void setMessage(String newMessage, int newType)
	{
		// Override the WARNING with an INFORMATION.
		// We have a bug that cause the warning to be displayed in all the tabs and not
		// only in the selected one. (TODO - Fix this)
		if (newType == IMessageProvider.WARNING)
		{
			newType = IMessageProvider.INFORMATION;
		}
		super.setMessage(newMessage, newType);
	}
}
