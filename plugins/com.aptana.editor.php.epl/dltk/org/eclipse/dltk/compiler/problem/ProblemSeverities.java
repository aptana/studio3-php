/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.compiler.problem;

public final class ProblemSeverities {
	
	private ProblemSeverities(){		
	}
	
	public static final int Ignore = -1; // during handling only
	public static final int Warning = 0; // during handling only

	public static final int Error = 1; // when bit is set: problem is error, if not it is a warning
	public static final int AbortCompilation = 2;
	public static final int AbortSourceModule = 4;
	public static final int AbortType = 8;
	public static final int AbortMethod = 16;
	public static final int Abort = 30; // 2r11110
	public static final int Optional = 32; // when bit is set: problem was configurable
	public static final int SecondaryError = 64;
	public static final int Fatal = 128; // when bit is set: problem was either a mandatory error, or an optional+treatOptionalErrorAsFatal	
}
