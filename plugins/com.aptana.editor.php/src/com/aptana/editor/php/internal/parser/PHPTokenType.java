/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.php.internal.parser;

/**
 * @author Max Stepanov
 *
 */
@SuppressWarnings("nls")
public enum PHPTokenType {

	CHARACTER_ESCAPE("constant.character.escape.php"),
	CLASS_OPERATOR("keyword.operator.class.php"),
	ARRAY_BEGIN("punctuation.section.array.begin.php"),
	ARRAY_END("punctuation.section.array.end.php"),
	VARIABLE_PUNCTUATION("punctuation.definition.variable.php"),
	FUNCTION_PUNCTUATION("punctuation.definition.function.php"),
	STATIC_PUNCTUATION("meta.function-call.static.php"),
	VARIABLE("variable.other.php"),
	NUMERIC("constant.numeric.php");
	
	private final String scope;
	
	private PHPTokenType(String scope) {
		this.scope = scope;
	}
	
	public String toString() {
		return scope;
	}
}
