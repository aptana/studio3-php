/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.parser2;

import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.php.internal.core.phpModel.parser.php5.CompletionLexer5;
import org.eclipse.php.internal.core.phpModel.parser.php5.PhpParser5;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.parser.PHPMimeType;
import com.aptana.editor.php.internal.parser2.nodes.NodeBuilderClient;
import com.aptana.editor.php.internal.parser2.nodes.PHPBlockNode;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.IParser;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.lexer.Range;

/**
 * @author Kevin Lindsey
 * @author Pavel Petrochenko
 */
public class PHPParser implements IParser
{
	private static String NESTED_LANGUAGE_ID = "nested_languages"; //$NON-NLS-1$

	private static String DEFAULT_GROUP = "default"; //$NON-NLS-1$

	private IParseNode _currentParentNode;

	/**
	 * PHPParser
	 */
	public PHPParser()
	{
	}

	/**
	 * @see com.aptana.ide.parsing.AbstractParser#createParseState(com.aptana.ide.parsing.IParseState)
	 */
	public IParseState createParseState(IParseState parent)
	{
		IParseState result;

		if (parent == null)
		{
			result = new PHPParseState();
		}
		else
		{
			result = new PHPParseState(parent);
		}
		// get nested parsers
		IParseState root = result.getRoot();
		IParser scriptDocParser = this.getParserForMimeType(PHPDocMimeType.MimeType);
		if (scriptDocParser != null)
		{
			result.addChildState(scriptDocParser.createParseState(root));
		}

		return result;
	}

//	/**
//	 * @see com.aptana.ide.parsing.AbstractParser#initializeLexer()
//	 */
//	public void initializeLexer() throws LexerException
//	{
//		ILexer lexer = this.getLexer();
//		String language = this.getLanguage();
//
//		// ignore whitespace
//		lexer.setIgnoreSet(language, new int[] { PHPTokenTypes.WHITESPACE });
//		lexer.setLanguageAndGroup(language, DEFAULT_GROUP);
//	}

	/**
	 * @see com.aptana.ide.parsing.AbstractParser#parseAll(com.aptana.ide.parsing.nodes.IParseNode)
	 */
	public synchronized void parseAll(IParseNode parentNode) throws ParseException, LexerException
	{
		
	}

	protected char[] lastSource;
	private NodeBuilderClient parserClient;
	protected PhpParser5 parser = new PhpParser5();
	private PHPLanguageRegistry _languageRegistry;

	/**
	 * Whether to collect variables.
	 */
	private boolean collectVariables = false;

	protected void parseStructure(ILexer lexer, IParseNode parentNode)
	{

		MatcherLexer mlexer = (MatcherLexer) lexer;

		char[] sourceUnsafe = mlexer.getSourceUnsafe();
		NodeBuilderClient nb = parserClient;
		if (sourceUnsafe == lastSource)
		{
			parserClient.populateNodes(parentNode, mlexer.getCurrentOffset(), sourceUnsafe);
			return;
		}
		parserClient = new NodeBuilderClient();
		lastSource = sourceUnsafe;
		CompletionLexer5 completionLexer5 = new CompletionLexer5(new CharArrayReader(sourceUnsafe, 0,
				sourceUnsafe.length));
		completionLexer5.setParserClient(parserClient);
		completionLexer5.setTasksPatterns(TaskPatternsProvider.getInstance().getPetternsForWorkspace());
		parser.setScanner(completionLexer5);
		parser.setParserClient(parserClient);
		try
		{
			parser.parse();
		}
		catch (Exception e)
		{
			PHPEditorPlugin.logError(e);
		}
		if (parserClient.hasSyntaxErrors() && nb != null)
		{
			nb.populateNodes(parentNode, mlexer.getCurrentOffset(), sourceUnsafe);
		}
		else
		{			
			parserClient.populateNodes(parentNode, mlexer.getCurrentOffset(), sourceUnsafe);
		}
	}

	/**
	 * @param stream
	 * @return parsed AST
	 */
	public PHPBlockNode parseAll(InputStream stream)
	{

		NodeBuilderClient parserClient = new NodeBuilderClient(collectVariables);
		CompletionLexer5 completionLexer5 = new CompletionLexer5(stream);
		completionLexer5.setParserClient(parserClient);
//		TODO - completionLexer5.setTasksPatterns(TaskPatternsProvider.getInstance().getPetternsForWorkspace());
		parser.setScanner(completionLexer5);
		parser.setParserClient(parserClient);
		try
		{
			parser.parse();
		}
		catch (Exception e)
		{
			PHPEditorPlugin.log(e);
		}
		return parserClient.populateNodes();
	}

