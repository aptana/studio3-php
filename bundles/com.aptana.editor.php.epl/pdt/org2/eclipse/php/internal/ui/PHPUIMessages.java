package org2.eclipse.php.internal.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PHPUIMessages {
	private static final String BUNDLE_NAME = "org2.eclipse.php.internal.ui.PHPUIMessages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	private static ResourceBundle fResourceBundle;

	private PHPUIMessages() {
	}

	public static ResourceBundle getResourceBundle() {
		try {
			if (fResourceBundle == null)
				fResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
		} catch (MissingResourceException x) {
			fResourceBundle = null;
		}
		return fResourceBundle;
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return ""; //$NON-NLS-1$
		}
	}
}
