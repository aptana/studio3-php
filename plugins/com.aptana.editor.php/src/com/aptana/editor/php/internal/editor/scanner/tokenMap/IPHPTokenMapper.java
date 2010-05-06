/**
 * 
 */
package com.aptana.editor.php.internal.editor.scanner.tokenMap;

import org.eclipse.jface.text.rules.IToken;

import com.aptana.editor.php.internal.editor.scanner.PHPCodeScanner;

/**
 * Map a PHP symbol number to an IToken. A mapper that implements this interface should have specific information about
 * the PHP version that was used while parsing the script.
 */
public interface IPHPTokenMapper
{

	/**
	 * Map the PHP symbol to an IToken
	 * 
	 * @param sym
	 * @param phpCodeScanner 
	 * @return A mapped {@link IToken}
	 */
	public IToken mapToken(int sym, PHPCodeScanner phpCodeScanner);
}
