/**
 * Aptana Studio
 * Copyright (c) 2005-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.contentAssist.preferences;

import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.AUTO_ACTIVATE_ON_IDENTIFIERS;
import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.CONTENT_ASSIST_FILTER_TYPE;
import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.CONTENT_ASSIST_INCLUDE_ALL;
import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.INSERT_FUNCTION_PARAMETERS;
import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.INSERT_MODE;
import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.INSERT_MODE_INSERT;
import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.INSERT_OPTIONAL_FUNCTION_PARAMETERS;
import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.INSERT_PARENTHESES_AFTER_METHOD_CALLS;
import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.INSERT_PARENTHESES_AFTER_NEW_INSTANCE;
import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.INSERT_SEMICOLON_AFTER_METHOD_CALLS;
import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.INSERT_SEMICOLON_AFTER_NEW_INSTANCE;
import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.PARAMETRS_TAB_JUMP;
import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.PARSE_UNSAVED_MODULE_ON_IDENTIFIERS_COMPLETION;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.aptana.editor.php.PHPEditorPlugin;

/**
 * Preferences initializer for content assist related preferences.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class ContentAssistPreferencesInitializer extends AbstractPreferenceInitializer
{
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = PHPEditorPlugin.getDefault().getPreferenceStore();
		store.setDefault(AUTO_ACTIVATE_ON_IDENTIFIERS, true);
		store.setDefault(CONTENT_ASSIST_FILTER_TYPE, CONTENT_ASSIST_INCLUDE_ALL);
		store.setDefault(PARSE_UNSAVED_MODULE_ON_IDENTIFIERS_COMPLETION, true);
		store.setDefault(INSERT_FUNCTION_PARAMETERS, true);
		store.setDefault(INSERT_OPTIONAL_FUNCTION_PARAMETERS, false);
		store.setDefault(INSERT_PARENTHESES_AFTER_METHOD_CALLS, false);
		store.setDefault(INSERT_PARENTHESES_AFTER_NEW_INSTANCE, false);
		store.setDefault(INSERT_SEMICOLON_AFTER_METHOD_CALLS, false);
		store.setDefault(INSERT_SEMICOLON_AFTER_NEW_INSTANCE, false);
		store.setDefault(INSERT_MODE, INSERT_MODE_INSERT);
		store.setDefault(PARAMETRS_TAB_JUMP, true);
	}
}
