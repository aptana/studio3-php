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
 */
@SuppressWarnings("nls")
public enum PHPTokenType
{

	ARRAY_BEGIN("punctuation.section.array.begin.php"),
	ARRAY_END("punctuation.section.array.end.php"),
	CHARACTER_ESCAPE("constant.character.escape.php"),
	CLASS_OPERATOR("keyword.operator.class.php"),
	COMMENT_BLOCK("comment.block.php"),
	COMMENT_HASH("comment.line.number-sign.php"),
	COMMENT_PHPDOC("comment.block.documentation.phpdoc.php"),
	COMMENT_SLASH("comment.line.double-slash.php"),
	CONSTANT("constant.php"),
	CONSTANT_LANGUAGE("constant.language.php"),
	CONSTANT_LANGUAGE_OTHER("constant.language.other.php"),
	CONSTANT_NUMERIC("constant.numeric.php"),
	CONSTANT_OTHER("constant.other.php"),
	ENTITY_CLASS("entity.name.type.class.php"),
	ENTITY_FUNCTION("entity.name.function.php"),
	FUNCTION_PUNCTUATION("punctuation.definition.function.php"),
	HEREDOC("string.unquoted.heredoc.php"),
	KEYWORD("keyword.php"),
	KEYWORD_CONTROL("keyword.control.php"),
	KEYWORD_CONTROL_IMPORT("keyword.control.import.php"),
	KEYWORD_NAMESPACE("keyword.namespace.php"),
	KEYWORD_OP_ARITHMETIC("keyword.operator.arithmetic.php"),
	KEYWORD_OP_ASSIGN("keyword.operator.assignment.php"),
	KEYWORD_OP_BITWISE("keyword.operator.bitwise.php"),
	KEYWORD_OP_CLASS("keyword.operator.class.php"),
	KEYWORD_OP_COMPARISON("keyword.operator.comparison.php"),
	KEYWORD_OP_INC_DEC("keyword.operator.increment-decrement.php"),
	KEYWORD_OP_LOGICAL("keyword.operator.logical.php"),
	KEYWORD_OTHER("keyword.other.phpdoc.php"),
	KEYWORD_OTHER_CLASS("keyword.other.class.php"),
	META_FUNCTION_CALL("meta.function-call.php"),
	META_FUNCTION_CALL_OBJECT("meta.function-call.object.php"),
	META_FUNCTION_CALL_STATIC("meta.function-call.static.php"),
	META_STRING_CONTENTS_SINGLE("meta.string-contents.quoted.single.php"),
	META_STRING_CONTENTS_DOUBLE("meta.string-contents.quoted.double.php"),
	NOWDOC("string.unquoted.nowdoc.php"),
	NUMERIC("constant.numeric.php"),
	PUNCTUATION_LBRACKET("variable.other.php keyword.operator.index-start.php"),
	PUNCTUATION_PARAM_LEFT("punctuation.definition.parameters.begin.php"),
	PUNCTUATION_PARAM_RIGHT("punctuation.definition.parameters.end.php"),
	PUNCTUATION_RBRACKET("variable.other.php keyword.operator.index-end.php"),
	PUNCTUATION_STRING_BEGIN("punctuation.definition.string.begin.php"),
	PUNCTUATION_STRING_END("punctuation.definition.string.end.php"),
	PUNCTUATION_TERMINATOR("punctuation.terminator.expression.php"),
	STATIC_PUNCTUATION("meta.function-call.static.php"),
	STORAGE_MODIFIER_ABSTRACT("storage.modifier.abstract.php"),
	STORAGE_MODIFIER_FINAL("storage.modifier.final.php"),
	STORAGE_MODIFIER_PRIVATE("storage.modifier.private.php"),
	STORAGE_MODIFIER_PROTECTED("storage.modifier.protected.php"),
	STORAGE_MODIFIER_PUBLIC("storage.modifier.public.php"),
	STORAGE_MODIFIER_STATIC("storage.modifier.static.php"),
	STORAGE_TYPE("storage.type.php"),
	STORAGE_TYPE_CLASS("storage.type.class.php"),
	STORAGE_TYPE_TRAIT("storage.type.trait.php"),
	STORAGE_TYPE_FUNCTION("storage.type.function.php"),
	STRING_DOUBLE("string.quoted.double.php"),
	STRING_QUOTED("string.quoted.php"),
	STRING_SINGLE("string.quoted.single.php"),
	SUPPORT_CLASS("support.class.php"),
	SUPPORT_CONSTANT("support.constant.php"),
	SUPPORT_FUNCTION("support.function.php"),
	SUPPORT_FUNCTION_BUILTIN("support.function.builtin_functions.php"),
	SUPPORT_FUNCTION_CONSTRUCT("support.function.construct.php"),
	VARIABLE_LANGUAGE("variable.language.php"),
	VARIABLE_OTHER("variable.other.php"),
	VARIABLE_OTHER_GLOBAL("variable.other.global.php"),
	VARIABLE_OTHER_GLOBAL_SAFER("variable.other.global.safer.php"),
	VARIABLE_OTHER_PROPERTY("variable.other.property.php"),
	VARIABLE_PUNCTUATION("punctuation.definition.variable.php");

	private final String scope;

	private PHPTokenType(String scope)
	{
		this.scope = scope;
	}

	public String toString()
	{
		return scope;
	}
}
