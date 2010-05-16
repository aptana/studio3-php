package com.aptana.editor.php.internal.ui.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.php.internal.ui.preferences.messages"; //$NON-NLS-1$
	public static String PHPDevelopmentPage_php4;
	public static String PHPDevelopmentPage_php5;
	public static String PHPDevelopmentPage_php53;
	public static String PHPDevelopmentPage_phpVersion;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
