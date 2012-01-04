/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.preferences;

import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_BLOCK;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_BLOCK_IN_CASE;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_BLOCK_IN_SWITCH;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_FUNCTION_DECLARATION;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_TYPE_DECLARATION;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.DEFAULT_FORMATTER_OFF;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.DEFAULT_FORMATTER_ON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.FORMATTER_INDENTATION_SIZE;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.FORMATTER_OFF;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.FORMATTER_OFF_ON_ENABLED;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.FORMATTER_ON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.FORMATTER_TAB_CHAR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.FORMATTER_TAB_SIZE;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_BREAK_IN_CASE;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_CASE_BODY;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_CURLY_BLOCKS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_FUNCTION_BODY;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_NAMESPACE_BLOCKS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_PHP_BODY;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_SWITCH_BODY;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.INDENT_TYPE_BODY;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.LINES_AFTER_FUNCTION_DECLARATION;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.LINES_AFTER_TYPE_DECLARATION;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.NEW_LINES_BEFORE_CATCH_STATEMENT;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.NEW_LINES_BEFORE_DO_WHILE_STATEMENT;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.NEW_LINES_BEFORE_ELSE_STATEMENT;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.NEW_LINES_BEFORE_IF_IN_ELSEIF_STATEMENT;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.NEW_LINES_BETWEEN_ARRAY_CREATION_ELEMENTS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.PRESERVED_LINES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_ARITHMETIC_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_ARROW_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_ASSIGNMENT_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_CASE_COLON_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_COLON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_COMMAS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_CONCATENATION_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_CONDITIONAL_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_FOR_SEMICOLON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_KEY_VALUE_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_NAMESPACE_SEPARATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_OPENING_ARRAY_ACCESS_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_OPENING_CONDITIONAL_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_OPENING_DECLARATION_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_OPENING_INVOCATION_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_OPENING_LOOP_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_OPENING_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_POSTFIX_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_PREFIX_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_RELATIONAL_OPERATORS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_SEMICOLON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_STATIC_INVOCATION_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_AFTER_UNARY_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_ARITHMETIC_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_ARROW_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_ASSIGNMENT_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_CASE_COLON_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_CLOSING_ARRAY_ACCESS_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_CLOSING_CONDITIONAL_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_CLOSING_DECLARATION_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_CLOSING_INVOCATION_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_CLOSING_LOOP_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_CLOSING_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_COLON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_COMMAS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_CONCATENATION_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_CONDITIONAL_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_FOR_SEMICOLON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_KEY_VALUE_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_NAMESPACE_SEPARATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_OPENING_ARRAY_ACCESS_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_OPENING_CONDITIONAL_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_OPENING_DECLARATION_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_OPENING_INVOCATION_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_OPENING_LOOP_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_OPENING_PARENTHESES;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_POSTFIX_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_PREFIX_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_RELATIONAL_OPERATORS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_SEMICOLON;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_STATIC_INVOCATION_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.SPACES_BEFORE_UNARY_OPERATOR;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.WRAP_COMMENTS;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.WRAP_COMMENTS_LENGTH;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.common.util.EditorUtil;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.formatter.PHPCodeFormatterPlugin;
import com.aptana.formatter.IDebugScopes;
import com.aptana.formatter.ui.CodeFormatterConstants;

