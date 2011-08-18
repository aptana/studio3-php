package com.aptana.editor.php.core.ast;

import java.io.IOException;
import java.io.Reader;

import org2.eclipse.php.internal.core.PHPVersion;
import org2.eclipse.php.internal.core.ast.nodes.ASTParser;
import org2.eclipse.php.internal.core.ast.scanner.AstLexer;

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
		return ASTParser.getLexer(null, reader, phpVersion, true, true, false);
	}

	/**
	 * Returns an {@link AstLexer} according to the given php version.<br>
	 * The lexer will be initialized with the given reader.
	 * 
	 * @param phpVersion
	 * @param reader
	 * @param createAST
	 *            Create an AST that the lexer will hold.
	 * @return An {@link AstLexer} instance.
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static AstLexer getAstLexer(PHPVersion phpVersion, Reader reader, boolean createAST) throws IOException
	{
		return ASTParser.getLexer(null, reader, phpVersion, true, true, createAST);
	}
}
