/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.compiler.util;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.dltk.compiler.InvalidInputException;



public class ScannerHelper {
//	 extended unicode support
	public static final int LOW_SURROGATE_MIN_VALUE = 0xDC00;
	public static final int HIGH_SURROGATE_MIN_VALUE = 0xD800;
	public static final int HIGH_SURROGATE_MAX_VALUE = 0xDBFF;
	public static final int LOW_SURROGATE_MAX_VALUE = 0xDFFF;
	
	// storage for internal flags (32 bits)						BIT USAGE
	public final static int Bit1 = 0x1; 						// return type (operator) | name reference kind (name ref) | add assertion (type decl) | useful empty statement (empty statement)
	public final static int Bit2 = 0x2; 						// return type (operator) | name reference kind (name ref) | has local type (type, method, field decl)
	public final static int Bit3 = 0x4; 						// return type (operator) | name reference kind (name ref) | implicit this (this ref)
	public final static int Bit4 = 0x8; 						// return type (operator) | first assignment to local (local decl) | undocumented empty block (block, type and method decl)
	public final static int Bit5 = 0x10; 						// value for return (expression) | has all method bodies (unit) | supertype ref (type ref)
	public final static int Bit6 = 0x20; 						// depth (name ref, msg) | ignore need cast check (cast expression)
	public final static int Bit7 = 0x40; 						// depth (name ref, msg) | operator (operator) | need runtime checkcast (cast expression) | label used (labelStatement)
	public final static int Bit8 = 0x80; 						// depth (name ref, msg) | operator (operator) | unsafe cast (cast expression)
	public final static int Bit9 = 0x100; 					// depth (name ref, msg) | operator (operator) | is local type (type decl)
	public final static int Bit10= 0x200; 					// depth (name ref, msg) | operator (operator) | is anonymous type (type decl)
	public final static int Bit11 = 0x400; 					// depth (name ref, msg) | operator (operator) | is member type (type decl)
	public final static int Bit12 = 0x800; 					// depth (name ref, msg) | operator (operator) | has abstract methods (type decl)
	public final static int Bit13 = 0x1000; 				// depth (name ref, msg) | is secondary type (type decl)
	public final static int Bit14 = 0x2000; 				// strictly assigned (reference lhs)
	public final static int Bit15 = 0x4000; 				// is unnecessary cast (expression) | is varargs (type ref) | isSubRoutineEscaping (try statement)
	public final static int Bit16 = 0x8000; 				// in javadoc comment (name ref, type ref, msg)
	public final static int Bit17 = 0x10000; 				// compound assigned (reference lhs)
	public final static int Bit18 = 0x20000;				// non null (expression)				
	public final static int Bit19 = 0x40000;
	public final static int Bit20 = 0x80000; 
	public final static int Bit21 = 0x100000; 		
	public final static int Bit22 = 0x200000; 			// parenthesis count (expression)
	public final static int Bit23 = 0x400000; 			// parenthesis count (expression)
	public final static int Bit24 = 0x800000; 			// parenthesis count (expression)
	public final static int Bit25 = 0x1000000; 			// parenthesis count (expression)
	public final static int Bit26 = 0x2000000; 			// parenthesis count (expression)
	public final static int Bit27 = 0x4000000; 			// parenthesis count (expression)
	public final static int Bit28 = 0x8000000; 			// parenthesis count (expression)
	public final static int Bit29 = 0x10000000; 		// parenthesis count (expression)
	public final static int Bit30 = 0x20000000; 		// elseif (if statement) | try block exit (try statement) | fall-through (case statement)
	public final static int Bit31 = 0x40000000; 		// local declaration reachable (local decl) | ignore raw type check (type ref) | discard entire assignment (assignment)
	public final static int Bit32 = 0x80000000; 		// reachable (statement)

