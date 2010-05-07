package com.aptana.editor.php.internal.builder.preferences;

import java.io.File;

/**
 * @author Pavel Petrochenko
 */
public class PHPExtension
{
	private String name;
	private String path;

	/**
	 * @param open
	 */
	public PHPExtension(String open)
	{
		name = new File(open).getName();
		path = new File(open).getAbsolutePath();
	}

	/**
	 * 
	 */
	public PHPExtension()
	{

	}

	/**
	 * @return name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return path
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * @param path
	 */
	public void setPath(String path)
	{
		this.path = path;
	}

}
