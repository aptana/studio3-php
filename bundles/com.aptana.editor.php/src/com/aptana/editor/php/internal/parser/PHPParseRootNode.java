/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser;

import beaver.Symbol;

import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.parsing.ast.ParseRootNode;

/**
 * PHP parse root node.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class PHPParseRootNode extends ParseRootNode
{

	private boolean isCached;

	/**
	 * Constructs a new PHP parse root node.
	 * 
	 * @param children
	 * @param start
	 * @param end
	 */
	public PHPParseRootNode(Symbol[] children, int start, int end)
	{
		super(children, start, end);
	}

	public String getLanguage()
	{
		return IPHPConstants.CONTENT_TYPE_PHP;
	}

	/**
	 * Set a 'cached' flag to indicate that this root node was taken from the cache, and was not just computed by the
	 * PHP parser.
	 * 
	 * @param isCached
	 */
	public void setIsCached(boolean isCached)
	{
		this.isCached = isCached;
	}

	/**
	 * Returns <code>true</code> if this root node was already computed and is now cached.
	 */
	public boolean isCached()
	{
		return isCached;
	}
}