	/**
	 * Parses contents.
	 * 
	 * @param reader -
	 *            reader to parse.
	 * @return parsed AST
	 */
	public PHPBlockNode parseAll(Reader reader)
	{

		NodeBuilderClient parserClient = new NodeBuilderClient(collectVariables);
		CompletionLexer5 completionLexer5 = new CompletionLexer5(reader);
		completionLexer5.setParserClient(parserClient);
		// TODO - completionLexer5.setTasksPatterns(TaskPatternsProvider.getInstance().getPetternsForWorkspace());
		parser.setScanner(completionLexer5);
		parser.setParserClient(parserClient);
		try
		{
			parser.parse();
		}
		catch (Exception e)
		{
			PHPEditorPlugin.logError(e);
		}
		return parserClient.populateNodes();
	}

	/**
	 * Sets whether parser should collect variables.
	 * 
	 * @param collect -
	 *            whether to collect variables.
	 */
	public void setCollectVariables(boolean collect)
	{
		this.collectVariables = collect;
	}

	/**
	 * @see com.aptana.ide.parsing.AbstractParser#getNextLexemeInLanguage()
	 */
	protected Lexeme getNextLexemeInLanguage() throws LexerException
	{
		Lexeme result = super.getNextLexemeInLanguage();

		// always need to look for language change
		result = parsePossibleComment(result);

		return result;
	}
	

	

	/**
	 * parsePossibleComment
	 * 
	 * @param lexeme
	 * @return Lexeme
	 * @throws LexerException
	 */
	Lexeme parsePossibleComment(Lexeme lexeme) throws LexerException
	{
		if (lexeme == null || lexeme == EOS)
		{
			return lexeme;
		}

		// need to look at languages here as cached lexemes may come for another
		// language
		int typeIndex = lexeme.typeIndex;
		String lang = lexeme.getToken().getLanguage();
		// test if this is a valid comment, if not return
		// *NOTE - due to the language check now in getNextLexemeInLanguage, we
		// may only need to check for JS language
		// here
		if (lang.equals(PHPMimeType.MimeType))
		{
			if (typeIndex == PHPTokenTypes.COMMENT)
			{
				GenericCommentNode node = new GenericCommentNode(lexeme.getStartingOffset(), lexeme.getEndingOffset(),
						"SDCOMMENT", //$NON-NLS-1$
						PHPMimeType.MimeType);
				this.getParseState().addCommentRegion(node);
			}
			if (typeIndex != PHPTokenTypes.DOC_COMMENT)
			{
				return lexeme;
			}
			else
			{
				if (lexeme.getText().indexOf("?>") != -1) //$NON-NLS-1$
				{
					Lexeme ls = new Lexeme(getLexer().getTokenList(PHPMimeType.MimeType).get(PHPTokenTypes.COMMENT),
							lexeme.getText(), lexeme.offset);
					return ls;
				}
			}
		}
		else if (lang.equals(PHPDocMimeType.MimeType))
		{
			if (typeIndex != PHPDocTokenTypes.START_DOCUMENTATION)
			{
				return lexeme;
			}

		}
		if (lang.equals(HTMLMimeType.MimeType))
		{
			
			return lexeme;
			

		}

		// language will change, logic is below

		String mimeType = PHPDocMimeType.MimeType;

		ILexer lexer = this.getLexer();

		// backup to the start of the lexeme
		lexer.setCurrentOffset(lexeme.offset);

		// find offset
		Range range = lexer.find("documentation-delimiter");

		// include the delimiter in the doc or comment
		int offset = (range.isEmpty()) ? lexer.getSourceLength() : range.getEndingOffset();
		
		try
		{
//			this.changeLanguage(mimeType, lexeme.offset + lexeme.length, _currentParentNode);
			this.changeLanguage(mimeType, offset, _currentParentNode);
		}
		catch (LexerException e)
		{
		}
		catch (ParseException e)
		{
		}

		this.advance();
		return this.currentLexeme;
	}

