/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.core;

import com.aptana.editor.common.text.CommonDoubleClickStrategy;

public class PHPDoubleClickStrategy extends CommonDoubleClickStrategy
{

	@Override
	protected boolean isIdentifierPart(char c)
	{
		return super.isIdentifierPart(c) || c == '&';
	}

}
