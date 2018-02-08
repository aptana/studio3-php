/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.editor.scanner;

import java.io.IOException;
import java.io.StringReader;

import java_cup.sym;
import java_cup.runtime.Symbol;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org2.eclipse.php.internal.core.PHPVersion;
import org2.eclipse.php.internal.core.ast.scanner.AstLexer;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.core.ast.ASTFactory;
import com.aptana.editor.php.internal.ui.editor.PHPVersionDocumentManager;

/**
 * A token scanner which returns {@link IToken}s for PHP tokens. These can later be mapped to colors.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPTokenScanner implements IPHPTokenScanner
{
	// We need that prefix for our PHP lexer
	protected static final String PHP_PREFIX = "<?php\n"; //$NON-NLS-1$ // $codepro.audit.disable platformSpecificLineSeparator
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
		super();
		if (phpVersion == null)
		{
			throw new IllegalArgumentException("A null PHP Version passed to the PHPTokenScanner"); //$NON-NLS-1$
		}
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
			IdeLog.logError(PHPEditorPlugin.getDefault(), "PHP token-scanner - Error getting the next token", e); //$NON-NLS-1$
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
			phpVersion = PHPVersionDocumentManager.getPHPVersion(document);
			if (phpVersion == null)
			{
				// Set the version to the default (5.3)
				// This will happen when an external file is opened in the editor.
				phpVersion = PHPVersionProvider.getDefaultPHPVersion();
			}
			lexer = ASTFactory.getAstLexer(phpVersion, new StringReader(fContents), true); // $codepro.audit.disable
																							// closeWhereCreated
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
			IdeLog.logError(PHPEditorPlugin.getDefault(), "PHP code-scanner - Error setting the range", e); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "PHP code-scanner - I/O error", e); //$NON-NLS-1$
		}
		origOffset = offset;
	}
	
	public String getContents()
	{
		return fContents;
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
	 * @see com.aptana.editor.php.internal.ui.editor.scanner.IPHPTokenScanner#getPHPVersion()
	 */
	public PHPVersion getPHPVersion()
	{
		return phpVersion;
	}
}
