/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter;

import com.aptana.formatter.ui.CodeFormatterConstants;

/**
 * PHP code formatter constants.<br>
 * Since the formatters will be saved in a unified XML file, it's important to have a unique key for every setting. The
 * PHP formatter constants are all starting with the {@link #FORMATTER_ID} string.
 */
public interface PHPFormatterConstants
{

	/**
	 * PHP formatter ID.
	 */
	String FORMATTER_ID = "php.formatter"; //$NON-NLS-1$

	String FORMATTER_TAB_CHAR = FORMATTER_ID + '.' + CodeFormatterConstants.FORMATTER_TAB_CHAR;
	String FORMATTER_TAB_SIZE = FORMATTER_ID + '.' + CodeFormatterConstants.FORMATTER_TAB_SIZE;

	// Wrapping
	String WRAP_COMMENTS = FORMATTER_ID + ".wrap.comments"; //$NON-NLS-1$
	String WRAP_COMMENTS_LENGTH = FORMATTER_ID + ".wrap.comments.length"; //$NON-NLS-1$

	// Indentation
	String INDENT_PHP_BODY = FORMATTER_ID + ".indent.php.body"; //$NON-NLS-1$
	String INDENT_CURLY_BLOCKS = FORMATTER_ID + ".indent.blocks"; //$NON-NLS-1$
	String INDENT_NAMESPACE_BLOCKS = FORMATTER_ID + ".indent.namespace.blocks"; //$NON-NLS-1$
	String INDENT_FUNCTION_BODY = FORMATTER_ID + ".indent.function.body"; //$NON-NLS-1$
	String INDENT_TYPE_BODY = FORMATTER_ID + ".indent.class.body"; //$NON-NLS-1$
	String INDENT_SWITCH_BODY = FORMATTER_ID + ".indent.switch.body"; //$NON-NLS-1$
	String INDENT_CASE_BODY = FORMATTER_ID + ".indent.case.body"; //$NON-NLS-1$
	String INDENT_BREAK_IN_CASE = FORMATTER_ID + ".indent.breakInCase"; //$NON-NLS-1$
	String FORMATTER_INDENTATION_SIZE = FORMATTER_ID + '.' + CodeFormatterConstants.FORMATTER_INDENTATION_SIZE;

	// New lines
	String NEW_LINES_BEFORE_ELSE_STATEMENT = FORMATTER_ID + ".newline.before.else"; //$NON-NLS-1$
	String NEW_LINES_BEFORE_IF_IN_ELSEIF_STATEMENT = FORMATTER_ID + ".newline.before.if.in.elseif"; //$NON-NLS-1$
	String NEW_LINES_BEFORE_CATCH_STATEMENT = FORMATTER_ID + ".newline.before.catch"; //$NON-NLS-1$
	String NEW_LINES_BEFORE_DO_WHILE_STATEMENT = FORMATTER_ID + ".newline.before.dowhile"; //$NON-NLS-1$
	String NEW_LINES_BETWEEN_ARRAY_CREATION_ELEMENTS = FORMATTER_ID + ".newline.between.array.creation.elements"; //$NON-NLS-1$

	// Empty lines
	String LINES_AFTER_FUNCTION_DECLARATION = FORMATTER_ID + ".line.after.function.declaration"; //$NON-NLS-1$
	String LINES_AFTER_TYPE_DECLARATION = FORMATTER_ID + ".line.after.class.declaration"; //$NON-NLS-1$
	String PRESERVED_LINES = FORMATTER_ID + ".line.preserve"; //$NON-NLS-1$

	// Braces position
	String BRACE_POSITION_FUNCTION_DECLARATION = FORMATTER_ID + ".brace.position.function.declaration"; //$NON-NLS-1$
	String BRACE_POSITION_TYPE_DECLARATION = FORMATTER_ID + ".brace.position.class.declaration"; //$NON-NLS-1$
	String BRACE_POSITION_BLOCK = FORMATTER_ID + ".brace.position.blocks"; //$NON-NLS-1$
	String BRACE_POSITION_BLOCK_IN_SWITCH = FORMATTER_ID + ".brace.position.switch.block"; //$NON-NLS-1$
	String BRACE_POSITION_BLOCK_IN_CASE = FORMATTER_ID + ".brace.position.case.block"; //$NON-NLS-1$

