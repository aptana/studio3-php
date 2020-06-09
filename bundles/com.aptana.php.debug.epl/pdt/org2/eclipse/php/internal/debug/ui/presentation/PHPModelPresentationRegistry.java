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
package org2.eclipse.php.internal.debug.ui.presentation;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.util.SafeRunnable;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * PHP debug model presentation registry.
 * 
 * @author shalom
 */
public class PHPModelPresentationRegistry {

	private static final String EXTENSION_POINT_NAME = "phpDebugModelPresentations"; //$NON-NLS-1$
	private static final String MODEL_PRESENTATION_TAG = "phpDebugModelPresentation"; //$NON-NLS-1$
	private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static final String VIEWER_CONFIGURATION_ATTRIBUTE = "detailsViewerConfiguration"; //$NON-NLS-1$

	/* Instance of this registry */
	private static PHPModelPresentationRegistry instance = null;
	private Dictionary debugModelPresentations = new Hashtable();
	private IDebugModelPresentation bestMatchPresentation;

	private PHPModelPresentationRegistry() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(PHPDebugEPLPlugin.PLUGIN_ID, EXTENSION_POINT_NAME);
		for (int i = 0; i < elements.length; i++) {
			final IConfigurationElement element = elements[i];
			if (MODEL_PRESENTATION_TAG.equals(element.getName())) {
				debugModelPresentations.put(element.getAttribute(ID_ATTRIBUTE), new DebugModelPresentationFactory(element));
			}
		}
	}

	private static PHPModelPresentationRegistry getInstance() {
		if (instance == null) {
			instance = new PHPModelPresentationRegistry();
		}
		return instance;
	}

	/**
	 * Returns the best match IDebugModelPresentation.
	 * @return An IDebugModelPresentation.
	 */
	public static IDebugModelPresentation getBestMatchPresentation() {
		PHPModelPresentationRegistry registry = getInstance();
		if (registry.bestMatchPresentation != null) {
			return registry.bestMatchPresentation;
		}
		try {
			DebugModelPresentationFactory bestModelPresentationFactory = null;
			Dictionary presentations = registry.debugModelPresentations;
			Enumeration e = presentations.elements();
			while (e.hasMoreElements()) {
				DebugModelPresentationFactory modelPresentationFactory = (DebugModelPresentationFactory) e.nextElement();
				if (PHPDebugEPLPlugin.PLUGIN_ID.equals(modelPresentationFactory.element.getNamespaceIdentifier())) {
					bestModelPresentationFactory = modelPresentationFactory;
				} else {
					registry.bestMatchPresentation = modelPresentationFactory.createParametersInitializer();
					return registry.bestMatchPresentation;
				}
			}
			if (bestModelPresentationFactory != null) {
				registry.bestMatchPresentation = bestModelPresentationFactory.createParametersInitializer();
				return registry.bestMatchPresentation;
			}
		} catch (Exception e) {
			PHPDebugEPLPlugin.logError(e);
		}
		return null;
	}

	private class DebugModelPresentationFactory {

		private IConfigurationElement element;
		private IDebugModelPresentation modelPresentation;

		public DebugModelPresentationFactory(IConfigurationElement element) {
			this.element = element;
		}

		public IDebugModelPresentation createParametersInitializer() {

			SafeRunner.run(new SafeRunnable("Error creation extension for extension-point org2.eclipse.php.internal.debug.core.phpDebugParametersInitializers") {
				public void run() throws Exception {
					modelPresentation = (IDebugModelPresentation) element.createExecutableExtension(CLASS_ATTRIBUTE);
				}
			});
			return modelPresentation;
		}
	}
}
