/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter;

import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_BLOCK;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_BLOCK_IN_CASE;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_BLOCK_IN_SWITCH;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_FUNCTION_DECLARATION;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.BRACE_POSITION_TYPE_DECLARATION;
import static com.aptana.editor.php.formatter.PHPFormatterConstants.FORMATTER_ID;
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

import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.texteditor.ITextEditor;

import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.util.EditorUtil;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.formatter.preferences.PHPFormatterModifyDialog;
import com.aptana.editor.php.internal.ui.editor.PHPSourceConfiguration;
import com.aptana.editor.php.internal.ui.editor.PHPSourceViewerConfiguration;
import com.aptana.formatter.AbstractScriptFormatterFactory;
import com.aptana.formatter.IScriptFormatter;
import com.aptana.formatter.preferences.PreferenceKey;
import com.aptana.formatter.ui.IFormatterModifyDialog;
import com.aptana.formatter.ui.IFormatterModifyDialogOwner;

/**
 * PHP formatter factory
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPFormatterFactory extends AbstractScriptFormatterFactory
{

	private static final PreferenceKey FORMATTER_PREF_KEY = new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID,
			FORMATTER_ID);

	private static final String FORMATTER_PREVIEW_FILE = "formatterPreview.php"; //$NON-NLS-1$

	private static final PreferenceKey[] KEYS = new PreferenceKey[] {
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, FORMATTER_INDENTATION_SIZE),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, FORMATTER_TAB_CHAR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, FORMATTER_TAB_SIZE),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, WRAP_COMMENTS),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, WRAP_COMMENTS_LENGTH),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, INDENT_PHP_BODY),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, INDENT_CURLY_BLOCKS),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, INDENT_NAMESPACE_BLOCKS),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, INDENT_CASE_BODY),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, INDENT_SWITCH_BODY),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, INDENT_FUNCTION_BODY),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, INDENT_TYPE_BODY),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, INDENT_BREAK_IN_CASE),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, NEW_LINES_BEFORE_CATCH_STATEMENT),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, NEW_LINES_BEFORE_DO_WHILE_STATEMENT),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, NEW_LINES_BEFORE_ELSE_STATEMENT),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, NEW_LINES_BEFORE_IF_IN_ELSEIF_STATEMENT),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, NEW_LINES_BETWEEN_ARRAY_CREATION_ELEMENTS),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, LINES_AFTER_FUNCTION_DECLARATION),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, LINES_AFTER_TYPE_DECLARATION),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, PRESERVED_LINES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, BRACE_POSITION_BLOCK),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, BRACE_POSITION_BLOCK_IN_CASE),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, BRACE_POSITION_BLOCK_IN_SWITCH),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, BRACE_POSITION_FUNCTION_DECLARATION),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, BRACE_POSITION_TYPE_DECLARATION),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_STATIC_INVOCATION_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_STATIC_INVOCATION_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_ASSIGNMENT_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_ASSIGNMENT_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_COMMAS),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_COMMAS),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_CASE_COLON_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_CASE_COLON_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_COLON),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_COLON),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_SEMICOLON),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_SEMICOLON),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_CONCATENATION_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_CONCATENATION_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_ARROW_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_ARROW_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_KEY_VALUE_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_KEY_VALUE_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_RELATIONAL_OPERATORS),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_RELATIONAL_OPERATORS),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_CONDITIONAL_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_CONDITIONAL_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_POSTFIX_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_POSTFIX_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_PREFIX_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_PREFIX_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_ARITHMETIC_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_ARITHMETIC_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_UNARY_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_UNARY_OPERATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_NAMESPACE_SEPARATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_NAMESPACE_SEPARATOR),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_FOR_SEMICOLON),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_FOR_SEMICOLON),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, FORMATTER_OFF_ON_ENABLED),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, FORMATTER_ON),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, FORMATTER_OFF),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_OPENING_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_OPENING_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_CLOSING_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_OPENING_DECLARATION_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_OPENING_DECLARATION_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_CLOSING_DECLARATION_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_OPENING_INVOCATION_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_OPENING_INVOCATION_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_CLOSING_INVOCATION_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_OPENING_ARRAY_ACCESS_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_OPENING_ARRAY_ACCESS_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_CLOSING_ARRAY_ACCESS_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_OPENING_LOOP_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_OPENING_LOOP_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_CLOSING_LOOP_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_OPENING_CONDITIONAL_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_OPENING_CONDITIONAL_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_CLOSING_CONDITIONAL_PARENTHESES) };

	public PreferenceKey[] getPreferenceKeys()
	{
		return KEYS;
	}

	public IScriptFormatter createFormatter(String lineSeparator, Map<String, String> preferences)
	{
		return new PHPFormatter(lineSeparator, preferences, getMainContentType());
	}

	public URL getPreviewContent()
	{
		return getClass().getResource(FORMATTER_PREVIEW_FILE);
	}

	public IFormatterModifyDialog createDialog(IFormatterModifyDialogOwner dialogOwner)
	{
		return new PHPFormatterModifyDialog(dialogOwner, this);
	}

	public SourceViewerConfiguration createSimpleSourceViewerConfiguration(ISharedTextColors colorManager,
			IPreferenceStore preferenceStore, ITextEditor editor, boolean configureFormatter)
	{
		return new PHPSourceViewerConfiguration(preferenceStore, (AbstractThemeableEditor) editor);
	}

	public PreferenceKey getFormatterPreferenceKey()
	{
		return FORMATTER_PREF_KEY;
	}

	public IPreferenceStore getPreferenceStore()
	{
		return PHPCodeFormatterPlugin.getDefault().getPreferenceStore();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.IScriptFormatterFactory#getPartitioningConfiguration()
	 */
	public Object getPartitioningConfiguration()
	{
		return PHPSourceConfiguration.getDefault();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.AbstractScriptFormatterFactory#getEclipsePreferences()
	 */
	@Override
	protected IEclipsePreferences getEclipsePreferences()
	{
		return EclipseUtil.instanceScope().getNode(PHPEplPlugin.PLUGIN_ID);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.AbstractScriptFormatterFactory#getFormatterTabPolicy()
	 */
	@Override
	protected String getFormatterTabPolicy(Map<String, String> preferences)
	{
		return preferences.get(FORMATTER_TAB_CHAR);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.AbstractScriptFormatterFactory#getEditorTabSize()
	 */
	@Override
	protected int getEditorTabSize()
	{
		return EditorUtil.getSpaceIndentSize(PHPEplPlugin.getDefault().getBundle().getSymbolicName());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.AbstractScriptFormatterFactory#getDefaultEditorTabSize()
	 */
	protected int getDefaultEditorTabSize()
	{
		return EditorUtil.getDefaultSpaceIndentSize(PHPEplPlugin.getDefault().getBundle().getSymbolicName());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.AbstractScriptFormatterFactory#getFormatterTabSizeKey()
	 */
	@Override
	protected String getFormatterTabSizeKey()
	{
		return FORMATTER_TAB_SIZE;
	}
}
