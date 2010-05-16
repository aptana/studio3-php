package com.aptana.editor.php.internal.ui.editor.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.aptana.editor.php.PHPEditorPlugin;

/**
 * PHP editor preferences initializer.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPEditorPreferenceInitializer extends AbstractPreferenceInitializer
{

	public static IPreferenceStore getPreferenceStore()
	{
		return PHPEditorPlugin.getDefault().getPreferenceStore();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(IPHPEditorPreferencesConstants.PHPEDITOR_EMPTY_NODE_CONTENT_IN_OUTLINE, true);
	}
}