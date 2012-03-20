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
package org2.eclipse.php.internal.core.util;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;

/**
 * @author guy.g
 */
public class ElementCreationProxy {
	IConfigurationElement element;
	String extensionPointName;
	Object elementObject;
	
	public ElementCreationProxy(IConfigurationElement element, String extensionPointName) {
		this.element = element;
		this.extensionPointName = extensionPointName;
	}
	
	public Object getObject() {
		if (elementObject == null) {
			SafeRunner.run(new SafeRunnable("Error creation " + element.getName() + "for extension-point " + extensionPointName) { //$NON-NLS-1$ //$NON-NLS-2$
				public void run() throws Exception {
					elementObject = element.createExecutableExtension("class"); //$NON-NLS-1$
				}
			});
		}
		return elementObject;
	}
}