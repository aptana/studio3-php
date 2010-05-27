/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.contentAssist;

import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.documentModel.parser.AbstractPhpLexer;
import org.eclipse.php.internal.core.documentModel.parser.PhpLexerFactory;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonContentAssistProcessor;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.indexer.ElementsIndexingUtils;
import com.aptana.editor.php.internal.indexer.IElementEntriesFilter;
import com.aptana.editor.php.internal.ui.editor.PHPVersionDocumentManager;

/**
 * Content assist processor for PHP.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
@SuppressWarnings("unused")
public class PHPContentAssistProcessor extends CommonContentAssistProcessor implements IContentAssistProcessor
{

	private static char[] autoactivationCharacters = new char[] { '>', '@', '$', ':' };
	private static char[] contextautoactivationCharacters = new char[] { '(' };
	private ITextViewer viewer;

	/**
	 * Constructs a new PHP content assist processor.
	 * 
	 * @param editor
	 */
	public PHPContentAssistProcessor(AbstractThemeableEditor editor)
	{
		super(editor);
	}

	/**
	 * Filters entries by module and modules this module might include.
	 * 
	 * @param input
	 *            - input to filter.
	 * @param module
	 *            - module.
	 * @param index
	 *            - index to use.
	 * @return set of filtered entries.
	 */
	public static Set<IElementEntry> filterByModule(Collection<IElementEntry> input, IModule module,
			IElementsIndex index)
	{
		IElementEntriesFilter filter = ElementsIndexingUtils.createIncludeFilter(module, index);
		if (filter == null)
		{
			Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();
			result.addAll(input);
			return result;
		}

		return filter.filter(input);
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
	{
		this.viewer = viewer;
		IDocument document = viewer.getDocument();
		PHPVersion phpVersion = PHPVersionDocumentManager.getPHPVersion(document);
		if (phpVersion == null)
		{
			phpVersion = PHPVersionProvider.getDefaultPHPVersion();
		}
		String content = document.get();

		// AstLexer astLexer = ASTFactory.getAstLexer(phpVersion, new StringReader(content));
		// ASTParser parser = ASTParser.newParser(new StringReader(content), phpVersion);
		// Program ast = parser.createAST(null);
		// ASTNode node = ast.getElementAt(offset);
	    AbstractPhpLexer lexer = PhpLexerFactory.createLexer(new StringReader(content), phpVersion);
	    lexer.initialize(-1);
	    lexer.setPatterns(null);
	    lexer.setAspTags(true);
	    
	    /*
		Symbol prev = null;
		Symbol prev2 = null;
		Symbol lastN = null;
		int pos = 0;
		StringBuilder contentS = null;
		try
		{
			while (true)
			{

				Symbol next_token = lexer.next_token();
				if (next_token.sym == ParserConstants.T_NAMESPACE || next_token.sym == ParserConstants.T_USE)
				{
					lastN = next_token;
					contentS = new StringBuilder();
				}
				else if (lastN != null)
				{
					if (next_token.sym == ParserConstants.T_STRING || next_token.sym == ParserConstants.T_NS_SEPARATOR)
					{
						if (contentS.length() == 0)
						{
							pos = next_token.left;
						}
						contentS.append(next_token.value == null ? '\\' : next_token.value);
					}
					else
					{
						lastN = null;
					}
				}
				if (lastN != null)
				{
					if (next_token.left <= offset && next_token.right >= offset)
					{
						return getNamespaceCompletionProposals(content, contentS.toString(), pos, contentS.length(), 1,
								viewer);
					}
				}
				if (next_token.left < offset && next_token.right > offset)
				{

					if (next_token.sym == ParserConstants5.T_CONSTANT_ENCAPSED_STRING)
					{
						if (prev2 != null || prev != null)
						{
							if (checkInclude(prev2) || checkInclude(prev))
							{
								String text = (String) next_token.value;
								String substring = text.substring(1, offset - next_token.left);
								return getFilePathCompletionProposals(substring, next_token.left + 1, substring
										.length(), 1, viewer);
							}
						}
						return null;
					}

					// System.out.println(next_token);
				}
				if (next_token.sym == 0)
				{
					break;
				}
				prev2 = prev;
				prev = next_token;
			}
		}
		catch (IOException e)
		{

		}
		if (isInCommentChecker.inComment)
		{
			if (isInCommentChecker.inPHPDoc)
			{
				return docProcessor.computeCompletionProposals(viewer, offset);
			}
			return null;
		}
		int startOffset = offset < content.length() ? offset : offset - 1;
		for (int a = startOffset; a >= 0; a--)
		{
			char c = content.charAt(a);
			if (c < ' ')
			{
				break;
			}
			if (c == '/')
			{
				if (a > 0)
				{
					if (content.charAt(a - 1) == '/')
					{
						return null;
					}
				}
			}
		}
		if (activationChar == '@' && autoActivated)
		{
			return new ICompletionProposal[0];
		}

		boolean forceActivation = false;
		Boolean fa = (Boolean) viewer.getTextWidget().getData("ASSIST_FORCE_ACTIVATION"); //$NON-NLS-1$
		if (fa != null)
		{
			forceActivation = fa;
		}

		int replaceLengthIncrease = countReplaceLengthIncrease(content, offset);

		// Calculates and sets completion context
		calculateCompletionContext(offset);

		ICompletionProposal[] computeCompletionProposalInternal = computeCompletionProposalInternal(offset, content,
				true, forceActivation);
		if (computeCompletionProposalInternal.length > 0)
		{
			PHPCompletionProposal pa = (PHPCompletionProposal) computeCompletionProposalInternal[0];
			pa.defaultSelection = true;
			pa.suggestedSelection = true;
			if (replaceLengthIncrease > 0)
			{
				computeCompletionProposalInternal = batchIncreaseReplaceLength(computeCompletionProposalInternal,
						replaceLengthIncrease);
			}
		}

		// resetting the force activation flag.
		viewer.getTextWidget().setData("ASSIST_FORCE_ACTIVATION", //$NON-NLS-1$
				false);
		return computeCompletionProposalInternal;
		*/
		return null;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset)
	{
		// TODO
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters()
	{
		return autoactivationCharacters;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters()
	{
		return contextautoactivationCharacters;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getErrorMessage()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
