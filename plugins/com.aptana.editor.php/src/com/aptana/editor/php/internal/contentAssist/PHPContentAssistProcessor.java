/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.contentAssist;

import static com.aptana.editor.php.internal.contentAssist.PHPContextCalculator.EXTENDS_PROPOSAL_CONTEXT_TYPE;
import static com.aptana.editor.php.internal.contentAssist.PHPContextCalculator.IMPLEMENTS_PROPOSAL_CONTEXT_TYPE;
import static com.aptana.editor.php.internal.contentAssist.PHPContextCalculator.NAMESPACE_PROPOSAL_CONTEXT_TYPE;
import static com.aptana.editor.php.internal.contentAssist.PHPContextCalculator.NEW_PROPOSAL_CONTEXT_TYPE;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org2.eclipse.php.core.compiler.PHPFlags;
import org2.eclipse.php.internal.core.PHPVersion;
import org2.eclipse.php.internal.core.documentModel.parser.AbstractPhpLexer;
import org2.eclipse.php.internal.core.documentModel.parser.PhpLexerFactory;
import org2.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.CollectionsUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonContentAssistProcessor;
import com.aptana.editor.common.contentassist.ICommonCompletionProposal;
import com.aptana.editor.common.contentassist.ILexemeProvider;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IIndexReporter;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.indexer.IReportable;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.builder.LocalModule;
import com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.indexer.AbstractPHPEntryValue;
import com.aptana.editor.php.internal.indexer.AccessModifierEntryFilter;
import com.aptana.editor.php.internal.indexer.ClassPHPEntryValue;
import com.aptana.editor.php.internal.indexer.ElementsIndexingUtils;
import com.aptana.editor.php.internal.indexer.FunctionPHPEntryValue;
import com.aptana.editor.php.internal.indexer.IEntryFilter;
import com.aptana.editor.php.internal.indexer.ModuleSubstitutionIndex;
import com.aptana.editor.php.internal.indexer.NamespacePHPEntryValue;
import com.aptana.editor.php.internal.indexer.PDTPHPModuleIndexer;
import com.aptana.editor.php.internal.indexer.PHPTypeProcessor;
import com.aptana.editor.php.internal.indexer.PublicsOnlyEntryFilter;
import com.aptana.editor.php.internal.indexer.UnpackedElementIndex;
import com.aptana.editor.php.internal.indexer.UnpackedEntry;
import com.aptana.editor.php.internal.indexer.VariablePHPEntryValue;
import com.aptana.editor.php.internal.indexer.language.PHPBuiltins;
import com.aptana.editor.php.internal.model.utils.TypeHierarchyUtils;
import com.aptana.editor.php.internal.parser.nodes.IPHPParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPBaseParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPClassParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPFunctionParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPVariableParseNode;
import com.aptana.editor.php.internal.parser.nodes.Parameter;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;
import com.aptana.editor.php.internal.ui.editor.PHPVersionDocumentManager;
import com.aptana.editor.php.internal.ui.editor.contentassist.PHPContextInformationValidator;
import com.aptana.editor.php.internal.ui.editor.outline.PHPDecoratingLabelProvider;
import com.aptana.editor.php.internal.ui.editor.outline.PHPOutlineItem;
import com.aptana.parsing.lexer.IRange;
import com.aptana.parsing.lexer.Range;

