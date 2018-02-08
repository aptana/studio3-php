/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.contentAssist.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.ui.IEditorInput;
import org2.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.contentassist.ILexemeProvider;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IIndexReporter;
import com.aptana.editor.php.indexer.IReportable;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.contentAssist.ContentAssistFilters;
import com.aptana.editor.php.internal.contentAssist.PHPContentAssistProcessor;
import com.aptana.editor.php.internal.contentAssist.PHPContextCalculator;
import com.aptana.editor.php.internal.contentAssist.PHPTokenType;
import com.aptana.editor.php.internal.contentAssist.ParsingUtils;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.indexer.AbstractPHPEntryValue;
import com.aptana.editor.php.internal.indexer.ModuleSubstitutionIndex;
import com.aptana.editor.php.internal.indexer.PDTPHPModuleIndexer;
import com.aptana.editor.php.internal.indexer.UnpackedElementIndex;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;
import com.aptana.ide.ui.io.internal.UniformFileStoreEditorInput;
import com.aptana.parsing.lexer.Lexeme;

/**
 * PHPOffsetMapper
 * 
 * @author Denis Denisenko, Shalom Gibly
 */
public class PHPOffsetMapper
{
	private static final String NEW = "new"; //$NON-NLS-1$

	/**
	 * Whether reported stack is global.
	 */
	private boolean reportedStackIsGlobal;
	private PHPSourceEditor phpSourceEditor;
	private String namespace;
	private Map<String, String> aliases;

	/**
	 * Constructs a new PHP offset mapper with a given PHP editor.
	 * 
	 * @param phpSourceEditor
	 */
	public PHPOffsetMapper(PHPSourceEditor phpSourceEditor)
	{
		this.phpSourceEditor = phpSourceEditor;
	}

	/**
	 * Global imports.
	 */
	private Set<String> globalImports;

	/**
	 * Returns the first matching {@link IElementEntry} origin for the given lexeme.
	 * 
	 * @param lexeme
	 * @param lexemeProvider
	 * @return An {@link IElementEntry} matching the lexeme origin, or null if none was found.
	 */
	public IElementEntry findEntry(Lexeme<PHPTokenType> lexeme, ILexemeProvider<PHPTokenType> lexemeProvider)
	{
		IDocument document = phpSourceEditor.getDocumentProvider().getDocument(phpSourceEditor.getEditorInput());
		String source = document.get();
		Set<IElementEntry> entries = collectEntries(source, lexeme);
		if (entries.isEmpty())
		{
			return null;
		}
		List<IElementEntry> sortedEntries = sortByModule(entries);
		return sortedEntries.get(0);
	}

	/**
	 * Find the ICodeLocation for the given lexeme.
	 * 
	 * @param lexeme
	 *            The current lexeme
	 * @param lexemeProvider
	 *            The lexeme provider, for cases that require lexeme inspection
	 */
	public ICodeLocation[] findTargets(Lexeme<PHPTokenType> lexeme, ILexemeProvider<PHPTokenType> lexemeProvider)
	{
		String source = null;
		IEditorInput editorInput = phpSourceEditor.getEditorInput();
		try
		{
			// Check if we are in an 'include' or 'require'
			IDocument document = phpSourceEditor.getDocumentProvider().getDocument(phpSourceEditor.getEditorInput());
			source = document.get();
			ITypedRegion partition = document.getPartition(lexeme.getStartingOffset());
			int previousPartitionEnd = (partition != null) ? partition.getOffset() - 1 : -1;
			if (previousPartitionEnd > 0
					&& (IPHPConstants.PHP_STRING_SINGLE.equals(partition.getType()) || IPHPConstants.PHP_STRING_DOUBLE
							.equals(partition.getType())))
			{
				// Because we have a different partition type for strings, we have to check the previous partition for
				// any include or require lexemes.
				// We also have to create a new lexeme provider just for this region check
				ILexemeProvider<PHPTokenType> newLexemeProvider = ParsingUtils.createLexemeProvider(document,
						previousPartitionEnd);
				int lexemePosition = newLexemeProvider.getLexemeFloorIndex(previousPartitionEnd - 1);
				Lexeme<PHPTokenType> importLexeme = PHPContextCalculator.findLexemeBackward(newLexemeProvider,
						lexemePosition, new String[] { PHPRegionTypes.PHP_INCLUDE, PHPRegionTypes.PHP_INCLUDE_ONCE,
								PHPRegionTypes.PHP_REQUIRE, PHPRegionTypes.PHP_REQUIRE_ONCE }, new String[] {
								PHPRegionTypes.WHITESPACE, PHPRegionTypes.PHP_TOKEN });
				if (importLexeme != null)
				{
					return getIncludeLocation(lexeme, source);
				}
			}
		}
		catch (Exception e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error locating code-location target", e); //$NON-NLS-1$
		}

		String fullPath = null;
		IFileStore remoteFileStore = null;
		List<IElementEntry> sortedEntries = sortByModule(collectEntries(source, lexeme));
		List<CodeLocation> locations = new ArrayList<CodeLocation>(5);
		for (IElementEntry entry : sortedEntries)
		{
			Object value = entry.getValue();
			if (value instanceof AbstractPHPEntryValue)
			{
				if (entry.getModule() != null)
				{
					fullPath = entry.getModule().getFullPath();
					if (editorInput instanceof UniformFileStoreEditorInput)
					{
						UniformFileStoreEditorInput uniformInput = (UniformFileStoreEditorInput) editorInput;
						if (uniformInput.isRemote())
						{
							if (uniformInput.getLocalFileStore().toString().equals(fullPath))
							{
								remoteFileStore = uniformInput.getFileStore();
							}
							fullPath = null;
						}
					}
					if (fullPath != null || remoteFileStore != null)
					{
						AbstractPHPEntryValue phpEntryValue = (AbstractPHPEntryValue) value;
						int startOffset = phpEntryValue.getStartOffset();
						Lexeme<PHPTokenType> startLexeme = new Lexeme<PHPTokenType>(new PHPTokenType(
								PHPRegionTypes.UNKNOWN_TOKEN), startOffset, startOffset, StringUtil.EMPTY);
						if (remoteFileStore != null)
						{
							locations.add(new CodeLocation(remoteFileStore, startLexeme));
						}
						locations.add(new CodeLocation(fullPath, startLexeme));
					}
				}
			}
		}
		return locations.toArray(new CodeLocation[locations.size()]);
	}

