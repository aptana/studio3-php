/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.editor.scanner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

import com.aptana.editor.common.text.rules.CommentScanner;
import com.aptana.editor.php.internal.parser.PHPTokenType;

@SuppressWarnings("nls")
public class PHPDocScanner extends CommentScanner
{

	private static Set<String> TAGS = new HashSet<String>();
	static
	{
		TAGS.add("@abstract");
		TAGS.add("@access");
		TAGS.add("@author");
		TAGS.add("@category");
		TAGS.add("@copyright");
		TAGS.add("@deprecated");
		TAGS.add("@example");
		TAGS.add("@filesource");
		TAGS.add("@final");
		TAGS.add("@global");
		TAGS.add("@ignore");
		TAGS.add("@internal");
		TAGS.add("@license");
		TAGS.add("@link");
		TAGS.add("@method");
		TAGS.add("@name");
		TAGS.add("@package");
		TAGS.add("@param");
		TAGS.add("@property");
		TAGS.add("@return");
		TAGS.add("@see");
		TAGS.add("@since");
		TAGS.add("@static");
		TAGS.add("@staticvar");
		TAGS.add("@subpackage");
		TAGS.add("@throws");
		TAGS.add("@todo");
		TAGS.add("@tutorial");
		TAGS.add("@uses");
		TAGS.add("@var");
		TAGS.add("@version");
		TAGS.add("@xlink");
	}

	public PHPDocScanner()
	{
		super(new Token(PHPTokenType.COMMENT_PHPDOC.toString()));
	}

	protected List<IRule> createRules()
	{
		List<IRule> rules = super.createRules();

		WordRule wordRule = new WordRule(new PHPTagDetector(), Token.UNDEFINED, true);
		IToken tagToken = new Token(PHPTokenType.KEYWORD_OTHER.toString());
		for (String tag : TAGS)
		{
			wordRule.addWord(tag, tagToken);
		}
		rules.add(wordRule);

		return rules;
	}

	/**
	 * Detects PHPDoc tags
	 * 
	 * @author cwilliams
	 */
	private static class PHPTagDetector implements IWordDetector
	{

		public boolean isWordStart(char c)
		{
			return c == '@';
		}

		public boolean isWordPart(char c)
		{
			return Character.isLetter(c);
		}

	}

}