/**
 * PHP formatter preference initializer.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPFormatterPreferenceInitializer extends AbstractPreferenceInitializer
{
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences store = EclipseUtil.defaultScope().getNode(PHPCodeFormatterPlugin.PLUGIN_ID);

		store.put(FORMATTER_TAB_CHAR, CodeFormatterConstants.EDITOR);
		store.put(FORMATTER_TAB_SIZE, Integer.toString(EditorUtil.getSpaceIndentSize(PHPEplPlugin.getDefault()
				.getBundle().getSymbolicName())));
		store.put(FORMATTER_INDENTATION_SIZE, "4"); //$NON-NLS-1$
		store.putBoolean(WRAP_COMMENTS, false);
		store.putInt(WRAP_COMMENTS_LENGTH, 80);
		store.putBoolean(INDENT_PHP_BODY, false);
		store.putBoolean(INDENT_CURLY_BLOCKS, true);
		store.putBoolean(INDENT_NAMESPACE_BLOCKS, false);
		store.putBoolean(INDENT_TYPE_BODY, true);
		store.putBoolean(INDENT_FUNCTION_BODY, true);
		store.putBoolean(INDENT_SWITCH_BODY, true);
		store.putBoolean(INDENT_CASE_BODY, true);
		store.putBoolean(INDENT_BREAK_IN_CASE, true);
		store.putBoolean(NEW_LINES_BEFORE_CATCH_STATEMENT, false);
		store.putBoolean(NEW_LINES_BEFORE_ELSE_STATEMENT, false);
		store.putBoolean(NEW_LINES_BEFORE_IF_IN_ELSEIF_STATEMENT, false);
		store.putBoolean(NEW_LINES_BEFORE_DO_WHILE_STATEMENT, false);
		store.putBoolean(NEW_LINES_BETWEEN_ARRAY_CREATION_ELEMENTS, false);
		store.putInt(LINES_AFTER_TYPE_DECLARATION, 1);
		store.putInt(LINES_AFTER_FUNCTION_DECLARATION, 1);
		store.putInt(PRESERVED_LINES, 1);
		store.put(BRACE_POSITION_BLOCK, CodeFormatterConstants.SAME_LINE);
		store.put(BRACE_POSITION_BLOCK_IN_CASE, CodeFormatterConstants.SAME_LINE);
		store.put(BRACE_POSITION_BLOCK_IN_SWITCH, CodeFormatterConstants.SAME_LINE);
		store.put(BRACE_POSITION_TYPE_DECLARATION, CodeFormatterConstants.SAME_LINE);
		store.put(BRACE_POSITION_FUNCTION_DECLARATION, CodeFormatterConstants.SAME_LINE);
		store.putInt(SPACES_BEFORE_STATIC_INVOCATION_OPERATOR, 0);
		store.putInt(SPACES_AFTER_STATIC_INVOCATION_OPERATOR, 0);
		store.putInt(SPACES_BEFORE_ASSIGNMENT_OPERATOR, 1);
		store.putInt(SPACES_AFTER_ASSIGNMENT_OPERATOR, 1);
		store.putInt(SPACES_BEFORE_COMMAS, 0);
		store.putInt(SPACES_AFTER_COMMAS, 1);
		store.putInt(SPACES_BEFORE_CASE_COLON_OPERATOR, 1);
		store.putInt(SPACES_AFTER_CASE_COLON_OPERATOR, 1);
		store.putInt(SPACES_BEFORE_SEMICOLON, 0);
		store.putInt(SPACES_AFTER_SEMICOLON, 1);
		store.putInt(SPACES_BEFORE_CONCATENATION_OPERATOR, 1);
		store.putInt(SPACES_AFTER_CONCATENATION_OPERATOR, 1);
		store.putInt(SPACES_BEFORE_ARROW_OPERATOR, 1);
		store.putInt(SPACES_AFTER_ARROW_OPERATOR, 1);
		store.putInt(SPACES_BEFORE_KEY_VALUE_OPERATOR, 1);
		store.putInt(SPACES_AFTER_KEY_VALUE_OPERATOR, 1);
		store.putInt(SPACES_BEFORE_RELATIONAL_OPERATORS, 1);
		store.putInt(SPACES_AFTER_RELATIONAL_OPERATORS, 1);
		store.putInt(SPACES_BEFORE_CONDITIONAL_OPERATOR, 1);
		store.putInt(SPACES_AFTER_CONDITIONAL_OPERATOR, 1);
		store.putInt(SPACES_BEFORE_POSTFIX_OPERATOR, 0);
		store.putInt(SPACES_AFTER_POSTFIX_OPERATOR, 0);
		store.putInt(SPACES_BEFORE_PREFIX_OPERATOR, 0);
		store.putInt(SPACES_AFTER_PREFIX_OPERATOR, 0);
		store.putInt(SPACES_BEFORE_ARITHMETIC_OPERATOR, 1);
		store.putInt(SPACES_AFTER_ARITHMETIC_OPERATOR, 1);
		store.putInt(SPACES_BEFORE_UNARY_OPERATOR, 0);
		store.putInt(SPACES_AFTER_UNARY_OPERATOR, 0);
		store.putInt(SPACES_BEFORE_FOR_SEMICOLON, 0);
		store.putInt(SPACES_AFTER_FOR_SEMICOLON, 1);
		store.put(FORMATTER_ON, DEFAULT_FORMATTER_ON);
		store.put(FORMATTER_OFF, DEFAULT_FORMATTER_OFF);
		store.putBoolean(FORMATTER_OFF_ON_ENABLED, false);
		store.putInt(SPACES_BEFORE_OPENING_PARENTHESES, 0);
		store.putInt(SPACES_AFTER_OPENING_PARENTHESES, 0);
		store.putInt(SPACES_BEFORE_CLOSING_PARENTHESES, 0);
		store.putInt(SPACES_BEFORE_OPENING_DECLARATION_PARENTHESES, 0);
		store.putInt(SPACES_AFTER_OPENING_DECLARATION_PARENTHESES, 0);
		store.putInt(SPACES_BEFORE_CLOSING_DECLARATION_PARENTHESES, 0);
		store.putInt(SPACES_BEFORE_OPENING_INVOCATION_PARENTHESES, 0);
		store.putInt(SPACES_AFTER_OPENING_INVOCATION_PARENTHESES, 0);
		store.putInt(SPACES_BEFORE_CLOSING_INVOCATION_PARENTHESES, 0);
		store.putInt(SPACES_BEFORE_OPENING_ARRAY_ACCESS_PARENTHESES, 0);
		store.putInt(SPACES_AFTER_OPENING_ARRAY_ACCESS_PARENTHESES, 0);
		store.putInt(SPACES_BEFORE_CLOSING_ARRAY_ACCESS_PARENTHESES, 0);
		store.putInt(SPACES_BEFORE_OPENING_LOOP_PARENTHESES, 1);
		store.putInt(SPACES_AFTER_OPENING_LOOP_PARENTHESES, 0);
		store.putInt(SPACES_BEFORE_CLOSING_LOOP_PARENTHESES, 0);
		store.putInt(SPACES_BEFORE_OPENING_CONDITIONAL_PARENTHESES, 1);
		store.putInt(SPACES_AFTER_OPENING_CONDITIONAL_PARENTHESES, 0);
		store.putInt(SPACES_BEFORE_CLOSING_CONDITIONAL_PARENTHESES, 0);

		// Not for UI customization
		store.putInt(SPACES_BEFORE_NAMESPACE_SEPARATOR, 0);
		store.putInt(SPACES_AFTER_NAMESPACE_SEPARATOR, 0);
		store.putInt(SPACES_BEFORE_COLON, 0);
		store.putInt(SPACES_AFTER_COLON, 1);
		try
		{
			store.flush();
		}
		catch (BackingStoreException e)
		{
			IdeLog.logError(PHPCodeFormatterPlugin.getDefault(), e, IDebugScopes.DEBUG);
		}
	}
}
