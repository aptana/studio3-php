package com.aptana.editor.php.internal.parser;

import org.eclipse.php.internal.core.PHPVersion;

import com.aptana.editor.php.core.IPHPVersionListener;
import com.aptana.parsing.IParseState;

/**
 * A PHP parse state that contains PHP version information that can be used by the PHP parse.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public interface IPHPParseState extends IParseState, IPHPVersionListener
{
	/**
	 * Returns the version associated to this parse state.
	 * 
	 * @return A {@link PHPVersion}.
	 */
	public PHPVersion getPHPVersion();
}
