package com.aptana.editor.php.internal.indexer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.php.internal.indexer.messages"; //$NON-NLS-1$
	public static String PHPDocUtils_documentedType;
	public static String PHPDocUtils_noAvailableDocs;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
