package com.aptana.editor.php;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.php.messages"; //$NON-NLS-1$
	public static String OpenDeclarationAction_cannotOpenDeclataion;
	public static String PHPEditorPlugin_indexingJobMessage;
	public static String PHPSourceEditor_markOccurrencesJob_name;
	private static ResourceBundle fResourceBundle;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
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
}
