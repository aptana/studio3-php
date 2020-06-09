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

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org2.eclipse.php.internal.debug.core.interpreter.preferences.PHPexeItem;
import org2.eclipse.php.internal.debug.ui.wizard.FragmentedWizard;
import org2.eclipse.php.internal.debug.ui.wizard.ICompositeFragmentFactory;
import org2.eclipse.php.internal.debug.ui.wizard.WizardFragment;
import org2.eclipse.php.internal.debug.ui.wizard.WizardFragmentsFactoryRegistry;
import org2.eclipse.php.internal.debug.ui.wizard.WizardModel;

public class PHPExeWizard extends FragmentedWizard implements INewWizard {

	public static final String MODEL = "phpExe"; //$NON-NLS-1$
	protected static final String FRAGMENT_GROUP_ID = "org2.eclipse.php.debug.ui.phpExeWizardCompositeFragment";
	private PHPexeItem[] existingItems;

	public PHPExeWizard(PHPexeItem[] existingItems) {
		this(existingItems, "Add new PHP Executable");
	}

	public PHPExeWizard(PHPexeItem[] existingItems, String title, WizardModel taskModel){
		super(title, null, taskModel);
		this.existingItems = existingItems;
		setRootFragment(createRootFragment());
	}

	public PHPExeWizard(PHPexeItem[] existingItems, String title) {
		super(title, null);
		this.existingItems = existingItems;
		setRootFragment(createRootFragment());
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.ui.wizards.FragmentedWizard#needsPreviousAndNextButtons()
	 */
	public boolean needsPreviousAndNextButtons() {
		return false;
	}

	private WizardFragment createRootFragment() {
		WizardFragment fragment = new WizardFragment() {
			WizardFragment[] children;

			@SuppressWarnings("unchecked")
			protected void createChildFragments(List list) {
				if (children != null) {
					loadChildren(children, list);
					return;
				}
				ICompositeFragmentFactory[] factories = WizardFragmentsFactoryRegistry.getFragmentsFactories(FRAGMENT_GROUP_ID);
				children = new WizardFragment[factories.length];
				for (int i = 0; i < factories.length; i++) {
					children[i] = factories[i].createWizardFragment();
					if (children[i] instanceof IPHPExeCompositeFragment) {
						((IPHPExeCompositeFragment)children[i]).setExistingItems(existingItems);
					}
				}
				loadChildren(children, list);
			}
		};
		return fragment;
	}

	@SuppressWarnings("unchecked")
	private void loadChildren(WizardFragment[] children, List list) {
		for (WizardFragment element : children) {
			list.add(element);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// Do nothing
	}
}