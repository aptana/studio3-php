package com.aptana.editor.php.internal.parser;

import org2.eclipse.php.internal.core.PHPVersion;

import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.parsing.IParseStateCacheKey;
import com.aptana.parsing.ParseState;
import com.aptana.parsing.ParseStateCacheKey;

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

	public PHPParseState(String source, int startingOffset, PHPVersion version, IModule module,
			ISourceModule sourceModule)
	{
		super(source, startingOffset);
		this.phpVersion = version;
		this.module = module;
		this.sourceModule = sourceModule;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.parser.IPHPParseState#getPHPVersion()
	 */
	public PHPVersion getPHPVersion()
	{
		return phpVersion;
	}

	public IModule getModule()
	{
		return this.module;
	}

	public ISourceModule getSourceModule()
	{
		return this.sourceModule;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.parsing.ParseState#getCacheKey(java.lang.String)
	 */
	@Override
	public IParseStateCacheKey getCacheKey(String contentTypeId)
	{
		// Note: not adding the sourceModule because it's just a wrapper over the module.
		// As for the module, it should be lightweight enough to be on the cache key (just has
		// a reference for IFile or File).
		return new ParseStateCacheKey(super.getCacheKey(contentTypeId), phpVersion, this.module);
	}

}
