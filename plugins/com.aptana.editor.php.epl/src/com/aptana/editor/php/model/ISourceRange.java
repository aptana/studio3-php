package com.aptana.editor.php.model;

/**
 * Element source range.
 * @author Denis Denisenko
 */
public interface ISourceRange {

	/**
	 * Gets length.
	 * @return length.
	 */
	int getLength();
	
	/**
	 * Gets offset.
	 * @return offset.
	 */
	int getOffset();
}
