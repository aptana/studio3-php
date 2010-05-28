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

import static com.aptana.editor.php.internal.contentAssist.PHPContextCalculator.EXTENDS_PROPOSAL_CONTEXT_TYPE;
import static com.aptana.editor.php.internal.contentAssist.PHPContextCalculator.IMPLEMENTS_PROPOSAL_CONTEXT_TYPE;
import static com.aptana.editor.php.internal.contentAssist.PHPContextCalculator.NEW_PROPOSAL_CONTEXT_TYPE;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.php.core.compiler.PHPFlags;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.documentModel.parser.AbstractPhpLexer;
import org.eclipse.php.internal.core.documentModel.parser.PhpLexerFactory;
import org.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;
import org.eclipse.swt.graphics.Image;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonContentAssistProcessor;
import com.aptana.editor.common.contentassist.LexemeProvider;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IIndexReporter;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.indexer.IReportable;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants;
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
import com.aptana.editor.php.internal.parser.nodes.PHPFunctionParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPVariableParseNode;
import com.aptana.editor.php.internal.parser.nodes.Parameter;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;
import com.aptana.editor.php.internal.ui.editor.PHPVersionDocumentManager;
import com.aptana.editor.php.internal.ui.editor.outline.PHPDecoratingLabelProvider;
import com.aptana.editor.php.internal.ui.editor.outline.PHPOutlineItem;
import com.aptana.parsing.lexer.IRange;
import com.aptana.parsing.lexer.Range;

