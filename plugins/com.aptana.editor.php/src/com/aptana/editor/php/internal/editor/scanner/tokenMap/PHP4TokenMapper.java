package com.aptana.editor.php.internal.editor.scanner.tokenMap;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.php.internal.core.ast.parser.ParserConstants4;

import com.aptana.editor.php.internal.editor.scanner.PHPCodeScanner;

public class PHP4TokenMapper implements IPHPTokenMapper, ParserConstants4
{

	@Override
	public IToken mapToken(int sym, PHPCodeScanner scanner)
	{
		return scanner.getToken("default.php"); //$NON-NLS-1$
	}

}
