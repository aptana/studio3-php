/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org2.eclipse.dltk.ast;

public final class PositionInformation {
	public final int sourceStart;
	public final int sourceEnd;
	public final int nameStart;
	public final int nameEnd;

	public PositionInformation(int nameStart, int nameEnd, int sourceStart,
			int sourceEnd) {

		this.nameStart = nameStart;
		this.nameEnd = nameEnd;
		this.sourceEnd = sourceEnd;
		this.sourceStart = sourceStart;
	}

	public String toString() {
		return "[" + sourceStart + "," + sourceEnd + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + nameEnd;
		result = prime * result + nameStart;
		result = prime * result + sourceEnd;
		result = prime * result + sourceStart;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PositionInformation other = (PositionInformation) obj;
		if (nameEnd != other.nameEnd)
			return false;
		if (nameStart != other.nameStart)
			return false;
		if (sourceEnd != other.sourceEnd)
			return false;
		if (sourceStart != other.sourceStart)
			return false;
		return true;
	}
}
