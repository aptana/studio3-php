package com.aptana.editor.php.core;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * Preferences initializer.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPCorePreferenceInitializer extends AbstractPreferenceInitializer
{
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences()
	{
		CorePreferenceConstants.initializeDefaultValues();
	}

}