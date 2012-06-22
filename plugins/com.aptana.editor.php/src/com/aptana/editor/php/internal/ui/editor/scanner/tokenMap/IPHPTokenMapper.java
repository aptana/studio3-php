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
	public static final String COOKIE = "$_COOKIE"; //$NON-NLS-1$
	public static final String GET = "$_GET"; //$NON-NLS-1$
	public static final String POST = "$_POST"; //$NON-NLS-1$
	public static final String REQUEST = "$_REQUEST"; //$NON-NLS-1$
	public static final String FILES = "$_FILES"; //$NON-NLS-1$
	public static final String GLOBALS = "$GLOBALS"; //$NON-NLS-1$
	public static final String ENV = "$_ENV"; //$NON-NLS-1$
	public static final String SERVER = "$_SERVER"; //$NON-NLS-1$
	public static final String SESSION = "$_SESSION"; //$NON-NLS-1$
	public static final String PHP_SELF = "$PHP_SELF"; //$NON-NLS-1$
	public static final String HTTP_POST_VARS = "$HTTP_POST_VARS"; //$NON-NLS-1$
	public static final String HTTP_GET_VARS = "$HTTP_GET_VARS"; //$NON-NLS-1$
	public static final String HTTP_ENV_VARS = "$HTTP_ENV_VARS"; //$NON-NLS-1$
	public static final String HTTP_SERVER_VARS = "$HTTP_SERVER_VARS"; //$NON-NLS-1$
	public static final String HTTP_COOKIE_VARS = "$HTTP_COOKIE_VARS"; //$NON-NLS-1$

	public static final String SELF = "self"; //$NON-NLS-1$
	public static final String PARENT = "parent"; //$NON-NLS-1$
	public static final String TRUE = "true"; //$NON-NLS-1$
	public static final String FALSE = "false"; //$NON-NLS-1$
	public static final String NULL = "null"; //$NON-NLS-1$
	public static final String ON = "on"; //$NON-NLS-1$
	public static final String OFF = "off"; //$NON-NLS-1$
	public static final String YES = "yes"; //$NON-NLS-1$
	public static final String NO = "no"; //$NON-NLS-1$
	public static final String NL = "nl"; //$NON-NLS-1$
	public static final String BR = "br"; //$NON-NLS-1$
	public static final String TAB = "tab"; //$NON-NLS-1$

	/**
	 * Map the PHP symbol to an IToken
	 * 
	 * @param sym
	 * @param phpCodeScanner
	 * @return A mapped {@link IToken}
	 */
	public IToken mapToken(Symbol sim, PHPCodeScanner phpCodeScanner);
}
