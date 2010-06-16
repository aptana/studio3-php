package com.aptana.editor.php.internal.contentAssist;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.documentModel.parser.AbstractPhpLexer;
import org.eclipse.php.internal.core.documentModel.parser.PhpLexerFactory;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.internal.ui.editor.PHPVersionDocumentManager;

/**
 * A PHP token scanner that scans and create tokens that are used when calculating the code assist scope.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPScopeScanner implements ITokenScanner
{
	private AbstractPhpLexer lexer;
	private int documetOffset;
	private int prevTokenOffset;

	@Override
	public int getTokenLength()
	{
		return lexer.yylength();
	}

	@Override
	public int getTokenOffset()
	{
		return documetOffset + lexer.getTokenStart();
	}

	@Override
	public IToken nextToken()
	{
		try
		{
			String token = lexer.getNextToken();
			int tokenOffset = getTokenOffset();
			if (prevTokenOffset == tokenOffset)
			{
				// we stumble into a case where the lexer failed to notify us with the end
				// token, so force a stop.
				return Token.EOF;
			}
			prevTokenOffset = tokenOffset;
			if (token == null)
			{
				return Token.EOF;
			}
			return new Token(token);
		}
		catch (IOException e)
		{
			PHPEditorPlugin.logError(e);
		}
		return Token.EOF;
	}

	@Override
	public void setRange(IDocument document, int offset, int length)
	{
		this.documetOffset = offset;
		this.prevTokenOffset = -1;
		PHPVersion phpVersion = PHPVersionDocumentManager.getPHPVersion(document);
		if (phpVersion == null)
		{
			phpVersion = PHPVersionProvider.getDefaultPHPVersion();
		}
		String content;
		try
		{
			content = document.get(offset, length);

			lexer = PhpLexerFactory.createLexer(new StringReader(content), phpVersion);
			// set initial lexer state - we use reflection here since we don't
			// know the constant value of
			// of this state in specific PHP version lexer
			int state = lexer.getClass().getField("ST_PHP_IN_SCRIPTING").getInt(lexer); //$NON-NLS-1$
			lexer.initialize(state);
			lexer.setPatterns(null);
			lexer.setAspTags(true);
		}
		catch (Exception e)
		{
			PHPEditorPlugin.logError(e);
		}
	}

}