/**
 * Content assist processor for PHP.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
@SuppressWarnings("unused")
public class PHPContentAssistProcessor extends CommonContentAssistProcessor implements IContentAssistProcessor
{
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	protected static final String DOLLAR_SIGN = "$"; //$NON-NLS-1$

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
	 * "$self" activation sequence.
	 */
	private static final String SELF_ACTIVATION_SEQUENCE = "self"; //$NON-NLS-1$

	/**
	 * "new" activation sequence.
	 */
	private static final String NEW_ACTIVATION_SEQUENCE = "new"; //$NON-NLS-1$
	private static final IRange EMPTY_RANGE = new Range(0, 0);

	private static Image fIcon53 = PHPEditorPlugin.getImage("icons/full/obj16/v53.png"); //$NON-NLS-1$
	private static Image fIcon5 = PHPEditorPlugin.getImage("icons/full/obj16/v5.png"); //$NON-NLS-1$
	private static Image fIcon4 = PHPEditorPlugin.getImage("icons/full/obj16/v4.png"); //$NON-NLS-1$
	private static Image fIcon53off = PHPEditorPlugin.getImage("icons/full/obj16/v53_off.png"); //$NON-NLS-1$
	private static Image fIcon5off = PHPEditorPlugin.getImage("icons/full/obj16/v5_off.png"); //$NON-NLS-1$
	private static Image fIcon4off = PHPEditorPlugin.getImage("icons/full/obj16/v4_off.png"); //$NON-NLS-1$

	private static char[] autoactivationCharacters = new char[] { '>', '@', '$', ':' };
	private static char[] contextautoactivationCharacters = new char[] { '(' };
	private static PHPDecoratingLabelProvider labelProvider = new PHPDecoratingLabelProvider();
	private ITextViewer viewer;
	private int offset;
	private String content;
	/**
	 * Whether reported stack is global.
	 */
	private boolean reportedScopeUnderClassOrFunction = true;

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

		AbstractPhpLexer lexer = PhpLexerFactory.createLexer(new StringReader(content), phpVersion);
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
			PHPEditorPlugin.logError(e);
			return null;
		}
		lexer.initialize(state);
		lexer.setPatterns(null);
		lexer.setAspTags(true);

		String prev = null;
		String prev2 = null;
		String lastN = null;
		int pos = 0;
		StringBuilder contentS = null;
		try
		{
			while (true)
			{
				String next_token = lexer.getNextToken();
				int left = lexer.getTokenStart();
				int right = left + lexer.yylength();
				String value = lexer.yytext();
				if (next_token == PHPRegionTypes.PHP_NAMESPACE || next_token == PHPRegionTypes.PHP_USE)
				{
					lastN = next_token;
					contentS = new StringBuilder();
				}
				else if (lastN != null)
				{
					if (next_token == PHPRegionTypes.PHP_STRING || next_token == PHPRegionTypes.PHP_NS_SEPARATOR)
					{
						if (contentS.length() == 0)
						{
							pos = left;
						}
						contentS.append(value == null ? '\\' : value);
					}
					else
					{
						lastN = null;
					}
				}
				if (lastN != null)
				{
					if (left <= offset && right >= offset)
					{
						return getNamespaceCompletionProposals(content, contentS.toString(), pos, contentS.length(), 1,
								viewer);
					}
				}
				if (left < offset && right > offset)
				{

					if (next_token == PHPRegionTypes.PHP_CONSTANT_ENCAPSED_STRING)
					{
						if (prev2 != null || prev != null)
						{
							if (checkInclude(prev2) || checkInclude(prev))
							{
								String substring = value.substring(1, offset - left);
								// FIXME: Shalom - Implement getFilePathCompletionProposals
								// return getFilePathCompletionProposals(substring, left + 1, substring.length(), 1,
								// viewer);
								return null;
							}
						}
						return null;
					}

					// System.out.println(next_token);
				}
				if (next_token == null)
				{
					break;
				}
				prev2 = prev;
				prev = next_token;
			}
		}
		catch (IOException e)
		{
			PHPEditorPlugin.logError(e);
		}
		if (isInState(lexer, "ST_PHP_COMMENT")) //$NON-NLS-1$
		{
			if (isInState(lexer, "ST_PHP_DOC_COMMENT")) //$NON-NLS-1$
			{
				// FIXME: Shalom - implement the docPorcessor
				// return docProcessor.computeCompletionProposals(viewer, offset);
				return null;
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
		// if (activationChar == '@' && autoActivated)
		// {
		// return new ICompletionProposal[0];
		// }

		boolean forceActivation = false;
		Boolean fa = (Boolean) viewer.getTextWidget().getData("ASSIST_FORCE_ACTIVATION"); //$NON-NLS-1$
		if (fa != null)
		{
			forceActivation = fa;
		}

		int replaceLengthIncrease = countReplaceLengthIncrease(content, offset);

		LexemeProvider<PHPTokenType> lexemeProvider = new LexemeProvider<PHPTokenType>(document, offset,
				new PHPScopeScanner())
		{
			@Override
			protected PHPTokenType getTypeFromData(Object data)
			{
				return new PHPTokenType(data.toString());
			}
		};
		// Calculates and sets completion context
		currentContext = contextCalculator.calculateCompletionContext(lexemeProvider, offset);

		ICompletionProposal[] computeCompletionProposalInternal = computeCompletionProposalInternal(offset, content,
				true, forceActivation);
		if (computeCompletionProposalInternal.length > 0)
		{
			PHPCompletionProposal pa = (PHPCompletionProposal) computeCompletionProposalInternal[0];
			pa.setIsDefaultSelection(true);
			pa.setIsSuggestedSelection(true);
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
	}

	/**
	 * Computes proposals.
	 * 
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
	public ICompletionProposal[] computeCompletionProposalInternal(final int offset, String content,
			boolean proposeBuiltins, boolean forceActivation)
	{
		final int start = offset == 0 ? 0 : offset - 1;

		this.offset = offset;
		this.content = content;

		List<String> callPath = ParsingUtils.parseCallPath(content, start, OPS, false);
		if (callPath == null || callPath.isEmpty())
		{
			return new ICompletionProposal[0];
		}

		if (callPath.size() > 1)
		{
			if (hasStaticDereferenceOperatorAfterTheFirst(callPath))
			{
				return new ICompletionProposal[0];
			}

			if (DEREFERENCE_OP.equals(callPath.get(1)))
			{
				return dereferencingCompletion(getIndex(content, start), callPath, start, getModule());
			}
			else
			{
				return dereferencingStaticCompletion(getIndex(content, start), callPath, start, getModule());
			}
		}
		else if (callPath.size() == 1)
		{
			this.hashCode();
			// if content assistant is not auto-activated and we should not
			// auto-activate on
			// identifiers, skipping the proposals computation.
			if (forceActivation
					&& !preferenceStore.getBoolean(IContentAssistPreferencesConstants.AUTO_ACTIVATE_ON_IDENTIFIERS))
			{
				return new ICompletionProposal[0];
			}
			String identifier = callPath.get(0);

			// TODO refactor new instance completion to use the new completion
			// context system
			// if (isNewInstanceCompletion(start, identifier, content))
			// {
			// return newInstanceCompletion(start, content, identifier,
			// getModule());
			// }

			return simpleIdentifierCompletion(start, content, identifier.toLowerCase(),
					reportedScopeUnderClassOrFunction, globalImports, getModule(), proposeBuiltins);
		}

		return new ICompletionProposal[0];
	}

	/**
	 * Returns a namespace-completion proposals.
	 * 
	 * @param text
	 * @param text2
	 * @param i
	 * @param length
	 * @param j
	 * @param viewer2
	 * @return
	 */
	private ICompletionProposal[] getNamespaceCompletionProposals(String text, String text2, int i, int length, int j,
			ITextViewer viewer2)
	{
		ArrayList<ICompletionProposal> completionProposals = new ArrayList<ICompletionProposal>();
		IElementsIndex index = getIndex(text, i);
		List<IElementEntry> entries = index.getEntriesStartingWith(IPHPIndexConstants.NAMESPACE_CATEGORY, EMPTY_STRING);
		HashSet<String> paths = new HashSet<String>();
		for (IElementEntry e : entries)
		{
			String entryPath = e.getEntryPath();
			if (!entryPath.startsWith(text2))
			{
				continue;
			}
			if (!paths.contains(entryPath))
			{
				paths.add(entryPath);
				completionProposals.add(createProposal(e, i + text2.length() - 1, text2, entryPath, e.getModule(),
						false, index, false));
			}
		}
		ICompletionProposal[] result = completionProposals.toArray(new ICompletionProposal[0]);
		if (result.length > 0)
		{
			((PHPCompletionProposal) result[0]).setIsDefaultSelection(true);
		}
		return result;
	}

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
		Set<IElementEntry> result = computeDereferenceEntries(index, callPath, offset == 0 ? 0 : offset - 1, module,
				false);
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
	 * @return dereference entries or null.
	 */
	public static Set<IElementEntry> computeDereferenceEntries(IElementsIndex index, List<String> callPath, int offset,
			IModule module, boolean exactMatch)
	{
		Set<IElementEntry> leftDereferenceEntries = computeDereferenceLeftEntries(index,
				pathEntryName(callPath.get(0)), offset, module);
		if (leftDereferenceEntries == null)
		{
			return null;
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

			result = computeDereferenceRightEntries(leftDereferenceEntries, index, pathEntryName(callPath.get(i)),
					offset, module, currentExactMatchFlag, applyAccessRestriction, innerCompletion);
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
		Set<IElementEntry> result = computeStaticDereferenceEntries(index, callPath, offset == 0 ? 0 : offset - 1,
				module, false);
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
				lastName = "$" + lastName; //$NON-NLS-1$
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
	 * @return set of entries or null.
	 */
	public static Set<IElementEntry> computeStaticDereferenceEntries(IElementsIndex index, List<String> callPath,
			int offset, IModule module, boolean exactMatch)
	{
		Set<IElementEntry> leftDereferenceEntries = computeStaticDereferenceLeftEntries(index, pathEntryName(callPath
				.get(0)), offset, module);
		if (leftDereferenceEntries == null || leftDereferenceEntries.isEmpty())
		{
			return null;
		}

		boolean innerCompletion = false;
		if (SELF_ACTIVATION_SEQUENCE.equals(callPath.get(0)))
		{
			innerCompletion = true;
		}

		boolean currentExactMatch = true;
		if (callPath.size() == 3)
		{
			currentExactMatch = exactMatch;
		}
		Set<IElementEntry> result = computeStaticDereferenceRightEntries(index, leftDereferenceEntries,
				pathEntryName(callPath.get(2)), offset, module, currentExactMatch, true, innerCompletion);
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
						offset, module, currentExactMatch, applyAccessRestriction, innerCompletion);
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
	 * @return set of entries or null.
	 */
	private static Set<IElementEntry> computeStaticDereferenceRightEntries(IElementsIndex index,
			Set<IElementEntry> leftEntries, String right, int offset, IModule module, boolean exactMatch,
			boolean applyAccessModifiers, boolean innerEntries)
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
				filter = new AccessModifierEntryFilter(customLeftTypes);
			}
			else
			{
				filter = new PublicsOnlyEntryFilter();
			}
		}

		Set<String> typesWithAncestors = TypeHierarchyUtils.addAllAncestors(customLeftTypes, index);

		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();
		// now searching for the possible right parts
		if (right.length() == 0)
		{
			Set<IElementEntry> varResults = ContentAssistCollectors.collectVariableEntries(index, DOLLAR_SIGN,
					typesWithAncestors, exactMatch);
			if (varResults != null)
			{
				result.addAll(varResults);
			}
			Set<IElementEntry> funcResults = ContentAssistCollectors.collectFunctionEntries(index, right,
					typesWithAncestors, exactMatch);
			if (funcResults != null)
			{
				result.addAll(funcResults);
			}
			Set<IElementEntry> constResults = ContentAssistCollectors.collectConstEntries(index, right,
					typesWithAncestors, exactMatch);
			if (constResults != null)
			{
				result.addAll(constResults);
			}
		}
		else if (right.startsWith(DOLLAR_SIGN))
		{
			result.addAll(ContentAssistCollectors.collectVariableEntries(index, right, typesWithAncestors, exactMatch));
		}
		else
		{
			result.addAll(ContentAssistCollectors.collectConstEntries(index, right, typesWithAncestors, exactMatch));
			result.addAll(ContentAssistCollectors.collectFunctionEntries(index, right, typesWithAncestors, exactMatch));
		}
		if (result == null || result.isEmpty())
		{
			return null;
		}

		result = ContentAssistFilters.filterStaticEntries(result);

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
			result.addAll(getEntryTypes(entry));
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
			HashSet<Object> result = new HashSet<Object>(1);
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
	 * @return set of entries or null.
	 */
	private static Set<IElementEntry> computeStaticDereferenceLeftEntries(IElementsIndex index, String left,
			int offset, IModule module)
	{
		if (left.startsWith("$")) //$NON-NLS-1$
		{
			return null;
		}

		if (SELF_ACTIVATION_SEQUENCE.equals(left))
		{
			IElementEntry currentClass = getCurrentClass(index, module, offset);
			if (currentClass == null)
			{
				return null;
			}
			LinkedHashSet<IElementEntry> result = new LinkedHashSet<IElementEntry>();
			result.add(currentClass);
			return result;
		}
		else
		{
			return getClassEntries(index, left, module);
		}
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
	 * @return variable entries.
	 */
	private static Set<IElementEntry> getClassEntries(IElementsIndex index, String clazz, IModule module)
	{
		List<IElementEntry> leftEntries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, clazz);
		if (leftEntries == null)
		{
			return null;
		}
		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>(leftEntries);
		for (IElementEntry entry : leftEntries)
		{
			if (module == null || module.equals(entry.getModule()))
			{
				result.add(entry);
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
	 * @return set of entries or null.
	 */
	private static Set<IElementEntry> computeDereferenceRightEntries(Set<IElementEntry> leftEntries,
			IElementsIndex index, String right, int offset, IModule module, boolean exactMatch,
			boolean applyAccessModifiers, boolean innerEntries)
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
				filter = new AccessModifierEntryFilter(customLeftTypes);
			}
			else
			{
				filter = new PublicsOnlyEntryFilter();
			}
		}

		Set<String> typesWithAncestors = TypeHierarchyUtils.addAllAncestors(customLeftTypes, index);

		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>();
		// now searching for the possible right parts
		if (right.length() == 0)
		{
			Set<IElementEntry> varResults = ContentAssistCollectors.collectVariableEntries(index, DOLLAR_SIGN,
					typesWithAncestors, exactMatch);
			if (varResults != null)
			{
				result.addAll(varResults);
			}
			Set<IElementEntry> funcResults = ContentAssistCollectors.collectFunctionEntries(index, right,
					typesWithAncestors, exactMatch);
			if (funcResults != null)
			{
				result.addAll(funcResults);
			}
		}
		else
		{
			result.addAll(ContentAssistCollectors.collectVariableEntries(index, DOLLAR_SIGN + right,
					typesWithAncestors, exactMatch));
			result.addAll(ContentAssistCollectors.collectFunctionEntries(index, right, typesWithAncestors, exactMatch));
		}
		if (result == null || result.isEmpty())
		{
			return null;
		}

		result = ContentAssistFilters.filterNonStaticVariables(result);

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
	 * @return set of entries or null.
	 */
	private static Set<IElementEntry> computeDereferenceLeftEntries(IElementsIndex index, String left, int offset,
			IModule module)
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
				return getVariableEntries(index, left);
			}
		}
		else
		{
			return getFunctionEntriesByCall(index, left);
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
	 * @return variable entries.
	 */
	private static Set<IElementEntry> getVariableEntries(IElementsIndex index, String var)
	{
		String varName = var.substring(1);
		List<IElementEntry> leftEntries = index.getEntries(IPHPIndexConstants.VAR_CATEGORY, varName);
		if (leftEntries == null)
		{
			return null;
		}
		Set<IElementEntry> result = new LinkedHashSet<IElementEntry>(leftEntries);
		result.addAll(leftEntries);
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
	 * @return completion proposals
	 */
	private ICompletionProposal[] simpleIdentifierCompletion(final int offset, String content, String identifier,
			boolean reportedStackIsGlobal, Set<String> globalImports, IModule module, boolean proposeBuiltins)
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

		ArrayList<Object> items = new ArrayList<Object>();

		IElementsIndex index;
		if (variableCompletion)
		{
			index = getIndex(content, offset);
		}
		else
		{
			index = getIndexOptimized(content, offset);
		}
		List<IElementEntry> entries = computeSimpleIdentifierEntries(reportedStackIsGlobal, globalImports, name,
				variableCompletion, index, false, module, true, currentContext, namespace, aliases);

		items.addAll(entries);

		if (proposeBuiltins)
		{
			String completionStart = name;
			if (completionStart.startsWith("\\")) { //$NON-NLS-1$
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
							if (pn.getNodeName().startsWith(DOLLAR_SIGN))
							{
								{
									items.add(pn);
								}
							}
						}
					}
					else
					{
						if (!(modelItem instanceof PHPVariableParseNode))
						{
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
	private static List<IElementEntry> computeSimpleIdentifierEntries(boolean reportedStackIsGlobal,
			Set<String> globalImports, String name, boolean variableCompletion, IElementsIndex index,
			boolean exactMatch, IModule module, boolean filter, ProposalContext proposalContext, String namespace,
			Map<String, String> aliases)
	{
		ArrayList<IElementEntry> namespaceEntries = new ArrayList<IElementEntry>();
		for (String s : aliases.keySet())
		{
			if (s.toLowerCase().startsWith(name))
			{
				namespaceEntries.add(new UnpackedEntry(-1, s, new NamespacePHPEntryValue(0, s), module));
			}
		}
		if (!name.startsWith("\\")) { //$NON-NLS-1$
			if (namespace != null && namespace.length() > 0)
			{
				name = namespace + '\\' + name;
			}
			for (String s : aliases.keySet())
			{
				if (name.startsWith(s.toLowerCase()))
				{
					name = aliases.get(s) + name.substring(s.length());
					break;
				}

			}
			List<IElementEntry> entriesStartingWith = index.getEntriesStartingWith(
					IPHPIndexConstants.NAMESPACE_CATEGORY, EMPTY_STRING);
			for (IElementEntry e : entriesStartingWith)
			{
				if (e.getEntryPath().startsWith(name))
				{
					namespaceEntries.add(e);
				}
			}
		}
		else
		{
			if (!variableCompletion)
			{
				name = name.substring(1);
				List<IElementEntry> entriesStartingWith = index.getEntriesStartingWith(
						IPHPIndexConstants.NAMESPACE_CATEGORY, EMPTY_STRING);
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
			}

			List<IElementEntry> defineEntries = null;
			// searching for defines
			if (exactMatch)
			{
				if (proposalContext == null || proposalContext.acceptModelElementType(IPHPIndexConstants.VAR_CATEGORY))
				{
					defineEntries = index.getEntries(IPHPIndexConstants.VAR_CATEGORY, name);
				}
			}
			else
			{
				if (proposalContext == null || proposalContext.acceptModelElementType(IPHPIndexConstants.VAR_CATEGORY))
				{
					defineEntries = index.getEntriesStartingWith(IPHPIndexConstants.VAR_CATEGORY, name);
				}
			}
			if (defineEntries != null)
			{
				List<IElementEntry> staticVariableEntries = new ArrayList<IElementEntry>();
				for (IElementEntry entry : defineEntries)
				{
					if (entry.getValue() instanceof AbstractPHPEntryValue)
					{
						if (PHPFlags.isStatic(((AbstractPHPEntryValue) entry.getValue()).getModifiers()))
						{
							staticVariableEntries.add(entry);
						}
					}
				}

				if (entries != null)
				{
					entries.addAll(staticVariableEntries);
				}
				else
				{
					entries = staticVariableEntries;
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
		ArrayList<IElementEntry> result = new ArrayList<IElementEntry>();
		result.addAll(filterResult);
		return result;
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
	 * Test if the given lexer is in a given state.
	 * 
	 * @param lexer
	 * @param lexerState
	 * @return True, if the lexer is in the same state as given; False, if not or we got an error in the check.
	 */
	private boolean isInState(AbstractPhpLexer lexer, String lexerState)
	{
		int state = lexer.yystate();
		try
		{
			// we use reflection here since we don't know the constant value of
			// of this specific PHP lexer
			int lexerStateValue = lexer.getClass().getField(lexerState).getInt(lexer);
			return lexerStateValue == state;
		}
		catch (Exception e)
		{
			PHPEditorPlugin.logError(e);
		}
		return false;
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

		reportedScopeUnderClassOrFunction = indexer.isReportedScopeUnderClassOrFunction();
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
	private List<ICompletionProposal> createProposals(final int offset, String name, ArrayList<Object> items,
			IModule module, boolean applyDollarSymbol, IElementsIndex index, boolean newInstanceCompletion)
	{
		List<Object> sortedItems = sortItems(items, module);
		String origName = name;
		int lastIndexOf = name.lastIndexOf('\\');
		if (lastIndexOf != -1)
		{
			name = name.substring(lastIndexOf + 1);
		}
		boolean notEmptyCompletion = name.length() > 0;
		boolean completionSetted = false;
		String lowerCase = name.toLowerCase();

		List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();

		Set<String> usedNames = new LinkedHashSet<String>();

		for (int i = 0; i < sortedItems.size(); i++)
		{
			Object item = sortedItems.get(i);

			PHPCompletionProposal proposal = null;

			if (item instanceof IPHPParseNode)
			{
				IPHPParseNode node = (IPHPParseNode) item;
				String firstName = node.getNodeName().toLowerCase();
				if (firstName.startsWith(lowerCase)
						|| (applyDollarSymbol && firstName.substring(1).startsWith(lowerCase)))
				{
					if (!usedNames.contains(firstName))
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
							usedNames.add(firstName);
						}
					}
				}
			}
			else if (item instanceof IElementEntry)
			{
				IElementEntry entry = (IElementEntry) item;
				String firstName = ElementsIndexingUtils.getLastNameInPath(entry.getEntryPath());
				if (entry.getValue() instanceof NamespacePHPEntryValue)
				{
					name = origName;
					lowerCase = origName;

					if (lowerCase.startsWith("\\")) { //$NON-NLS-1$
						firstName = "\\" + firstName; //$NON-NLS-1$
					}
					else if (!usedNames.contains(firstName))
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
								if (!usedNames.contains(firstName))
								{
									proposal = createProposal(entry, offset, name, firstName, module,
											applyDollarSymbol, index, newInstanceCompletion);
									if (proposal != null)
									{
										usedNames.add(firstName);
										result.add(proposal);
									}

								}
							}
							continue;
						}
						proposal = createProposal(entry, offset, name, k, module, applyDollarSymbol, index,
								newInstanceCompletion);
						if (proposal != null)
						{
							usedNames.add(lowerCase);
						}
					}
				}
				if (firstName != null && firstName.toLowerCase().startsWith(lowerCase))
				{
					if (!usedNames.contains(firstName))
					{
						proposal = createProposal(entry, offset, name, firstName, module, applyDollarSymbol, index,
								newInstanceCompletion);
						if (proposal != null)
						{
							usedNames.add(firstName);
						}
					}
				}
			}

			if (proposal != null)
			{
				result.add(proposal);

				if (!completionSetted && notEmptyCompletion)
				{
					proposal.setIsDefaultSelection(true);
					proposal.setIsSuggestedSelection(true);
					completionSetted = true;
				}
			}
		}
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
	 *            - index to use.
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
		IDocumentationResolver resolver = new EntryDocumentationResolver(proposalContent, index, val, module, entry);
		if (entry.getValue() instanceof NamespacePHPEntryValue)
		{
			image = labelProvider.getImage(val);
		}
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
		String fileOloc = EMPTY_STRING;
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
		String dispString = node.getNodeName();
		IContextInformation contextInformation = null;
		IDocumentationResolver resolver = new IDocumentationResolver()
		{

			public String resolveDocumentation()
			{
				String additionalInfo = ContentAssistUtils.getDocumentation(node, name2);
				return additionalInfo;
			}

		};
		int objType = 0;
		String fileOloc = EMPTY_STRING;
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
						if (types.iterator().next().toString().startsWith(IPHPIndexConstants.LAMBDA_TYPE))
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
				if (nodeEntry.getType() == IPHPParseNode.KEYWORD_NODE && ("extends".equals(nodeEntry.getNodeName()) || //$NON-NLS-1$
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
			builder.append("("); //$NON-NLS-1$

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
					builder.append(")"); //$NON-NLS-1$
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

			builder.append(")"); //$NON-NLS-1$
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
						if (!insertOptionals && !EMPTY_STRING.equals(parameters[i].getDefaultValue()))
						{
							break; // once we have a default value we can stop
							// (since the optional params are always
							// right to the mandatory ones)
						}
						String parameterName = parameters[i].getVariableName();

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

					int start = builder.length() + result.replaceString.length();
					// make sure that the last param fits the 'optional
					// insertion' policy
					if (insertOptionals
							|| (!insertOptionals && EMPTY_STRING.equals(parameters[parameters.length - 1]
									.getDefaultValue())))
					{
						builder.append(parameters[parameters.length - 1].getVariableName());
						int length = builder.length() + result.replaceString.length() - start;

						if (result.positions != null)
						{
							result.positions.add(new Position(start, length));
						}
						commaAppended = false;
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
			return EMPTY_STRING;
		}
		else if (entryValue instanceof FunctionPHPEntryValue)
		{
			Set<Object> returnTypes = ((FunctionPHPEntryValue) entryValue).getReturnTypes();
			if (returnTypes == null || returnTypes.size() == 0)
			{
				return EMPTY_STRING;
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
				return EMPTY_STRING;
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
			return EMPTY_STRING;
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
			if (node.getType() == IPHPParseNode.KEYWORD_NODE && "new".equals(node.getNodeName())) { //$NON-NLS-1$
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
		if (entry.getCategory() != IPHPIndexConstants.FUNCTION_CATEGORY)
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
	 * Performs the items sorting.
	 * 
	 * @param items
	 *            - items to sort.
	 * @param localModule
	 *            - local module.
	 * @return sorted items.
	 */
	private List<Object> sortItems(List<Object> items, IModule localModule)
	{
		// first dividing items for local and external ones
		List<Object> locals = new ArrayList<Object>();
		List<Object> externals = new ArrayList<Object>();

		for (Object item : items)
		{
			if (item instanceof IElementEntry)
			{
				if (localModule == null || localModule.equals(((IElementEntry) item).getModule()))
				{
					locals.add(item);
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

		List<Object> result = new ArrayList<Object>();
		// local items should have the original order and come first
		result.addAll(locals);
		// Sort the externals in a way that the classes will appear first, the
		// functions follow and the variables at the end.
		Collections.sort(externals, new Comparator<Object>()
		{

			public int compare(Object o1, Object o2)
			{
				int o1IsBuiltIn = 0;
				int o2IsBuiltIn = 0;

				String name1 = null;
				if (o1 instanceof IElementEntry)
				{
					name1 = ElementsIndexingUtils.getLastNameInPath(((IElementEntry) o1).getEntryPath());
				}
				else if (o1 instanceof IPHPParseNode)
				{
					name1 = ((IPHPParseNode) o1).getNodeName().toLowerCase();
					o1IsBuiltIn = 1;
				}
				else
				{
					return 0;
				}

				String name2 = null;
				if (o2 instanceof IElementEntry)
				{
					name2 = ElementsIndexingUtils.getLastNameInPath(((IElementEntry) o2).getEntryPath());
				}
				else if (o2 instanceof IPHPParseNode)
				{
					name2 = ((IPHPParseNode) o2).getNodeName().toLowerCase();
					o2IsBuiltIn = 1;
				}
				else
				{
					return 0;
				}
				int builtInCompare = o2IsBuiltIn - o1IsBuiltIn;
				if (builtInCompare == 0)
				{
					return name1.compareTo(name2);
				}
				return builtInCompare;
			}

		});
		result.addAll(externals);

		return result;
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