	// Spaces
	String SPACES_BEFORE_STATIC_INVOCATION_OPERATOR = FORMATTER_ID + ".spaces.before.staticInvocation"; //$NON-NLS-1$
	String SPACES_AFTER_STATIC_INVOCATION_OPERATOR = FORMATTER_ID + ".spaces.after.staticInvocation"; //$NON-NLS-1$
	String SPACES_BEFORE_ASSIGNMENT_OPERATOR = FORMATTER_ID + ".spaces.before.assignment"; //$NON-NLS-1$
	String SPACES_AFTER_ASSIGNMENT_OPERATOR = FORMATTER_ID + ".spaces.after.assignment"; //$NON-NLS-1$
	String SPACES_BEFORE_COMMAS = FORMATTER_ID + ".spaces.before.commas"; //$NON-NLS-1$
	String SPACES_AFTER_COMMAS = FORMATTER_ID + ".spaces.after.commas"; //$NON-NLS-1$
	String SPACES_BEFORE_CASE_COLON_OPERATOR = FORMATTER_ID + ".spaces.after.case.colon"; //$NON-NLS-1$
	String SPACES_AFTER_CASE_COLON_OPERATOR = FORMATTER_ID + ".spaces.before.case.colon"; //$NON-NLS-1$
	String SPACES_BEFORE_COLON = FORMATTER_ID + ".spaces.before.colon"; //$NON-NLS-1$
	String SPACES_AFTER_COLON = FORMATTER_ID + ".spaces.after.colon"; //$NON-NLS-1$
	String SPACES_BEFORE_SEMICOLON = FORMATTER_ID + ".spaces.before.semicolon"; //$NON-NLS-1$
	String SPACES_AFTER_SEMICOLON = FORMATTER_ID + ".spaces.after.semicolon"; //$NON-NLS-1$
	String SPACES_BEFORE_CONCATENATION_OPERATOR = FORMATTER_ID + ".spaces.before.dot"; //$NON-NLS-1$
	String SPACES_AFTER_CONCATENATION_OPERATOR = FORMATTER_ID + ".spaces.after.dot"; //$NON-NLS-1$
	String SPACES_BEFORE_ARROW_OPERATOR = FORMATTER_ID + ".spaces.before.arrow"; //$NON-NLS-1$
	String SPACES_AFTER_ARROW_OPERATOR = FORMATTER_ID + ".spaces.after.arrow"; //$NON-NLS-1$
	String SPACES_BEFORE_KEY_VALUE_OPERATOR = FORMATTER_ID + ".spaces.before.keyValue"; //$NON-NLS-1$
	String SPACES_AFTER_KEY_VALUE_OPERATOR = FORMATTER_ID + ".spaces.after.keyValue"; //$NON-NLS-1$
	String SPACES_BEFORE_RELATIONAL_OPERATORS = FORMATTER_ID + ".spaces.before.relational"; //$NON-NLS-1$
	String SPACES_AFTER_RELATIONAL_OPERATORS = FORMATTER_ID + ".spaces.after.relational"; //$NON-NLS-1$
	String SPACES_BEFORE_CONDITIONAL_OPERATOR = FORMATTER_ID + ".spaces.before.conditional"; //$NON-NLS-1$
	String SPACES_AFTER_CONDITIONAL_OPERATOR = FORMATTER_ID + ".spaces.after.conditional"; //$NON-NLS-1$
	String SPACES_BEFORE_POSTFIX_OPERATOR = FORMATTER_ID + ".spaces.before.postfix"; //$NON-NLS-1$
	String SPACES_AFTER_POSTFIX_OPERATOR = FORMATTER_ID + ".spaces.after.postfix"; //$NON-NLS-1$
	String SPACES_BEFORE_PREFIX_OPERATOR = FORMATTER_ID + ".spaces.before.prefix"; //$NON-NLS-1$
	String SPACES_AFTER_PREFIX_OPERATOR = FORMATTER_ID + ".spaces.after.prefix"; //$NON-NLS-1$
	String SPACES_BEFORE_ARITHMETIC_OPERATOR = FORMATTER_ID + ".spaces.before.arithmetic"; //$NON-NLS-1$
	String SPACES_AFTER_ARITHMETIC_OPERATOR = FORMATTER_ID + ".spaces.after.arithmetic"; //$NON-NLS-1$
	String SPACES_BEFORE_UNARY_OPERATOR = FORMATTER_ID + ".spaces.before.unary"; //$NON-NLS-1$
	String SPACES_AFTER_UNARY_OPERATOR = FORMATTER_ID + ".spaces.after.unary"; //$NON-NLS-1$
	String SPACES_BEFORE_NAMESPACE_SEPARATOR = FORMATTER_ID + ".spaces.before.namespaceSeparator"; //$NON-NLS-1$
	String SPACES_AFTER_NAMESPACE_SEPARATOR = FORMATTER_ID + ".spaces.after.namespaceSeparator"; //$NON-NLS-1$
	String SPACES_BEFORE_FOR_SEMICOLON = FORMATTER_ID + ".spaces.before.forSemicolon"; //$NON-NLS-1$
	String SPACES_AFTER_FOR_SEMICOLON = FORMATTER_ID + ".spaces.after.forSemicolon"; //$NON-NLS-1$
	// Parentheses keys (note that we need to keep the first two keys as they are for backward compatibility)
	String SPACES_BEFORE_OPENING_PARENTHESES = FORMATTER_ID + ".spaces.before.parentheses"; //$NON-NLS-1$
	String SPACES_AFTER_OPENING_PARENTHESES = FORMATTER_ID + ".spaces.after.parentheses"; //$NON-NLS-1$
	String SPACES_BEFORE_CLOSING_PARENTHESES = FORMATTER_ID + ".spaces.before.parentheses.closing"; //$NON-NLS-1$
	String SPACES_BEFORE_OPENING_DECLARATION_PARENTHESES = FORMATTER_ID + ".spaces.before.declaration.parentheses.opening"; //$NON-NLS-1$
	String SPACES_AFTER_OPENING_DECLARATION_PARENTHESES = FORMATTER_ID + ".spaces.after.declaration.parentheses.opening"; //$NON-NLS-1$
	String SPACES_BEFORE_CLOSING_DECLARATION_PARENTHESES = FORMATTER_ID + ".spaces.before.declaration.parentheses.closing"; //$NON-NLS-1$
	String SPACES_BEFORE_OPENING_INVOCATION_PARENTHESES = FORMATTER_ID + ".spaces.before.invocation.parentheses.opening"; //$NON-NLS-1$
	String SPACES_AFTER_OPENING_INVOCATION_PARENTHESES = FORMATTER_ID + ".spaces.after.invocation.parentheses.opening"; //$NON-NLS-1$
	String SPACES_BEFORE_CLOSING_INVOCATION_PARENTHESES = FORMATTER_ID + ".spaces.before.invocation.parentheses.closing"; //$NON-NLS-1$
	String SPACES_BEFORE_OPENING_ARRAY_ACCESS_PARENTHESES = FORMATTER_ID + ".spaces.before.array.access.parentheses.opening"; //$NON-NLS-1$
	String SPACES_AFTER_OPENING_ARRAY_ACCESS_PARENTHESES = FORMATTER_ID + ".spaces.after.array.access.parentheses.opening"; //$NON-NLS-1$
	String SPACES_BEFORE_CLOSING_ARRAY_ACCESS_PARENTHESES = FORMATTER_ID + ".spaces.before.array.access.parentheses.closing"; //$NON-NLS-1$
	String SPACES_BEFORE_OPENING_LOOP_PARENTHESES = FORMATTER_ID + ".spaces.before.loop.parentheses.opening"; //$NON-NLS-1$
	String SPACES_AFTER_OPENING_LOOP_PARENTHESES = FORMATTER_ID + ".spaces.after.loop.parentheses.opening"; //$NON-NLS-1$
	String SPACES_BEFORE_CLOSING_LOOP_PARENTHESES = FORMATTER_ID + ".spaces.before.loop.parentheses.closing"; //$NON-NLS-1$
	String SPACES_BEFORE_OPENING_CONDITIONAL_PARENTHESES = FORMATTER_ID + ".spaces.before.conditional.parentheses.opening"; //$NON-NLS-1$
	String SPACES_AFTER_OPENING_CONDITIONAL_PARENTHESES = FORMATTER_ID + ".spaces.after.conditional.parentheses.opening"; //$NON-NLS-1$
	String SPACES_BEFORE_CLOSING_CONDITIONAL_PARENTHESES = FORMATTER_ID + ".spaces.before.conditional.parentheses.closing"; //$NON-NLS-1$
	// On/Off
	String FORMATTER_OFF_ON_ENABLED = FORMATTER_ID + ".formatter.on.off.enabled"; //$NON-NLS-1$
	String FORMATTER_ON = FORMATTER_ID + ".formatter.on"; //$NON-NLS-1$
	String FORMATTER_OFF = FORMATTER_ID + ".formatter.off"; //$NON-NLS-1$
	String DEFAULT_FORMATTER_OFF = "@formatter:off"; //$NON-NLS-1$
	String DEFAULT_FORMATTER_ON = "@formatter:on"; //$NON-NLS-1$

}
