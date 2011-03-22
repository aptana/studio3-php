/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org2.eclipse.dltk.compiler.problem;

import java.util.Arrays;

import org2.eclipse.dltk.compiler.util.Messages;

import com.aptana.editor.php.epl.PHPEplPlugin;

public class DefaultProblem extends CategorizedProblem {

	private static int hashCode(Object[] array) {
		final int prime = 31;
		if (array == null)
			return 0;
		int result = 1;
		for (int index = 0; index < array.length; index++) {
			result = prime * result
					+ (array[index] == null ? 0 : array[index].hashCode());
		}
		return result;
	}

	private String fileName;

	private int id;

	private int startPosition, endPosition, line, column;

	private int severity;

	private String[] arguments;

	private String message;

	//XXX - Aptana Mod
	public static final String MARKER_TYPE_PROBLEM = "com.aptana.editor.php.epl.problem"; //$NON-NLS-1$
	public static final String MARKER_TYPE_TASK = "com.aptana.editor.php.epl.task"; //$NON-NLS-1$
	
//	public static final String MARKER_TYPE_PROBLEM = "org2.eclipse.dltk.core.problem"; //$NON-NLS-1$
//
//	public static final String MARKER_TYPE_TASK = "org2.eclipse.dltk.core.task"; //$NON-NLS-1$

	public static final Object[] EMPTY_VALUES = {};

	public DefaultProblem(String originatingFileName, String message, int id,
			String[] stringArguments, int severity, int startPosition,
			int endPosition, int line, int column) {
		this.fileName = originatingFileName;
		this.message = message;
		this.id = id;
		this.arguments = stringArguments;
		this.severity = severity;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.line = line;
		this.column = column;
	}

	public DefaultProblem(String originatingFileName, String message, int id,
			String[] stringArguments, int severity, int startPosition,
			int endPosition, int line) {
		this(originatingFileName, message, id, stringArguments, severity,
				startPosition, endPosition, line, 0);
	}

	public DefaultProblem(String message, int id, String[] stringArguments,
			int severity, int startPosition, int endPosition, int line) {
		this(NONAME, message, id, stringArguments, severity, startPosition,
				endPosition, line, 0);
	}

	private static final String NONAME = ""; //$NON-NLS-1$

	public String errorReportSource(char[] unitSource) {
		return errorReportSource(unitSource, 0);
	}

	public String errorReportSource(char[] unitSource, int tagBits) {
		// extra from the source the inaccurate token
		// and "highlight" it using some underneath ^^^^^
		// put some context around too.

		// this code assumes that the font used in the console is fixed size

		// sanity .....
		if ((this.startPosition > this.endPosition)
				|| ((this.startPosition < 0) && (this.endPosition < 0))
				|| unitSource.length == 0)
			return Messages.problem_noSourceInformation;

		StringBuffer errorBuffer = new StringBuffer();
		if (PHPEplPlugin.DEBUG) {
			System.err.println("TODO: Add correct code here. DefaultProblem"); //$NON-NLS-1$
		}
		// if ((tagBits & Main.Logger.EMACS) == 0) {
		// errorBuffer.append(' ').append(Messages.bind(Messages.problem_atLine,
		// String.valueOf(this.line)));
		// errorBuffer.append(Util.LINE_SEPARATOR);
		// }
		errorBuffer.append('\t');

		char c;
		final char SPACE = '\u0020';
		final char MARK = '^';
		final char TAB = '\t';
		// the next code tries to underline the token.....
		// it assumes (for a good display) that token source does not
		// contain any \r \n. This is false on statements !
		// (the code still works but the display is not optimal !)

		// expand to line limits
		int length = unitSource.length, begin, end;
		for (begin = this.startPosition >= length ? length - 1
				: this.startPosition; begin > 0; begin--) {
			if ((c = unitSource[begin - 1]) == '\n' || c == '\r')
				break;
		}
		for (end = this.endPosition >= length ? length - 1 : this.endPosition; end + 1 < length; end++) {
			if ((c = unitSource[end + 1]) == '\r' || c == '\n')
				break;
		}

		// trim left and right spaces/tabs
		while ((c = unitSource[begin]) == ' ' || c == '\t')
			begin++;
		while ((c = unitSource[end]) == ' ' || c == '\t')
			end--;

		// copy source
		errorBuffer.append(unitSource, begin, end - begin + 1);
		errorBuffer.append(System.getProperty("line.separator")).append("\t"); //$NON-NLS-1$ //$NON-NLS-2$

		// compute underline
		for (int i = begin; i < this.startPosition; i++) {
			errorBuffer.append((unitSource[i] == TAB) ? TAB : SPACE);
		}
		for (int i = this.startPosition; i <= (this.endPosition >= length ? length - 1
				: this.endPosition); i++) {
			errorBuffer.append(MARK);
		}
		return errorBuffer.toString();
	}

	/**
	 * Answer back the original arguments recorded into the problem.
	 * 
	 * @return java.lang.String[]
	 */
	public String[] getArguments() {
		return this.arguments;
	}

	/**
	 * @see org2.eclipse.dltk.core.compiler.CategorizedProblem#getCategoryID()
	 */
	public int getCategoryID() {
		// return ProblemReporter.getProblemCategory(this.severity, this.id);
		if (PHPEplPlugin.DEBUG) {
			System.err
					.println("TODO: DefaultProblem getCategoryID always return 0. Fix it."); //$NON-NLS-1$
		}
		return 0;
	}

	/**
	 * Answer the type of problem.
	 * 
	 * @see org2.eclipse.dltk.core.compiler.IProblem#getID()
	 * @return int
	 */
	public int getID() {
		return this.id;
	}

