/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org2.eclipse.php.internal.core.corext;

import java.util.Arrays;
import java.util.Comparator;

import org2.eclipse.php.internal.core.ast.nodes.ASTNode;

public class SourceRange implements ISourceRange {

	private final int fOffset;
	private final int fLength;

	public SourceRange(int offset, int length) {
		fLength = length;
		fOffset = offset;
	}

	public SourceRange(ASTNode node) {
		this(node.getStart(), node.getLength());
	}

	// public SourceRange(IProblem problem) {
	// this(problem.getSourceStart(), problem.getSourceEnd() -
	// problem.getSourceStart() + 1);
	// }

	/*
	 * @see ISourceRange#getLength()
	 */
	public int getLength() {
		return fLength;
	}

	/*
	 * @see ISourceRange#getOffset()
	 */
	public int getOffset() {
		return fOffset;
	}

	public int getEndExclusive() {
		return getOffset() + getLength();
	}

	public int getEndInclusive() {
		return getEndExclusive() - 1;
	}

	/*
	 * non java doc for debugging only
	 */
	public String toString() {
		return "<offset: " + fOffset + " length: " + fLength + "/>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Sorts the given ranges by offset (backwards). Note: modifies the
	 * parameter.
	 * 
	 * @param ranges
	 *            the ranges to sort
	 * @return the sorted ranges, which are identical to the parameter ranges
	 */
	public static ISourceRange[] reverseSortByOffset(ISourceRange[] ranges) {
		Comparator<ISourceRange> comparator = new Comparator<ISourceRange>() {
			public int compare(ISourceRange o1, ISourceRange o2)
			{
				return o2.getOffset() - o1.getOffset();
			}
		};
		Arrays.sort(ranges, comparator);
		return ranges;
	}

	/*
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ISourceRange))
			return false;
		return ((ISourceRange) obj).getOffset() == fOffset
				&& ((ISourceRange) obj).getLength() == fLength;
	}

	/*
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return fLength ^ fOffset;
	}

	public boolean covers(ASTNode node) {
		return covers(new SourceRange(node));
	}

	public boolean covers(SourceRange range) {
		return getOffset() <= range.getOffset()
				&& getEndInclusive() >= range.getEndInclusive();
	}

	/**
	 * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=130161 (Java
	 * Model returns ISourceRanges [-1, 0] if source not available).
	 * 
	 * @param range
	 *            a source range, can be <code>null</code>
	 * @return <code>true</code> iff range is not null and range.getOffset() is
	 *         not -1
	 */
	public static boolean isAvailable(ISourceRange range) {
		return range != null && range.getOffset() != -1;
	}
}
