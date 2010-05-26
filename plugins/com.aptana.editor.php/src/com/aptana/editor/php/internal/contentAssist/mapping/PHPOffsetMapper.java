/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.contentAssist.mapping;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.rules.IToken;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IIndexReporter;
import com.aptana.editor.php.indexer.IReportable;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.FileSystemBuildPath;
import com.aptana.editor.php.internal.builder.FileSystemModule;
import com.aptana.editor.php.internal.builder.IBuildPath;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.indexer.AbstractPHPEntryValue;
import com.aptana.editor.php.internal.indexer.ModuleSubstitutionIndex;
import com.aptana.editor.php.internal.indexer.PDTPHPModuleIndexer;
import com.aptana.editor.php.internal.indexer.UnpackedElementIndex;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;
import com.aptana.parsing.lexer.Lexeme;

/**
 * PHPOffsetMapper
 * 
 * @author Denis Denisenko
 */
@SuppressWarnings( { "unused", "unchecked" })
public class PHPOffsetMapper
{
	/**
	 * Whether reported stack is global.
	 */
	private boolean reportedStackIsGlobal;
	private PHPSourceEditor phpSourceEditor;

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

	// TODO - Shalom Implement
	// /**
	// * {@inheritDoc}
	// */
	// public ICodeLocation findTarget(Lexeme lexeme)
	// {
	// int offset = lexeme.getEndingOffset() - 1;
	// String source = getFileService().getSource();
	//
	// if (PHPTokenTypes.getIntValue(lexeme.getType()) == PHPTokenTypes.REQUIRE
	// || PHPTokenTypes.getIntValue(lexeme.getType()) == PHPTokenTypes.REQUIRE_ONCE
	// || PHPTokenTypes.getIntValue(lexeme.getType()) == PHPTokenTypes.INCLUDE
	// || PHPTokenTypes.getIntValue(lexeme.getType()) == PHPTokenTypes.INCLUDE_ONCE)
	// {
	// return getIncludeLocation(lexeme, source);
	// }
	//
	// boolean isFunctionCall = isFunctionCall(lexeme, source);
	// boolean isConstructor = isConstructorCall(lexeme, source);
	//
	// IModule module = getModule();
	// if (module == null)
	// {
	// return null;
	// }
	//
	// Set<IElementEntry> entries = null;
	//
	// IElementsIndex index = getIndex(source, offset);
	//
	// // trying to get dereference entries
	// List<String> callPath = ParsingUtils.parseCallPath(source, offset, PHPContentAssistProcessor.OPS, false);
	// if (callPath == null || callPath.isEmpty())
	// {
	// return null;
	// }
	//
	// if (callPath.size() > 1)
	// {
	// if (PHPContentAssistProcessor.DEREFERENCE_OP.equals(callPath.get(1)))
	// {
	// entries = PHPContentAssistProcessor.computeDereferenceEntries(index, callPath, offset, module, true);
	// }
	// else
	// {
	// entries = PHPContentAssistProcessor.computeStaticDereferenceEntries(index, callPath, offset, module,
	// true);
	// }
	// }
	// else
	// {
	// String toFind = callPath.get(callPath.size() - 1);
	// boolean variableCompletion = false;
	//			if (toFind.startsWith("$")) //$NON-NLS-1$
	// {
	// variableCompletion = true;
	// toFind = toFind.substring(1);
	// }
	//
	// List<IElementEntry> res = PHPContentAssistProcessor.computeSimpleIdentifierEntries(reportedStackIsGlobal,
	// globalImports, toFind, variableCompletion, index, true, module, false, namespace, aliases);
	// if (res != null)
	// {
	// entries = new LinkedHashSet<IElementEntry>();
	// entries.addAll(res);
	// }
	// }
	//
	// if (entries == null)
	// {
	// return null;
	// }
	//
	// if (isFunctionCall && !isConstructor)
	// {
	// entries = PHPContentAssistProcessor.filterAllButFunctions(entries, index);
	// }
	// else if (isConstructor)
	// {
	// entries = PHPContentAssistProcessor.filterAllButClasses(entries, index);
	// }
	// else
	// {
	// entries = PHPContentAssistProcessor.filterAllButVariablesAndClasses(entries, index);
	// }
	//
	// if (entries == null || entries.size() == 0)
	// {
	// return null;
	// }
	//
	// String fullPath = null;
	// // String resultText = null;
	// int resultOffset = -1;
	//
	// List<IElementEntry> sortedEntries = sortByModule(entries);
	//
	// for (IElementEntry entry : sortedEntries)
	// {
	// if (entry.getValue() instanceof AbstractPHPEntryValue)
	// {
	// if (entry.getModule() != null)
	// {
	// fullPath = entry.getModule().getFullPath();
	// }
	// resultOffset = ((AbstractPHPEntryValue) entry.getValue()).getStartOffset();
	// // resultText = ElementsIndexingUtils.getLastNameInPath(entry.getEntryPath());
	// break;
	// }
	// }
	// if (fullPath == null)
	// {
	// return null;
	// }
	//
	// Lexeme startLexeme = new FakeLexeme("", resultOffset);
	// return new CodeLocation(fullPath, startLexeme);
	// }
	//
	// /**
	// * Gets include location.
	// *
	// * @param lexeme
	// * - include lexeme.
	// * @param source
	// * - source.
	// * @return location or null.
	// */
	// private ICodeLocation getIncludeLocation(Lexeme lexeme, String source)
	// {
	// String moduleName = getIncludeModuleName(lexeme, source);
	// if (moduleName == null)
	// {
	// return null;
	// }
	//
	// if (getModule() == null)
	// {
	// return null;
	// }
	//
	// IBuildPath buildPath = getModule().getBuildPath();
	//
	// if (buildPath == null)
	// {
	// return null;
	// }
	//
	// try
	// {
	// Path path = new Path(moduleName);
	// if (path.isAbsolute())
	// {
	// return null;
	// }
	//
	// IModule includedModule = buildPath.resolveRelativePath(getModule(), path);
	// if (includedModule == null)
	// {
	// return null;
	// }
	//
	// Lexeme startLexeme = new FakeLexeme("", 0);
	// return new CodeLocation(includedModule.getFullPath(), startLexeme);
	// }
	// catch (Throwable th)
	// {
	// // skip
	// }
	//
	// return null;
	// }