	/**
	 * Answers a readable name for the category which this problem belongs to,
	 * or null if none could be found. FOR TESTING PURPOSE
	 * 
	 * @return java.lang.String
	 */
	public String getInternalCategoryMessage() {
		switch (getCategoryID()) {
		case CAT_UNSPECIFIED:
			return "unspecified"; //$NON-NLS-1$
		case CAT_BUILDPATH:
			return "buildpath"; //$NON-NLS-1$
		case CAT_SYNTAX:
			return "syntax"; //$NON-NLS-1$
		case CAT_IMPORT:
			return "import"; //$NON-NLS-1$
		case CAT_TYPE:
			return "type"; //$NON-NLS-1$
		case CAT_MEMBER:
			return "member"; //$NON-NLS-1$
		case CAT_INTERNAL:
			return "internal"; //$NON-NLS-1$		
		case CAT_CODE_STYLE:
			return "code style"; //$NON-NLS-1$
		case CAT_POTENTIAL_PROGRAMMING_PROBLEM:
			return "potential programming problem"; //$NON-NLS-1$
		case CAT_NAME_SHADOWING_CONFLICT:
			return "name shadowing conflict"; //$NON-NLS-1$
		case CAT_DEPRECATION:
			return "deprecation"; //$NON-NLS-1$
		case CAT_UNNECESSARY_CODE:
			return "unnecessary code"; //$NON-NLS-1$
		case CAT_UNCHECKED_RAW:
			return "unchecked/raw"; //$NON-NLS-1$
		case CAT_NLS:
			return "nls"; //$NON-NLS-1$
		case CAT_RESTRICTION:
			return "restriction"; //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Returns the marker type associated to this problem.
	 * 
	 * @see org2.eclipse.dltk.core.compiler.CategorizedProblem#getMarkerType()
	 */
	public String getMarkerType() {
		return this.id == IProblem.Task ? MARKER_TYPE_TASK
				: MARKER_TYPE_PROBLEM;
	}

	/**
	 * Answer a localized, human-readable message string which describes the
	 * problem.
	 * 
	 * @return java.lang.String
	 */
	public String getMessage() {
		return this.message;
	}

	public int getColumn() {
		return this.column;
	}

	/**
	 * Answer the file name in which the problem was found.
	 * 
	 * @return String
	 */
	public String getOriginatingFileName() {
		return this.fileName;
	}

	/**
	 * Answer the end position of the problem (inclusive), or -1 if unknown.
	 * 
	 * @return int
	 */
	public int getSourceEnd() {
		return this.endPosition;
	}

	/**
	 * Answer the line number in source where the problem begins.
	 * 
	 * @return int
	 */
	public int getSourceLineNumber() {
		return this.line;
	}

	/**
	 * Answer the start position of the problem (inclusive), or -1 if unknown.
	 * 
	 * @return int
	 */
	public int getSourceStart() {
		return this.startPosition;
	}

	/*
	 * Helper method: checks the severity to see if the Error bit is set.
	 * 
	 * @return boolean
	 */
	public boolean isError() {
		return (this.severity & ProblemSeverities.Error) != 0;
	}

	/*
	 * Helper method: checks the severity to see if the Error bit is not set.
	 * 
	 * @return boolean
	 */
	public boolean isWarning() {
		return (this.severity & ProblemSeverities.Error) == 0;
	}

	public void setOriginatingFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Set the end position of the problem (inclusive), or -1 if unknown.
	 * 
	 * Used for shifting problem positions.
	 * 
	 * @param sourceEnd
	 *            the new value of the sourceEnd of the receiver
	 */
	public void setSourceEnd(int sourceEnd) {
		this.endPosition = sourceEnd;
	}

	/**
	 * Set the line number in source where the problem begins.
	 * 
	 * @param lineNumber
	 *            the new value of the line number of the receiver
	 */
	public void setSourceLineNumber(int lineNumber) {

		this.line = lineNumber;
	}

	/**
	 * Set the start position of the problem (inclusive), or -1 if unknown.
	 * 
	 * Used for shifting problem positions.
	 * 
	 * @param sourceStart
	 *            the new value of the source start position of the receiver
	 */
	public void setSourceStart(int sourceStart) {
		this.startPosition = sourceStart;
	}

	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Pb"); //$NON-NLS-1$
		final int maskedId = this.id & IProblem.IgnoreCategoriesMask;
		if (maskedId != 0) {
			sb.append('#');
			sb.append(maskedId);
		}
		sb.append(' ');
		sb.append(line);
		sb.append('[');
		sb.append(startPosition);
		sb.append(".."); //$NON-NLS-1$
		sb.append(endPosition);
		sb.append(']');
		sb.append(':');
		if (this.message != null) {
			sb.append(this.message);
		} else {
			if (this.arguments != null) {
				for (int i = 0; i < this.arguments.length; i++) {
					sb.append(' ');
					sb.append(this.arguments[i]);
				}
			}
		}
		return sb.toString();
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + DefaultProblem.hashCode(arguments);
		result = prime * result + column;
		result = prime * result + endPosition;
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + id;
		result = prime * result + line;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + severity;
		result = prime * result + startPosition;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DefaultProblem other = (DefaultProblem) obj;
		if (!Arrays.equals(arguments, other.arguments))
			return false;
		if (column != other.column)
			return false;
		if (endPosition != other.endPosition)
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (id != other.id)
			return false;
		if (line != other.line)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (severity != other.severity)
			return false;
		if (startPosition != other.startPosition)
			return false;
		return true;
	}
}
