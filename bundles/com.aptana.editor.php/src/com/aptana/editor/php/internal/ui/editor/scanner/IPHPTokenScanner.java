package com.aptana.editor.php.internal.ui.editor.scanner;

import org.eclipse.jface.text.rules.ITokenScanner;
import org2.eclipse.php.internal.core.PHPVersion;

/**
 * A PHP token scanner interface.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public interface IPHPTokenScanner extends ITokenScanner
{

	/**
	 * Returns the PHP version that was set to this token scanner.
	 * 
	 * @return {@link PHPVersion}
	 */
	public PHPVersion getPHPVersion();

	public String getContents();
}
