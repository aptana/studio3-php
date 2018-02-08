package com.aptana.editor.php.internal.contentAssist;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.php.internal.contentAssist.messages"; //$NON-NLS-1$
	public static String ContentAssistUtils_noAvailableDocumentation;
	public static String EntryDocumentationResolver_resolvedReturnTypes;
	public static String EntryDocumentationResolver_resolvedTypes;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
