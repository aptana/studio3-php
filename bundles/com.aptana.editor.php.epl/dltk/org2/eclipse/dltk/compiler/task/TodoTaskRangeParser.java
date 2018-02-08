/*******************************************************************************
 * Copyright (c) 2008 xored software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org2.eclipse.dltk.compiler.task;

public class TodoTaskRangeParser extends TodoTaskSimpleParser {

	/**
	 * @param preferences
	 */
	public TodoTaskRangeParser(ITodoTaskPreferences preferences) {
		super(preferences);
	}

	private static final int ALLOC_INCREMENT = 1024;

	private boolean checkRanges = true;

	/**
	 * @return the checkRanges
	 */
	public boolean isCheckRanges() {
		return checkRanges;
	}

	/**
	 * @param value
	 *            the checkRanges to set
	 */
	public void setCheckRanges(boolean value) {
		this.checkRanges = value;
	}

	private int[] ranges = new int[ALLOC_INCREMENT];
	private int rangeCount = 0;

	protected void reset() {
		rangeCount = 0;
	}

	/**
	 * @param sourceStart
	 * @param sourceEnd
	 */
	protected void excludeRange(int sourceStart, int sourceEnd) {
		if (rangeCount * 2 >= ranges.length) {
			final int[] newArray = new int[ranges.length + ALLOC_INCREMENT];
			System.arraycopy(ranges, 0, newArray, 0, ranges.length);
			ranges = newArray;
		}
		ranges[rangeCount * 2] = sourceStart;
		ranges[rangeCount * 2 + 1] = sourceEnd;
		++rangeCount;
	}

	/**
	 * Validates that the specified location does not hit the excluded ranges.
	 * 
	 * @param location
	 * @return
	 */
	protected boolean isValid(int location) {
		for (int i = 0; i < rangeCount; ++i) {
			if (location >= ranges[i * 2] && location < ranges[i * 2 + 1]) {
				return false;
			}
		}
		return true;
	}

	protected int findCommentStart(char[] content, int begin, int end) {
		if (!checkRanges) {
			return super.findCommentStart(content, begin, end);
		}
		for (int i = begin; i < end; ++i) {
			if (content[i] == '#' && isValid(i)) {
				return i + 1;
			}
		}
		return -1;
	}

}
