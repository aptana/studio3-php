/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.compiler.problem;

import org.eclipse.dltk.compiler.CharOperation;


public abstract class CategorizedProblem implements IProblem {
	/**
	 * List of standard category IDs used by the problems, more categories will
	 * be added in the future.
	 */
	public static final int CAT_UNSPECIFIED = 0;
	/** Category for problems related to buildpath */
	public static final int CAT_BUILDPATH = 10;
	/** Category for fatal problems related to syntax */
	public static final int CAT_SYNTAX = 20;
	/** Category for fatal problems in import statements */
	public static final int CAT_IMPORT = 30;
	/**
	 * Category for fatal problems related to types, could be addressed by some
	 * type change
	 */
	public static final int CAT_TYPE = 40;
	/**
	 * Category for fatal problems related to type members, could be addressed
	 * by some field or method change
	 */
	public static final int CAT_MEMBER = 50;
	/**
	 * Category for fatal problems which could not be addressed by external
	 * changes, but require an edit to be addressed
	 */
	public static final int CAT_INTERNAL = 60;	
	/** Category for optional problems related to coding style practices */
	public static final int CAT_CODE_STYLE = 80;
	/** Category for optional problems related to potential programming flaws */
	public static final int CAT_POTENTIAL_PROGRAMMING_PROBLEM = 90;
	/** Category for optional problems related to naming conflicts */
	public static final int CAT_NAME_SHADOWING_CONFLICT = 100;
	/** Category for optional problems related to deprecation */
	public static final int CAT_DEPRECATION = 110;
	/** Category for optional problems related to unnecessary code */
	public static final int CAT_UNNECESSARY_CODE = 120;
	/** Category for optional problems related to type safety in generics */
	public static final int CAT_UNCHECKED_RAW = 130;
	/**
	 * Category for optional problems related to internationalization of String
	 * literals
	 */
	public static final int CAT_NLS = 140;
	/** Category for optional problems related to access restrictions */
	public static final int CAT_RESTRICTION = 150;

	/**
	 * Returns an integer identifying the category of this problem. Categories,
	 * like problem IDs are defined in the context of some marker type. Custom
	 * implementations of <code>CategorizedProblem</code> may choose arbitrary
	 * values for problem/category IDs, as long as they are associated with a
	 * different marker type. Standard script problem markers (i.e. marker type is
	 * "org.eclipse.dltk.core.problem") carry an attribute "categoryId"
	 * persisting the originating problem category ID as defined by this
	 * method).
	 * 
	 * @return id - an integer identifying the category of this problem
	 */
	public abstract int getCategoryID();

	/**
	 * Returns the marker type associated to this problem, if it gets persisted
	 * into a marker by the Standard Script problems are associated to
	 * marker type "org.eclipse.dltk.core.problem"). Note: problem markers are
	 * expected to extend "org.eclipse.core.resources.problemmarker" marker
	 * type.
	 * 
	 * @return the type of the marker which would be associated to the problem
	 */
	public abstract String getMarkerType();

	/**
	 * Returns the names of the extra marker attributes associated to this
	 * problem when persisted into a marker. Extra attributes
	 * are only optional, and are allowing client customization of generated
	 * markers. By default, no EXTRA attributes is persisted, and a categorized
	 * problem only persists the following attributes:
	 * <ul>
	 * <li> <code>IMarker#MESSAGE</code> -&gt; {@link IProblem#getMessage()}</li>
	 * <li> <code>IMarker#SEVERITY</code> -&gt;
	 * <code> IMarker#SEVERITY_ERROR</code> or
	 * <code>IMarker#SEVERITY_WARNING</code> depending on
	 * {@link IProblem#isError()} or {@link IProblem#isWarning()}</li>
	 * <li> <code>IMarker#CHAR_START</code> -&gt;
	 * {@link IProblem#getSourceStart()}</li>
	 * <li> <code>IMarker#CHAR_END</code> -&gt;
	 * {@link IProblem#getSourceEnd()}</li>
	 * <li> <code>IMarker#LINE_NUMBER</code> -&gt;
	 * {@link IProblem#getSourceLineNumber()}</li>
	 * </ul>
	 * The names must be eligible for marker creation, as defined by
	 * <code>IMarker#setAttributes(String[], Object[])</code>, and there must
	 * be as many names as values according to
	 * {@link #getExtraMarkerAttributeValues()}. Note that extra marker
	 * attributes will be inserted after default ones (as described in
	 * {@link CategorizedProblem#getMarkerType()}, and thus could be used to
	 * override defaults.
	 * 
	 * @return the names of the corresponding marker attributes
	 */
	public String[] getExtraMarkerAttributeNames() {
		return CharOperation.NO_STRINGS;
	}

	/**
	 * Returns the respective values for the extra marker attributes associated
	 * to this problem when persisted into a marker. Each
	 * value must correspond to a matching attribute name, as defined by
	 * {@link #getExtraMarkerAttributeNames()}. The values must be eligible for
	 * marker creation, as defined by
	 * <code>IMarker#setAttributes(String[], Object[])</code>.
	 * 
	 * @return the values of the corresponding extra marker attributes
	 */
	public Object[] getExtraMarkerAttributeValues() {
		return DefaultProblem.EMPTY_VALUES;
	}
}
