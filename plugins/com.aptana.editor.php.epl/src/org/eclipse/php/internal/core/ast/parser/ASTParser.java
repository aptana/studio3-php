/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org.eclipse.php.internal.core.ast.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.core.phpModel.javacup.runtime.Scanner;
import org.eclipse.php.internal.core.phpModel.javacup.runtime.Symbol;
import org.eclipse.php.internal.core.phpModel.javacup.runtime.lr_parser;


/**
 * A PHP language parser for creating abstract syntax trees (ASTs).<p>
 * Example: Create basic AST from source string
 * <pre>
 * String source = ...;
 * Program program = ASTParser.parse(source);  
 * </pre>
 */
public class ASTParser {

	// php 5 analysis
	public static final PhpAstParser5 PHP_AST_PARSER5 = new PhpAstParser5();
	public static final PhpAstLexer5 PHP_AST_LEXER5 = new PhpAstLexer5(ASTParser.EMPTY_STRING_READER);
	
	// php 5.3 analysis
	public static final PhpAstParser53 PHP_AST_PARSER5_3 = new PhpAstParser53();
	public static final PhpAstLexer53 PHP_AST_LEXER5_3 = new PhpAstLexer53(ASTParser.EMPTY_STRING_READER);

	// php 4 analysis	
	public static final PhpAstParser4 PHP_AST_PARSER4 = new PhpAstParser4();
	public static final PhpAstLexer4 PHP_AST_LEXER4 = new PhpAstLexer4(ASTParser.EMPTY_STRING_READER);

	// empty buffer
	public static final StringReader EMPTY_STRING_READER = new StringReader(""); //$NON-NLS-1$

	/**
	 * @param phpCode String - represents the source code of the PHP program
	 * @param aspTagsAsPhp boolean - true if % is used as PHP process intructor   
	 * @return the {@link Program} node generated from the given source
	 * @throws Exception
	 */
	public static final Program parse(String phpCode, boolean aspTagsAsPhp) throws Exception {
		StringReader reader = new StringReader(phpCode);
		return parse(reader, aspTagsAsPhp, PHPVersion.PHP5_3);
	}

	/**
	 * @param phpFile File - represents the source file of the PHP program
	 * @param aspTagsAsPhp boolean - true if % is used as PHP process intructor   
	 * @return the {@link Program} node generated from the given source PHP file
	 * @throws Exception
	 */
	public static final Program parse(File phpFile, boolean aspTagsAsPhp) throws Exception {
		final Reader reader = new FileReader(phpFile);
		return parse(reader, aspTagsAsPhp, PHPVersion.PHP5_3);
	}

	public static final Program parse(final IDocument phpDocument, boolean aspTagsAsPhp, PHPVersion phpVersion) throws Exception {
		return parse(phpDocument, aspTagsAsPhp, phpVersion, 0, phpDocument.getLength());
	}

	public static final Program parse(final IDocument phpDocument, boolean aspTagsAsPhp, PHPVersion phpVersion, final int offset, final int length) throws Exception {
		final Reader reader = new InputStreamReader(new InputStream() {
			private int index = offset;
			private final int size = offset + length;

			public int read() throws IOException {
				try {
					if (index < size) {
						return phpDocument.getChar(index++);
					}
					return -1;
				} catch (BadLocationException e) {
					throw new IOException(e.getMessage());
				}
			}
		});
		return parse(reader, aspTagsAsPhp, phpVersion);
	}

	public static final Program parse(IDocument phpDocument, boolean aspTagsAsPhp) throws Exception {
		return parse(phpDocument, aspTagsAsPhp, PHPVersion.PHP5_3);
	}

	/**
	 * @see #parse(String, boolean)
	 */
	public static final Program parse(String phpCode) throws Exception {
		return parse(phpCode, true);
	}

	/**
	 * @see #parse(File, boolean)
	 */
	public static final Program parse(File phpFile) throws Exception {
		return parse(phpFile, true);
	}

