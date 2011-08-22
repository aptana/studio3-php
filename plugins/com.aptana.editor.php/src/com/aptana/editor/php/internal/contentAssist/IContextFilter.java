package com.aptana.editor.php.internal.contentAssist;

import com.aptana.editor.php.indexer.IElementEntry;

/**
 * A context filter interface for filter implementations that can be queried when computing the content assist
 * proposals.
 * 
 * @author Denis Denisenko, Shalom Gibly
 */
interface IContextFilter
{
	/**
	 * Whether this filter accepts element entry.
	 * 
	 * @param element
	 *            - element to check.
	 * @return true if element is accepted, false otherwise.
	 */
	boolean acceptElementEntry(IElementEntry element);

	/**
	 * Whether this filter accepts built-in element.
	 * 
	 * @param builtinElement
	 *            - built-in element to check.
	 * @return true if element is accepted, false otherwise.
	 */
	boolean acceptBuiltin(Object builtinElement);

	/**
	 * Whether this context allows displaying external proposals from Rubles/Snippets.
	 * 
	 * @return True, if external proposals are allowed to be displayed; False, otherwise.
	 */
	boolean acceptExternalProposals();
}
