/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.indexer;

import org.eclipse.osgi.util.NLS;

/**
 * @author Robin
 * @author Pavel Petrochenko
 */
public final class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.php.indexer.messages"; //$NON-NLS-1$

	/**
	 * Messages
	 */
	private Messages()
	{
	}

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	/**
	 * 
	 */
	public static String PHPGlobalIndexer_IndexChanged;

	/**
	 * 
	 */
	public static String PHPGlobalIndexer_IndexChanged2;

	/**
	 * 
	 */
	public static String PHPGlobalIndexer_IndexNew;

	public static String PHPGlobalIndexer_initializinIndex;

	/**
	 * 
	 */
	public static String PHPGlobalIndexer_ModulesLeft;

	/**
	 * 
	 */
	public static String PHPGlobalIndexer_PHP_Index;

	/**
	 * 
	 */
	public static String PHPGlobalIndexer_PHP_Index2;

	/**
	 * 
	 */
	public static String PHPGlobalIndexer_ProviderDecl;

	/**
	 * 
	 */
	public static String PHPGlobalIndexer_RemovingModuleIndex2;

	public static String PHPGlobalIndexer_savingIndex;

	/**
	 * 
	 */
	public static String PHPGlobalIndexer_UnableLoad;
}
