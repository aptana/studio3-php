package com.aptana.editor.php.internal.parser;

import org.eclipse.php.internal.core.PHPVersion;

import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.internal.builder.IModule;
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
	@Override
	public PHPVersion getPHPVersion()
	{
		return phpVersion;
	}

	@Override
	public void phpVersionChanged(PHPVersion newVersion)
	{
		this.phpVersion = newVersion;
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
}
