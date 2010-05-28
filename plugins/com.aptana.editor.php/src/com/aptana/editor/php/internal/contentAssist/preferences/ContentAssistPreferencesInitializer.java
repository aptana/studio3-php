package com.aptana.editor.php.internal.contentAssist.preferences;

import static com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants.*;

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
