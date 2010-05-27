package com.aptana.editor.php.internal.contentAssist;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.internal.builder.IModule;

@SuppressWarnings("unused")
class EntryDocumentationResolver implements IDocumentationResolver
{
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
		/* FIXME: Shalom - Implement me
		if (val instanceof FunctionPHPEntryValue)
		{
			FunctionPHPEntryValue pl = (FunctionPHPEntryValue) val;
			String findFunctionPHPDocComment = PHPDocUtils.findFunctionPHPDocComment(entry, pl.getStartOffset());
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
					.parseFunctionPHPDoc(findFunctionPHPDocComment) : null;
			String docString = PHPFileLanguageService.computeDocumentation(parseFunctionPHPDoc, sig);

			String typesString = computeEntryTypesDisplayString(entry, index);
			if (typesString != null && typesString.length() > 0)
			{
				docString += "<br><b>Resolved return types:</b> " + typesString;
			}

			return docString;
		}
		else if (val instanceof ClassPHPEntryValue)
		{
			ClassPHPEntryValue pl = (ClassPHPEntryValue) val;
			String findFunctionPHPDocComment = PHPDocUtils.findFunctionPHPDocComment(module, pl.getStartOffset());
			StringBuffer bf = new StringBuffer();
			bf.append(proposalContent);
			String sig = bf.toString();
			FunctionDocumentation parseFunctionPHPDoc = findFunctionPHPDocComment != null ? PHPDocUtils
					.parseFunctionPHPDoc(findFunctionPHPDocComment) : null;
			String docString = PHPFileLanguageService.computeDocumentation(parseFunctionPHPDoc, sig);

			/*
			 * String typesString = computeEntryTypesDisplayString(entry, index); if (typesString != null &&
			 * typesString.length() > 0) { docString += "<br>Types: " + typesString; }
			 *

			return docString;
		}
		else if (val instanceof VariablePHPEntryValue)
		{
			VariablePHPEntryValue pl = (VariablePHPEntryValue) val;
			int startOffset = pl.getStartOffset();
			String findFunctionPHPDocComment = PHPDocUtils.findFunctionPHPDocComment(module, startOffset);
			StringBuffer bf = new StringBuffer();
			bf.append(proposalContent);
			String sig = bf.toString();
			FunctionDocumentation parseFunctionPHPDoc = findFunctionPHPDocComment != null ? PHPDocUtils
					.parseFunctionPHPDoc(findFunctionPHPDocComment) : null;
			if (pl.isParameter())
			{
				parseFunctionPHPDoc = null;
			}
			String docString = PHPFileLanguageService.computeDocumentation(parseFunctionPHPDoc, sig);

			String typesString = computeEntryTypesDisplayString(entry, index);
			if (typesString != null && typesString.length() > 0)
			{
				docString += "<br><b>Resolved types:</b> " + typesString;
			}

			return docString;
		}
		*/
		return null;
	}
}
