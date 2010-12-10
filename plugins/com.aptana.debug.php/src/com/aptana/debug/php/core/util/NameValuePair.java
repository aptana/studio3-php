/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.debug.php.core.util;

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