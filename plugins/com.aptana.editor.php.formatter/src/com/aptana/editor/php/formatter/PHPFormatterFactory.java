/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter;

import static com.aptana.editor.php.formatter.PHPFormatterConstants.*;

import java.net.URL;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.texteditor.ITextEditor;

import com.aptana.editor.common.AbstractThemeableEditor;
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
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, INDENT_BLOCKS),
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
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_PARENTHESES),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_BEFORE_FOR_SEMICOLON),
			new PreferenceKey(PHPCodeFormatterPlugin.PLUGIN_ID, SPACES_AFTER_FOR_SEMICOLON) };

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
}