	/**
	 * Collect a set of {@link IElementEntry}s.
	 * 
	 * @param offset
	 * @param source
	 * @param lexeme
	 * @return A collection of IElementEntries
	 */
	@SuppressWarnings("unchecked")
	private Set<IElementEntry> collectEntries(String source, Lexeme<PHPTokenType> lexeme)
	{
		boolean isFunctionCall = isFunctionCall(lexeme, source);
		boolean isConstructor = isConstructorCall(lexeme, source);

		int offset = lexeme.getEndingOffset();
		IModule module = phpSourceEditor.getModule();
		if (module == null)
		{
			return Collections.EMPTY_SET;
		}

		Set<IElementEntry> entries = null;

		IElementsIndex index = getIndex(source, offset);

		// trying to get dereference entries
		IDocument document = phpSourceEditor.getDocumentProvider().getDocument(phpSourceEditor.getEditorInput());
		List<String> callPath = ParsingUtils.parseCallPath(null, source, offset, PHPContentAssistProcessor.OPS, false,
				document);
		if (callPath == null || callPath.isEmpty())
		{
			return Collections.EMPTY_SET;
		}

		if (callPath.size() > 1)
		{
			if (PHPContentAssistProcessor.DEREFERENCE_OP.equals(callPath.get(1)))
			{
				entries = PHPContentAssistProcessor.computeDereferenceEntries(index, callPath, offset, module, true,
						aliases, namespace);
			}
			else
			{
				entries = PHPContentAssistProcessor.computeStaticDereferenceEntries(index, callPath, offset, module,
						true, aliases, namespace);
			}
		}
		else
		{
			String toFind = callPath.get(callPath.size() - 1);
			boolean variableCompletion = false;
			List<IElementEntry> res = new ArrayList<IElementEntry>();
			if (toFind.length() > 0 && toFind.charAt(0) == '$')
			{
				variableCompletion = true;
				toFind = toFind.substring(1);
			}
			else
			{
				// Constants first
				res.addAll(PHPContentAssistProcessor.computeDefines(toFind, index, null, true));

			}
			toFind = toFind.toLowerCase();
			res.addAll(PHPContentAssistProcessor.computeSimpleIdentifierEntries(reportedStackIsGlobal, globalImports,
					toFind, variableCompletion, index, true, module, false, namespace, aliases));

			if (res != null)
			{
				entries = new LinkedHashSet<IElementEntry>();
				entries.addAll(res);
			}
		}

		if (entries == null)
		{
			return Collections.EMPTY_SET;
		}

		if (isFunctionCall && !isConstructor)
		{
			entries = ContentAssistFilters.filterAllButFunctions(entries, index);
		}
		else if (isConstructor)
		{
			entries = ContentAssistFilters.filterAllButClasses(entries, index);
		}
		else
		{
			entries = ContentAssistFilters.filterAllButVariablesAndClasses(entries, index);
		}

		if (entries == null || entries.size() == 0)
		{
			return Collections.EMPTY_SET;
		}
		return entries;
	}

