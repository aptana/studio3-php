/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.indexer;

import com.aptana.editor.php.core.IPHPTypeConstants;


/**
 * PHP index constants.
 * @author Denis Denisenko
 */
public interface IPHPIndexConstants extends IPHPTypeConstants
{
	/**
	 * CLASS_CATEGORY
	 */
	static final int CLASS_CATEGORY = 1;
	/**
	 * FUNCTION_CATEGORY
	 */
	static final int FUNCTION_CATEGORY = 2;
	
	/**
	 * VAR_CATEGORY
	 */
	static final int VAR_CATEGORY = 3;
	/**
	 * CONST_CATEGORY
	 */
	static final int CONST_CATEGORY = 4;
	
	/**
	 * IMPORT_CATEGORY
	 */
	static final int IMPORT_CATEGORY = 5;
	/**
	 * NAMESPACE_CATEGORY
	 */
	static final int NAMESPACE_CATEGORY = 6;
	
	/**
	 * LAMBDA_FUNCTION_CATEGORY
	 */
	static final int LAMBDA_FUNCTION_CATEGORY = 7;
}
