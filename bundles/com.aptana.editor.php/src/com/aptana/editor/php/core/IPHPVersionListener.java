package com.aptana.editor.php.core;

import org2.eclipse.php.internal.core.PHPVersion;

/**
 * A listener interface for any implementation that wish to be notified when the project's PHP version changes.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public interface IPHPVersionListener
{
	/**
	 * A notification method that is triggered when a different PHP version is set.
	 * 
	 * @param newVersion
	 */
	public void phpVersionChanged(PHPVersion newVersion);
}
