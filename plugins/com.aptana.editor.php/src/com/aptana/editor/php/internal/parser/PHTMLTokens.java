/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Shalom
 */
@SuppressWarnings("nls")
public class PHTMLTokens
{
	public static final short UNKNOWN = -1;
	public static final short EOF = 0;
	public static final short PHP = 200;
	public static final short PHP_END = 201;
	public static final short PHP_HEREDOC = 202;
	private static final short MAXIMUM = 3;
	private static final short OFFSET = 200;

	private static final String[] NAMES = { "EOF", "PHP", "PHP_END", "PHP_HEREDOC" };
	private static final String NAME_UNKNOWN = "UNKNOWN"; //$NON-NLS-1$

	private static Map<String, Short> nameIndexMap;

	/**
	 * @param token
	 * @return
	 */
	public static String getTokenName(short token)
	{
		init();
		token -= OFFSET;
		if (token < 0 || token > MAXIMUM)
		{
			return NAME_UNKNOWN;
		}
		return NAMES[(int)token];
	}

	public static short getToken(String tokenName)
	{
		init();
		Short token = nameIndexMap.get(tokenName);
		return (token == null) ? UNKNOWN : token;
	}

	private static void init()
	{
		if (nameIndexMap == null)
		{
			nameIndexMap = new HashMap<String, Short>();
			short index = OFFSET;
			for (String name : NAMES)
			{
				nameIndexMap.put(name, index++);
			}
		}
	}
}
