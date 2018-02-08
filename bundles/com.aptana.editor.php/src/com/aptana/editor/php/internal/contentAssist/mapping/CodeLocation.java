package com.aptana.editor.php.internal.contentAssist.mapping;

import org.eclipse.core.filesystem.IFileStore;

import com.aptana.editor.php.internal.contentAssist.PHPTokenType;
import com.aptana.parsing.lexer.Lexeme;

public class CodeLocation implements ICodeLocation
{

	private String path;
	private Lexeme<PHPTokenType> lexeme;
	private IFileStore remoteFileStore;

	public CodeLocation(String path, Lexeme<PHPTokenType> lexeme)
	{
		this.path = path;
		this.lexeme = lexeme;
	}

	public CodeLocation(IFileStore remoteFileStore, Lexeme<PHPTokenType> lexeme)
	{
		this.lexeme = lexeme;
		this.remoteFileStore = remoteFileStore;
	}

	public String getFullPath()
	{
		return path;
	}

	public Lexeme<PHPTokenType> getStartLexeme()
	{
		return lexeme;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.contentAssist.mapping.ICodeLocation#getRemoteFileStore()
	 */
	public IFileStore getRemoteFileStore()
	{
		return remoteFileStore;
	}
}
