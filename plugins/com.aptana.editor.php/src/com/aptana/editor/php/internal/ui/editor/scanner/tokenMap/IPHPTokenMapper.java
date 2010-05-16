/**
 * 
 */
package com.aptana.editor.php.internal.ui.editor.scanner.tokenMap;

import java_cup.runtime.Symbol;

import org.eclipse.jface.text.rules.IToken;

import com.aptana.editor.php.internal.ui.editor.scanner.PHPCodeScanner;

/**
 * Map a PHP symbol number to an IToken. A mapper that implements this interface should have specific information about
 * the PHP version that was used while parsing the script.
 */
public interface IPHPTokenMapper
{
	public static final String THIS = "$this"; //$NON-NLS-1$
	public static final String SELF = "self"; //$NON-NLS-1$
	public static final String PARENT = "parent"; //$NON-NLS-1$

	/**
	 * Map the PHP symbol to an IToken
	 * 
	 * @param sym
	 * @param phpCodeScanner
	 * @return A mapped {@link IToken}
	 */
	public IToken mapToken(Symbol sym, PHPCodeScanner phpCodeScanner);
}
