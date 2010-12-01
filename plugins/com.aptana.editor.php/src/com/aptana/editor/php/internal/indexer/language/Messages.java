/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
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
