package com.aptana.editor.php.internal.editor.scanner;

import java.util.LinkedList;
import java.util.Queue;

import java_cup.runtime.Symbol;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.theme.IThemeManager;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.preferences.PHPVersionProvider;
import com.aptana.editor.php.internal.editor.scanner.tokenMap.IPHPTokenMapper;
import com.aptana.editor.php.internal.editor.scanner.tokenMap.PHPTokenMapperFactory;

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
		return tokenMapper.mapToken(sym, this);
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
			PHPEditorPlugin.logError(e);
		}
		return null;
	}

	public IToken getToken(String tokenName)
	{
		return getThemeManager().getToken(tokenName);
	}

	protected IThemeManager getThemeManager()
	{
		return CommonEditorPlugin.getDefault().getThemeManager();
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
	}
}