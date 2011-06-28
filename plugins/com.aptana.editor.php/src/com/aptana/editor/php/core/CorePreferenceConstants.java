package com.aptana.editor.php.core;

import java.util.Locale;

import org.eclipse.jface.preference.IPreferenceStore;

import com.aptana.editor.php.PHPEditorPlugin;

public class CorePreferenceConstants
{

	public interface Keys
	{
		public static final String PHP_VERSION = IPHPCoreConstants.PHP_OPTIONS_PHP_VERSION;
		public static final String EDITOR_USE_ASP_TAGS = "use_asp_tags"; //$NON-NLS-1$
	}

	public static IPreferenceStore getPreferenceStore()
	{
		return PHPEditorPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Initializes the given preference store with the default values.
	 * 
	 * @param store
	 *            the preference store to be initialized
	 */
	public static void initializeDefaultValues()
	{
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(Keys.PHP_VERSION, IPHPCoreConstants.PHP53);
		store.setDefault(Keys.EDITOR_USE_ASP_TAGS, false);

		store.setDefault(IPHPCoreConstants.FORMATTER_USE_TABS, true);
		store.setDefault(IPHPCoreConstants.FORMATTER_INDENTATION_SIZE, IPHPCoreConstants.DEFAULT_INDENTATION_SIZE);

		if ((store.getString(IPHPCoreConstants.WORKSPACE_DEFAULT_LOCALE)).equals("")) { //$NON-NLS-1$
			store.setValue(IPHPCoreConstants.WORKSPACE_DEFAULT_LOCALE, Locale.getDefault().toString());
			store.setDefault(IPHPCoreConstants.WORKSPACE_LOCALE, Locale.getDefault().toString());
		}
		store.setDefault(
				com.aptana.editor.common.contentassist.IPreferenceConstants.CONTEXT_INFORMATION_ACTIVATION_CHARACTERS,
				"(,"); //$NON-NLS-1$
	}

	// Don't instantiate
	private CorePreferenceConstants()
	{
	}
}
