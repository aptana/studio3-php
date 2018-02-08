package com.aptana.editor.php.internal.parser;

import org2.eclipse.php.internal.core.PHPVersion;

import com.aptana.core.util.ImmutableTupleN;
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
	private final static ImmutableTupleN cacheKey = new ImmutableTupleN();

	public PHPParseState(String source, int startingOffset, PHPVersion version, IModule module,
			ISourceModule sourceModule)
	{
		super(source, startingOffset);
		this.phpVersion = version;
		this.module = module;
		this.sourceModule = sourceModule;
	}

	@Override
	protected ImmutableTupleN calculateCacheKey()
	{
		return cacheKey; // empty immutable tuple
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
		// TODO: on PHP errors are only reported in a reparse, so, we use the same cache-key all the time just say
		// that a reparse is required because otherwise errors are not displayed if the ast is in the cache.
		// This is something that should probably be fixed... instead of using the build collector
		// to flush errors, the errors should be added to the ParseResult and later applied from that
		// parse result to actual markers.
		//
		// See:
		// com.aptana.editor.php.internal.parser.PHPParser.parse(IParseState, WorkingParseResult): ast.flushErrors()
		return new ParseStateCacheKey(super.getCacheKey(contentTypeId))
		{
			@Override
			public boolean requiresReparse(IParseStateCacheKey newCacheKey)
			{
				return true;
			}
		};
	}

}
