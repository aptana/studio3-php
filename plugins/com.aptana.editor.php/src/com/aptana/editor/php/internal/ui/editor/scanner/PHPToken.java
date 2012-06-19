package com.aptana.editor.php.internal.ui.editor.scanner;

import java.lang.ref.WeakReference;

import java_cup.runtime.Symbol;

import org.eclipse.jface.text.rules.Token;

public class PHPToken extends Token
{

	private WeakReference<String> fContents;

	public PHPToken(Symbol symbol, WeakReference<String> contents)
	{
		super(symbol);
		this.fContents = contents;
	}

	public Symbol getSymbol()
	{
		return (Symbol) super.getData();
	}

	public String getSymbolValue()
	{
		// Note: don't check for null contents because this should only be used
		// from the scanner while the contents are still available (so, this
		// situation would really be an error).
		String contents = fContents.get();
		Symbol sym = getSymbol();

		return contents.substring(sym.left, sym.right);
	}

}
