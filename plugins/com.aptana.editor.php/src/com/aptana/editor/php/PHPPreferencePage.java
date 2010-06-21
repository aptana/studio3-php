package com.aptana.editor.php;

import com.aptana.editor.php.ui.preferences.GenericRootPreferencePage;

/**
 * The root preference page for all the other PHP preference pages.<br>
 * At the moment, this root only extends the {@link GenericRootPreferencePage} and display links to the pages that are
 * nested into it. This will change in the near future once we put more content that fits the general PHP settings.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPPreferencePage extends GenericRootPreferencePage
{
	protected static String PAGE_ID = "com.aptana.editor.php.preferences.php"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.ui.preferences.GenericRootPreferencePage#getPageId()
	 */
	@Override
	protected String getPageId()
	{
		return PAGE_ID;
	}

}