	/**
	 * @see #parse(Reader, boolean)
	 */
	public static final Program parse(Reader reader) throws Exception {
		return parse(reader, true, PHPVersion.PHP5_3);
	}

	/**
	 * @param reader
	 * @return the {@link Program} node generated from the given {@link Reader}
	 * @throws Exception
	 */
	public static Program parse(Reader reader, boolean aspTagsAsPhp, PHPVersion phpVersion) throws Exception {
		final Scanner lexer = getLexer(reader, phpVersion, aspTagsAsPhp);
		final lr_parser phpParser = getParser(phpVersion);
		phpParser.setScanner(lexer);

		final Symbol symbol = phpParser.parse();
		return symbol == null ? null : (Program) symbol.value;
	}

	/**
	 * Constructs a scanner from a given reader
	 * @param reader
	 * @param phpVersion
	 * @param aspTagsAsPhp
	 * @return
	 * @throws IOException
	 */
	private static Scanner getLexer(Reader reader, PHPVersion phpVersion, boolean aspTagsAsPhp) throws IOException {
		if (PHPVersion.PHP5_3.equals(phpVersion)) {
			// TODO: Shalom - test this change
			// final PhpAstLexer53 lexer5 = new PhpAstLexer53(reader);
			final PhpAstLexer53 lexer5 = getLexer53(reader);
			lexer5.setUseAspTagsAsPhp(aspTagsAsPhp);
			return lexer5;
		}
		if (PHPVersion.PHP4.equals(phpVersion)) {
			final PhpAstLexer4 lexer4 = getLexer4(reader);
			lexer4.setUseAspTagsAsPhp(aspTagsAsPhp);
			return lexer4;
		} else if (PHPVersion.PHP5.equals(phpVersion)) {
			final PhpAstLexer5 lexer5 = getLexer5(reader);
			lexer5.setUseAspTagsAsPhp(aspTagsAsPhp);
			return lexer5;
		} else {
			throw new IllegalArgumentException("Unrecognized PHP version" + phpVersion); //$NON-NLS-1$
		}
	}

	private static lr_parser getParser(PHPVersion phpVersion) {
		if (PHPVersion.PHP5_3.equals(phpVersion)) {
			final PhpAstParser53 parser = new PhpAstParser53();
			return parser;
		}
		if (PHPVersion.PHP4.equals(phpVersion)) {
			return PHP_AST_PARSER4;
		} else if (PHPVersion.PHP5.equals(phpVersion)) {
			return PHP_AST_PARSER5;
		} else {
			throw new IllegalArgumentException("Unrecognized PHP version" + phpVersion); //$NON-NLS-1$
		}

	}

	/**
	 * @param reader
	 * @return the singleton {@link PhpAstLexer5}
	 */
	public static PhpAstLexer5 getLexer5(Reader reader) throws IOException {
		final PhpAstLexer5 phpAstLexer5 = ASTParser.PHP_AST_LEXER5;
		phpAstLexer5.yyreset(reader);
		phpAstLexer5.resetCommentList();
		return phpAstLexer5;
	}

	/**
	 * @param reader
	 * @return the singleton {@link PhpAstLexer5}
	 */
	public static PhpAstLexer4 getLexer4(Reader reader) throws IOException {
		final PhpAstLexer4 phpAstLexer4 = ASTParser.PHP_AST_LEXER4;
		phpAstLexer4.yyreset(reader);
		phpAstLexer4.resetCommentList();
		return phpAstLexer4;
	}
	
	/**
	 * @param reader
	 * @return the singleton {@link PhpAstLexer53}
	 */
	public static PhpAstLexer53 getLexer53(Reader reader) throws IOException {
		final PhpAstLexer53 phpAstLexer53 = ASTParser.PHP_AST_LEXER5_3;
		phpAstLexer53.yyreset(reader);
		phpAstLexer53.resetCommentList();
		return phpAstLexer53;
	}

	
}
