package com.aptana.editor.php.internal.contentAssist.mapping;

import com.aptana.editor.php.internal.contentAssist.PHPTokenType;
import com.aptana.parsing.lexer.Lexeme;

public class CodeLocation implements ICodeLocation
{

	private String path;
	private Lexeme<PHPTokenType> lexeme;

	public CodeLocation(String path, Lexeme<PHPTokenType> lexeme)
	{
		this.path = path;
		this.lexeme = lexeme;
	}

	public String getFullPath()
	{
		return path;
	}

	public Lexeme<PHPTokenType> getStartLexeme()
	{
		return lexeme;
	}

}
