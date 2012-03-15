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
package org2.eclipse.php.internal.core.compiler.ast.nodes;

import java.util.LinkedList;
import java.util.List;

import com.aptana.editor.php.epl.PHPEplPlugin;

import org2.eclipse.dltk.ast.references.SimpleReference;
import org2.eclipse.dltk.ast.references.TypeReference;
import org2.eclipse.dltk.ast.references.VariableReference;
import org2.eclipse.php.internal.core.ast.nodes.AST;
import org2.eclipse.php.internal.core.ast.nodes.Comment;
import org2.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocTag;

public class PHPDocTag extends Comment implements IPHPDocTag {

	private static final long serialVersionUID = 7543654184965368295L;
	
	public static final String ERROR = "ERROR!!!"; //$NON-NLS-1$
	public static final String THROWS_NAME = "throws"; //$NON-NLS-1$
	public static final String VERSION_NAME = "version"; //$NON-NLS-1$
	public static final String USES_NAME = "uses"; //$NON-NLS-1$
	public static final String TUTORIAL_NAME = "tutorial"; //$NON-NLS-1$
	public static final String SUBPACKAGE_NAME = "subpackage"; //$NON-NLS-1$
	public static final String SINCE_NAME = "since"; //$NON-NLS-1$
	public static final String LINK_NAME = "link"; //$NON-NLS-1$
	public static final String LICENSE_NAME = "license"; //$NON-NLS-1$
	public static final String INTERNAL_NAME = "internal"; //$NON-NLS-1$
	public static final String IGNORE_NAME = "ignore"; //$NON-NLS-1$
	public static final String FILESOURCE_NAME = "filesource"; //$NON-NLS-1$
	public static final String EXAMPLE_NAME = "example"; //$NON-NLS-1$
	public static final String DESC_NAME = "desc"; //$NON-NLS-1$
	public static final String COPYRIGHT_NAME = "copyright"; //$NON-NLS-1$
	public static final String CATEGORY_NAME = "category"; //$NON-NLS-1$
	public static final String ACCESS_NAME = "access"; //$NON-NLS-1$
	public static final String PACKAGE_NAME = "package"; //$NON-NLS-1$
	public static final String VAR_NAME = "var"; //$NON-NLS-1$
	public static final String TODO_NAME = "todo"; //$NON-NLS-1$
	public static final String STATICVAR_NAME = "staticvar"; //$NON-NLS-1$
	public static final String STATIC_NAME = "static"; //$NON-NLS-1$
	public static final String SEE_NAME = "see"; //$NON-NLS-1$
	public static final String PARAM_NAME = "param"; //$NON-NLS-1$
	public static final String RETURN_NAME = "return"; //$NON-NLS-1$
	public static final String NAME_NAME = "name"; //$NON-NLS-1$
	public static final String GLOBAL_NAME = "global"; //$NON-NLS-1$
	public static final String FINAL_NAME = "final"; //$NON-NLS-1$
	public static final String DEPRECATED_NAME = "deprecated"; //$NON-NLS-1$
	public static final String AUTHOR_NAME = "author"; //$NON-NLS-1$
	public static final String ABSTRACT_NAME = "abstract"; //$NON-NLS-1$
	public static final String PROPERTY_NAME = "property"; //$NON-NLS-1$
	public static final String PROPERTY_READ_NAME = "property-read"; //$NON-NLS-1$
	public static final String PROPERTY_WRITE_NAME = "property-write"; //$NON-NLS-1$
	public static final String METHOD_NAME = "method"; //$NON-NLS-1$
	
	private static final SimpleReference[] EMPTY = {};
	private final int tagKind;
	private String value;
	private SimpleReference[] references;

	public PHPDocTag(int start, int end, AST ast, int tagKind, String value) {
		super(start, end, ast, PHPDocTag.TYPE_PHPDOC);
		this.tagKind = tagKind;
		this.value = PHPEplPlugin.getDefault().sharedString(value);
		updateReferences(start, end);
	}

	private static int getNonWhitespaceIndex(String line, int startIndex) {
		int i = startIndex;
		for (; i < line.length(); ++i) {
			if (!Character.isWhitespace(line.charAt(i))) {
				return i;
			}
		}
		return i;
	}

	private static int getWhitespaceIndex(String line, int startIndex) {
		int i = startIndex;
		for (; i < line.length(); ++i) {
			if (Character.isWhitespace(line.charAt(i))) {
				return i;
			}
		}
		return i;
	}
	
	private static int getClassStartIndex(String line, int startIndex) {
		int i = startIndex;
		for (; i < line.length(); ++i) {
			if (line.charAt(i) != '|') {
				return i;
			}
		}
		return i;
	}
	
	private static int getClassEndIndex(String line, int startIndex) {
		int i = startIndex;
		for (; i < line.length(); ++i) {
			if (line.charAt(i) == '|') {
				return i;
			}
		}
		return i;
	}