	/**
	 * Advance to the next lexeme in the lexeme stream
	 * 
	 * @throws LexerException
	 */
	protected void advance() throws LexerException
	{
		ILexer lexer = this.getLexer();
		Lexeme currentLexeme = EOS;

		if (this._currentParentNode != null && this.currentLexeme != null && this.currentLexeme != EOS)
		{
			this._currentParentNode.setEndingLexeme(this.currentLexeme);
		}

		if (lexer.isEOS() == false)
		{
			boolean inWhitespace = true;
			boolean lastWasEOL = false;

			while (inWhitespace)
			{
				if (lexer.isEOS() == false)
				{
					currentLexeme = this.getNextLexemeInLanguage();

					if (currentLexeme != null && currentLexeme != EOS)
					{

						//if (currentLexeme.typeIndex != PHPTokenTypes.LINE) // This was commented out since it removed the __LINE__ keyword, and it's not EOL
						//{
						
						// add all non-EOL lexemes to our final list for
						// display purposes
						this.addLexeme(currentLexeme);
						
						//}
						if (currentLexeme.typeIndex == PHPTokenTypes.COMMENT)
						{
							if (currentLexeme.getText().indexOf("?>") != -1&&currentLexeme.getText().startsWith("//"))
							{
								this.currentLexeme = currentLexeme;
								return;
							}
						}

						// determine if token is in the WHITESPACE category
						if (currentLexeme.getCategoryIndex() == TokenCategories.WHITESPACE)
						{
							lastWasEOL = (currentLexeme.typeIndex == PHPTokenTypes.LINE);
						}
						else
						{
							inWhitespace = false;

							if (lastWasEOL)
							{
								currentLexeme.setAfterEOL();
							}
						}
					}
					else
					{
						// couldn't recover from error, so mark as end of stream
						// NOTE: We may want to throw an exception here since we
						// should be able to return at least an ERROR token
						currentLexeme = EOS;
						inWhitespace = false;
					}
				}
				else
				{
					// we've reached the end of the source text
					currentLexeme = EOS;
					inWhitespace = false;
				}
			}
		}

		this.currentLexeme = currentLexeme;
	}

//	/**
//	 * @see com.aptana.ide.parsing.AbstractParser#addChildParsers()
//	 */
//	protected void addChildParsers() throws ParserInitializationException
//	{
//		super.addChildParsers();
//
//		if (this._languageRegistry == null)
//		{
//			this._languageRegistry = new PHPLanguageRegistry();
//
//			IExtensionRegistry registry = Platform.getExtensionRegistry();
//
//			if (registry != null)
//			{
//				IExtensionPoint extensionPoint = registry.getExtensionPoint(PHPEditorPlugin.PLUGIN_ID, NESTED_LANGUAGE_ID);
//				IExtension[] extensions = extensionPoint.getExtensions();
//
//				for (int i = 0; i < extensions.length; i++)
//				{
//					IExtension extension = extensions[i];
//
//					IParser[] parsers = this._languageRegistry.loadFromExtension(extension);
//
//					for (int j = 0; j < parsers.length; j++)
//					{
//						this.addChildParser(parsers[j]);
//					}
//				}
//			}
//		}
//
//		// this.addChildParser(new PHPDocParser());
//		// this.addChildParser(new JSCommentParser());
//	}

	@Override
	public IParseNode parse(IParseState parseState) throws Exception
	{
		/*
		 String source = new String(parseState.getSource());
		RubyScript root = new RubyScript(parseState.getStartingOffset(), parseState.getStartingOffset()
				+ source.length());
		RubyStructureBuilder builder = new RubyStructureBuilder(root);
		SourceElementVisitor visitor = new SourceElementVisitor(builder);
		visitor.acceptNode(fParser.parse(source).getAST());
		parseState.setParseResult(root);

		return root;
		 */
		ILexer lexer = this.getLexer();

		lexer.setLanguageAndGroup(this.getLanguage(), DEFAULT_GROUP);

		// begin colorization loop
		this.advance();

		while (this.isEOS() == false)
		{
			if (this.isType(PHPTokenTypes.COMMENT) || this.isType(PHPTokenTypes.DOC_COMMENT))
			{
				int pos = currentLexeme.getText().indexOf("?>");
				lexer.setCurrentOffset(this.currentLexeme.offset + pos);
				// remove lexeme from lexeme cache
				this.getLexemeList().remove(this.currentLexeme);
				currentLexeme.getToken();
				Lexeme la = new Lexeme(currentLexeme.getToken(), currentLexeme.getText().substring(0, pos),
						this.currentLexeme.offset);
				addLexeme(la);
				break;
			}

			if (this.isType(PHPTokenTypes.CLOSE_TAG))
			{
				// rewind lexer to the start of the close tag so it can be lexed
				// as an HTML token
				lexer.setCurrentOffset(this.currentLexeme.offset);

				// remove lexeme from lexeme cache
				this.getLexemeList().remove(this.currentLexeme);

				// we're done scanning
				break;
			}
			else
			{
				this.advance();
			}
		}

		parseStructure(lexer, parentNode);
		return null;
	}

}