	public final static long Bit32L = 0x80000000L; 		
	public final static long Bit33L = 0x100000000L;
	public final static long Bit34L = 0x200000000L;
	public final static long Bit35L = 0x400000000L;
	public final static long Bit36L = 0x800000000L;
	public final static long Bit37L = 0x1000000000L;
	public final static long Bit38L = 0x2000000000L;
	public final static long Bit39L = 0x4000000000L;
	public final static long Bit40L = 0x8000000000L;
	public final static long Bit41L = 0x10000000000L;
	public final static long Bit42L = 0x20000000000L;
	public final static long Bit43L = 0x40000000000L;
	public final static long Bit44L = 0x80000000000L;
	public final static long Bit45L = 0x100000000000L;
	public final static long Bit46L = 0x200000000000L;
	public final static long Bit47L = 0x400000000000L;
	public final static long Bit48L = 0x800000000000L;
	public final static long Bit49L = 0x1000000000000L;
	public final static long Bit50L = 0x2000000000000L;
	public final static long Bit51L = 0x4000000000000L;
	public final static long Bit52L = 0x8000000000000L;
	public final static long Bit53L = 0x10000000000000L;
	public final static long Bit54L = 0x20000000000000L;
	public final static long Bit55L = 0x40000000000000L;
	public final static long Bit56L = 0x80000000000000L;
	public final static long Bit57L = 0x100000000000000L;
	public final static long Bit58L = 0x200000000000000L;
	public final static long Bit59L = 0x400000000000000L;
	public final static long Bit60L = 0x800000000000000L;
	public final static long Bit61L = 0x1000000000000000L;
	public final static long Bit62L = 0x2000000000000000L;
	public final static long Bit63L = 0x4000000000000000L;
	public final static long Bit64L = 0x8000000000000000L;
	
	public final static long[] Bits = { 
		Bit1, Bit2, Bit3, Bit4, Bit5, Bit6,
		Bit7, Bit8, Bit9, Bit10, Bit11, Bit12, 
		Bit13, Bit14, Bit15, Bit16, Bit17, Bit18, 
		Bit19, Bit20, Bit21, Bit22, Bit23, Bit24, 
		Bit25, Bit26, Bit27, Bit28, Bit29, Bit30, 
		Bit31, Bit32, Bit33L, Bit34L, Bit35L, Bit36L, 
		Bit37L, Bit38L, Bit39L, Bit40L, Bit41L, Bit42L, 
		Bit43L, Bit44L, Bit45L, Bit46L, Bit47L, Bit48L, 
		Bit49L, Bit50L, Bit51L, Bit52L, Bit53L, Bit54L, 
		Bit55L, Bit56L, Bit57L, Bit58L, Bit59L, Bit60L, 
		Bit61L, Bit62L, Bit63L, Bit64L,
	};
	
	private static final int START_INDEX = 0;

	private static final int PART_INDEX = 1;

	private static long[][][] Tables;

	public final static int MAX_OBVIOUS = 128;

	public final static int[] OBVIOUS_IDENT_CHAR_NATURES = new int[MAX_OBVIOUS];

	public final static int C_JLS_SPACE = 0x100;

	public final static int C_SPECIAL = 0x80;

	public final static int C_IDENT_START = 0x40;

	public final static int C_UPPER_LETTER = 0x20;

	public final static int C_LOWER_LETTER = 0x10;

	public final static int C_IDENT_PART = 0x8;

	public final static int C_DIGIT = 0x4;

	public final static int C_SEPARATOR = 0x2;

	public final static int C_SPACE = 0x1;

