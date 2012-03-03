/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.util;

import com.aptana.core.util.StringUtil;

/**
 * A simple structure of name and value.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class NameValuePair
{
	public String name;
	public String value;

	/**
	 * Constructs a new Name-Value pair. The name and the value are set to an empty string.
	 */
	public NameValuePair()
	{
		name = StringUtil.EMPTY;
		value = StringUtil.EMPTY;
	}

	/**
	 * Constructs a new Name-Value pair
	 * 
	 * @param name
	 * @param value
	 */
	public NameValuePair(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	/**
	 * Parses a pair string in a 'x=y' format and returns a new NameValuePair.
	 * 
	 * @param pair
	 *            A string in a format of 'x=y'
	 * @return A new NameValuePair; Null if could not parse the pair string.
	 */
	public static NameValuePair fromPairString(String pair)
	{
		String[] nv = pair.split("="); //$NON-NLS-1$
		if (nv.length == 2)
		{
			return new NameValuePair(nv[0], nv[1]);
		}
		else if (nv.length == 1)
		{
			return new NameValuePair(nv[0], StringUtil.EMPTY);
		}
		else
		{
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null || !(obj instanceof NameValuePair))
		{
			return false;
		}
		NameValuePair other = (NameValuePair) obj;
		if (other.name.equals(this.name))
		{
			return other.value == this.value || (other.value != null && other.value.equals(this.value));
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int hash = name.hashCode();
		if (value != null)
		{
			hash += value.hashCode();
		}
		return hash;
	}

	/**
	 * Returns a String representation of this pair in a form of name=value.
	 */
	public String toString()
	{
		return name + '=' + ((value != null) ? value : StringUtil.EMPTY);
	}

}