	/**
	 * Gets include location.
	 * 
	 * @param lexeme
	 *            - include lexeme.
	 * @param source
	 *            - source.
	 * @return location or null.
	 */
	private ICodeLocation[] getIncludeLocation(Lexeme<PHPTokenType> lexeme, String source)
	{
		String moduleName = getIncludeModuleName(lexeme, source);
		if (moduleName == null)
		{
			return null;
		}

		IModule module = phpSourceEditor.getModule();
		if (module == null)
		{
			return null;
		}

		IBuildPath buildPath = module.getBuildPath();

		if (buildPath == null)
		{
			return null;
		}

		try
		{
			Path path = new Path(moduleName);
			if (path.isAbsolute())
			{
				return null;
			}

			IModule includedModule = buildPath.resolveRelativePath(module, path);
			if (includedModule == null)
			{
				return null;
			}

			Lexeme<PHPTokenType> startLexeme = new Lexeme<PHPTokenType>(new PHPTokenType(PHPRegionTypes.UNKNOWN_TOKEN),
					0, 0, StringUtil.EMPTY);
			return new CodeLocation[] { new CodeLocation(includedModule.getFullPath(), startLexeme) };
		}
		catch (Throwable th) // $codepro.audit.disable emptyCatchClause
		{
			// skip
		}

		return null;
	}

	/**
	 * Gets include module name.
	 * 
	 * @param lexeme
	 *            - include lexeme.
	 * @param source
	 *            - source.
	 * @return module name or null
	 */
	private String getIncludeModuleName(Lexeme<PHPTokenType> lexeme, String source)
	{
		if (lexeme != null)
		{
			String includeString = lexeme.getText();
			if (includeString != null && includeString.length() > 2)
			{
				return includeString.substring(1, includeString.length() - 1);
			}
		}
		return null;
	}

	/**
	 * Checks whether lexeme has "new" in the left.
	 * 
	 * @param lexeme
	 *            - lexeme.
	 * @param source
	 *            - source.
	 * @return true if constructor, false otherwise.
	 */
	private boolean isConstructorCall(Lexeme<PHPTokenType> lexeme, String source)
	{
		int searchStringPos = NEW.length() - 1;

		// going left searching for the "new" sequence
		for (int i = lexeme.getStartingOffset() - 1; i >= 0; i--)
		{
			if (searchStringPos == -1)
			{
				return true;
			}

			if (i > source.length() - 1)
			{
				return false;
			}
			char ch = source.charAt(i);
			if (ch == NEW.charAt(searchStringPos))
			{
				searchStringPos--;
			}
			else if (!Character.isWhitespace(ch))
			{
				return false;
			}
		}

		return searchStringPos == -1;
	}

	/**
	 * Checks whether lexeme is function call.
	 * 
	 * @param lexeme
	 *            - lexeme.
	 * @param source
	 *            - source.
	 * @return true if function call, false otherwise
	 */
	private boolean isFunctionCall(Lexeme<PHPTokenType> lexeme, String source)
	{
		// going right searching for "(" character
		for (int i = lexeme.getEndingOffset() + 1; i < source.length(); i++)
		{
			char ch = source.charAt(i);
			if (ch == '(')
			{
				return true;
			}
			else if (!Character.isWhitespace(ch))
			{
				return false;
			}
		}

		return false;
	}

	/**
	 * Gets elements index for a module.
	 * 
	 * @param content
	 *            - module content.
	 * @param offset
	 * @return elements index
	 */
	public IElementsIndex getIndex(String content, int offset)
	{
		IModule currentModule = phpSourceEditor.getModule();
		if (currentModule == null)
		{
			return PHPGlobalIndexer.getInstance().getIndex();
		}

		final UnpackedElementIndex index = new UnpackedElementIndex();
		PDTPHPModuleIndexer indexer = new PDTPHPModuleIndexer(false, offset);

		indexer.setUpdateTaskTags(false);
		indexer.indexModule(content, currentModule, new IIndexReporter()
		{

			public IElementEntry reportEntry(int category, String entryPath, IReportable value, IModule module)
			{
				return index.addEntry(category, entryPath, value, module);
			}

		});

		reportedStackIsGlobal = indexer.isReportedScopeGlobal();
		globalImports = indexer.getGlobalImports();
		namespace = indexer.getNamespace();
		aliases = indexer.getAliases();
		ModuleSubstitutionIndex result = new ModuleSubstitutionIndex(currentModule, index, PHPGlobalIndexer
				.getInstance().getIndex());
		return result;
	}

	/**
	 * Sorts entries by module.
	 * 
	 * @param entries
	 *            - entries.
	 * @return sorted entries.
	 */
	private List<IElementEntry> sortByModule(Set<IElementEntry> entries)
	{

		if (entries == null)
		{
			return null;
		}

		// current implementation just puts entries from the current module first, other entries last.
		List<IElementEntry> currentModuleEntries = new ArrayList<IElementEntry>();
		List<IElementEntry> otherEntries = new ArrayList<IElementEntry>();

		IModule currentModule = phpSourceEditor.getModule();

		for (IElementEntry entry : entries)
		{
			if (currentModule != null && entry.getModule() != null && currentModule.equals(entry.getModule()))
			{
				currentModuleEntries.add(entry);
			}
			else
			{
				otherEntries.add(entry);
			}
		}

		List<IElementEntry> toReturn = new ArrayList<IElementEntry>();
		toReturn.addAll(currentModuleEntries);
		toReturn.addAll(otherEntries);

		return toReturn;
	}
}