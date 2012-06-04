package com.aptana.editor.php.internal.contentAssist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;
import org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.internal.builder.LocalModule;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.indexer.ClassPHPEntryValue;
import com.aptana.editor.php.internal.indexer.FunctionPHPEntryValue;
import com.aptana.editor.php.internal.indexer.PHPDocUtils;
import com.aptana.editor.php.internal.indexer.PHPTypeProcessor;
import com.aptana.editor.php.internal.indexer.VariablePHPEntryValue;
import com.aptana.editor.php.internal.parser.phpdoc.FunctionDocumentation;
import com.aptana.ui.util.UIUtils;

class EntryDocumentationResolver implements IDocumentationResolver
{
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private final String proposalContent;
	private final IElementsIndex index;
	private final Object val;
	private final IElementEntry entry;

	protected EntryDocumentationResolver(String proposalContent, IElementsIndex index, Object val, IElementEntry entry)
	{
		this.proposalContent = proposalContent;
		this.index = index;
		this.val = val;
		this.entry = entry;
	}

	public String resolveDocumentation()
	{
		IDocument document = resolveDocument();
		if (val instanceof FunctionPHPEntryValue)
		{
			FunctionPHPEntryValue pl = (FunctionPHPEntryValue) val;
			PHPDocBlock findFunctionPHPDocComment = PHPDocUtils.findFunctionPHPDocComment(entry, document,
					pl.getStartOffset());
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
			FunctionDocumentation parseFunctionPHPDoc = (findFunctionPHPDocComment != null) ? PHPDocUtils
					.getFunctionDocumentation(findFunctionPHPDocComment) : null;
			String docString = PHPDocUtils.computeDocumentation(parseFunctionPHPDoc, document, sig);

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
			PHPDocBlock findFunctionPHPDocComment = PHPDocUtils.findFunctionPHPDocComment(entry, document,
					pl.getStartOffset());
			StringBuffer bf = new StringBuffer();
			bf.append(proposalContent);
			String sig = bf.toString();
			FunctionDocumentation parseFunctionPHPDoc = (findFunctionPHPDocComment != null) ? PHPDocUtils
					.getFunctionDocumentation(findFunctionPHPDocComment) : null;
			String docString = PHPDocUtils.computeDocumentation(parseFunctionPHPDoc, document, sig);

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
			PHPDocBlock findFunctionPHPDocComment = PHPDocUtils.findFunctionPHPDocComment(entry, document, startOffset);
			StringBuffer bf = new StringBuffer();
			bf.append(proposalContent);
			String sig = bf.toString();
			FunctionDocumentation parseFunctionPHPDoc = (findFunctionPHPDocComment != null) ? PHPDocUtils
					.getFunctionDocumentation(findFunctionPHPDocComment) : null;
			if (pl.isParameter())
			{
				parseFunctionPHPDoc = null;
			}
			String docString = PHPDocUtils.computeDocumentation(parseFunctionPHPDoc, document, sig);

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
	 * Resolves the right document to be used when computing the documentation out of an IDocument (happens when the
	 * docs are in an opened editor).<br>
	 * The method retrieves the entry's {@link IModule}, and in case it's a local module, tries to locate an open editor
	 * that match the input on that module.
	 * 
	 * @return An {@link IDocument} or <code>null</code>
	 */
	protected IDocument resolveDocument()
	{
		IDocument document = null;
		if (entry != null)
		{
			IModule module = entry.getModule();
			if (!(module instanceof LocalModule))
			{
				return null;
			}
			IWorkbenchPage activePage = UIUtils.getActivePage();
			if (activePage != null)
			{
				IFile moduleFile = ((LocalModule) module).getFile();
				// locate an open editor.
				IEditorPart editor = activePage.findEditor(new FileEditorInput(moduleFile));
				if (editor != null)
				{
					ISourceViewer sourceViewer = (ISourceViewer) editor.getAdapter(ISourceViewer.class);
					if (sourceViewer != null)
					{
						document = sourceViewer.getDocument();
					}
				}
			}
		}
		return document;
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

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.contentAssist.IDocumentationResolver#getProposalContent()
	 */
	public String getProposalContent()
	{
		return this.proposalContent;
	}
}
