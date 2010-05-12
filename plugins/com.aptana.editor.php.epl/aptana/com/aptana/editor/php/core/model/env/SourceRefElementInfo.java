/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package com.aptana.editor.php.core.model.env;

import com.aptana.editor.php.core.model.ISourceRange;
import com.aptana.editor.php.core.model.SourceRange;

public class SourceRefElementInfo extends ModelElementInfo {
	protected int fSourceRangeStart, fSourceRangeEnd;

	public int getDeclarationSourceEnd() {
		return fSourceRangeEnd;
	}

	public int getDeclarationSourceStart() {
		return fSourceRangeStart;
	}
	
	protected ISourceRange getSourceRange() {
		return new SourceRange(fSourceRangeStart, fSourceRangeEnd - fSourceRangeStart + 1);
	}
	
	protected void setSourceRangeEnd(int end) {
		fSourceRangeEnd = end;
	}
	
	protected void setSourceRangeStart(int start) {
		fSourceRangeStart = start;
	}

}
