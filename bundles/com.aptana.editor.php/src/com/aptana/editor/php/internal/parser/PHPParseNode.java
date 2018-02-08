/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser;

import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.parsing.ast.ParseNode;

class PHPParseNode extends ParseNode
{

	public String getLanguage()
	{
		return IPHPConstants.CONTENT_TYPE_PHP;
	}

}
