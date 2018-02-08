package com.aptana.editor.php.core.model;

import com.aptana.editor.php.core.model.ISourceRange;

/**
 * Source range.
 * @author Denis Denisenko
 */
public class SourceRange implements ISourceRange
{
	/**
	 * Offset.
	 */
	private int length;
	
	/**
	 * Length.
	 */
	private int offset;
	
	/**
	 * SourceRange constructor.
	 * @param offset - offset.
	 * @param length - length.
	 */
	public SourceRange(int offset, int length)
	{
		this.offset = offset;
		this.length = length;
	}
	
	/**
	 * SourceRange constructor.
	 * @param offset - offset.
	 */
	public SourceRange(int offset)
	{
		this.offset = offset;
		this.length = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getOffset()
	{
		return offset;
	}

}
