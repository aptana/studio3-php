package com.aptana.editor.php.internal.contentAssist;

import com.aptana.editor.php.indexer.IElementEntry;

/**
 * Abstract context filter.
 * 
 * @author Denis Denisenko
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
}
