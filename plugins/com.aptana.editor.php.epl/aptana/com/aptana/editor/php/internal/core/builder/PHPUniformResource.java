package com.aptana.editor.php.internal.core.builder;

import java.io.File;
import java.net.URI;

import org.eclipse.core.runtime.IPath;

import com.aptana.core.resources.AbstractUniformResource;

/**
 * A PHP uniform resource implementation
 */
public class PHPUniformResource extends AbstractUniformResource
{

	private URI uri;

	/**
	 * Constructs a new PHPUniformResource with a given {@link File}.
	 * 
	 * @param file
	 */
	public PHPUniformResource(File file)
	{
		uri = file.toURI();
	}

	/**
	 * Constructs a new PHPUniformResource with a given {@link IPath}.
	 * 
	 * @param path
	 */
	public PHPUniformResource(IPath path)
	{
		uri = path.toFile().toURI();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.core.resources.IUniformResource#getURI()
	 */
	public URI getURI()
	{
		return uri;
	}
}