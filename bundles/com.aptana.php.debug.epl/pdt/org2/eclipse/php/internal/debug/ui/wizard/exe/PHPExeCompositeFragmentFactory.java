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

import org.eclipse.swt.widgets.Composite;
import org2.eclipse.php.internal.debug.ui.wizard.CompositeFragment;
import org2.eclipse.php.internal.debug.ui.wizard.ICompositeFragmentFactory;
import org2.eclipse.php.internal.debug.ui.wizard.IControlHandler;
import org2.eclipse.php.internal.debug.ui.wizard.WizardFragment;

public class PHPExeCompositeFragmentFactory implements ICompositeFragmentFactory {

	public CompositeFragment createComposite(Composite parent, IControlHandler controlHandler) {
		return new PHPExeCompositeFragment(parent, controlHandler, true);
	}

	public WizardFragment createWizardFragment() {
		return new PHPExeWizardFragment();
	}
}
