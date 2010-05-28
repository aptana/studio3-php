package com.aptana.editor.php.internal.contentAssist;

import com.aptana.parsing.lexer.ITypePredicate;

/**
 * A PHP token type predicate that is used with the content assist processor.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPTokenType implements ITypePredicate
{

	private String type;

	/**
	 * Constructs a new PHPTokenType with the type name
	 * 
	 * @param type
	 */
	public PHPTokenType(String type)
	{
		this.type = type;
	}

	@Override
	public boolean isDefined()
	{
		return true;
	}

	public String getType()
	{
		return type;
	}
}
