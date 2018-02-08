package com.aptana.editor.php.internal.indexer;

import java.util.Map;
import java.util.Set;

/**
 * An interface for any PHPFunctionEntryValue type (a regular function or a lambda function)
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 * @since Aptana PHP 3.0
 */
public interface IPHPFunctionEntryValue
{
	/**
	 * Returns a parameters Set.
	 * 
	 * @return Function parameters set
	 */
	public Map<String, Set<Object>> getParameters();

	/**
	 * Returns the start offset positions for the function parameters, if any.
	 * 
	 * @return The start offset positions for the function parameters. Can be null.
	 */
	public int[] getParameterStartPositions();

	/**
	 * Returns the start offset of the entry.
	 * 
	 * @return The starting offset of the entry
	 */
	public int getStartOffset();
}
