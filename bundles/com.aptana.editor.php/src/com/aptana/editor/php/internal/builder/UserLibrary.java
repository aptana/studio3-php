/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Pavel Petrochenko
 */
public class UserLibrary implements IPHPLibrary
{

	private String name;

	private List<String> directories;

	public UserLibrary(String serializedLibrary)
	{
		String[] split = serializedLibrary.split(File.pathSeparator);
		name = split[0].trim();
		directories = new ArrayList<String>();
		for (int a = 1; a < split.length; a++)
		{
			directories.add(split[a].trim());
		}
	}

	public UserLibrary(String text, String[] dirs)
	{
		this.name = text;
		directories = new ArrayList<String>(Arrays.asList(dirs));
	}

	public List<String> getDirectories()
	{
		return new ArrayList<String>(directories);
	}

	public String getName()
	{
		return name;
	}

	public String toString()
	{
		StringBuilder bld = new StringBuilder();
		bld.append(name);
		bld.append(File.pathSeparator);
		for (String s : directories)
		{
			bld.append(s);
			bld.append(File.pathSeparator);
		}
		bld.deleteCharAt(bld.length() - 1);
		return bld.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserLibrary other = (UserLibrary) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getId()
	{
		return name;
	}

	public boolean isTurnedOn()
	{
		return LibraryManager.getInstance().isTurnedOn(this);
	}

}
