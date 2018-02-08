/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.model.impl;

import com.aptana.editor.php.core.model.ISourceRange;

/**
 * Source range.
 * 
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
	 * 
	 * @param offset
	 *            - offset.
	 * @param length
	 *            - length.
	 */
	public SourceRange(int offset, int length)
	{
		this.offset = offset;
		this.length = length;
	}

	/**
	 * SourceRange constructor.
	 * 
	 * @param offset
	 *            - offset.
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
