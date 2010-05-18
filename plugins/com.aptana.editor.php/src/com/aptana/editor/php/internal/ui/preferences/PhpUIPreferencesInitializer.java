package com.aptana.editor.php.internal.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.aptana.editor.php.PHPEditorPlugin;

/**
 * PHP UI preferences initializer.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PhpUIPreferencesInitializer extends AbstractPreferenceInitializer
{

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(IPhpPreferenceConstants.PHPEDITOR_INITIAL_PROJECT_FILE_NAME, "index.php"); //$NON-NLS-1$
		store.setDefault(IPhpPreferenceConstants.PHPEDITOR_INITIAL_PROJECT_FILE_CREATE, true);
		store.setDefault(IPhpPreferenceConstants.PHPEDITOR_INITIAL_CONTENTS, "<?php\n    phpinfo();\n?>"); //$NON-NLS-1$
	}

	public static IPreferenceStore getPreferenceStore()
	{
		return PHPEditorPlugin.getDefault().getPreferenceStore();
	}
}
