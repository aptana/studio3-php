package com.aptana.editor.php.internal.contentAssist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.indexer.ClassPHPEntryValue;
import com.aptana.editor.php.internal.indexer.FunctionPHPEntryValue;
import com.aptana.editor.php.internal.indexer.PHPDocUtils;
import com.aptana.editor.php.internal.indexer.PHPTypeProcessor;
import com.aptana.editor.php.internal.indexer.VariablePHPEntryValue;
import com.aptana.editor.php.internal.parser.phpdoc.FunctionDocumentation;

class EntryDocumentationResolver implements IDocumentationResolver
{
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private final String proposalContent;
	private final IElementsIndex index;
	private final Object val;
	private final IModule module;
	private final IElementEntry entry;

	public EntryDocumentationResolver(String proposalContent, IElementsIndex index, Object val, IModule module,
			IElementEntry entry)
	{
		this.proposalContent = proposalContent;
		this.index = index;
		this.val = val;
		this.module = module;
		this.entry = entry;
	}

	public String resolveDocumentation()
	{
		if (val instanceof FunctionPHPEntryValue)
		{
			FunctionPHPEntryValue pl = (FunctionPHPEntryValue) val;
			PHPDocBlock findFunctionPHPDocComment = PHPDocUtils.findFunctionPHPDocComment(entry, pl.getStartOffset());
			StringBuffer bf = new StringBuffer();
			bf.append(proposalContent);
			bf.append('(');
			if (!pl.getParameters().isEmpty())
			{
				for (String s : pl.getParameters().keySet())
				{
					bf.append('$');
					bf.append(s);
					bf.append(',');
				}
				bf.deleteCharAt(bf.length() - 1);
			}
			bf.append(')');
			String sig = bf.toString();
			FunctionDocumentation parseFunctionPHPDoc = findFunctionPHPDocComment != null ? PHPDocUtils
					.getFunctionDocumentation(findFunctionPHPDocComment) : null;
			String docString = PHPDocUtils.computeDocumentation(parseFunctionPHPDoc, sig);

			String typesString = computeEntryTypesDisplayString(entry, index);
			if (typesString != null && typesString.length() > 0)
			{
				String resolvedStr = Messages.EntryDocumentationResolver_resolvedReturnTypes;
				docString += "<br><b>" + resolvedStr + "</b> " + typesString; //$NON-NLS-1$ //$NON-NLS-2$
			}

			return docString;
		}
		else if (val instanceof ClassPHPEntryValue)
		{
			ClassPHPEntryValue pl = (ClassPHPEntryValue) val;
			PHPDocBlock findFunctionPHPDocComment = PHPDocUtils.findFunctionPHPDocComment(module, pl.getStartOffset());
			StringBuffer bf = new StringBuffer();
			bf.append(proposalContent);
			String sig = bf.toString();
			FunctionDocumentation parseFunctionPHPDoc = findFunctionPHPDocComment != null ? PHPDocUtils
					.getFunctionDocumentation(findFunctionPHPDocComment) : null;
			String docString = PHPDocUtils.computeDocumentation(parseFunctionPHPDoc, sig);

			/*
			 * String typesString = computeEntryTypesDisplayString(entry, index); if (typesString != null &&
			 * typesString.length() > 0) { docString += "<br>Types: " + typesString; }
			 */

			return docString;
		}
		else if (val instanceof VariablePHPEntryValue)
		{
			VariablePHPEntryValue pl = (VariablePHPEntryValue) val;
			int startOffset = pl.getStartOffset();
			PHPDocBlock findFunctionPHPDocComment = PHPDocUtils.findFunctionPHPDocComment(module, startOffset);
			StringBuffer bf = new StringBuffer();
			bf.append(proposalContent);
			String sig = bf.toString();
			FunctionDocumentation parseFunctionPHPDoc = findFunctionPHPDocComment != null ? PHPDocUtils
					.getFunctionDocumentation(findFunctionPHPDocComment) : null;
			if (pl.isParameter())
			{
				parseFunctionPHPDoc = null;
			}
			String docString = PHPDocUtils.computeDocumentation(parseFunctionPHPDoc, sig);

			String typesString = computeEntryTypesDisplayString(entry, index);
			if (typesString != null && typesString.length() > 0)
			{
				String resolvedStr = Messages.EntryDocumentationResolver_resolvedTypes;
				docString += "<br><b>" + resolvedStr + "</b> " + typesString; //$NON-NLS-1$ //$NON-NLS-2$
			}

			return docString;
		}
		return null;
	}

	/**
	 * Computes display string for the entry types (performs types eveluation if needed).
	 * 
	 * @param entry
	 *            - entry.
	 * @return display string
	 */
	private static String computeEntryTypesDisplayString(IElementEntry entry, IElementsIndex indexer)
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

			Set<String> resolvedTypes = PHPTypeProcessor.processTypes(returnTypes, indexer);

			if (resolvedTypes.size() == 0)
			{
				return EMPTY_STRING;
			}
			List<String> typesList = new ArrayList<String>(resolvedTypes.size());
			typesList.addAll(resolvedTypes);

			return getTypesDisplayString(typesList, entry);
		}
		else if (entryValue instanceof VariablePHPEntryValue)
		{
			Set<Object> variableTypes = ((VariablePHPEntryValue) entryValue).getTypes();
			if (variableTypes == null || variableTypes.size() == 0)
			{
				return EMPTY_STRING;
			}

			Set<String> resolvedTypes = PHPTypeProcessor.processTypes(variableTypes, indexer);

			if (resolvedTypes.size() == 0)
			{
				return EMPTY_STRING;
			}
			List<String> typesList = new ArrayList<String>(resolvedTypes.size());
			typesList.addAll(resolvedTypes);

			return getTypesDisplayString(typesList, entry);
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
}
