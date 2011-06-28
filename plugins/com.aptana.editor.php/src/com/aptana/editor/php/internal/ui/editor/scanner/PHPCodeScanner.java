/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.editor.scanner;

import java.util.LinkedList;
import java.util.Queue;

import java_cup.runtime.Symbol;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.internal.ui.editor.scanner.tokenMap.IPHPTokenMapper;
import com.aptana.editor.php.internal.ui.editor.scanner.tokenMap.PHPTokenMapperFactory;

/**
 * Hook the php token scanner and the parser to tokenize
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPCodeScanner implements ITokenScanner
{

	private IPHPTokenScanner fScanner;
	private Queue<QueuedToken> queue;
	private int fLength;
	private int fOffset;
	private IDocument document;
	private int originalDocumentOffset;
	private IToken lastToken;

	/**
	 * Constructs a new PHPCodeScanner
	 */
	public PHPCodeScanner()
	{
		fScanner = new PHPTokenScanner(PHPVersionProvider.getPHPVersion(null));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenLength()
	 */
	public int getTokenLength()
	{
		return fLength;
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
		IToken token = pop();
		if (token.isEOF())
			return Token.EOF;
		Symbol sym = (Symbol) token.getData();

		IPHPTokenMapper tokenMapper = PHPTokenMapperFactory.getMapper(fScanner.getPHPVersion());
		token = tokenMapper.mapToken(sym, this);
		// token scope is "default.php" and last was "storage.type.function.php", make this one
		// "entity.name.function.php"
		if (scopeEquals(lastToken, "storage.type.function.php") //$NON-NLS-1$
				&& (scopeEquals(token, StringUtil.EMPTY) || scopeEquals(token, "constant.other.php"))) //$NON-NLS-1$
		{
			token = getToken("entity.name.function.php"); //$NON-NLS-1$
		}
		// token scope is "default.php" and last was "storage.type.class.php", make this one
		// "entity.name.type.class.php"
		else if (scopeEquals(lastToken, "storage.type.class.php") //$NON-NLS-1$
				&& (scopeEquals(token, StringUtil.EMPTY) || scopeEquals(token, "constant.other.php"))) //$NON-NLS-1$
		{
			token = getToken("entity.name.type.class.php"); //$NON-NLS-1$
		}

		if (token.isOther())
		{
			lastToken = token;
		}
		return token;
	}

	private boolean scopeEquals(IToken token, String scope)
	{
		return token != null && token.getData() != null && scope.equals(token.getData());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(org.eclipse.jface.text.IDocument, int, int)
	 */
	public void setRange(IDocument document, int offset, int length)
	{
		this.document = document;
		this.originalDocumentOffset = offset;
		fScanner.setRange(document, offset, length);
		reset();
	}

	/**
	 * Returns the string value in the document that was represented by the given symbol.<br>
	 * The extraction of the value is done from the document range that was set in the
	 * {@link #setRange(IDocument, int, int)} call.
	 * 
	 * @param sym
	 * @return A String value extracted from the document
	 */
	public String getSymbolValue(Symbol sym)
	{
		try
		{
			return document.get(originalDocumentOffset + sym.left - PHPTokenScanner.PHP_PREFIX.length(), sym.right
					- sym.left);
		}
		catch (Exception e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "PHP code-scanner - Error getting a symbol value", e); //$NON-NLS-1$
		}
		return null;
	}

	public IToken getToken(String tokenName)
	{
		return new Token(tokenName);
	}

	public int peek()
	{
		int oldOffset = getTokenOffset();
		int oldLength = getTokenLength();
		IToken next = pop();
		push(next);
		fOffset = oldOffset;
		fLength = oldLength;
		if (next.isEOF())
		{
			return -1;
		}
		Integer data = (Integer) next.getData();
		return data.intValue();
	}

	private IToken pop()
	{
		IToken token = null;
		if (queue == null || queue.isEmpty())
		{
			token = fScanner.nextToken();
			fOffset = fScanner.getTokenOffset();
			fLength = fScanner.getTokenLength();
		}
		else
		{
			QueuedToken queued = queue.poll();
			fOffset = queued.getOffset();
			fLength = queued.getLength();
			token = queued.getToken();
		}
		if (token == null || token.isEOF())
			return Token.EOF;
		return token;
	}

	private void push(IToken next)
	{
		if (queue == null)
		{
			queue = new LinkedList<QueuedToken>();
		}
		queue.add(new QueuedToken(next, getTokenOffset(), getTokenLength()));
	}

	private void reset()
	{
		queue = null;
		lastToken = null;
	}
}