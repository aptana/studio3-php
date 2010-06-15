package com.aptana.editor.php.util;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.php.util.messages"; //$NON-NLS-1$
	public static String CoreUtility_buildAll;
	public static String CoreUtility_buildProject;
	public static String CoreUtility_rebuilding;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
