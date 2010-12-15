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
package org.eclipse.php.internal.debug.ui.wizard.exe;

import org.eclipse.php.internal.debug.ui.wizard.CompositeFragment;
import org.eclipse.php.internal.debug.ui.wizard.ICompositeFragmentFactory;
import org.eclipse.php.internal.debug.ui.wizard.IControlHandler;
import org.eclipse.php.internal.debug.ui.wizard.WizardFragment;
import org.eclipse.swt.widgets.Composite;

/**
 * @author michael
 */
public class PathMapperFragmentFactory implements ICompositeFragmentFactory {

	public CompositeFragment createComposite(Composite parent, IControlHandler controlHandler) {
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IPHPHelpContextIds.ADDING_A_SERVER_LOCATION_PATH_MAP);
		return new PathMapperCompositeFragment(parent, controlHandler, true);
	}

	public WizardFragment createWizardFragment() {
		return new PathMapperWizardFragment();
	}
}