package com.aptana.editor.php.internal.contentAssist;

import com.aptana.editor.php.indexer.IElementEntry;

/**
 * Implementation of context filter that accepts all entities.
 * 
 * @author Denis Denisenko
 */
class AcceptAllContextFilter implements IContextFilter
{
	/**
	 * {@inheritDoc}
	 */
	public boolean acceptBuiltin(Object builtinElement)
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean acceptElementEntry(IElementEntry element)
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.contentAssist.IContextFilter#acceptExternalProposals()
	 */
	public boolean acceptExternalProposals()
	{
		return true;
	}
}