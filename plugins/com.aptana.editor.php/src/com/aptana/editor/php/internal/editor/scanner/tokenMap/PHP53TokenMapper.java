package com.aptana.editor.php.internal.editor.scanner.tokenMap;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.php.internal.core.ast.parser.ParserConstants53;
import org.eclipse.php.internal.core.phpModel.javacup.runtime.Symbol;

import com.aptana.editor.php.internal.editor.scanner.PHPCodeScanner;

public class PHP53TokenMapper implements IPHPTokenMapper, ParserConstants53
{

	@Override
	public IToken mapToken(Symbol sym, PHPCodeScanner scanner)
	{
		return scanner.getToken("default.php"); //$NON-NLS-1$
	}

}