	private void updateReferences(int start, int end) {
		
		int valueStart = start + getTagKind(tagKind).length() + 1;
		
		if (tagKind == PHPDocTagKinds.RETURN || tagKind == PHPDocTagKinds.VAR || tagKind == PHPDocTagKinds.THROWS) {
			
			int wordStart = getNonWhitespaceIndex(value, 0);
			int wordEnd = getWhitespaceIndex(value, wordStart);
			if (wordStart < wordEnd) {
				
				String word = value.substring(wordStart, wordEnd);

				int classStart = getClassStartIndex(word, 0);
				int classEnd = getClassEndIndex(word, classStart);
				List<TypeReference> types = new LinkedList<TypeReference>();
				
				while (classStart < classEnd) {
					String className = word.substring(classStart, classEnd);
					types.add(new TypeReference(valueStart + wordStart + classStart, valueStart + wordStart + classEnd, className));
					
					classStart = getClassStartIndex(word, classEnd);
					classEnd = getClassEndIndex(word, classStart);
				}
				if (types.size() > 0) {
					references = types.toArray(new TypeReference[types.size()]);
				}
			}
		} else if (tagKind == PHPDocTagKinds.PARAM) {
			
			int firstWordStart = getNonWhitespaceIndex(value, 0);
			int firstWordEnd = getWhitespaceIndex(value, firstWordStart);
			if (firstWordStart < firstWordEnd) {
				
				int secondWordStart = getNonWhitespaceIndex(value, firstWordEnd);
				int secondWordEnd = getWhitespaceIndex(value, secondWordStart);
				if (secondWordStart < secondWordEnd) {

					String firstWord = value.substring(firstWordStart, firstWordEnd);
					String secondWord = value.substring(secondWordStart, secondWordEnd);
					if (firstWord.charAt(0) == '$') {
						references = new SimpleReference[2];
						references[0] = new VariableReference(valueStart + firstWordStart, valueStart + firstWordEnd, firstWord);
						references[1] = new TypeReference(valueStart + secondWordStart, valueStart + secondWordEnd, secondWord);
					} else if (secondWord.charAt(0) == '$') {
						references = new SimpleReference[2];
						references[0] = new VariableReference(valueStart + secondWordStart, valueStart + secondWordEnd, secondWord);
						references[1] = new TypeReference(valueStart + firstWordStart, valueStart + firstWordEnd, firstWord);
					}
				}
			}
		}
		if (references == null) {
			references = EMPTY;
		}
	}

	public int getTagKind() {
		return this.tagKind;
	}

	public String getValue() {
		return value;
	}

	public SimpleReference[] getReferences() {
		return references;
	}

//	public void adjustStart(int start) {
//		setStart(sourceStart() + start);
//		setEnd(sourceEnd() + start);
//	}

	public static String getTagKind(int kind) {
		switch (kind) {
			case PHPDocTagKinds.ABSTRACT:
				return ABSTRACT_NAME;
			case PHPDocTagKinds.AUTHOR:
				return AUTHOR_NAME;
			case PHPDocTagKinds.DEPRECATED:
				return DEPRECATED_NAME;
			case PHPDocTagKinds.FINAL:
				return FINAL_NAME;
			case PHPDocTagKinds.GLOBAL:
				return GLOBAL_NAME;
			case PHPDocTagKinds.NAME:
				return NAME_NAME;
			case PHPDocTagKinds.RETURN:
				return RETURN_NAME;
			case PHPDocTagKinds.PARAM:
				return PARAM_NAME;
			case PHPDocTagKinds.SEE:
				return SEE_NAME;
			case PHPDocTagKinds.STATIC:
				return STATIC_NAME;
			case PHPDocTagKinds.STATICVAR:
				return STATICVAR_NAME;
			case PHPDocTagKinds.TODO:
				return TODO_NAME;
			case PHPDocTagKinds.VAR:
				return VAR_NAME;
			case PHPDocTagKinds.PACKAGE:
				return PACKAGE_NAME;
			case PHPDocTagKinds.ACCESS:
				return ACCESS_NAME;
			case PHPDocTagKinds.CATEGORY:
				return CATEGORY_NAME;
			case PHPDocTagKinds.COPYRIGHT:
				return COPYRIGHT_NAME;
			case PHPDocTagKinds.DESC:
				return DESC_NAME;
			case PHPDocTagKinds.EXAMPLE:
				return EXAMPLE_NAME;
			case PHPDocTagKinds.FILESOURCE:
				return FILESOURCE_NAME;
			case PHPDocTagKinds.IGNORE:
				return IGNORE_NAME;
			case PHPDocTagKinds.INTERNAL:
				return INTERNAL_NAME;
			case PHPDocTagKinds.LICENSE:
				return LICENSE_NAME;
			case PHPDocTagKinds.LINK:
				return LINK_NAME;
			case PHPDocTagKinds.SINCE:
				return SINCE_NAME;
			case PHPDocTagKinds.SUBPACKAGE:
				return SUBPACKAGE_NAME;
			case PHPDocTagKinds.TUTORIAL:
				return TUTORIAL_NAME;
			case PHPDocTagKinds.USES:
				return USES_NAME;
			case PHPDocTagKinds.VERSION:
				return VERSION_NAME;
			case PHPDocTagKinds.THROWS:
				return THROWS_NAME;
			case PHPDocTagKinds.PROPERTY:
				return PROPERTY_NAME;
			case PHPDocTagKinds.PROPERTY_READ:
				return PROPERTY_READ_NAME;
			case PHPDocTagKinds.PROPERTY_WRITE:
				return PROPERTY_WRITE_NAME;
			case PHPDocTagKinds.METHOD:
				return METHOD_NAME;
		}
		return ERROR;
	}

	/**
	 * Returns {@link #getTagKind()} for this doc tag type.
	 */
	public int getID()
	{
		return getTagKind();
	}
}
