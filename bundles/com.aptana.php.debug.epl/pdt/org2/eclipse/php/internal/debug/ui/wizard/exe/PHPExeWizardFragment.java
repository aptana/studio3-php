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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org2.eclipse.php.internal.debug.core.interpreter.preferences.PHPexeItem;
import org2.eclipse.php.internal.debug.core.preferences.PHPexes;
import org2.eclipse.php.internal.debug.ui.wizard.CompositeWizardFragment;
import org2.eclipse.php.internal.debug.ui.wizard.IWizardHandle;
import org2.eclipse.php.internal.debug.ui.wizard.WizardControlWrapper;
import org2.eclipse.php.internal.debug.ui.wizard.WizardModel;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

public class PHPExeWizardFragment extends CompositeWizardFragment implements IPHPExeCompositeFragment {

	private PHPExeCompositeFragment compositeFragment;
	private PHPexeItem phpExeItem;
	private PHPexeItem[] existingItems;

	public Composite getComposite() {
		return compositeFragment;
	}

	public Composite createComposite(Composite parent, IWizardHandle handle) {
		compositeFragment = new PHPExeCompositeFragment(parent, new WizardControlWrapper(handle), false);
		return compositeFragment;
	}

	public void enter() {
		if (compositeFragment != null) {
			try {
				phpExeItem = (PHPexeItem) getWizardModel().getObject(PHPExeWizard.MODEL);
				if (phpExeItem == null) {
					phpExeItem = new PHPexeItem();
				}
				compositeFragment.setData(phpExeItem);
				compositeFragment.setExistingItems(existingItems);
			} catch (Exception e) {
				PHPDebugEPLPlugin.logError(e);
			}
		} else {
			PHPDebugEPLPlugin.logError("Could not display the PHPExeItems wizard (component is null)."); //$NON-NLS-1$
		}
	}

	public boolean isComplete() {
		if (compositeFragment == null) {
			return super.isComplete();
		}
		return super.isComplete() && compositeFragment.isComplete();
	}

	public void exit() {
		if (compositeFragment != null) {
			WizardModel model = getWizardModel();
			model.putObject(PHPExeWizard.MODEL, compositeFragment.getPHPExeItem());
		}
	}

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		super.performFinish(monitor);
		if (compositeFragment != null) {
			compositeFragment.performOk();
		}
	}

	public void performCancel(IProgressMonitor monitor) throws CoreException {
		super.performCancel(monitor);
		// Clear any added server
		if (getWizardModel().getObject(PHPExeWizard.MODEL) != null) {
			getWizardModel().putObject(PHPExeWizard.MODEL, null);
			PHPexes.getInstance().save();
		}
	}

	public void setExistingItems(PHPexeItem[] existingItems) {
		this.existingItems = existingItems;
	}
}