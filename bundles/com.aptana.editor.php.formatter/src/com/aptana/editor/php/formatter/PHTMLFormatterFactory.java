/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter;

import java.util.Map;

import com.aptana.editor.html.formatter.HTMLFormatterFactory;
import com.aptana.formatter.IScriptFormatter;

/**
 * PHP formatter factory
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHTMLFormatterFactory extends HTMLFormatterFactory
{

	public IScriptFormatter createFormatter(String lineSeparator, Map<String, String> preferences)
	{
		return new PHTMLFormatter(lineSeparator, preferences, getMainContentType());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.AbstractScriptFormatterFactory#isContributingToUI()
	 */
	@Override
	public boolean isContributingToUI()
	{
		return false;
	}
}
