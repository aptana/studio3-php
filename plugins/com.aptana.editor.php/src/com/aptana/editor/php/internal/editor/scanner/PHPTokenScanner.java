package com.aptana.editor.php.internal.editor.scanner;

import java.io.IOException;
import java.io.StringReader;

import java_cup.sym;
import java_cup.runtime.Symbol;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.ast.scanner.AstLexer;

import com.aptana.editor.html.parsing.HTMLTokenScanner;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.ast.ASTFactory;

/**
 * A token scanner which returns {@link IToken}s for PHP tokens. These can later be mapped to colors.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPTokenScanner extends HTMLTokenScanner implements IPHPTokenScanner
{
	// We need that prefix for our PHP lexer
	protected static final String PHP_PREFIX = "<?php\n"; //$NON-NLS-1$
	private int fTokenLength;
	private int fOffset;

	private int origOffset;
	private String fContents;
	private AstLexer lexer;
	private PHPVersion phpVersion;
	private Symbol nextNextSymbol;

	/**
	 * Constructs a new PHPTokenScanner with a given {@link PHPVersion}.
	 * 
	 * @param phpVersion
	 */
	public PHPTokenScanner(PHPVersion phpVersion)
	{
		if (phpVersion == null)
		{
			throw new IllegalArgumentException("A null PHP Version passed to the PHPTokenScanner"); //$NON-NLS-1$
		}
		// TODO: Shalom - Since this is only called once per studio session, we need to listen to the PHP versions
		// changes and update the phpVersion instance variable
		this.phpVersion = phpVersion;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenLength()
	 */
	public int getTokenLength()
	{
		return fTokenLength;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenOffset()
	 */
	public int getTokenOffset()
	{
		return fOffset;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
	 */
	public IToken nextToken()
	{
		Symbol nextSymbol = nextNextSymbol;
		try
		{
			nextNextSymbol = lexer.next_token();
		}
		catch (Exception e)
		{
			PHPEditorPlugin.logError(e);
			nextNextSymbol = null;
		}
		IToken token = createToken(nextSymbol);

		fTokenLength = 0;
		if (token != Token.EOF)
		{
			// Check for a white-space gap with the next-next-symbol (if not EOF)
			int whiteSpacesGap = 0;
			if (nextNextSymbol != null && nextNextSymbol.sym != sym.EOF)
			{
				whiteSpacesGap = nextNextSymbol.left - nextSymbol.right;
			}
			fTokenLength = nextSymbol.right - nextSymbol.left + whiteSpacesGap;
			fOffset = origOffset + nextSymbol.left - PHP_PREFIX.length();
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(org.eclipse.jface.text.IDocument, int, int)
	 */
	public void setRange(IDocument document, int offset, int length)
	{
		try
		{
			fContents = document.get(offset, length);
			fContents = PHP_PREFIX + fContents;
			lexer = ASTFactory.getAstLexer(phpVersion, new StringReader(fContents));
			// read the next token already, so we can always calculate the spaces between the
			// tokens and return the right offset and length.
			try
			{
				nextNextSymbol = lexer.next_token();
			}
			catch (Exception e)
			{
				nextNextSymbol = null;
			}
		}
		catch (BadLocationException e)
		{
			PHPEditorPlugin.logError(e);
		}
		catch (IOException e)
		{
			PHPEditorPlugin.logError(e);
		}
		origOffset = offset;
	}

	/**
	 * Creates an {@link IToken} from a {@link Symbol}
	 * 
	 * @param symbol
	 * @return {@link IToken}
	 */
	private IToken createToken(Symbol symbol)
	{
		if (symbol == null || symbol.sym == sym.EOF)
		{
			return Token.EOF;
		}
		else
		{
			return new Token(symbol);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.editor.scanner.IPHPTokenScanner#getPHPVersion()
	 */
	@Override
	public PHPVersion getPHPVersion()
	{
		return phpVersion;
	}
}
