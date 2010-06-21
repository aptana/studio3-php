package com.aptana.editor.php.internal.contentAssist.mapping;

import com.aptana.editor.php.internal.contentAssist.PHPTokenType;
import com.aptana.parsing.lexer.Lexeme;

public interface ICodeLocation
{
	public Lexeme<PHPTokenType> getStartLexeme();

	public String getFullPath();
}
