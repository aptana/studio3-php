package com.aptana.editor.php.internal.contentAssist.mapping;

import org.eclipse.core.filesystem.IFileStore;

import com.aptana.editor.php.internal.contentAssist.PHPTokenType;
import com.aptana.parsing.lexer.Lexeme;

public interface ICodeLocation
{
	Lexeme<PHPTokenType> getStartLexeme();

	String getFullPath();

	IFileStore getRemoteFileStore();
}
