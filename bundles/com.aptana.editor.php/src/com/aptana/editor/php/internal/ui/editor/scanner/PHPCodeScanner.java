/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.editor.scanner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import java_cup.runtime.Symbol;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.CommonUtil;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.internal.parser.PHPTokenType;
import com.aptana.editor.php.internal.ui.editor.scanner.tokenMap.IPHPTokenMapper;
import com.aptana.editor.php.internal.ui.editor.scanner.tokenMap.PHPTokenMapperFactory;

/**
 * Hook the php token scanner and the parser to tokenize
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPCodeScanner implements ITokenScanner
{

	private final IPHPTokenScanner fScanner;
	private Queue<QueuedToken> queue;
	private int fLength;
	private int fOffset;
	private IDocument document;
	private int originalDocumentOffset;
	private IToken lastToken;
	private boolean inFunctionDeclaration;

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
		{
			return Token.EOF;
		}
		IPHPTokenMapper tokenMapper = PHPTokenMapperFactory.getMapper(fScanner.getPHPVersion());
		token = tokenMapper.mapToken((Symbol) token.getData(), this);
		if (scopeEquals(token, PHPTokenType.STORAGE_TYPE_FUNCTION))
		{
			inFunctionDeclaration = true;
		}
		// token scope is "default.php" and last was "storage.type.function.php", make this one
		// "entity.name.function.php"
		else if (scopeEquals(lastToken, PHPTokenType.STORAGE_TYPE_FUNCTION)
				&& (scopeEquals(token, StringUtil.EMPTY) || scopeEquals(token, PHPTokenType.CONSTANT_OTHER)))
		{
			token = getToken(PHPTokenType.ENTITY_FUNCTION.toString());
		}
		// token scope is "default.php" and last was "storage.type.class.php", make this one
		// "entity.name.type.class.php"
		else if (scopeEquals(lastToken, PHPTokenType.STORAGE_TYPE_CLASS)
				&& (scopeEquals(token, StringUtil.EMPTY) || scopeEquals(token, PHPTokenType.CONSTANT_OTHER)))
		{
			token = getToken(PHPTokenType.ENTITY_CLASS.toString());
		}
		// When we hit right paren, jump out of function declaration
		else if (inFunctionDeclaration && scopeEquals(lastToken, PHPTokenType.PUNCTUATION_PARAM_RIGHT))
		{
			inFunctionDeclaration = false;
		}
		// Only use these special scopes for parens in function definitions
		else if (!inFunctionDeclaration
				&& (scopeEquals(token, PHPTokenType.PUNCTUATION_PARAM_LEFT) || scopeEquals(token,
						PHPTokenType.PUNCTUATION_PARAM_RIGHT)))
		{
			token = getToken(StringUtil.EMPTY);
		}
		// number inside array access
		else if (scopeEquals(lastToken, PHPTokenType.PUNCTUATION_LBRACKET)
				&& scopeEquals(token, PHPTokenType.CONSTANT_NUMERIC))
		{
			token = getToken("variable.other.php constant.numeric.php"); //$NON-NLS-1$
		}
		// ->identifier
		else if (scopeEquals(lastToken, PHPTokenType.KEYWORD_OP_CLASS)
				&& (scopeEquals(token, StringUtil.EMPTY) || scopeEquals(token, PHPTokenType.CONSTANT_OTHER)))
		{
			// Lookahead to see if there's a trailing paren! If so, make it a function-call, otherwise it's a property
			int lastOffset = getTokenOffset();
			int lastLength = getTokenLength();
			List<QueuedToken> popped = new ArrayList<QueuedToken>();
			IToken next = pop();
			while (next.isWhitespace())
			{
				popped.add(new QueuedToken(next, getTokenOffset(), getTokenLength()));
				next = pop();
			}
			popped.add(new QueuedToken(next, getTokenOffset(), getTokenLength()));

			if (!next.isEOF())
			{
				IToken nextMapped = tokenMapper.mapToken((Symbol) next.getData(), this);
				if (scopeEquals(nextMapped, PHPTokenType.PUNCTUATION_PARAM_LEFT))
				{
					token = getToken(PHPTokenType.META_FUNCTION_CALL_OBJECT);
				}
				else
				{
					token = getToken(PHPTokenType.VARIABLE_OTHER_PROPERTY);
				}
			}
			else
			{
				token = getToken(PHPTokenType.VARIABLE_OTHER_PROPERTY);
			}
			// Push the tokens we looked ahead back onto our queue
			for (QueuedToken addBack : popped)
			{
				push(addBack.getToken(), addBack.getOffset(), addBack.getLength());
			}
			fLength = lastLength;
			fOffset = lastOffset;
		}
		// function calls
		else if (scopeEquals(token, PHPTokenType.CONSTANT_OTHER))
		{
			// Lookahead to see if there's a trailing paren! If so, make it a function-call
			int lastOffset = getTokenOffset();
			int lastLength = getTokenLength();
			List<QueuedToken> popped = new ArrayList<QueuedToken>();
			IToken next = pop();
			while (next.isWhitespace())
			{
				popped.add(new QueuedToken(next, getTokenOffset(), getTokenLength()));
				next = pop();
			}
			popped.add(new QueuedToken(next, getTokenOffset(), getTokenLength()));

			if (!next.isEOF())
			{
				IToken nextMapped = tokenMapper.mapToken((Symbol) next.getData(), this);
				if (scopeEquals(nextMapped, PHPTokenType.PUNCTUATION_PARAM_LEFT))
				{
					token = getToken(PHPTokenType.META_FUNCTION_CALL);
				}
			}

			// Push the tokens we looked ahead back onto our queue
			for (QueuedToken addBack : popped)
			{
				push(addBack.getToken(), addBack.getOffset(), addBack.getLength());
			}
			fLength = lastLength;
			fOffset = lastOffset;
		}

		if (token.isOther())
		{
			lastToken = token;
		}

		if (inFunctionDeclaration)
		{
			return getToken("meta.function.php " + token.getData()); //$NON-NLS-1$
		}

		return token;
	}

	private boolean scopeEquals(IToken token, PHPTokenType type)
	{
		return scopeEquals(token, type.toString());
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
			// Note: don't check for null contents because this should only be used
			// from the scanner while the contents are still available (so, this
			// situation would really be an error).
			String contents = fScanner.getContents();
			
			return contents.substring(sym.left, sym.right);
		}
		catch (Exception e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "PHP code-scanner - Error getting a symbol value", e); //$NON-NLS-1$
		}
		return null;
	}

	public IToken getToken(PHPTokenType type)
	{
		return getToken(type.toString());
	}

	public IToken getToken(String tokenName)
	{
		return CommonUtil.getToken(tokenName);
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
		{
			return Token.EOF;
		}
		return token;
	}

	private void push(IToken next)
	{
		push(next, getTokenOffset(), getTokenLength());
	}

	private void push(IToken next, int offset, int length)
	{
		if (queue == null)
		{
			queue = new LinkedList<QueuedToken>();
		}
		queue.add(new QueuedToken(next, offset, length));
	}

	private void reset()
	{
		queue = null;
		inFunctionDeclaration = false;
		lastToken = null;
	}
}