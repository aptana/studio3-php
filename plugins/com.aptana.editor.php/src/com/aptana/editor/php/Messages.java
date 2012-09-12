/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.php.messages"; //$NON-NLS-1$
	public static String OpenDeclarationAction_cannotOpenDeclataion;
	public static String PHPEditorPlugin_DefaultPHPProjectTemplate_Description;
	public static String PHPEditorPlugin_DefaultPHPProjectTemplate_Name;
	public static String PHPEditorPlugin_indexingJobMessage;
	public static String PHPSourceEditor_markOccurrencesJob_name;
	public static String openDeclaration_label;
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
