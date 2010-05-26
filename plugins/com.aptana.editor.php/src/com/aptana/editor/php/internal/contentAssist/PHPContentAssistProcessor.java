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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.indexer.ElementsIndexingUtils;
import com.aptana.editor.php.internal.indexer.IElementEntriesFilter;

/**
 * TODO
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPContentAssistProcessor implements IContentAssistProcessor
{

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters()
	{
		// TODO Auto-generated method stub
		return null;
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
