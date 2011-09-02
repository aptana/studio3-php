package com.aptana.editor.php.internal.ui.preferences;

import java.text.MessageFormat;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;

import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.preferences.IPreferenceConstants;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.core.IPHPConstants;

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
		store.setDefault(IPhpPreferenceConstants.PHPEDITOR_INITIAL_CONTENTS, "<?php\n    phpinfo();\n?>"); //$NON-NLS-1$ // $codepro.audit.disable platformSpecificLineSeparator

		IEclipsePreferences prefs = EclipseUtil.defaultScope().getNode(CommonEditorPlugin.PLUGIN_ID);
		prefs.putBoolean(MessageFormat.format("{0}:{1}", IPHPConstants.CONTENT_TYPE_HTML_PHP, //$NON-NLS-1$
				IPreferenceConstants.PARSE_ERROR_ENABLED), true);
	}

	public static IPreferenceStore getPreferenceStore()
	{
		return PHPEditorPlugin.getDefault().getPreferenceStore();
	}
}
