package com.aptana.editor.php.core.ast;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.ast.parser.ASTParser;
import org.eclipse.php.internal.core.ast.parser.AstLexer;

/**
 * A factory for AstLexers
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class ASTFactory
{
	/**
	 * Returns an {@link AstLexer} according to the given php version.<br>
	 * The lexer will be initialized with the given reader.
	 * 
	 * @param phpVersion
	 * @param reader
	 * @return An {@link AstLexer} instance.
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static AstLexer getAstLexer(PHPVersion phpVersion, Reader reader) throws IOException
	{
		switch (phpVersion)
		{
			case PHP4:
				return ASTParser.getLexer4(reader);
			case PHP5:
				return ASTParser.getLexer5(reader);
			case PHP5_3:
				return ASTParser.getLexer53(reader);
		}
		throw new IllegalArgumentException("Unknown php version"); //$NON-NLS-1$
	}
}
