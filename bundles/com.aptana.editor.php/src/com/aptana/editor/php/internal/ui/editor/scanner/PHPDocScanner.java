/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.editor.scanner;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.rules.Token;

import com.aptana.editor.common.text.rules.CommentScanner;
import com.aptana.editor.php.internal.parser.PHPTokenType;

@SuppressWarnings("nls")
public class PHPDocScanner extends CommentScanner
{

	private static Map<String, String> TAGS = new HashMap<String, String>();
	static
	{
		TAGS.put("@abstract", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@access", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@author", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@category", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@copyright", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@deprecated", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@example", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@filesource", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@final", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@global", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@ignore", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@internal", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@license", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@link", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@method", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@name", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@package", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@param", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@property", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@return", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@see", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@since", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@static", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@staticvar", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@subpackage", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@throws", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@todo", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@tutorial", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@uses", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@var", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@version", PHPTokenType.KEYWORD_OTHER.toString());
		TAGS.put("@xlink", PHPTokenType.KEYWORD_OTHER.toString());
	}

	public PHPDocScanner()
	{
		super(new Token(PHPTokenType.COMMENT_PHPDOC.toString()), TAGS);
	}
}