/**
 * Content assist processor for PHP.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPContentAssistProcessor extends CommonContentAssistProcessor implements IContentAssistProcessor
{
	/**
	 * Max look-behind chars when computing the lexemes for the context-info detection.
	 */
	private static final int CONTEXT_INFO_MAX_LOOK_BEHIND = 100;
	private static final IContextInformation[] EMPTY_CONTEXT_INFO = new IContextInformation[0];
	private static final int EXTERNAL_CLASS_PROPOSAL_RELEVANCE = ICommonCompletionProposal.RELEVANCE_MEDIUM - 10;
	private static final int EXTERNAL_FUNCTION_PROPOSAL_RELEVANCE = ICommonCompletionProposal.RELEVANCE_MEDIUM - 15;
	private static final int EXTERNAL_CONSTANT_PROPOSAL_RELEVANCE = ICommonCompletionProposal.RELEVANCE_MEDIUM - 20;
	private static final int EXTERNAL_DEFAULT_PROPOSAL_RELEVANCE = ICommonCompletionProposal.RELEVANCE_MEDIUM - 25;

	private static final ICompletionProposal[] EMPTY_PROPOSAL = new ICompletionProposal[0];
	protected static final String DOLLAR_SIGN = "$"; //$NON-NLS-1$

	/**
	 * The global namespace char, which is also used as a namespace separator.
	 */
	public static final String GLOBAL_NAMESPACE = "\\"; //$NON-NLS-1$
	/**
	 * Dereference operator.
	 */
	public static final String DEREFERENCE_OP = "->"; //$NON-NLS-1$

	/**
	 * Static dereference operator.
	 */
	public static final String STATIC_DEREFERENCE_OP = "::"; //$NON-NLS-1$

	/**
	 * Possible dereference operators.
	 */
	public static final String[] OPS = new String[] { DEREFERENCE_OP, STATIC_DEREFERENCE_OP };

	/**
	 * "$this" activation sequence.
	 */
	private static final String THIS_ACTIVATION_SEQUENCE = "$this"; //$NON-NLS-1$

	/**
	 * "self" activation sequence.
	 */
	private static final String SELF_ACTIVATION_SEQUENCE = "self"; //$NON-NLS-1$

	/**
	 * "static" activation sequence.
	 */
	private static final String STATIC_ACTIVATION_SEQUENCE = "static"; //$NON-NLS-1$

	/**
	 * "parent" activation sequence.
	 */
	private static final String PARENT_ACTIVATION_SEQUENCE = "parent"; //$NON-NLS-1$

	/**
	 * Content assist suggestions that we should only allow under classes
	 */
	@SuppressWarnings("nls")
	private static final Set<String> ALLOW_ONLY_UNDER_CLASS = new HashSet<String>(Arrays.asList("$this", "self",
			"parent", "public", "private", "protected")); //$NON-NLS-4$

	private static final IRange EMPTY_RANGE = new Range(0, 0);

	private static Image fIcon53 = PHPEditorPlugin.getImage("icons/full/obj16/v53.png"); //$NON-NLS-1$
	private static Image fIcon5 = PHPEditorPlugin.getImage("icons/full/obj16/v5.png"); //$NON-NLS-1$
	private static Image fIcon4 = PHPEditorPlugin.getImage("icons/full/obj16/v4.png"); //$NON-NLS-1$
	private static Image fIcon53off = PHPEditorPlugin.getImage("icons/full/obj16/v53_off.png"); //$NON-NLS-1$
	private static Image fIcon5off = PHPEditorPlugin.getImage("icons/full/obj16/v5_off.png"); //$NON-NLS-1$
	private static Image fIcon4off = PHPEditorPlugin.getImage("icons/full/obj16/v4_off.png"); //$NON-NLS-1$

	// Auto-activation chars. Note that we omit the $ sign, as we have to verify its context. The $ is handled as any
	// other char in the isValidAutoActivationLocation()
	private static char[] autoactivationCharacters = new char[] { '>', '@', ':', '\\', ' ' };
	private static final char[] contextInformationActivationChars = { '(', ',' };
	private static PHPDecoratingLabelProvider labelProvider = new PHPDecoratingLabelProvider();
	private ITextViewer viewer;
	private int offset;
	private String content;
	/**
	 * Whether reported scope is global.
	 */
	private boolean reportedScopeIsGlobal = true;
	/**
	 * Whether reported scope is under a class definition.
	 */
	private boolean reportedScopeIsUnderClass = false;

	/**
	 * Reported imports.
	 */
	private Set<String> globalImports;
	private Map<String, String> aliases;
	private String namespace;
	private IModule module;
	/**
	 * Current proposal context. By default this instance accepts all and everything.
	 */
	private ProposalContext currentContext;
	private PHPContextCalculator contextCalculator;
	private IPreferenceStore preferenceStore;
	private boolean isOutOfWorkspace;
	private IDocument document;

	/**
	 * Constructs a new PHP content assist processor.
	 * 
	 * @param editor
	 */
	public PHPContentAssistProcessor(AbstractThemeableEditor editor)
	{
		super(editor);
		if (editor == null)
		{
			throw new IllegalArgumentException("Expected PHPSourceEditor, but got null"); //$NON-NLS-1$
		}
		if (!(editor instanceof PHPSourceEditor))
		{
			throw new IllegalArgumentException("Expected PHPSourceEditor, but got " + editor.getClass().getName()); //$NON-NLS-1$
		}
		preferenceStore = PHPEditorPlugin.getDefault().getPreferenceStore();
		currentContext = new ProposalContext(new AcceptAllContextFilter(), true, true, null);
		contextCalculator = new PHPContextCalculator();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CommonContentAssistProcessor#isValidAutoActivationLocation(char, int,
	 * org.eclipse.jface.text.IDocument, int)
	 */
	@Override
	public boolean isValidAutoActivationLocation(char c, int keyCode, IDocument document, int offset)
	{
		// make sure we don't popup a proposal when typing the <?php
		try
		{
			String openPhp = document.get(Math.max(0, offset - 4),
					Math.min(Math.min(offset, 4), document.getLength() - 1));
			if (openPhp.endsWith("<?") || openPhp.endsWith("<?p") || openPhp.endsWith("<?ph")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			{
				return false;
			}
		}
		catch (Exception e) // $codepro.audit.disable emptyCatchClause
		{
			// ignore it and just use the lexemeProvider
		}
		ILexemeProvider<PHPTokenType> lexemeProvider = ParsingUtils.createLexemeProvider(document, offset);
		currentContext = contextCalculator.calculateCompletionContext(lexemeProvider, offset, c);
		if (!currentContext.acceptModelsElements() && !currentContext.isAutoActivateCAAfterApply())
		{
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CommonContentAssistProcessor#isValidIdentifier(char, int)
	 */
	public boolean isValidIdentifier(char c, int keyCode)
	{
		return Character.isJavaIdentifierStart(c) || Character.isJavaIdentifierPart(c);
	}

	/**
	 * Calls the SnippetsCompletionProcessor to contribute any relevant snippets for the offset, in case the current
	 * context allows snippet proposals.
	 * 
	 * @see com.aptana.editor.common.CommonContentAssistProcessor#addSnippetProposals(org.eclipse.jface.text.ITextViewer,
	 *      int)
	 */
	@Override
	protected Collection<ICompletionProposal> addSnippetProposals(ITextViewer viewer, int offset)
	{
		if (currentContext != null && !currentContext.acceptExternalProposals())
		{
			return Collections.emptyList();
		}
		return super.addSnippetProposals(viewer, offset);
	}

	/**
	 * (Experimental) This hooks our Ruble scripting up to Content Assist, allowing them to contribute possible
	 * proposals, in case the current context allows snippet proposals. (non-Javadoc)
	 * 
	 * @see com.aptana.editor.common.CommonContentAssistProcessor#addRubleProposals(org.eclipse.jface.text.ITextViewer,
	 *      int)
	 */
	@Override
	protected List<ICompletionProposal> addRubleProposals(ITextViewer viewer, int offset)
	{
		if (currentContext != null && !currentContext.acceptExternalProposals())
		{
			return Collections.emptyList();
		}
		return super.addRubleProposals(viewer, offset);
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
	{
		this.viewer = viewer;
		this.document = viewer.getDocument();
		return computeCompletionProposals(document, offset);
	}

	/**
	 * Compute the proposals using an offset and a document. Note: this is a convenient way of separating the UI (the
	 * ITextViewer) from the actual computation, so it can be tested easily.
	 * 
	 * @param viewer
	 * @param offset
	 * @param document
	 * @return
	 */
	public ICompletionProposal[] computeCompletionProposals(IDocument document, int offset)
	{
		// First, check if we are in a PHP partition
		ITypedRegion partition;
		try
		{
			// Make sure that if the offset is at the end of the document, we test for the offset-1. This
			// is done due to a bug in the Studio that returns the default partition and not the php-default
			// partition.
			int length = document.getLength();
			if (length == offset && length > 0)
			{
				partition = document.getPartition(offset - 1);
			}
			else
			{
				partition = document.getPartition(offset);
			}
			if (!partition.getType().startsWith(IPHPConstants.PREFIX))
			{
				// The partition does not start with our __php_ prefix, so do not return any proposals.
				return null;
			}
		}
		catch (BadLocationException e1)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error computing PHP completion proposals", e1); //$NON-NLS-1$
			return null;
		}
		PHPVersion phpVersion = PHPVersionDocumentManager.getPHPVersion(document);
		if (phpVersion == null)
		{
			phpVersion = PHPVersionProvider.getDefaultPHPVersion();
		}

		ILexemeProvider<PHPTokenType> lexemeProvider = ParsingUtils.createLexemeProvider(document, offset);
		// Calculates and sets completion context
		currentContext = contextCalculator.calculateCompletionContext(lexemeProvider, offset);

		String content = document.get();

		AbstractPhpLexer lexer = PhpLexerFactory.createLexer(new StringReader(content), phpVersion); // $codepro.audit.disable
																										// closeWhereCreated
		int state = -1;
		try
		{
			// set initial lexer state - we use reflection here since we don't
			// know the constant value of
			// of this state in specific PHP version lexer
			state = lexer.getClass().getField("ST_PHP_IN_SCRIPTING").getInt(lexer); //$NON-NLS-1$
		}
		catch (Exception e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error grabbing a field from the PHP lexer", e); //$NON-NLS-1$
			return null;
		}
		lexer.initialize(state);
		lexer.setPatterns(null);
		lexer.setAspTags(true);

		String prev = null;
		String prev2 = null;
		String lastN = null;
		StringBuilder contentS = null;
		try
		{
			while (true)
			{
				String next_token = lexer.getNextToken();
				int left = lexer.getTokenStart();
				int right = left + lexer.yylength();
				String value = lexer.yytext();
				if (next_token == PHPRegionTypes.PHP_NS_SEPARATOR || next_token == PHPRegionTypes.PHP_USE)
				{
					lastN = next_token;
					contentS = new StringBuilder();
				}
				else if (lastN != null)
				{
					if (next_token == PHPRegionTypes.PHP_STRING || next_token == PHPRegionTypes.PHP_NS_SEPARATOR)
					{
						contentS.append((value == null) ? '\\' : value);
					}
					else
					{
						lastN = null;
					}
				}
				// if (lastN != null)
				// {
				// if (left <= offset && right >= offset)
				// {
				// return getNamespaceCompletionProposals(content, contentS.toString(), offset,
				// contentS.length(), 1,
				// viewer);
				// }
				// }
				if (left < offset && right > offset)
				{

					if (next_token == PHPRegionTypes.PHP_CONSTANT_ENCAPSED_STRING)
					{
						if (prev2 != null || prev != null)
						{
							if (checkInclude(prev2) || checkInclude(prev))
							{
								// String substring = value.substring(1, offset - left);
								// FIXME: Shalom - Implement getFilePathCompletionProposals
								// return getFilePathCompletionProposals(substring, left + 1, substring.length(), 1,
								// viewer);
								return EMPTY_PROPOSAL;
							}
						}
						return EMPTY_PROPOSAL;
					}

					// System.out.println(next_token);
				}
				if (next_token == null
						|| (PHPRegionTypes.PHP_CLOSETAG.equals(next_token) && PHPRegionTypes.PHP_CLOSETAG.equals(prev)))
				{
					break;
				}
				prev2 = prev;
				prev = next_token;
			}
		}
		catch (IOException e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error computing PHP completion proposals", e); //$NON-NLS-1$
		}

		int startOffset = (offset < content.length()) ? offset : offset - 1;
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
						return EMPTY_PROPOSAL;
					}
				}
			}
		}
		// if (activationChar == '@' && autoActivated)
		// {
		// return new ICompletionProposal[0];
		// }

		boolean forceActivation = false;
		// The only reason why we test for a null viewer here is to allow testing without any ITextViewer attachment.
		Boolean fa = (viewer != null && viewer.getTextWidget() != null) ? (Boolean) viewer.getTextWidget().getData(
				"ASSIST_FORCE_ACTIVATION") : null; //$NON-NLS-1$
		if (fa != null)
		{
			forceActivation = fa;
		}

		int replaceLengthIncrease = countReplaceLengthIncrease(content, offset);

		ICompletionProposal[] computeCompletionProposalInternal = computeCompletionProposalInternal(partition, offset,
				content, true, forceActivation);
		if (computeCompletionProposalInternal.length > 0)
		{
			if (replaceLengthIncrease > 0)
			{
				computeCompletionProposalInternal = batchIncreaseReplaceLength(computeCompletionProposalInternal,
						replaceLengthIncrease);
			}
		}

		// resetting the force activation flag.
		if (viewer != null && viewer.getTextWidget() != null)
		{
			viewer.getTextWidget().setData("ASSIST_FORCE_ACTIVATION", false);//$NON-NLS-1$
		}
		return computeCompletionProposalInternal;
	}

	/**
	 * Computes proposals.
	 * 
	 * @param partition
	 * @param offset
	 *            - offset.
	 * @param content
	 *            - content.
	 * @param proposeBuiltins
	 *            - whether to propose built-ins.
	 * @param forceActivation
	 *            - whether force activation occured.
	 * @return proposals
	 */
	public ICompletionProposal[] computeCompletionProposalInternal(ITypedRegion partition, final int offset,
			String content, boolean proposeBuiltins, boolean forceActivation)
	{
		final int start = (offset == 0) ? 0 : offset - 1;

		this.offset = offset;
		this.content = content;

		List<String> callPath = ParsingUtils.parseCallPath(partition, content, start, OPS, false, document);
		if (callPath == null || callPath.isEmpty())
		{
			return EMPTY_PROPOSAL;
		}

		if (callPath.size() > 1)
		{
			if (hasStaticDereferenceOperatorAfterTheFirst(callPath))
			{
				return EMPTY_PROPOSAL;
			}

			// Foo::hello()->goodbye()...
			if (content.charAt(start) == '(')
			{
				return EMPTY_PROPOSAL;
			}
			if (DEREFERENCE_OP.equals(callPath.get(1)))
			{
				return dereferencingCompletion(getIndex(content, start), callPath, start, getModule());
			}
			else if (callPath.size() > 3 && DEREFERENCE_OP.equals(callPath.get(3)))
			{
				// We have a case like A::$B-> so we treat $B as a simple identifier
				return dereferencingCompletion(getIndex(content, start), callPath, start, getModule());
			}
			else
			{
				return dereferencingStaticCompletion(getIndex(content, start), callPath, start, getModule());
			}
		}
		else if (callPath.size() == 1)
		{
			// if content assistant is not auto-activated and we should not
			// auto-activate on
			// identifiers, skipping the proposals computation.
			if (forceActivation
					&& !preferenceStore.getBoolean(IContentAssistPreferencesConstants.AUTO_ACTIVATE_ON_IDENTIFIERS))
			{
				return EMPTY_PROPOSAL;
			}
			String identifier = callPath.get(0);

			// TODO refactor new instance completion to use the new completion
			// context system
			// if (isNewInstanceCompletion(start, identifier, content))
			// {
			// return newInstanceCompletion(start, content, identifier,
			// getModule());
			// }

			// Check if we are dealing with a namespace completion
			if (identifier.startsWith(GLOBAL_NAMESPACE))
			{
				return computeNamespaceCompletion(start, content, identifier, globalImports, getModule(),
						proposeBuiltins, true);
			}

			return simpleIdentifierCompletion(start, content, identifier, globalImports, getModule(), proposeBuiltins,
					true, false);
		}

		return EMPTY_PROPOSAL;
	}

	private ICompletionProposal[] computeNamespaceCompletion(int offset, String content, String identifier,
			Set<String> globalImports, IModule module, boolean proposeBuiltins, boolean filter)
	{
		// We need to trim out our namespace separator at the beginning of the identifier.
		identifier = identifier.substring(1);
		return simpleIdentifierCompletion(offset, content, identifier, globalImports, module, proposeBuiltins, filter,
				true);
	}

	// /**
	// * Returns a namespace-only completion proposals.
	// * @param index
	// *
	// * @param content
	// * @param identifier
	// * @param offset
	// * @param collectItemsFromScope
	// * @param proposals
	// * @param length
	// * @return
	// * @return
	// */
	// private Set<ICompletionProposal> computeNamespaceCompletionProposals(IElementsIndex index, String content, String
	// identifier, int offset, boolean collectItemsFromScope)
	// {
	// Set<ICompletionProposal> nsProposals = new TreeSet<ICompletionProposal>();
	// List<IElementEntry> entries = index.getEntriesStartingWith(IPHPIndexConstants.NAMESPACE_CATEGORY,
	// StringUtil.EMPTY);
	// HashSet<String> paths = new HashSet<String>();
	// for (IElementEntry e : entries)
	// {
	// String entryPath = e.getEntryPath();
	// if (!entryPath.startsWith(identifier))
	// {
	// continue;
	// }
	// if (!paths.contains(entryPath))
	// {
	// paths.add(entryPath);
	// PHPCompletionProposal proposal = createProposal(e, offset, identifier, entryPath, e.getModule(), false,
	// index, false);
	// if (proposal != null)
	// {
	// nsProposals.add(proposal);
	// }
	// if (collectItemsFromScope)
	// {
	// // We should continue to collect any relevant proposal from this namespace scope
	// ICompletionProposal[] identifiersCompletion = simpleIdentifierCompletion(offset, content, identifier, false,
	// null, module, false, true, true);
	// for (ICompletionProposal p : identifiersCompletion)
	// {
	// nsProposals.add(p);
	// }
	// }
	// }
	// }
	// return nsProposals;
	// }

	/**
	 * Performs dereferencing completion.
	 * 
	 * @param index
	 *            - index to use.
	 * @param callPath
	 *            - call path
	 * @param module
	 *            - current module.
	 * @return completion proposals
	 */
	private ICompletionProposal[] dereferencingCompletion(IElementsIndex index, List<String> callPath, int offset,
			IModule module)
	{
		Set<IElementEntry> result = computeDereferenceEntries(index, callPath, (offset == 0) ? 0 : offset - 1, module,
				false, aliases, namespace);
		if (result == null || result.isEmpty())
		{
			return new ICompletionProposal[] {};
		}

		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		Set<String> usedName = new LinkedHashSet<String>();
		for (IElementEntry currentEntry : result)
		{
			String lastName = ElementsIndexingUtils.getLastNameInPath(currentEntry.getEntryPath());
			if (!usedName.contains(lastName))
			{
				ICompletionProposal proposal = createProposal(currentEntry, offset, callPath.get(callPath.size() - 1),
						lastName, module, false, index, false);
				if (proposal != null)
				{
					proposals.add(proposal);
				}
				usedName.add(lastName);
			}
		}
		ICompletionProposal[] toReturn = new ICompletionProposal[proposals.size()];
		return proposals.toArray(toReturn);
	}

	/**
	 * Computes dereference entries.
	 * 
	 * @param index
	 *            - index to use.
	 * @param callPath
	 *            - call path.
	 * @param offset
	 *            - offset.
	 * @param module
	 *            - module.
	 * @param exactMatch
	 *            - whether to perform the exact match of the last entry.
	 * @param namespace
	 *            - current namespace
	 * @return dereference entries or null.
	 */
	@SuppressWarnings("unchecked")
	public static Set<IElementEntry> computeDereferenceEntries(IElementsIndex index, List<String> callPath, int offset,
			IModule module, boolean exactMatch, Map<String, String> aliases, String namespace)
	{
		String entryName = callPath.get(0);
		
		

		Set<IElementEntry> leftDereferenceEntries = null;
		
		
		/*
		 * fix self::$x->
		 * firas abd alrahman doonfrs@gmail.com
		 * check if entryname is static to use computeStaticDereferenceLeftEntries instead of computeDereferenceLeftEntries
		 */
		if( STATIC_DEREFERENCE_OP.equals(callPath.get(1)) || SELF_ACTIVATION_SEQUENCE.equals(entryName) || STATIC_ACTIVATION_SEQUENCE.equals(entryName))
		{
			leftDereferenceEntries = computeStaticDereferenceLeftEntries(index, pathEntryName(entryName),
					offset, module, aliases, namespace);
		}else
		{
			leftDereferenceEntries = computeDereferenceLeftEntries(index, pathEntryName(entryName),
					offset, module, aliases, namespace);
		}
		
		
		if (leftDereferenceEntries == null)
		{
			return null;
		}
		if (leftDereferenceEntries.isEmpty() && ParsingUtils.isFunctionCall(callPath.get(0)))
		{
			// In this case, we have a function call right at the beginning on the call-path. So we compute
			// a simple identifier for that call and add it to our list, so the code assist will suggest completion
			// for the return type of that call.
			leftDereferenceEntries.addAll(computeSimpleIdentifierEntries(true, Collections.EMPTY_SET,
					pathEntryName(entryName), false, index, false, module, false, StringUtil.EMPTY,
					Collections.EMPTY_MAP));
		}
		boolean innerCompletion = false;
		if (THIS_ACTIVATION_SEQUENCE.equals(callPath.get(0)))
		{
			innerCompletion = true;
		}

		Set<IElementEntry> result = null;
		for (int i = 2; i < callPath.size(); i += 2)
		{
			boolean currentExactMatchFlag = true;
			boolean applyAccessRestriction = false;

			if (i == callPath.size() - 1)
			{
				currentExactMatchFlag = exactMatch;
				applyAccessRestriction = true;
			}

			String callPathEntry = callPath.get(i);
			result = computeDereferenceRightEntries(leftDereferenceEntries, index, pathEntryName(callPathEntry),
					offset, module, currentExactMatchFlag, applyAccessRestriction, innerCompletion, aliases, namespace);
			if (result == null || result.isEmpty())
			{
				return null;
			}
			leftDereferenceEntries = result;
		}
		return result;
	}

	/**
	 * Performs static dereferencing completion.
	 * 
	 * @param index
	 *            - index to use.
	 * @param callPath
	 *            - call path.
	 * @param module
	 *            - current module.
	 * @return completion proposals
	 */
	private ICompletionProposal[] dereferencingStaticCompletion(IElementsIndex index, List<String> callPath,
			int offset, IModule module)
	{
		Set<IElementEntry> result = computeStaticDereferenceEntries(index, callPath, (offset == 0) ? 0 : offset - 1,
				module, false, aliases, namespace);
		if (result == null || result.isEmpty())
		{
			return new ICompletionProposal[] {};
		}

		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		Set<String> usedName = new LinkedHashSet<String>();
		for (IElementEntry currentEntry : result)
		{
			String lastName = ElementsIndexingUtils.getLastNameInPath(currentEntry.getEntryPath());
			if (isVariableEntry(currentEntry) && !isConstVariable(currentEntry))
			{
				lastName = "$" + lastName; //$NON-NLS-1$ // $codepro.audit.disable stringConcatenationInLoop
			}
			if (!usedName.contains(lastName))
			{
				ICompletionProposal proposal = createProposal(currentEntry, offset, callPath.get(callPath.size() - 1),
						lastName, module, !isConstVariable(currentEntry), index, false);
				if (proposal != null)
				{
					proposals.add(proposal);
					usedName.add(lastName);
				}
			}
		}
		ICompletionProposal[] toReturn = new ICompletionProposal[proposals.size()];
		return proposals.toArray(toReturn);
	}

	/**
	 * Computes static dereference entries.
	 * 
	 * @param index
	 *            - index to use.
	 * @param callPath
	 *            - call path.
	 * @param offset
	 *            - offset.
	 * @param module
	 *            - module.
	 * @param exactMatch
	 *            - whether to perform an exact match for the last entry.
	 * @param namespace
	 * @return set of entries or null.
	 */
	public static Set<IElementEntry> computeStaticDereferenceEntries(IElementsIndex index, List<String> callPath,
			int offset, IModule module, boolean exactMatch, Map<String, String> aliases, String namespace)
	{
		Set<IElementEntry> leftDereferenceEntries = computeStaticDereferenceLeftEntries(index,
				pathEntryName(callPath.get(0)), offset, module, aliases, namespace);
		if (leftDereferenceEntries == null || leftDereferenceEntries.isEmpty())
		{
			return null;
		}

		boolean selfCompletion = SELF_ACTIVATION_SEQUENCE.equals(callPath.get(0));
		boolean staticCompletion = STATIC_ACTIVATION_SEQUENCE.equals(callPath.get(0));
		boolean parentCompletion = PARENT_ACTIVATION_SEQUENCE.equals(callPath.get(0));

		boolean currentExactMatch = true;
		if (callPath.size() == 3)
		{
			currentExactMatch = exactMatch;
		}
		if (parentCompletion && leftDereferenceEntries.size() == 1)
		{
			// Make sure we grab the right namespace scope from the entry itself
			Object entryValue = leftDereferenceEntries.iterator().next().getValue();
			if (entryValue instanceof ClassPHPEntryValue)
			{
				namespace = ((ClassPHPEntryValue) entryValue).getNameSpace();
			}
		}
		Set<IElementEntry> result = computeStaticDereferenceRightEntries(index, leftDereferenceEntries,
				pathEntryName(callPath.get(2)), offset, module, currentExactMatch, true, selfCompletion
						|| staticCompletion, parentCompletion, aliases, namespace);
		if (result == null || result.isEmpty())
		{
			return null;
		}

		// if call path has more then one reference, we need to count other
		// references
		// through the usual dereferencing algorithm
		if (callPath.size() > 3)
		{
			leftDereferenceEntries = result;

			for (int i = 4; i < callPath.size(); i += 2)
			{
				currentExactMatch = true;
				boolean applyAccessRestriction = false;
				if (i == callPath.size() - 1)
				{
					currentExactMatch = exactMatch;
					applyAccessRestriction = true;
				}
				result = computeDereferenceRightEntries(leftDereferenceEntries, index, pathEntryName(callPath.get(i)),
						offset, module, currentExactMatch, applyAccessRestriction, selfCompletion, aliases, namespace);
				if (result == null || result.isEmpty())
				{
					return null;
				}
				leftDereferenceEntries = result;
			}
		}
		return result;
	}

	/**
	 * Computes right entries for the static dereferencing completion.
	 * 
	 * @param index
	 *            - index to use.
	 * @param leftEntries
	 *            - left dereferencing entries.
	 * @param right
	 *            - right side of the dereferencing.
	 * @param offset
	 *            - offset.
	 * @param module
	 *            - module.
	 * @param exactMatch
	 *            - whether to check for exact match of the right side.
	 * @param applyAccessModifiers
	 *            - whether to filter entries by access modifiers.
	 * @param innerEntries
	 *            - whether inner entries are being computed. Like $this->... or self::...
	 * @param parentEntries
	 * @return set of entries or null.
	 */
	private static Set<IElementEntry> computeStaticDereferenceRightEntries(IElementsIndex index,
			Set<IElementEntry> leftEntries, String right, int offset, IModule module, boolean exactMatch,
			boolean applyAccessModifiers, boolean innerEntries, boolean parentEntries, Map<String, String> aliases,
			String namespace)
	{
		if (leftEntries == null || leftEntries.isEmpty())
		{
			return null;
		}

		Set<Object> leftTypes = getEntriesTypes(leftEntries);

		if (leftTypes == null || leftTypes.isEmpty())
		{
			return null;
		}
		// here we have all the types, left part might be of
		// now resolving it to the simple custom types
		Set<String> resolvedLeftTypes = PHPTypeProcessor.processTypes(leftTypes, index);
		if (resolvedLeftTypes == null || resolvedLeftTypes.isEmpty())
		{
			return null;
		}
		Set<String> customLeftTypes = PHPTypeProcessor.getCustomTypes(resolvedLeftTypes);
		if (customLeftTypes == null || customLeftTypes.isEmpty())
		{
			return null;
		}

		IEntryFilter filter = null;
		if (applyAccessModifiers)
		{
			if (innerEntries)
			{
				filter = new AccessModifierEntryFilter(customLeftTypes, false);
			}
			else if (parentEntries)
			{
				filter = new AccessModifierEntryFilter(customLeftTypes, true);
			}
			else
			{
				filter = new PublicsOnlyEntryFilter();
			}
		}

		Set<String> typesWithAncestors = TypeHierarchyUtils.addAllAncestors(customLeftTypes, index, namespace, aliases);

		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();
		// now searching for the possible right parts
		if (right.length() == 0)
		{
			Set<IElementEntry> varResults = ContentAssistCollectors.collectVariableEntries(index, DOLLAR_SIGN,
					typesWithAncestors, exactMatch, aliases, namespace);
			if (varResults != null)
			{
				result.addAll(varResults);
			}
			Set<IElementEntry> funcResults = ContentAssistCollectors.collectFunctionEntries(index, right,
					typesWithAncestors, exactMatch, aliases, namespace);
			if (funcResults != null)
			{
				result.addAll(funcResults);
			}
			Set<IElementEntry> constResults = ContentAssistCollectors.collectConstEntries(index, right,
					typesWithAncestors, exactMatch, aliases, namespace);
			if (constResults != null)
			{
				result.addAll(constResults);
			}
		}
		else if (right.startsWith(DOLLAR_SIGN))
		{
			result.addAll(ContentAssistCollectors.collectVariableEntries(index, right, typesWithAncestors, exactMatch,
					aliases, namespace));
		}
		else
		{
			result.addAll(ContentAssistCollectors.collectConstEntries(index, right, typesWithAncestors, exactMatch,
					aliases, namespace));
			result.addAll(ContentAssistCollectors.collectFunctionEntries(index, right, typesWithAncestors, exactMatch,
					aliases, namespace));
		}
		if (result == null || result.isEmpty())
		{
			return null;
		}

		if (!parentEntries)
		{
			result = ContentAssistFilters.filterStaticEntries(result);
		}

		if (filter != null)
		{
			result = filter.filter(result);
		}

		return ContentAssistFilters.filterByModule(result, module, index);
	}

	/**
	 * Gets the types for the number of entries.
	 * 
	 * @param entries
	 *            - entries.
	 * @return types
	 */
	private static Set<Object> getEntriesTypes(Collection<IElementEntry> entries)
	{
		Set<Object> result = new LinkedHashSet<Object>();
		for (IElementEntry entry : entries)
		{
			Set<Object> entryTypes = getEntryTypes(entry);
			Set<Object> splittedTypes = new HashSet<Object>(entryTypes.size() + 2);
			for (Object type : entryTypes)
			{
				if (type instanceof String)
				{
					String t = type.toString();
					String[] types = t.split("\\|"); //$NON-NLS-1$
					for (String splittedType : types)
					{
						String resolvedType = splittedType.trim();
						if (resolvedType.startsWith(GLOBAL_NAMESPACE))
						{
							resolvedType = resolvedType.substring(1);
						}
						splittedTypes.add(resolvedType);
					}
				}
				else
				{
					splittedTypes.add(type);
				}

			}
			result.addAll(splittedTypes);
		}

		return result;
	}

	/**
	 * Gets entry types.
	 * 
	 * @param entry
	 *            - entry, which types to get.
	 * @return set of entry types.
	 */
	private static Set<Object> getEntryTypes(IElementEntry entry)
	{
		if (isVariableEntry(entry))
		{
			return ((VariablePHPEntryValue) entry.getValue()).getTypes();
		}
		else if (isFunctionEntry(entry))
		{
			return ((FunctionPHPEntryValue) entry.getValue()).getReturnTypes();
		}
		else if (isClassEntry(entry))
		{
			Set<Object> result = new HashSet<Object>(1);
			result.add(ElementsIndexingUtils.getFirstNameInPath(entry.getEntryPath()));
			return result;
		}

		return Collections.emptySet();
	}

	/**
	 * Computes left static dereference entries.
	 * 
	 * @param index
	 *            - index to use.
	 * @param left
	 *            - left name.
	 * @param offset
	 *            - offset.
	 * @param module
	 *            - current module.
	 * @param namespace
	 * @return set of entries or null.
	 */
	private static Set<IElementEntry> computeStaticDereferenceLeftEntries(IElementsIndex index, String left,
			int offset, IModule module, Map<String, String> aliases, String namespace)
	{

		if (left.startsWith("$")) //$NON-NLS-1$
		{
			// static dereferencing is allowed on PHP 5.3 variables
			IProject project = getProject(module);
			if (project != null && !PHPVersionProvider.isPHP53(project))
			{
				return null;
			}
			else
			{
				return computeDereferenceLeftEntries(index, left, offset, module, aliases, namespace);
			}
		}

		if (SELF_ACTIVATION_SEQUENCE.equals(left) || STATIC_ACTIVATION_SEQUENCE.equals(left))
		{
			IElementEntry currentClass = getCurrentClass(index, module, offset);
			if (currentClass == null)
			{
				return null;
			}
			Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();
			result.add(currentClass);
			return result;
		}
		else if (PARENT_ACTIVATION_SEQUENCE.equals(left))
		{
			IElementEntry currentClass = getCurrentClass(index, module, offset);
			if (currentClass == null)
			{
				return null;
			}
			Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();
			Object clazz = currentClass.getValue();
			if (clazz instanceof ClassPHPEntryValue)
			{
				ClassPHPEntryValue classPHPEntryValue = (ClassPHPEntryValue) clazz;
				String superClassname = classPHPEntryValue.getSuperClassname();
				if (superClassname != null)
				{
					String resolvedSuperclassNamespace = resolveNamespace(superClassname);
					if (StringUtil.isEmpty(resolvedSuperclassNamespace))
					{
						resolvedSuperclassNamespace = namespace;
					}
					Set<IElementEntry> superClassEntry = getClassEntries(index, superClassname, module, aliases,
							resolvedSuperclassNamespace, true);
					if (superClassEntry != null && superClassEntry.size() == 1)
					{
						result.addAll(superClassEntry);
					}
				}
			}

			return result;
		}
		else
		{
			return getClassEntries(index, left, module, aliases, namespace, true);
		}
	}

	private static String resolveNamespace(String path)
	{
		if (StringUtil.isEmpty(path))
		{
			return path;
		}
		try
		{
			IPath p = Path.fromOSString(path.replaceAll("\\\\", "/")); //$NON-NLS-1$ //$NON-NLS-2$
			if (p.segmentCount() > 1)
			{
				String resolvedNamespace = p.removeLastSegments(1).toString();
				if (resolvedNamespace.startsWith(GLOBAL_NAMESPACE) || resolvedNamespace.startsWith("/")) //$NON-NLS-1$
				{
					resolvedNamespace = resolvedNamespace.substring(1);
				}
				return resolvedNamespace.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else
			{
				return StringUtil.EMPTY;
			}
		}
		catch (Exception e)
		{
			// ignore
		}
		return StringUtil.EMPTY;
	}

	/**
	 * Resolve the {@link IProject} from the {@link IModule}
	 * 
	 * @param module
	 * @return
	 */
	private static IProject getProject(IModule module)
	{
		if (module == null || !(module instanceof LocalModule))
		{
			return null;
		}
		LocalModule lm = (LocalModule) module;
		IFile file = lm.getFile();
		if (file != null)
		{
			return file.getProject();
		}
		return null;
	}

	/**
	 * Gets the class, offset is in.
	 * 
	 * @param index
	 *            - index to use.
	 * @param module
	 *            - module.
	 * @param offset
	 *            - offset.
	 * @return class entry or null.
	 */
	private static IElementEntry getCurrentClass(IElementsIndex index, IModule module, int offset)
	{
		List<IElementEntry> entries = index.getModuleEntries(module);
		for (IElementEntry entry : entries)
		{
			if (entry.getCategory() == IPHPIndexConstants.CLASS_CATEGORY
					&& entry.getValue() instanceof ClassPHPEntryValue)
			{
				ClassPHPEntryValue val = (ClassPHPEntryValue) entry.getValue();
				if (val.getStartOffset() < offset && val.getEndOffset() > offset)
				{
					return entry;
				}
			}
		}

		return null;
	}

	/**
	 * Gets class entries.
	 * 
	 * @param index
	 *            - index to use.
	 * @param clazz
	 *            - class name.
	 * @param namespace
	 * @param isStaticDereferencing
	 * @return variable entries.
	 */
	private static Set<IElementEntry> getClassEntries(IElementsIndex index, String clazz, IModule module,
			Map<String, String> aliases, String namespace, boolean isStaticDereferencing)
	{
		if (clazz != null && clazz.startsWith(GLOBAL_NAMESPACE))
		{
			clazz = clazz.substring(1);
		}

		List<IElementEntry> namespaceEntries = getNamespaceEntries(clazz, module, aliases);
		List<IElementEntry> leftEntries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, clazz);
		if (CollectionsUtil.isEmpty(leftEntries))
		{
			clazz = getNameByAlias(clazz, index, namespace, aliases, namespaceEntries);
			leftEntries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, clazz);
		}
		if (leftEntries == null)
		{
			leftEntries = new ArrayList<IElementEntry>();
		}
		if (leftEntries.isEmpty())
		{
			Set<String> classMap = new HashSet<String>(1);
			classMap.add(clazz);
			Set<IElementEntry> entries = ContentAssistCollectors.collectBuiltinTypeEntries(classMap, true);
			leftEntries.addAll(entries);
		}
		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();
		for (IElementEntry entry : leftEntries)
		{
			if (isStaticDereferencing || module == null || module.equals(entry.getModule()))
			{
				ClassPHPEntryValue value = (ClassPHPEntryValue) entry.getValue();
				if (!ContentAssistUtils.isFilterByNamespace() || TypeHierarchyUtils.isInNamespace(value, namespace))
				{
					result.add(entry);
				}
			}
		}
		return result;
	}

	/**
	 * Gets path entry name.
	 * 
	 * @param entry
	 *            - entry.
	 * @return path entry name.
	 */
	private static String pathEntryName(String entry)
	{
		if (ParsingUtils.isFunctionCall(entry))
		{
			return ParsingUtils.getFunctionNameFromCall(entry);
		}
		else
		{
			return entry;
		}
	}

	/**
	 * Computes entries for the dereferencing completion.
	 * 
	 * @param index
	 *            - index to use.
	 * @param leftEntries
	 *            - left dereferencing entries.
	 * @param right
	 *            - right side of the dereferencing.
	 * @param offset
	 *            - offset.
	 * @param module
	 *            - module.
	 * @param exactMatch
	 *            - whether to check for exact match of the right side.
	 * @param applyAccessModifiers
	 *            - whether to filter entries by access modifiers.
	 * @param innerEntries
	 *            - whether inner entries are being computed. Like $this->... or self::...
	 * @param aliases
	 * @param namespace
	 * @return set of entries or null.
	 */
	private static Set<IElementEntry> computeDereferenceRightEntries(Set<IElementEntry> leftEntries,
			IElementsIndex index, String right, int offset, IModule module, boolean exactMatch,
			boolean applyAccessModifiers, boolean innerEntries, Map<String, String> aliases, String namespace)
	{
		if (leftEntries == null || leftEntries.isEmpty())
		{
			return null;
		}

		Set<Object> leftTypes = getEntriesTypes(leftEntries);

		if (leftTypes == null || leftTypes.isEmpty())
		{
			return null;
		}
		// here we have all the types, left part might be of
		// now resolving it to the simple custom types
		Set<String> resolvedLeftTypes = PHPTypeProcessor.processTypes(leftTypes, index);
		if (resolvedLeftTypes == null || resolvedLeftTypes.isEmpty())
		{
			return null;
		}
		Set<String> customLeftTypes = PHPTypeProcessor.getCustomTypes(resolvedLeftTypes);
		if (customLeftTypes == null || customLeftTypes.isEmpty())
		{
			return null;
		}

		IEntryFilter filter = null;
		if (applyAccessModifiers)
		{
			if (innerEntries)
			{
				filter = new AccessModifierEntryFilter(customLeftTypes, false);
			}
			else
			{
				filter = new PublicsOnlyEntryFilter();
			}
		}

		// FIXME - Shalom (???) - Have the ancestors look at the current module and remove any other ancestor from a
		// different module and a similar name

		// Determine the type and its ancestors. Use the namespace and the aliases (the 'use' statements within this
		// namespace) when detecting the ancestors.
		Set<String> typesWithAncestors = TypeHierarchyUtils.addAllAncestors(customLeftTypes, index, namespace, aliases);

		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();
		// now searching for the possible right parts
		boolean filterNonStatic = true;
		if (right.length() == 0)
		{
			Set<IElementEntry> varResults = ContentAssistCollectors.collectVariableEntries(index, DOLLAR_SIGN,
					typesWithAncestors, exactMatch, aliases, namespace);
			if (varResults != null)
			{
				result.addAll(varResults);
			}
			Set<IElementEntry> funcResults = ContentAssistCollectors.collectFunctionEntries(index, right,
					typesWithAncestors, exactMatch, aliases, namespace);
			if (funcResults != null)
			{
				result.addAll(funcResults);
			}
		}
		else
		{
			String var;
			if (right.startsWith(DOLLAR_SIGN))
			{
				var = right;
				filterNonStatic = false;
			}
			else
			{
				var = DOLLAR_SIGN + right;
			}
			result.addAll(ContentAssistCollectors.collectVariableEntries(index, var, typesWithAncestors, exactMatch,
					aliases, namespace));
			result.addAll(ContentAssistCollectors.collectFunctionEntries(index, right, typesWithAncestors, exactMatch,
					aliases, namespace));
		}
		if (result == null || result.isEmpty())
		{
			return null;
		}

		if (filterNonStatic)
		{
			result = ContentAssistFilters.filterNonStaticVariables(result);
		}
		if (filter != null)
		{
			result = filter.filter(result);
		}

		return ContentAssistFilters.filterByModule(result, module, index);
	}

	/**
	 * Computes left dereference entries.
	 * 
	 * @param index
	 *            - index to use.
	 * @param left
	 *            - left name.
	 * @param offset
	 *            - offset.
	 * @param module
	 *            - current module.
	 * @param namespace
	 *            - current namespace
	 * @return set of entries or null.
	 */
	private static Set<IElementEntry> computeDereferenceLeftEntries(IElementsIndex index, String left, int offset,
			IModule module, Map<String, String> aliases, String namespace)
	{
		if (left.startsWith("$")) //$NON-NLS-1$
		{
			if (THIS_ACTIVATION_SEQUENCE.equals(left))
			{
				IElementEntry currentClass = getCurrentClass(index, module, offset);
				if (currentClass == null)
				{
					return null;
				}
				Set<IElementEntry> result = new LinkedHashSet<IElementEntry>(1);
				result.add(currentClass);
				return result;
			}
			else
			{
				return getVariableEntries(index, left, namespace);
			}
		}
		else
		{
			if (ParsingUtils.isFunctionCall(left))
			{
				return getFunctionEntriesByCall(index, left);
			}
			else
			{
				return getClassEntries(index, left, module, aliases, namespace, false);
			}
		}
	}

	/**
	 * Gets all possible function return types.
	 * 
	 * @param index
	 *            - index to use.
	 * @param callString
	 *            - function call (not including brackets and parameters ).
	 * @return possible types or null.
	 */
	private static Set<IElementEntry> getFunctionEntriesByCall(IElementsIndex index, String callString)
	{
		List<IElementEntry> leftEntries = index.getEntries(IPHPIndexConstants.FUNCTION_CATEGORY, callString);
		if (leftEntries == null)
		{
			return null;
		}
		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>(leftEntries.size());
		result.addAll(leftEntries);
		return result;
	}

	/**
	 * Gets variable entries.
	 * 
	 * @param index
	 *            - index to use.
	 * @param var
	 *            - variable name.
	 * @param namespace
	 *            - namespace
	 * @return variable entries.
	 */
	private static Set<IElementEntry> getVariableEntries(IElementsIndex index, String var, String namespace)
	{
		String varName = var.substring(1);
		List<IElementEntry> leftEntries = index.getEntries(IPHPIndexConstants.VAR_CATEGORY, varName);
		if (leftEntries == null)
		{
			return null;
		}
		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();
		for (IElementEntry entry : leftEntries)
		{
			VariablePHPEntryValue value = (VariablePHPEntryValue) entry.getValue();
			if (!ContentAssistUtils.isFilterByNamespace() || TypeHierarchyUtils.isInNamespace(value, namespace))
			{
				result.add(entry);
			}
		}
		return result;
	}

	/**
	 * Performs a completion for a simple identifier.
	 * 
	 * @param offset
	 *            - offset.
	 * @param content
	 *            - content.
	 * @param identifier
	 *            - identifier to complete.
	 * @param reportedStackIsGlobal
	 *            - whether imported stack is global.
	 * @param globalImports
	 *            - global imports set.
	 * @param module
	 *            - module.
	 * @param proposeBuiltins
	 *            - whether to propose built-ins.
	 * @param ignorIndexNamespace
	 * @return completion proposals
	 */
	private ICompletionProposal[] simpleIdentifierCompletion(final int offset, String content, String identifier,
			Set<String> globalImports, IModule module, boolean proposeBuiltins, boolean filter,
			boolean ignorIndexNamespace)
	{
		String name = identifier;

		if (name == null)
		{
			return new ICompletionProposal[] {};
		}

		// only allowing empty names for "extends" and "implements" proposal
		// types
		if (name.length() == 0)
		{
			if (currentContext == null)
			{
				return new ICompletionProposal[] {};
			}

			if (!(EXTENDS_PROPOSAL_CONTEXT_TYPE.equals(currentContext.getType())
					|| IMPLEMENTS_PROPOSAL_CONTEXT_TYPE.equals(currentContext.getType()) || NEW_PROPOSAL_CONTEXT_TYPE
						.equals(currentContext.getType())))
			{
				return new ICompletionProposal[] {};
			}

		}

		boolean variableCompletion = false;

		if (name.startsWith(DOLLAR_SIGN))
		{
			name = name.substring(1);
			variableCompletion = true;
		}

		List<Object> items = new ArrayList<Object>();

		IElementsIndex index;
		if (variableCompletion)
		{
			index = getIndex(content, offset);
		}
		else
		{
			index = getIndexOptimized(content, offset);
		}
		String namespaceToUse = ignorIndexNamespace ? StringUtil.EMPTY : namespace;
		List<IElementEntry> entries = computeSimpleIdentifierEntries(reportedScopeIsGlobal, globalImports, name,
				variableCompletion, index, false, module, filter, currentContext, namespaceToUse, aliases);

		items.addAll(entries);

		if (proposeBuiltins)
		{
			String completionStart = name;
			if (completionStart.startsWith(GLOBAL_NAMESPACE))
			{
				completionStart = completionStart.substring(1);
			}
			List<Object> modelItems = variableCompletion ? ContentAssistUtils.selectModelElements(DOLLAR_SIGN
					+ completionStart, false) : ContentAssistUtils.selectModelElements(completionStart, false);
			if (modelItems != null)
			{
				for (Object modelItem : modelItems)
				{
					if (variableCompletion)
					{
						if (modelItem instanceof PHPVariableParseNode)
						{
							items.add(modelItem);
						}
						else if (modelItem instanceof IPHPParseNode && !(modelItem instanceof PHPFunctionParseNode))
						{
							IPHPParseNode pn = (IPHPParseNode) modelItem;
							String nodeName = pn.getNodeName();
							if (nodeName.startsWith(DOLLAR_SIGN))
							{
								if (!reportedScopeIsUnderClass && ALLOW_ONLY_UNDER_CLASS.contains(nodeName))
								{
									continue;
								}
								items.add(pn);
							}
						}
					}
					else
					{
						if (!(modelItem instanceof PHPVariableParseNode))
						{
							String nodeName = ((PHPBaseParseNode) modelItem).getNodeName();
							// make sure we don't offer items that should only appear under a class/interface.
							if (!reportedScopeIsUnderClass && ALLOW_ONLY_UNDER_CLASS.contains(nodeName))
							{
								continue;
							}
							items.add(modelItem);
						}
					}
				}
			}
		}

		List<ICompletionProposal> result = createProposals(offset, name, items, module, true, index, false);

		ICompletionProposal[] proposals = new ICompletionProposal[result.size()];
		return result.toArray(proposals);
	}

	/**
	 * Gets elements index for a module taking into account perfomance preferences.
	 * 
	 * @param content
	 *            - module content.
	 * @return elements index
	 */
	private IElementsIndex getIndexOptimized(String content, int offset)
	{
		if (isOutOfWorkspace
				|| preferenceStore
						.getBoolean(IContentAssistPreferencesConstants.PARSE_UNSAVED_MODULE_ON_IDENTIFIERS_COMPLETION))
		{
			return getIndex(content, offset);
		}

		return PHPGlobalIndexer.getInstance().getIndex();
	}

	/**
	 * Computes entries for simple identifier completion.
	 * 
	 * @param reportedStackIsGlobal
	 *            - whether reported stack is global.
	 * @param globalImports
	 *            - global imports set.
	 * @param name
	 *            - name to complete.
	 * @param variableCompletion
	 *            - whether completing variable or not.
	 * @param index
	 *            - index to use.
	 * @param exactMatch
	 *            - whether to check for exact match of the names.
	 * @param module
	 *            - module.
	 * @return list of entries or null.
	 */
	public static List<IElementEntry> computeSimpleIdentifierEntries(boolean reportedStackIsGlobal,
			Set<String> globalImports, String name, boolean variableCompletion, IElementsIndex index,
			boolean exactMatch, IModule module, boolean filter, String namespace, Map<String, String> aliases)
	{
		return computeSimpleIdentifierEntries(reportedStackIsGlobal, globalImports, name, variableCompletion, index,
				exactMatch, module, filter, null, namespace, aliases);
	}

	/**
	 * Computes entries for simple identifier completion.
	 * 
	 * @param reportedStackIsGlobal
	 *            - whether reported stack is global.
	 * @param globalImports
	 *            - global imports set.
	 * @param name
	 *            - name to complete.
	 * @param variableCompletion
	 *            - whether completing variable or not.
	 * @param index
	 *            - index to use.
	 * @param exactMatch
	 *            - whether to check for exact match of the names.
	 * @param module
	 *            - module.
	 * @param proposalContext
	 *            - proposal context.
	 * @return list of entries or null.
	 */
	@SuppressWarnings("unused")
	private static List<IElementEntry> computeSimpleIdentifierEntries(boolean reportedStackIsGlobal,
			Set<String> globalImports, String name, boolean variableCompletion, IElementsIndex index,
			boolean exactMatch, IModule module, boolean filter, ProposalContext proposalContext, String namespace,
			Map<String, String> aliases)
	{
		List<IElementEntry> namespaceEntries = getNamespaceEntries(name, module, aliases);
		// [http://php.net/manual/en/language.namespaces.faq.php]
		// "Names that contain a backslash but do not begin with a backslash like my\name can be resolved in 2 different
		// ways. If there is an import statement that aliases another name to my, then the import alias is applied to
		// the my in my\name.Otherwise, the current namespace name is prepended to my\name."
		if (proposalContext != null && NAMESPACE_PROPOSAL_CONTEXT_TYPE.equals(proposalContext.getType()))
		{
			// Do nothing
			// We keep the name as it is, since we are in a 'use' statement.
		}
		else if (!name.startsWith(GLOBAL_NAMESPACE))
		{
			name = getNameByAlias(name, index, namespace, aliases, namespaceEntries);
		}
		else
		{
			if (!variableCompletion)
			{
				name = name.substring(1);
				List<IElementEntry> entriesStartingWith = index.getEntriesStartingWith(
						IPHPIndexConstants.NAMESPACE_CATEGORY, StringUtil.EMPTY);
				for (IElementEntry e : entriesStartingWith)
				{
					if (e.getEntryPath().startsWith(name))
					{
						namespaceEntries.add(e);
					}
				}

			}
		}
		List<IElementEntry> entries;
		if (variableCompletion
				&& (proposalContext == null || proposalContext.acceptModelElementType(IPHPIndexConstants.VAR_CATEGORY)))
		{
			if (exactMatch)
			{
				entries = index.getEntries(IPHPIndexConstants.VAR_CATEGORY, name);
			}
			else
			{
				entries = index.getEntriesStartingWith(IPHPIndexConstants.VAR_CATEGORY, name);
			}
		}
		else
		{
			// searching for methods
			if (exactMatch)
			{
				entries = new ArrayList<IElementEntry>();
				if (proposalContext == null || proposalContext.acceptModelElementType(IPHPIndexConstants.VAR_CATEGORY))
				{
					entries.addAll(index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, name));
				}

				if (proposalContext == null
						|| proposalContext.acceptModelElementType(IPHPIndexConstants.FUNCTION_CATEGORY))
				{
					entries.addAll(index.getEntries(IPHPIndexConstants.FUNCTION_CATEGORY, name));
				}

			}
			else
			{
				entries = new ArrayList<IElementEntry>();
				if (proposalContext == null
						|| proposalContext.acceptModelElementType(IPHPIndexConstants.CLASS_CATEGORY))
				{
					entries.addAll(index.getEntriesStartingWith(IPHPIndexConstants.CLASS_CATEGORY, name));
				}

				if (proposalContext == null
						|| proposalContext.acceptModelElementType(IPHPIndexConstants.FUNCTION_CATEGORY))
				{
					entries.addAll(index.getEntriesStartingWith(IPHPIndexConstants.FUNCTION_CATEGORY, name));
				}

				if (proposalContext == null
						|| proposalContext.acceptModelElementType(IPHPIndexConstants.CONST_CATEGORY))
				{
					entries.addAll(index.getEntriesStartingWith(IPHPIndexConstants.CONST_CATEGORY, name));
				}

				if (proposalContext == null
						|| proposalContext.acceptModelElementType(IPHPIndexConstants.NAMESPACE_CATEGORY))
				{
					// In case the name start with the current namespace, remove that prefix.
					String ns = name;
					if (ns.startsWith(namespace + GLOBAL_NAMESPACE))
					{
						ns = ns.substring(namespace.length() + 1);
					}
					// tokenize the namespace segments. Collect the namespaces from the longest namespace possible until
					// we don't get any more assist.
					Set<IElementEntry> collectedNs = new LinkedHashSet<IElementEntry>();
					collectedNs.addAll(index.getEntriesStartingWith(IPHPIndexConstants.NAMESPACE_CATEGORY, ns));
					int separatorIndex = ns.lastIndexOf(GLOBAL_NAMESPACE);
					while (separatorIndex > 0)
					{
						String nsLookup = ns.substring(0, separatorIndex);
						collectedNs.addAll(index
								.getEntriesStartingWith(IPHPIndexConstants.NAMESPACE_CATEGORY, nsLookup));
						separatorIndex = nsLookup.lastIndexOf(GLOBAL_NAMESPACE);
					}
					entries.addAll(collectedNs);
					if (proposalContext != null && NAMESPACE_PROPOSAL_CONTEXT_TYPE.equals(proposalContext.getType()))
					{
						// When we are dealing with a 'use' statement, collect and display the Classes that exists under
						// the 'used' namespace.
						entries.addAll(index.getEntriesStartingWith(IPHPIndexConstants.CLASS_CATEGORY, name));
					}

				}
			}

			// searching for defines
			Set<IElementEntry> defineEntries = computeDefines(name, index, proposalContext, exactMatch);
			if (defineEntries != null)
			{
				if (entries != null)
				{
					entries.addAll(defineEntries);
				}
				else
				{
					entries = new ArrayList<IElementEntry>(defineEntries);
				}
			}
		}

		entries.addAll(namespaceEntries);
		if (filter)
		{
			entries = ContentAssistFilters.filterFieldsAndMembers(entries);
		}
		if (!reportedStackIsGlobal)
		{
			entries = ContentAssistFilters.filterGlobalVariables(entries, globalImports);
		}
		Set<IElementEntry> filterResult = ContentAssistFilters.filterByModule(entries, module, index);
		List<IElementEntry> result = new ArrayList<IElementEntry>();
		result.addAll(filterResult);
		return result;
	}

	/**
	 * Returns the 'DEFINE' entries with the given name.
	 * 
	 * @param name
	 *            The define's name.
	 * @param index
	 * @param proposalContext
	 * @param exactMatch
	 * @return A {@link List} of define element entries, or <code>null</code>.
	 */
	public static Set<IElementEntry> computeDefines(String name, IElementsIndex index, ProposalContext proposalContext,
			boolean exactMatch)
	{
		List<IElementEntry> varEntries = null;
		if (proposalContext == null || proposalContext.acceptModelElementType(IPHPIndexConstants.CONST_CATEGORY))
		{
			if (exactMatch)
			{
				varEntries = index.getEntries(IPHPIndexConstants.CONST_CATEGORY, name);
			}
			else
			{
				varEntries = index.getEntriesStartingWith(IPHPIndexConstants.CONST_CATEGORY, name);
			}
		}
		if (varEntries != null)
		{
			Set<IElementEntry> staticVariableEntries = new LinkedHashSet<IElementEntry>(5);
			for (IElementEntry entry : varEntries)
			{
				if (entry.getValue() instanceof AbstractPHPEntryValue)
				{
					if (PHPFlags.isNamedConstant(((AbstractPHPEntryValue) entry.getValue()).getModifiers()))
					{
						staticVariableEntries.add(entry);
					}
				}
			}
			return staticVariableEntries;
		}
		return null;
	}

	/**
	 * @param name
	 * @param module
	 * @param aliases
	 * @return
	 */
	private static List<IElementEntry> getNamespaceEntries(String name, IModule module, Map<String, String> aliases)
	{
		List<IElementEntry> namespaceEntries = new ArrayList<IElementEntry>();
		if (aliases != null)
		{
			for (String s : aliases.keySet())
			{
				if (s.toLowerCase().startsWith(name))
				{
					namespaceEntries.add(new UnpackedEntry(-1, s, new NamespacePHPEntryValue(0, s), module));
				}
			}
		}
		return namespaceEntries;
	}

	/**
	 * Returns the name of the identifier we complete with regards to the aliases we have in the script.<br>
	 * This follows the rules defined at http://php.net/manual/en/language.namespaces.faq.php: "Names that contain a
	 * backslash but do not begin with a backslash like my\name can be resolved in 2 different ways. If there is an
	 * import statement that aliases another name to my, then the import alias is applied to the my in
	 * my\name.Otherwise, the current namespace name is prepended to my\name."
	 * 
	 * @param name
	 * @param index
	 * @param namespace
	 * @param aliases
	 * @param namespaceEntries
	 * @return The identifier name with regards to the namespace aliases
	 */
	private static String getNameByAlias(String name, IElementsIndex index, String namespace,
			Map<String, String> aliases, List<IElementEntry> namespaceEntries)
	{

		boolean foundAlias = false;
		String lowerCaseName = (name != null) ? name.toLowerCase() : StringUtil.EMPTY;
		if (!CollectionsUtil.isEmpty(aliases))
		{
			for (String s : aliases.keySet())
			{
				if (lowerCaseName.startsWith(s.toLowerCase()))
				{
					name = aliases.get(s) + name.substring(s.length()); // $codepro.audit.disable
					foundAlias = true;
					break;
				}
			}
		}
		if (!foundAlias)
		{
			if (namespace != null && namespace.length() > 0 && !name.startsWith(namespace + GLOBAL_NAMESPACE))
			{
				// Calling namespace\ is just like calling the current namespace, so we have to check it here and
				// return the current name.
				if (name.startsWith("namespace\\")) { //$NON-NLS-1$
					name = namespace + GLOBAL_NAMESPACE + name.substring(10);
				}
				else
				{
					name = namespace + GLOBAL_NAMESPACE + name;
				}
			}
		}
		List<IElementEntry> entriesStartingWith = index.getEntriesStartingWith(IPHPIndexConstants.NAMESPACE_CATEGORY,
				StringUtil.EMPTY);
		for (IElementEntry e : entriesStartingWith)
		{
			if (e.getEntryPath().startsWith(name))
			{
				namespaceEntries.add(e);
			}
		}
		return name;
	}

	/**
	 * Checks whether the call path contains static dereference operator after the first one.
	 * 
	 * @param callPath
	 *            - call path
	 * @return true if contains, false otherwise.
	 */
	private boolean hasStaticDereferenceOperatorAfterTheFirst(List<String> callPath)
	{
		if (callPath.size() < 3)
		{
			return false;
		}

		for (int i = 2; i < callPath.size(); i++)
		{
			if (STATIC_DEREFERENCE_OP.equals(callPath.get(i)))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Increases each proposal replace length to the one specified. Only instances of PHPCompletionProposal are
	 * modified.
	 * 
	 * @param proposals
	 *            - proposals to modify. The proposals in the list ARE modified by the method. null-safe.
	 * @param replaceLength
	 *            - new replace length.
	 * @return the same proposals list as the one specified in proposals argument.
	 */
	private static ICompletionProposal[] batchIncreaseReplaceLength(ICompletionProposal[] proposals,
			int replaceLengthIncrease)
	{
		if (proposals == null || proposals.length == 0 || replaceLengthIncrease == 0)
		{
			return proposals;
		}

		for (int i = 0; i < proposals.length; i++)
		{
			ICompletionProposal proposal = proposals[i];
			if (proposal instanceof PHPCompletionProposal)
			{
				PHPCompletionProposal phpProposal = ((PHPCompletionProposal) proposal);
				phpProposal.setReplacementLength(phpProposal.getReplacementLength() + replaceLengthIncrease);
			}

		}

		return proposals;
	}

	/**
	 * Gets elements index for a module.
	 * 
	 * @param content
	 *            - module content.
	 * @return elements index
	 */
	private IElementsIndex getIndex(String content, int offset)
	{
		IModule currentModule = getModule();
		if (currentModule == null)
		{
			return PHPGlobalIndexer.getInstance().getIndex();
		}

		final UnpackedElementIndex index = new UnpackedElementIndex();
		PDTPHPModuleIndexer indexer = new PDTPHPModuleIndexer(false, offset);
		indexer.indexModule(content, currentModule, new IIndexReporter()
		{

			public IElementEntry reportEntry(int category, String entryPath, IReportable value, IModule module)
			{
				return index.addEntry(category, entryPath, value, module);
			}

		});

		reportedScopeIsGlobal = indexer.isReportedScopeGlobal();
		reportedScopeIsUnderClass = indexer.isReportedScopeUnderClass();
		globalImports = indexer.getGlobalImports();
		aliases = indexer.getAliases();
		namespace = indexer.getNamespace();
		ModuleSubstitutionIndex result = new ModuleSubstitutionIndex(currentModule, index, PHPGlobalIndexer
				.getInstance().getIndex());
		return result;
	}

	/**
	 * Creates completion proposals for the list of entries or parse nodes.
	 * 
	 * @param offset
	 *            - offset.
	 * @param name
	 *            - name to complete.
	 * @param items
	 *            - items to create proposal for.
	 * @param module
	 *            - local module.
	 * @param applyDollarSymbol
	 *            - whether to apply dollar symbol
	 * @param index
	 *            - index to use.
	 * @param newInstanceCompletion
	 *            - whether the new instance completion is on.
	 * @return
	 */
	private List<ICompletionProposal> createProposals(final int offset, String name, List<Object> items,
			IModule module, boolean applyDollarSymbol, IElementsIndex index, boolean newInstanceCompletion)
	{
		String origName = name;
		int lastIndexOf = name.lastIndexOf('\\');
		if (lastIndexOf != -1)
		{
			name = name.substring(lastIndexOf + 1);
		}
		String lowerCase = name.toLowerCase();
		List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
		Set<Integer> usedProposalHash = new LinkedHashSet<Integer>();
		Map<Object, PHPCompletionProposal> itemsToProposals = new HashMap<Object, PHPCompletionProposal>();
		for (Object item : items)
		{
			PHPCompletionProposal proposal = null;
			if (item instanceof IPHPParseNode)
			{
				IPHPParseNode node = (IPHPParseNode) item;
				String firstName = node.getNodeName().toLowerCase();
				if (firstName.startsWith(lowerCase)
						|| (applyDollarSymbol && firstName.substring(1).startsWith(lowerCase)))
				{
					int itemHash = firstName.hashCode() + item.hashCode();
					if (!usedProposalHash.contains(itemHash))
					{
						if (node.getNodeName().charAt(0) == '$')
						{
							proposal = createProposal(node, offset, DOLLAR_SIGN + name, newInstanceCompletion);
						}
						else
						{
							proposal = createProposal(node, offset, name, newInstanceCompletion);
						}

						if (proposal != null)
						{
							usedProposalHash.add(itemHash);
						}
					}
				}
			}
			else if (item instanceof IElementEntry)
			{
				IElementEntry entry = (IElementEntry) item;
				String firstName;
				if (entry.getCategory() != IPHPIndexConstants.CONST_CATEGORY
						&& entry.getCategory() != IPHPIndexConstants.NAMESPACE_CATEGORY)
				{
					firstName = ElementsIndexingUtils.getLastNameInPath(entry.getEntryPath());
				}
				else
				{
					firstName = entry.getEntryPath().replaceAll(String.valueOf(IElementsIndex.DELIMITER),
							STATIC_DEREFERENCE_OP);
				}
				int itemHash = firstName.hashCode() + item.hashCode();
				if (entry.getValue() instanceof NamespacePHPEntryValue)
				{
					name = origName;
					lowerCase = origName.toLowerCase();

					if (lowerCase.startsWith(GLOBAL_NAMESPACE))
					{
						firstName = GLOBAL_NAMESPACE + firstName; // $codepro.audit.disable stringConcatenationInLoop
					}
					else if (!usedProposalHash.contains(firstName))
					{
						String k = firstName;
						int p = k.indexOf(name);
						if (p != -1)
						{
							k = k.substring(p);
						}
						else
						{
							if (firstName != null && firstName.toLowerCase().startsWith(lowerCase))
							{
								if (!usedProposalHash.contains(firstName))
								{
									proposal = createProposal(entry, offset, name, firstName, module,
											applyDollarSymbol, index, newInstanceCompletion);
									if (proposal != null)
									{
										usedProposalHash.add(itemHash);
										result.add(proposal);
										itemsToProposals.put(item, proposal);
									}

								}
							}
							continue;
						}
						proposal = createProposal(entry, offset, name, k, module, applyDollarSymbol, index,
								newInstanceCompletion);
						if (proposal != null)
						{
							usedProposalHash.add(itemHash);
						}
					}
				}
				else if (currentContext != null && !usedProposalHash.contains(firstName)
						&& NAMESPACE_PROPOSAL_CONTEXT_TYPE.equals(currentContext.getType())
						&& entry.getValue() instanceof ClassPHPEntryValue)
				{
					ClassPHPEntryValue classEntry = (ClassPHPEntryValue) entry.getValue();
					String classNamespace = classEntry.getNameSpace();
					if (classNamespace != null && classNamespace.length() > 0)
					{
						int prefixIndex = origName.indexOf(classNamespace);
						if (prefixIndex > -1)
						{
							// we have to make some adjustments here since we compute the 'use' namespace and then the
							// Class name.
							String completionContent = classNamespace + '\\' + firstName;
							proposal = createProposal(entry, offset, name, completionContent, module,
									applyDollarSymbol, index, newInstanceCompletion);
							if (proposal != null)
							{
								usedProposalHash.add(itemHash);
								result.add(proposal);
								itemsToProposals.put(item, proposal);
							}
							continue;
						}
					}
				}
				String lowerCaseFirstName = firstName.toLowerCase();
				if (firstName != null
						&& (lowerCaseFirstName.startsWith(lowerCase) || entry.getEntryPath().toLowerCase()
								.startsWith(lowerCase)))
				{
					if (!usedProposalHash.contains(firstName))
					{
						String n = name;
						if (!lowerCaseFirstName.startsWith(lowerCase))
						{
							// In this case set the name to an empty string to force the proposal to insert at the
							// current offset
							n = ""; //$NON-NLS-1$
						}
						proposal = createProposal(entry, offset, n, firstName, module, applyDollarSymbol, index,
								newInstanceCompletion);
						if (proposal != null)
						{
							usedProposalHash.add(itemHash);
						}
					}
				}
			}

			if (proposal != null)
			{
				result.add(proposal);
				itemsToProposals.put(item, proposal);
			}
		}
		prioritizeItems(itemsToProposals, module);
		return result;
	}

	/**
	 * Creates proposal for the index entry.
	 * 
	 * @param entry
	 *            - index entry.
	 * @param offset
	 *            - offset.
	 * @param name
	 *            - current name to complete.
	 * @param applyDollarSymbol
	 *            - whether to apply Dollar symbol.
	 * @param proposalContent
	 *            - proposal contents.
	 * @param localModule
	 *            - local module.
	 * @param index
	 *            - index to use
	 * @param newInstanceCompletion
	 *            - whether the new instance completion is on.
	 * @return proposalContent - proposal content.
	 */
	private PHPCompletionProposal createProposal(final IElementEntry entry, int offset, String name,
			final String proposalContent, IModule localModule, boolean applyDollarSymbol, final IElementsIndex index,
			boolean newInstanceCompletion)
	{
		if (!currentContext.acceptModelsElements())
		{
			return null;
		}

		if (!currentContext.getContextFilter().acceptElementEntry(entry))
		{
			return null;
		}

		offset = offset + 1;
		String replaceString = proposalContent;

		ProposalEnhancement enhancement = getEnhancement(entry, proposalContent, newInstanceCompletion);
		if (enhancement != null)
		{
			replaceString = enhancement.replaceString;
		}

		int replOffset = offset - name.length();
		int replLength = name.length();
		int cursorPos = proposalContent.length();
		if (enhancement != null)
		{
			cursorPos += enhancement.cursorShift;
		}

		Image image = null;
		final IModule module = entry.getModule();
		final Object val = entry.getValue();
		IDocumentationResolver resolver = new EntryDocumentationResolver(proposalContent, index, val, entry);
		// if (entry.getValue() instanceof NamespacePHPEntryValue)
		// {
		image = labelProvider.getImage(val);
		// }
		// TODO - Shalom - Check if this is needed, now that we use a decorated label provider.
		// if (entry.getValue() instanceof FunctionPHPEntryValue)
		// {
		// FunctionPHPEntryValue value = (FunctionPHPEntryValue) val;
		// int modifiers = value.getModifiers();
		// image = ItemDecoratingUtils.getFunctionImage(labelProvider, value.isMethod(), modifiers);
		// }
		// else if (entry.getValue() instanceof ClassPHPEntryValue)
		// {
		// ClassPHPEntryValue value = (ClassPHPEntryValue) val;
		// int modifiers = value.getModifiers();
		// image = ItemDecoratingUtils.getClassImage(labelProvider, modifiers);
		// }
		// else if (entry.getValue() instanceof VariablePHPEntryValue)
		// {
		// VariablePHPEntryValue value = (VariablePHPEntryValue) val;
		// int modifiers = value.getModifiers();
		// image = ItemDecoratingUtils.getVariableImage(labelProvider, value.isParameter(), value.isLocal(), value
		// .isField(), modifiers);
		// }

		String dispString = proposalContent;

		// if we propose variable, we should add $ in the front
		if (applyDollarSymbol && isVariableEntry(entry) && !dispString.startsWith(DOLLAR_SIGN))
		{
			dispString = DOLLAR_SIGN + dispString;
		}
		IContextInformation contextInformation = null;
		int objType = 0;
		String fileOloc = StringUtil.EMPTY;
		if (!(entry.getValue() instanceof NamespacePHPEntryValue))
		{
			if (localModule != null && localModule.equals(entry.getModule()))
			{

				dispString += "-[local]"; //$NON-NLS-1$
			}
			else
			{
				IModule m = entry.getModule();
				if (m != null)
				{
					dispString += "-[" + m.getShortName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				else
				{
					// the module might be null in a case on a built-in module (such
					// as the PHPFunctions5 file)
					if (dispString.length() == 0)
					{
						dispString = entry.getEntryPath();
					}
				}
			}
		}
		else
		{
			dispString += "-[namespace]"; //$NON-NLS-1$
		}
		String immediateTypesDisplayString = getImmediateTypesDisplayString(entry);
		if (immediateTypesDisplayString != null && immediateTypesDisplayString.length() > 0)
		{
			dispString += "-(" + immediateTypesDisplayString + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		PHPCompletionProposal cp = null;
		if ((currentContext != null && currentContext.isAutoActivateCAAfterApply()) || autoActivateAfterProposal(entry))
		{
			cp = new AutoActivateContentAssistProposal(replaceString, replOffset, replLength, cursorPos, image,
					dispString, contextInformation, null, objType, fileOloc, null);
			cp.setViewer(viewer);
		}
		else
		{
			cp = new PHPCompletionProposal(replaceString, replOffset, replLength, cursorPos, image, dispString,
					contextInformation, null, objType, fileOloc, null);
		}

		cp.setViewer(viewer);
		if (enhancement != null)
		{
			if (enhancement.positions != null)
			{
				increasePositions(enhancement.positions, replOffset);
				enhancement.caretExitOffset += replOffset;
				cp.setPositions(enhancement.positions, enhancement.caretExitOffset);
			}
		}
		cp.setResolver(resolver);
		cp.setTriggerCharacters(getProposalTriggerCharacters());
		return cp;
	}

	/**
	 * Finds classes recursively and adds to the argument list.
	 * 
	 * @param node
	 *            - node to search in.
	 * @param offset
	 *            - offset.
	 * @param classesToFill
	 *            - classes to fill.
	 */
	private PHPCompletionProposal createProposal(final IPHPParseNode node, int offset, String name,
			boolean newInstanceCompletion)
	{
		if (!currentContext.acceptBuiltins())
		{
			return null;
		}
		if (!currentContext.getContextFilter().acceptBuiltin(node))
		{
			return null;
		}
		offset = offset + 1;
		final String name2 = node.getNodeName();
		// if (node instanceof PHPFunctionParseNode) {
		// name2 = name2 + "()";
		// }
		String replaceString = name2;
		ProposalEnhancement enhancement = getEnhancement(node, replaceString, newInstanceCompletion);
		if (enhancement != null)
		{
			replaceString = enhancement.replaceString;
		}
		int replOffset = offset - name.length();
		int replLength = name.length();
		int cursorPos = name2.length();
		if (enhancement != null)
		{
			cursorPos += enhancement.cursorShift;
		}
		// if (node instanceof PHPFunctionParseNode) {
		// cursorPos--;
		// }
		PHPOutlineItem outlineItem = new PHPOutlineItem(EMPTY_RANGE, node);
		Image image = labelProvider.getImage(outlineItem);
		final String dispString = node.getNodeName();
		IContextInformation contextInformation = null;
		IDocumentationResolver resolver = new IDocumentationResolver()
		{
			public String resolveDocumentation()
			{
				String additionalInfo = ContentAssistUtils.getDocumentation(node, name2);
				return additionalInfo;
			}

			public String getProposalContent()
			{
				return dispString;
			}
		};
		int objType = 0;
		String fileOloc = StringUtil.EMPTY;
		Image[] images = new Image[3];
		if (PHPBuiltins.getInstance().existsInPHP4(node))
		{
			images[0] = fIcon4;
		}
		else
		{
			images[0] = fIcon4off;
		}
		if (PHPBuiltins.getInstance().existsInPHP5(node))
		{
			images[1] = fIcon5;
		}
		else
		{
			images[1] = fIcon5off;
		}
		if (PHPBuiltins.getInstance().existsInPHP53(node))
		{
			images[2] = fIcon53;
		}
		else
		{
			images[2] = fIcon53off;
		}
		PHPCompletionProposal cp = null;
		if ((currentContext != null && currentContext.isAutoActivateCAAfterApply()) || autoActivateAfterProposal(node))
		{
			cp = new AutoActivateContentAssistProposal(replaceString, replOffset, replLength, cursorPos, image,
					dispString, contextInformation, null, objType, fileOloc, images);
		}
		else
		{
			cp = new PHPCompletionProposal(replaceString, replOffset, replLength, cursorPos, image, dispString,
					contextInformation, null, objType, fileOloc, images);
		}
		cp.setViewer(viewer);
		if (enhancement != null)
		{
			if (enhancement.positions != null)
			{
				increasePositions(enhancement.positions, replOffset);
				enhancement.caretExitOffset += replOffset;
				cp.setPositions(enhancement.positions, enhancement.caretExitOffset);
			}
		}
		cp.setResolver(resolver);
		return cp;
	}

	/**
	 * Gets proposal enhancement.
	 * 
	 * @param entry
	 *            - either a PHPNode or IElementEntry
	 * @param proposalContent
	 *            - proposal content.
	 * @param newInstanceCompletion
	 *            - whether the new instance completion is on.
	 * @return proposal enhancement or null.
	 */
	private ProposalEnhancement getEnhancement(Object entry, String proposalContent, boolean newInstanceCompletion)
	{
		boolean insertAllowed = preferenceStore.getString(IContentAssistPreferencesConstants.INSERT_MODE).equals(
				IContentAssistPreferencesConstants.INSERT_MODE_INSERT)
				|| !checkParenthesesAlreadyExist();
		if (!insertAllowed)
		{
			return null;
		}

		ProposalEnhancement result = null;
		if (newInstanceCompletion)
		{
			if (preferenceStore.getBoolean(IContentAssistPreferencesConstants.INSERT_PARENTHESES_AFTER_NEW_INSTANCE))
			{
				if (result == null)
				{
					result = new ProposalEnhancement();
					result.replaceString = proposalContent;
				}
				result.replaceString = result.replaceString + "()"; //$NON-NLS-1$

				result.positions = new ArrayList<Position>();
				result.positions.add(new Position(result.replaceString.length() - 1));
				result.cursorShift = 1;
			}

			if (preferenceStore.getBoolean(IContentAssistPreferencesConstants.INSERT_SEMICOLON_AFTER_NEW_INSTANCE))
			{
				if (result == null)
				{
					result = new ProposalEnhancement();
					result.replaceString = proposalContent;
				}
				result.replaceString = result.replaceString + ";"; //$NON-NLS-1$
				result.cursorShift = 1;
			}

			if (result != null)
			{
				result.caretExitOffset = result.replaceString.length();
			}

			return result;
		}
		else
		{
			if (entry instanceof IElementEntry)
			{
				IElementEntry elementEntry = (IElementEntry) entry;
				Object value = elementEntry.getValue();
				if (value instanceof FunctionPHPEntryValue)
				{
					return getFunctionEntryEnhancement(elementEntry, proposalContent);
				}
				if (value instanceof VariablePHPEntryValue)
				{
					VariablePHPEntryValue m = (VariablePHPEntryValue) value;
					Set<Object> types = m.getTypes();
					if (types.size() == 1)
					{
						Object[] array = types.toArray(new Object[1]);
						if (array[0] != null && array[0].toString() != null)
							if (array[0].toString().startsWith(IPHPIndexConstants.LAMBDA_TYPE))
							{
								return getFunctionEntryEnhancement(elementEntry, proposalContent);
							}
					}
				}

			}
			else if (entry instanceof PHPFunctionParseNode)
			{
				return getFunctionNodeEnhancement((PHPFunctionParseNode) entry, proposalContent);
			}
			else if (entry instanceof IPHPParseNode)
			{
				// adding space after "extends", "implements" and "new" keywords
				IPHPParseNode nodeEntry = (IPHPParseNode) entry;
				if (nodeEntry.getNodeType() == IPHPParseNode.KEYWORD_NODE
						&& ("extends".equals(nodeEntry.getNodeName()) || //$NON-NLS-1$
								"implements".equals(nodeEntry.getNodeName()) || //$NON-NLS-1$
						"new".equals(nodeEntry.getNodeName()))) //$NON-NLS-1$
				{
					result = new ProposalEnhancement();
					result.replaceString = nodeEntry.getNodeName() + " "; //$NON-NLS-1$
					result.caretExitOffset = result.replaceString.length();
					result.cursorShift = 1;
					result.positions = new ArrayList<Position>();
				}
			}

			return result;
		}
	}

	/**
	 * Checks if parentheses already exist after the offset we are going to insert to.
	 * 
	 * @return true if parentheses already exist, false otherwise.
	 */
	private boolean checkParenthesesAlreadyExist()
	{
		for (int i = offset; i < content.length(); i++)
		{
			char ch = content.charAt(i);
			if (ch == '(')
			{
				return true;
			}

			if (!Character.isWhitespace(ch))
			{
				return false;
			}
		}

		return false;
	}

	/**
	 * Gets enhancement for function entry. Enhancement positions are related to the beginning of the proposal. Caller
	 * is responsible for mapping positions on the document.
	 * 
	 * @param elementEntry
	 *            - function element entry.
	 * @param proposalContent
	 *            - proposal content
	 * @return enhancement or null
	 */
	private ProposalEnhancement getFunctionEntryEnhancement(IElementEntry elementEntry, String proposalContent)
	{
		ProposalEnhancement result = null;
		// if (!(elementEntry.getValue() instanceof FunctionPHPEntryValue)) {
		//			throw new IllegalArgumentException("Only functions are accepted."); //$NON-NLS-1$
		// }

		if (preferenceStore.getBoolean(IContentAssistPreferencesConstants.INSERT_PARENTHESES_AFTER_METHOD_CALLS)
				|| preferenceStore.getBoolean(IContentAssistPreferencesConstants.INSERT_FUNCTION_PARAMETERS))
		{
			if (result == null)
			{
				result = new ProposalEnhancement();
				result.replaceString = proposalContent;
			}

			StringBuilder builder = new StringBuilder();
			builder.append('(');

			if (preferenceStore.getBoolean(IContentAssistPreferencesConstants.INSERT_FUNCTION_PARAMETERS))
			{
				if (elementEntry.getValue() instanceof VariablePHPEntryValue)
				{
					VariablePHPEntryValue vv = (VariablePHPEntryValue) elementEntry.getValue();
					String sm = (String) vv.getTypes().iterator().next();
					int indexOf = sm.indexOf('(');
					int la = sm.indexOf(')');
					String pNames = sm.substring(indexOf + 1, la);
					String[] parNames = pNames.split(","); //$NON-NLS-1$
					if (preferenceStore.getBoolean(IContentAssistPreferencesConstants.PARAMETRS_TAB_JUMP))
					{
						result.positions = new ArrayList<Position>();
					}
					updateResult(result, builder, Arrays.asList(parNames));
					builder.append(')');
					result.replaceString += builder.toString();
					result.cursorShift = 1;
					return result;
				}
				FunctionPHPEntryValue entryValue = (FunctionPHPEntryValue) elementEntry.getValue();

				if (entryValue.getParameters() != null)
				{
					if (preferenceStore.getBoolean(IContentAssistPreferencesConstants.PARAMETRS_TAB_JUMP))
					{
						result.positions = new ArrayList<Position>();
					}

					List<String> parameterNames = new ArrayList<String>(entryValue.getParameters().size());
					if (preferenceStore
							.getBoolean(IContentAssistPreferencesConstants.INSERT_OPTIONAL_FUNCTION_PARAMETERS))
					{
						parameterNames.addAll(entryValue.getParameters().keySet());
					}
					else
					{
						Iterator<String> namesIterator = entryValue.getParameters().keySet().iterator();
						boolean[] mandatoryParams = entryValue.getMandatoryParams();
						for (boolean isMandatory : mandatoryParams)
						{
							String name = namesIterator.next();
							if (isMandatory)
							{
								parameterNames.add(name);
							}
						}
					}
					updateResult(result, builder, parameterNames);
				}
			}

			builder.append(')');
			result.replaceString += builder.toString();
			result.cursorShift = 1;
		}

		if (elementEntry.getValue() instanceof FunctionPHPEntryValue
				&& preferenceStore.getBoolean(IContentAssistPreferencesConstants.INSERT_SEMICOLON_AFTER_METHOD_CALLS))
		{
			if (result == null)
			{
				result = new ProposalEnhancement();
				result.replaceString = proposalContent;
			}

			result.replaceString += ";"; //$NON-NLS-1$
			result.cursorShift = 1;
		}

		if (result != null)
		{
			result.caretExitOffset = result.replaceString.length();
		}

		return result;
	}

	private void updateResult(ProposalEnhancement result, StringBuilder builder, List<String> parameterNames)
	{
		for (int i = 0; i < parameterNames.size() - 1; i++)
		{
			String parameterName = parameterNames.get(i);

			int start = builder.length() + result.replaceString.length();

			builder.append('$');
			builder.append(parameterName);

			int length = builder.length() + result.replaceString.length() - start;
			builder.append(", "); //$NON-NLS-1$

			if (result.positions != null)
			{
				result.positions.add(new Position(start, length));
			}
		}

		if (parameterNames.size() > 0)
		{
			int start = builder.length() + result.replaceString.length();
			builder.append('$');
			builder.append(parameterNames.get(parameterNames.size() - 1));
			int length = builder.length() + result.replaceString.length() - start;
			if (result.positions != null)
			{
				result.positions.add(new Position(start, length));
			}
		}
		else
		{
			result.positions.add(new Position(builder.length() + result.replaceString.length(), 0));
		}
	}

	/**
	 * Gets enhancement for function entry.
	 * 
	 * @param node
	 *            - function node.
	 * @param proposalContent
	 *            - proposal content
	 * @return enhancement or null
	 */
	private ProposalEnhancement getFunctionNodeEnhancement(PHPFunctionParseNode node, String proposalContent)
	{
		ProposalEnhancement result = null;

		if (preferenceStore.getBoolean(IContentAssistPreferencesConstants.INSERT_PARENTHESES_AFTER_METHOD_CALLS)
				|| preferenceStore.getBoolean(IContentAssistPreferencesConstants.INSERT_FUNCTION_PARAMETERS))
		{
			if (result == null)
			{
				result = new ProposalEnhancement();
				result.replaceString = proposalContent;
			}
			StringBuilder builder = new StringBuilder();
			builder.append('(');
			if (preferenceStore.getBoolean(IContentAssistPreferencesConstants.INSERT_FUNCTION_PARAMETERS))
			{
				Parameter[] parameters = node.getParameters();
				if (parameters != null && parameters.length != 0)
				{
					boolean insertOptionals = preferenceStore
							.getBoolean(IContentAssistPreferencesConstants.INSERT_OPTIONAL_FUNCTION_PARAMETERS);
					if (preferenceStore.getBoolean(IContentAssistPreferencesConstants.PARAMETRS_TAB_JUMP))
					{
						result.positions = new ArrayList<Position>();
					}
					boolean commaAppended = false;
					for (int i = 0; i < parameters.length - 1; i++)
					{
						// If we do not allow the insertion of optional params
						// we screen out any parameter with an empty string as a
						// default value.
						// TODO - It will be better if a non-existing default
						// value will be null, and not an empty string (this
						// implementation might be incorrect when
						// the default value is an empty string)
						if (parameters[i].getDefaultValue() != null && !insertOptionals
								&& !StringUtil.EMPTY.equals(parameters[i].getDefaultValue()))
						{
							break; // once we have a default value we can stop
							// (since the optional params are always
							// right to the mandatory ones)
						}
						String parameterName = parameters[i].getVariableName();
						if (parameterName != null && !parameterName.startsWith(DOLLAR_SIGN))
						{
							parameterName = DOLLAR_SIGN + parameterName; // $codepro.audit.disable
																			// stringConcatenationInLoop
							int start = builder.length() + result.replaceString.length();

							builder.append(parameterName);

							int length = builder.length() + result.replaceString.length() - start;

							builder.append(", "); //$NON-NLS-1$
							commaAppended = true;
							if (result.positions != null)
							{
								result.positions.add(new Position(start, length));
							}
						}
					}

					int start = builder.length() + result.replaceString.length();
					// make sure that the last param fits the 'optional
					// insertion' policy
					if (insertOptionals || parameters[parameters.length - 1].getDefaultValue() == null
							|| !insertOptionals
							&& StringUtil.EMPTY.equals(parameters[parameters.length - 1].getDefaultValue()))
					{
						String name = parameters[parameters.length - 1].getVariableName();
						if (name != null && !name.startsWith(DOLLAR_SIGN))
						{
							name = DOLLAR_SIGN + name;
							builder.append(name);
							int length = builder.length() + result.replaceString.length() - start;

							if (result.positions != null)
							{
								result.positions.add(new Position(start, length));
							}
							commaAppended = false;
						}
					}

					if (commaAppended)
					{
						builder.delete(builder.length() - 2, builder.length());
					}
				}
				else
				{
					result.positions = new ArrayList<Position>();
					result.positions.add(new Position(builder.length() + result.replaceString.length(), 0));
				}
			}
			builder.append(')');

			result.replaceString += builder.toString();
			result.cursorShift = 1;
		}

		if (preferenceStore.getBoolean(IContentAssistPreferencesConstants.INSERT_SEMICOLON_AFTER_METHOD_CALLS))
		{
			if (result == null)
			{
				result = new ProposalEnhancement();
				result.replaceString = proposalContent;
			}
			result.replaceString += ";"; //$NON-NLS-1$
			result.cursorShift = 1;
		}

		if (result != null)
		{
			result.caretExitOffset = result.replaceString.length();
		}

		return result;
	}

	/**
	 * Gets display string for the types of the entry that can be determinate immediately.
	 * 
	 * @param entry
	 *            - entry.
	 * @return display string
	 */
	private String getImmediateTypesDisplayString(IElementEntry entry)
	{
		Object entryValue = entry.getValue();
		if (entryValue instanceof ClassPHPEntryValue)
		{
			return StringUtil.EMPTY;
		}
		else if (entryValue instanceof FunctionPHPEntryValue)
		{
			Set<Object> returnTypes = ((FunctionPHPEntryValue) entryValue).getReturnTypes();
			if (CollectionsUtil.isEmpty(returnTypes))
			{
				return StringUtil.EMPTY;
			}

			List<String> knownTypes = new ArrayList<String>();
			for (Object type : returnTypes)
			{
				if (type instanceof String)
				{
					knownTypes.add((String) type);
				}
			}

			if (knownTypes.size() == 0)
			{
				return "..."; //$NON-NLS-1$
			}

			String typesString = getTypesDisplayString(knownTypes, entry);
			if (knownTypes.size() != returnTypes.size())
			{
				typesString += ", ..."; //$NON-NLS-1$
			}

			return typesString;
		}
		else if (entryValue instanceof VariablePHPEntryValue)
		{
			Set<Object> variableTypes = ((VariablePHPEntryValue) entryValue).getTypes();
			if (variableTypes == null || variableTypes.size() == 0)
			{
				return StringUtil.EMPTY;
			}

			List<String> knownTypes = new ArrayList<String>();
			for (Object type : variableTypes)
			{
				if (type instanceof String)
				{
					knownTypes.add((String) type);
				}
			}

			if (knownTypes.size() == 0)
			{
				return "..."; //$NON-NLS-1$
			}

			String typesString = getTypesDisplayString(knownTypes, entry);
			if (variableTypes.size() != knownTypes.size())
			{
				typesString += ", ..."; //$NON-NLS-1$
			}

			return typesString;
		}
		else
		{
			return StringUtil.EMPTY;
		}
	}

	/**
	 * Gets display string for the list of types.
	 * 
	 * @param types
	 *            - types.
	 * @return display string
	 */
	private static String getTypesDisplayString(List<String> types, IElementEntry entry)
	{
		List<String> sortedTypes = new ArrayList<String>(types.size());
		sortedTypes.addAll(types);
		Collections.sort(sortedTypes);

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < sortedTypes.size() - 1; i++)
		{
			String type = sortedTypes.get(i);
			result.append(type);
			result.append(", "); //$NON-NLS-1$
		}

		result.append(sortedTypes.get(sortedTypes.size() - 1));

		return ContentAssistUtils.truncateLineIfNeeded(result.toString());
	}

	/**
	 * Gets whether content assist auto-activation is required after current item proposal.
	 * 
	 * @param item
	 *            - item.
	 * @return
	 */
	private boolean autoActivateAfterProposal(Object item)
	{
		if (item instanceof IPHPParseNode)
		{
			IPHPParseNode node = (IPHPParseNode) item;
			if (node.getNodeType() == IPHPParseNode.KEYWORD_NODE && "new".equals(node.getNodeName())) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	/**
	 * Increases each position offset by the offset specified.
	 * 
	 * @param positions
	 *            - positions, which offsets to increase.
	 * @param offset
	 *            - offset to increase positions by.
	 */
	private void increasePositions(List<Position> positions, int offset)
	{
		for (Position pos : positions)
		{
			pos.offset += offset;
		}
	}

	/**
	 * Gets current module.
	 * 
	 * @return current module.
	 */
	private IModule getModule()
	{
		if (module == null)
		{
			PHPSourceEditor phpSourceEditor = (PHPSourceEditor) editor;
			module = phpSourceEditor.getModule();
			isOutOfWorkspace = phpSourceEditor.isOutOfWorkspace();
		}
		return module;
	}

	/**
	 * Gets whether entry is variable entry.
	 * 
	 * @param entry
	 *            - entry.
	 * @return true if variable entry, false otherwise
	 */
	private static boolean isVariableEntry(IElementEntry entry)
	{
		if (entry.getCategory() != IPHPIndexConstants.VAR_CATEGORY)
		{
			return false;
		}

		if (entry.getValue() == null)
		{
			return false;
		}
		return entry.getValue() instanceof VariablePHPEntryValue;
	}

	/**
	 * Checks whether variable is constant (final + static)
	 * 
	 * @param entry
	 *            - variable entry.
	 * @return true if constant, false otherwise.
	 */
	private static boolean isConstVariable(IElementEntry entry)
	{
		if (!isVariableEntry(entry))
		{
			return false;
		}

		int modifier = ((VariablePHPEntryValue) entry.getValue()).getModifiers();

		return PHPFlags.isStatic(modifier) && PHPFlags.isFinal(modifier);
	}

	/**
	 * Gets whether entry is function entry.
	 * 
	 * @param entry
	 *            - entry.
	 * @return true if variable entry, false otherwise
	 */
	private static boolean isFunctionEntry(IElementEntry entry)
	{
		if (entry.getCategory() != IPHPIndexConstants.FUNCTION_CATEGORY
				&& entry.getCategory() != IPHPIndexConstants.LAMBDA_FUNCTION_CATEGORY)
		{
			return false;
		}

		if (entry.getValue() == null)
		{
			return false;
		}
		return entry.getValue() instanceof FunctionPHPEntryValue;
	}

	/**
	 * Gets whether entry is class entry.
	 * 
	 * @param entry
	 *            - entry.
	 * @return true if variable entry, false otherwise
	 */
	private static boolean isClassEntry(IElementEntry entry)
	{
		if (entry.getCategory() != IPHPIndexConstants.CLASS_CATEGORY)
		{
			return false;
		}

		if (entry.getValue() == null)
		{
			return false;
		}
		return entry.getValue() instanceof ClassPHPEntryValue;
	}

	/**
	 * Prioritize the items that will be displayed in the CA.
	 * 
	 * @param itemsToProposals
	 *            A map of elements (php items) to their proposals.
	 * @param localModule
	 *            - local module.
	 * @return sorted items.
	 */
	private void prioritizeItems(Map<Object, PHPCompletionProposal> itemsToProposals, IModule localModule)
	{
		// extract the external items into this list.
		List<Object> externals = new ArrayList<Object>();

		for (Object item : itemsToProposals.keySet())
		{
			if (item instanceof IElementEntry)
			{
				if (localModule == null || localModule.equals(((IElementEntry) item).getModule()))
				{
					// local items should have the original order and come first
					itemsToProposals.get(item).setRelevance(ICommonCompletionProposal.RELEVANCE_HIGH + 5);
				}
				else
				{
					externals.add(item);
				}
			}
			else
			{
				externals.add(item);
			}
		}

		// Prioritize the external items.
		// We set the priority to classes, functions, constants and then all the rest.
		for (Object item : externals)
		{
			if (item instanceof IElementEntry)
			{
				IElementEntry entry = (IElementEntry) item;
				switch (entry.getCategory())
				{
					case IPHPIndexConstants.CLASS_CATEGORY:
						itemsToProposals.get(item).setRelevance(EXTERNAL_CLASS_PROPOSAL_RELEVANCE);
						break;
					case IPHPIndexConstants.FUNCTION_CATEGORY:
						itemsToProposals.get(item).setRelevance(EXTERNAL_FUNCTION_PROPOSAL_RELEVANCE);
						break;
					case IPHPIndexConstants.CONST_CATEGORY:
						itemsToProposals.get(item).setRelevance(EXTERNAL_CONSTANT_PROPOSAL_RELEVANCE);
						break;
					default:
						itemsToProposals.get(item).setRelevance(EXTERNAL_DEFAULT_PROPOSAL_RELEVANCE);
				}
			}
			else if (item instanceof PHPBaseParseNode)
			{
				PHPBaseParseNode parseNode = (PHPBaseParseNode) item;
				switch (parseNode.getNodeType())
				{
					case IPHPParseNode.KEYWORD_NODE:
						// Set high priority for PHP keywords.
						itemsToProposals.get(item).setRelevance(ICommonCompletionProposal.RELEVANCE_HIGH - 2);
						break;
					case IPHPParseNode.CLASS_NODE:
						itemsToProposals.get(item).setRelevance(EXTERNAL_CLASS_PROPOSAL_RELEVANCE - 2);
						break;
					case IPHPParseNode.FUNCTION_NODE:
						itemsToProposals.get(item).setRelevance(EXTERNAL_FUNCTION_PROPOSAL_RELEVANCE - 2);
						break;
					case IPHPParseNode.CONST_NODE:
						itemsToProposals.get(item).setRelevance(EXTERNAL_CONSTANT_PROPOSAL_RELEVANCE - 2);
						break;
					default:
						itemsToProposals.get(item).setRelevance(EXTERNAL_DEFAULT_PROPOSAL_RELEVANCE - 2);
				}
			}
			else
			{
				// All the rest of the PHP built-in API item can
				itemsToProposals.get(item).setRelevance(ICommonCompletionProposal.RELEVANCE_LOW + 10);
			}
		}
	}

	/**
	 * Counts replace length increase when override insertion mode is on.
	 * 
	 * @param content
	 *            - document content.
	 * @param offset
	 *            - insertion offset.
	 * @return replace length increase.
	 */
	private int countReplaceLengthIncrease(String content, int offset)
	{
		if (!preferenceStore.getString(IContentAssistPreferencesConstants.INSERT_MODE).equals(
				IContentAssistPreferencesConstants.INSERT_MODE_OVERWRITE))
		{
			return 0;
		}

		for (int i = offset; i < content.length(); i++)
		{
			char ch = content.charAt(i);
			if (!Character.isJavaIdentifierPart(ch))
			{
				return i - offset;
			}
		}

		return 0;
	}

	// Returns true if the given token is one of the PHP import tokens (include/require)
	private boolean checkInclude(String token)
	{
		if (token != null)
		{
			return PHPRegionTypes.PHP_INCLUDE.equals(token) || PHPRegionTypes.PHP_INCLUDE_ONCE.equals(token)
					|| PHPRegionTypes.PHP_REQUIRE.equals(token) || PHPRegionTypes.PHP_REQUIRE_ONCE.equals(token);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.common.CommonContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer
	 * , int)
	 */
	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset)
	{
		IDocument document = viewer.getDocument();
		if (document == null)
		{
			return EMPTY_CONTEXT_INFO;
		}
		String content = document.get();
		// We traverse back the partitions until we are 'far enough' for the context information calculation.
		// We need to do that because the ParsingUtils.createLexemeProvider(offset) call only computes the default-PHP
		// partition, and stops on PHP-strings partitions etc. (see https://jira.appcelerator.org/browse/APSTUD-3595)
		int start = offset;
		try
		{
			ITypedRegion partition = document.getPartition(offset);
			while (partition != null && partition.getOffset() > 0
					&& !CompositePartitionScanner.START_SWITCH_TAG.equals(partition.getType()))
			{
				partition = document.getPartition(partition.getOffset() - 1);
				if (offset - partition.getOffset() > CONTEXT_INFO_MAX_LOOK_BEHIND)
				{
					break;
				}
			}
			start = partition.getOffset();
		}
		catch (Exception e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), e);
		}

		ILexemeProvider<PHPTokenType> lexemeProvider = ParsingUtils.createLexemeProvider(document, start, offset);
		CallInfo info = PHPContextCalculator.calculateCallInfo(lexemeProvider, offset);
		if (info == null)
		{
			return EMPTY_CONTEXT_INFO;
		}
		List<?> items = ContentAssistUtils.selectModelElements(info.getName(), true);
		// if no built-in items found, trying to find the custom ones
		if (items.size() == 0)
		{
			Set<IElementEntry> entries = null;

			IElementsIndex index = getIndex(content, offset);
			ITypedRegion partition = viewer.getDocument().getDocumentPartitioner().getPartition(offset);
			// trying to get dereference entries
			List<String> callPath = ParsingUtils.parseCallPath(partition, content, info.getNameEndPos(), OPS, false,
					document);
			if (callPath == null || callPath.isEmpty())
			{
				return EMPTY_CONTEXT_INFO;
			}

			if (callPath.size() > 1)
			{
				if (DEREFERENCE_OP.equals(callPath.get(1)))
				{
					entries = computeDereferenceEntries(index, callPath, info.getNameEndPos(), getModule(), true,
							aliases, namespace);
				}
				else
				{
					entries = computeStaticDereferenceEntries(index, callPath, info.getNameEndPos(), getModule(), true,
							aliases, namespace);
				}
			}
			else
			{
				List<IElementEntry> res = computeSimpleIdentifierEntries(reportedScopeIsGlobal, globalImports,
						info.getName(), false, index, true, getModule(), false, namespace, aliases);
				if (res != null)
				{
					entries = new LinkedHashSet<IElementEntry>();
					entries.addAll(res);
				}
			}

			if (entries == null)
			{
				return EMPTY_CONTEXT_INFO;
			}

			// FIXME: Shalom - What about class constructors?
			entries = ContentAssistFilters.filterAllButFunctions(entries, index);
			if (entries.size() == 0)
			{
				return EMPTY_CONTEXT_INFO;
			}

			IElementEntry funcEntry = entries.iterator().next();
			IContextInformation ci = PHPContextCalculator.computeArgContextInformation(funcEntry, info.getNameEndPos());
			if (ci == null || StringUtil.isEmpty(ci.getInformationDisplayString()))
			{
				return EMPTY_CONTEXT_INFO;
			}
			IContextInformation[] res = new IContextInformation[1];
			res[0] = ci;
			return res;
		}
		else
		{
			IPHPParseNode pn = (IPHPParseNode) items.get(0);
			IContextInformation[] ici = null;
			IContextInformation ci = null;
			if (pn instanceof PHPFunctionParseNode)
			{
				ci = PHPContextCalculator.computeArgContextInformation((PHPFunctionParseNode) pn, info.getNameEndPos());
			}
			else if (pn instanceof PHPClassParseNode)
			{
				ci = PHPContextCalculator.computeConstructorContextInformation((PHPClassParseNode) pn,
						info.getNameEndPos());
			}

			if (ci != null)
			{
				ici = new IContextInformation[] { ci };
			}
			else
			{
				ici = EMPTY_CONTEXT_INFO;
			}
			return ici;
		}
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters()
	{
		return autoactivationCharacters;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters()
	{
		return contextInformationActivationChars;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator()
	{
		return new PHPContextInformationValidator();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CommonContentAssistProcessor#getPreferenceNodeQualifier()
	 */
	protected String getPreferenceNodeQualifier()
	{
		return PHPEditorPlugin.PLUGIN_ID;
	}
}
