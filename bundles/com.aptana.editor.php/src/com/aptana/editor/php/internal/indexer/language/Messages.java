/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer.language;

import org.eclipse.osgi.util.NLS;

/**
 * @author Pavel Petrochenko
 */
public final class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.php.internal.indexer.language.messages"; //$NON-NLS-1$

	/**
	 * KEYWORD_LABEL
	 */
	public static String KEYWORD_LABEL;

	/**
	 * KEYWORD_LABEL
	 */
	public static String SUPERGLOBAL_LABEL;

	/**
	 * MAGIC_CONSTANT_LABEL
	 */
	public static String MAGIC_CONSTANT_LABEL;

	/**
	 * MAGIC_METHOD_LABEL
	 */
	public static String MAGIC_METHOD_LABEL;

	public static String PHPBuiltins_addingPhp4;

	public static String PHPBuiltins_addingPhp5;

	public static String PHPBuiltins_addingPhp53;

	public static String PHPBuiltins_indexingLibraries;

	public static String PHPBuiltins_languageSupportTaskName;

	/**
	 * PREDEFINED_CONSTANT_LABEL
	 */
	public static String PREDEFINED_CONSTANT_LABEL;

	private Messages()
	{

	}

	static
	{
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