	/**
	 * Gets include module name.
	 * 
	 * @param lexeme
	 *            - include lexeme.
	 * @param source
	 *            - source.
	 * @return module name or null
	 */
	private String getIncludeModuleName(Lexeme lexeme, String source)
	{
		int offset = lexeme.getEndingOffset();

		// // searching for the first round bracket
		// int firstBaracketPos = -1;
		// for (; offset < source.length(); offset++)
		// {
		// char ch = source.charAt(offset);
		// if (ch == '(')
		// {
		// firstBaracketPos = offset;
		// break;
		// }
		// }
		//
		// if (firstBaracketPos == -1)
		// {
		// return null;
		// }
		//
		// // searching for the second round bracket
		// int secondBaracketPos = -1;
		// for (; offset < source.length(); offset++)
		// {
		// char ch = source.charAt(offset);
		// if (ch == ')')
		// {
		// secondBaracketPos = offset;
		// break;
		// }
		// }
		//
		// if (secondBaracketPos == -1)
		// {
		// return null;
		// }
		//
		// String includeArgumentsString = source.substring(firstBaracketPos + 1, secondBaracketPos);

		int lineEndPos = offset;
		for (int i = lineEndPos; i < source.length(); i++)
		{
			char ch = source.charAt(i);
			if (ch == '\r' || ch == '\n' || ch == ';' || i == source.length() - 1)
			{
				lineEndPos = i;
				break;
			}
		}

		String includeArgumentsString = source.substring(offset, lineEndPos);

		char toSearch;
		if (includeArgumentsString.contains("\"")) //$NON-NLS-1$
		{
			toSearch = '"';
		}
		else
		{
			toSearch = '\'';
		}

		int firstQuotePos = -1;
		int secondQuotePos = -1;

		// searching for double quotes
		firstQuotePos = includeArgumentsString.indexOf(toSearch);
		if (firstQuotePos == -1)
		{
			return null;
		}

		secondQuotePos = includeArgumentsString.lastIndexOf(toSearch);
		if (secondQuotePos == -1)
		{
			return null;
		}

		if (firstQuotePos == secondQuotePos)
		{
			return null;
		}

		return includeArgumentsString.substring(firstQuotePos + 1, secondQuotePos);
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
	private boolean isConstructorCall(Lexeme lexeme, String source)
	{
		final String NEW = "new"; //$NON-NLS-1$

		int searchStringPos = NEW.length() - 1;

		// going left searching for the "new" sequence
		for (int i = lexeme.getStartingOffset() - 1; i >= 0; i--)
		{
			if (searchStringPos == -1)
			{
				return true;
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
	private boolean isFunctionCall(Lexeme lexeme, String source)
	{
		// going right searching for "(" character
		for (int i = lexeme.getEndingOffset(); i < source.length(); i++)
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
	 * Gets current module.
	 * 
	 * @param resource
	 * @return current module.
	 */
	// private IModule getModule()
	// {
	// String struri = getSourceProvider().getSourceURI();
	// URI uri;
	// try
	// {
	// uri = new URI(struri);
	// }
	// catch (URISyntaxException e)
	// {
	// PHPEditorPlugin.logError(e);
	// return null;
	// }
	// if (uri.isAbsolute())
	// {
	// IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
	// if (files != null && files.length != 0)
	// {
	// return BuildPathManager.getInstance().getModuleByResource(files[0]);
	// }
	// }
	//
	// String filePath = struri;
	//		if (filePath.startsWith("file://")) //$NON-NLS-1$
	// {
	// filePath = filePath.substring(7);
	// }
	// File file = new File(filePath);
	// return new FileSystemModule(new File(file.getName()), new FileSystemBuildPath(file.getParentFile()));
	// }

	String namespace;
	Map<String, String> aliases;

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