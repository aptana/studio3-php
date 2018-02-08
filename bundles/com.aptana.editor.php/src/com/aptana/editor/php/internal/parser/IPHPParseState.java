package com.aptana.editor.php.internal.parser;

import org2.eclipse.php.internal.core.PHPVersion;

import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.parsing.IParseState;

/**
 * A PHP parse state that contains PHP version information that can be used by the PHP parse.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public interface IPHPParseState extends IParseState
{
	/**
	 * Returns the version associated to this parse state.
	 * 
	 * @return A {@link PHPVersion}.
	 */
	public PHPVersion getPHPVersion();


	/**
	 * Returns the {@link IModule} bound to this parse state
	 * 
	 * @return {@link IModule}
	 */
	public IModule getModule();

	/**
	 * Returns the {@link ISourceModule} bound to this parse state
	 * 
	 * @return {@link ISourceModule}
	 */
	public ISourceModule getSourceModule();
}
