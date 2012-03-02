package com.aptana.editor.php.internal.parser;

import org2.eclipse.php.internal.core.PHPVersion;

import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.ParseState;

/**
 * A PHP parse state implementation with the ability to set and get a PHP version.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPParseState extends ParseState implements IPHPParseState
{

	private PHPVersion phpVersion;
	private IModule module;
	private ISourceModule sourceModule;

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.parser.IPHPParseState#getPHPVersion()
	 */
	public PHPVersion getPHPVersion()
	{
		return phpVersion;
	}

	public void phpVersionChanged(PHPVersion newVersion)
	{
		setPHPVersion(newVersion);
	}

	public void setPHPVersion(PHPVersion version)
	{
		this.phpVersion = version;
	}

	public void setModule(IModule module)
	{
		this.module = module;
	}

	public IModule getModule()
	{
		return this.module;
	}

	public void setSourceModule(ISourceModule sourceModule)
	{
		this.sourceModule = sourceModule;
	}

	public ISourceModule getSourceModule()
	{
		return this.sourceModule;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.parsing.ParseState#requiresReparse(com.aptana.parsing.IParseState)
	 */
	@Override
	public boolean requiresReparse(IParseState newState)
	{
		// Force a re-parse to avoid the cache.
		// At the moment, the PHP error markers are flushed independently.
		return true;
	}
}
