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

import org.eclipse.php.internal.debug.ui.wizard.CompositeWizardFragment;
import org.eclipse.php.internal.debug.ui.wizard.IWizardHandle;
import org.eclipse.php.internal.debug.ui.wizard.WizardControlWrapper;
import org.eclipse.php.internal.debug.ui.wizard.WizardModel;
import org.eclipse.swt.widgets.Composite;

import com.aptana.debug.php.epl.PHPDebugEPLPlugin;
import com.aptana.webserver.core.AbstractWebServerConfiguration;

/**
 * @author michael
 */
public class PathMapperWizardFragment extends CompositeWizardFragment {

	private PathMapperCompositeFragment compositeFragment;
	private AbstractWebServerConfiguration server;

	public Composite getComposite() {
		return compositeFragment;
	}

	public Composite createComposite(Composite parent, IWizardHandle handle) {
		compositeFragment = new PathMapperCompositeFragment(parent, new WizardControlWrapper(handle), false);
		return compositeFragment;
	}

	public void enter() {
		if (compositeFragment != null) {
			try {
				server = (AbstractWebServerConfiguration) getWizardModel().getObject(WizardModel.SERVER);
				if (server != null) {
					compositeFragment.setData(server);
				}
			} catch (Exception e) {
				PHPDebugEPLPlugin.logError(e);
			}
		} else {
			PHPDebugEPLPlugin.logError("Could not display the Servers wizard (component is null)."); //$NON-NLS-1$
		}
	}

	public boolean isComplete() {
		if (compositeFragment == null) {
			return super.isComplete();
		}
		return super.isComplete() && compositeFragment.isComplete();
	}

	public void exit() {
		try {
			if (compositeFragment != null) {
				compositeFragment.performOk();
			}
		} catch (Exception e) {
		}
	}
}