	static {
		OBVIOUS_IDENT_CHAR_NATURES[0] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[1] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[2] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[3] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[4] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[5] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[6] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[7] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[8] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[14] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[15] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[16] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[17] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[18] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[19] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[20] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[21] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[22] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[23] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[24] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[25] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[26] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[27] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[127] = C_IDENT_PART;

		for (int i = '0'; i <= '9'; i++)
			OBVIOUS_IDENT_CHAR_NATURES[i] = C_DIGIT | C_IDENT_PART;

		for (int i = 'a'; i <= 'z'; i++)
			OBVIOUS_IDENT_CHAR_NATURES[i] = C_LOWER_LETTER | C_IDENT_PART
					| C_IDENT_START;
		for (int i = 'A'; i <= 'Z'; i++)
			OBVIOUS_IDENT_CHAR_NATURES[i] = C_UPPER_LETTER | C_IDENT_PART
					| C_IDENT_START;

		OBVIOUS_IDENT_CHAR_NATURES['_'] = C_SPECIAL | C_IDENT_PART
				| C_IDENT_START;
		OBVIOUS_IDENT_CHAR_NATURES['$'] = C_SPECIAL | C_IDENT_PART
				| C_IDENT_START;

		OBVIOUS_IDENT_CHAR_NATURES[9] = C_SPACE | C_JLS_SPACE; // \ u0009:
																// HORIZONTAL
																// TABULATION
		OBVIOUS_IDENT_CHAR_NATURES[10] = C_SPACE | C_JLS_SPACE; // \ u000a: LINE
																// FEED
		OBVIOUS_IDENT_CHAR_NATURES[11] = C_SPACE;
		OBVIOUS_IDENT_CHAR_NATURES[12] = C_SPACE | C_JLS_SPACE; // \ u000c: FORM
																// FEED
		OBVIOUS_IDENT_CHAR_NATURES[13] = C_SPACE | C_JLS_SPACE; // \ u000d:
																// CARRIAGE
																// RETURN
		OBVIOUS_IDENT_CHAR_NATURES[28] = C_SPACE;
		OBVIOUS_IDENT_CHAR_NATURES[29] = C_SPACE;
		OBVIOUS_IDENT_CHAR_NATURES[30] = C_SPACE;
		OBVIOUS_IDENT_CHAR_NATURES[31] = C_SPACE;
		OBVIOUS_IDENT_CHAR_NATURES[32] = C_SPACE | C_JLS_SPACE; // \ u0020:
																// SPACE

		OBVIOUS_IDENT_CHAR_NATURES['.'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[':'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[';'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[','] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['['] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[']'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['('] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[')'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['{'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['}'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['+'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['-'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['*'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['/'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['='] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['&'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['|'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['?'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['<'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['>'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['!'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['%'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['^'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['~'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['"'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['\''] = C_SEPARATOR;
	}

	static {
		Tables = new long[2][][];
		Tables[START_INDEX] = new long[2][];
		Tables[PART_INDEX] = new long[3][];
		try {
			DataInputStream inputStream = new DataInputStream(
					ScannerHelper.class.getResourceAsStream("start1.rsc")); //$NON-NLS-1$
			long[] readValues = new long[1024];
			for (int i = 0; i < 1024; i++) {
				readValues[i] = inputStream.readLong();
			}
			inputStream.close();
			Tables[START_INDEX][0] = readValues;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			DataInputStream inputStream = new DataInputStream(
					ScannerHelper.class.getResourceAsStream("start2.rsc")); //$NON-NLS-1$
			long[] readValues = new long[1024];
			for (int i = 0; i < 1024; i++) {
				readValues[i] = inputStream.readLong();
			}
			inputStream.close();
			Tables[START_INDEX][1] = readValues;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			DataInputStream inputStream = new DataInputStream(
					ScannerHelper.class.getResourceAsStream("part1.rsc")); //$NON-NLS-1$
			long[] readValues = new long[1024];
			for (int i = 0; i < 1024; i++) {
				readValues[i] = inputStream.readLong();
			}
			inputStream.close();
			Tables[PART_INDEX][0] = readValues;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			DataInputStream inputStream = new DataInputStream(
					ScannerHelper.class.getResourceAsStream("part2.rsc")); //$NON-NLS-1$
			long[] readValues = new long[1024];
			for (int i = 0; i < 1024; i++) {
				readValues[i] = inputStream.readLong();
			}
			inputStream.close();
			Tables[PART_INDEX][1] = readValues;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			DataInputStream inputStream = new DataInputStream(
					ScannerHelper.class.getResourceAsStream("part14.rsc")); //$NON-NLS-1$
			long[] readValues = new long[1024];
			for (int i = 0; i < 1024; i++) {
				readValues[i] = inputStream.readLong();
			}
			inputStream.close();
			Tables[PART_INDEX][2] = readValues;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final static boolean isBitSet(long[] values, int i) {
		try {
			return (values[i / 64] & Bits[i % 64]) != 0;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public static boolean isScriptIdentifierPart(char c) {
		if (c < MAX_OBVIOUS) {
			return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_IDENT_PART) != 0;
		}
		return Character.isJavaIdentifierPart(c);
	}

	public static boolean isScriptIdentifierPart(char high, char low) {
		int codePoint = toCodePoint(high, low);
		switch ((codePoint & 0x1F0000) >> 16) {
		case 0:
			return Character.isJavaIdentifierPart((char) codePoint);
		case 1:
			return isBitSet(Tables[PART_INDEX][0], codePoint & 0xFFFF);
		case 2:
			return isBitSet(Tables[PART_INDEX][1], codePoint & 0xFFFF);
		case 14:
			return isBitSet(Tables[PART_INDEX][2], codePoint & 0xFFFF);
		}
		return false;
	}

	public static boolean isScriptIdentifierStart(char c) {
		if (c < MAX_OBVIOUS) {
			return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_IDENT_START) != 0;
		}
		return Character.isJavaIdentifierStart(c);
	}

	public static boolean isScriptIdentifierStart(char high, char low) {
		int codePoint = toCodePoint(high, low);
		switch ((codePoint & 0x1F0000) >> 16) {
		case 0:
			return Character.isJavaIdentifierStart((char) codePoint);
		case 1:
			return isBitSet(Tables[START_INDEX][0], codePoint & 0xFFFF);
		case 2:
			return isBitSet(Tables[START_INDEX][1], codePoint & 0xFFFF);
		}
		return false;
	}

	private static int toCodePoint(char high, char low) {
		return (high - HIGH_SURROGATE_MIN_VALUE) * 0x400
				+ (low - LOW_SURROGATE_MIN_VALUE) + 0x10000;
	}

 	public static boolean isDigit(char c) throws InvalidInputException {
		if (c < ScannerHelper.MAX_OBVIOUS) {
			return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_DIGIT) != 0;
		}
		if (Character.isDigit(c)) {
			throw new InvalidInputException("Invalid_Digit"); //$NON-NLS-1$
		}
		return false;
	}

	public static int digit(char c, int radix) {
		if (c < ScannerHelper.MAX_OBVIOUS) {
			switch (radix) {
			case 8:
				if (c >= 48 && c <= 55) {
					return c - 48;
				}
				return -1;
			case 10:
				if (c >= 48 && c <= 57) {
					return c - 48;
				}
				return -1;
			case 16:
				if (c >= 48 && c <= 57) {
					return c - 48;
				}
				if (c >= 65 && c <= 70) {
					return c - 65 + 10;
				}
				if (c >= 97 && c <= 102) {
					return c - 97 + 10;
				}
				return -1;
			}
		}
		return Character.digit(c, radix);
	}

	public static int getNumericValue(char c) {
		if (c < ScannerHelper.MAX_OBVIOUS) {
			switch (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c]) {
			case C_DIGIT:
				return c - '0';
			case C_LOWER_LETTER:
				return 10 + c - 'a';
			case C_UPPER_LETTER:
				return 10 + c - 'A';
			}
		}
		return Character.getNumericValue(c);
	}

	public static char toUpperCase(char c) {
		if (c < MAX_OBVIOUS) {
			if ((ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_UPPER_LETTER) != 0) {
				return c;
			} else if ((ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_LOWER_LETTER) != 0) {
				return (char) (c - 32);
			}
		}
		return Character.toLowerCase(c);
	}

	public static char toLowerCase(char c) {
		if (c < MAX_OBVIOUS) {
			if ((ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_LOWER_LETTER) != 0) {
				return c;
			} else if ((ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_UPPER_LETTER) != 0) {
				return (char) (32 + c);
			}
		}
		return Character.toLowerCase(c);
	}

	public static boolean isLowerCase(char c) {
		if (c < MAX_OBVIOUS) {
			return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_LOWER_LETTER) != 0;
		}
		return Character.isLowerCase(c);
	}

	public static boolean isUpperCase(char c) {
		if (c < MAX_OBVIOUS) {
			return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_UPPER_LETTER) != 0;
		}
		return Character.isUpperCase(c);
	}

	/**
	 * Include also non JLS whitespaces.
	 * 
	 * return true if Character.isWhitespace(c) would return true
	 */
	public static boolean isWhitespace(char c) {
		if (c < MAX_OBVIOUS) {
			return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_SPACE) != 0;
		}
		return Character.isWhitespace(c);
	}

	public static boolean isLetter(char c) {
		if (c < MAX_OBVIOUS) {
			return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & (ScannerHelper.C_UPPER_LETTER | ScannerHelper.C_LOWER_LETTER)) != 0;
		}
		return Character.isLetter(c);
	}

	public static boolean isLetterOrDigit(char c) {
		if (c < MAX_OBVIOUS) {
			return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & (ScannerHelper.C_UPPER_LETTER
					| ScannerHelper.C_LOWER_LETTER | ScannerHelper.C_DIGIT)) != 0;
		}
		return Character.isLetterOrDigit(c);
	}
}
