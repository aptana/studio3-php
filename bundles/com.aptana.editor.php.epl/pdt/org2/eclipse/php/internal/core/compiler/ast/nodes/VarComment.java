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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org2.eclipse.dltk.ast.references.TypeReference;
import org2.eclipse.dltk.ast.references.VariableReference;
import org2.eclipse.php.internal.core.ast.nodes.AST;
import org2.eclipse.php.internal.core.ast.nodes.Comment;

public class VarComment extends Comment
{

	private VariableReference variableReference;
	private TypeReference[] typeReference;

	public VarComment(int start, int end, AST ast, VariableReference variableReference, TypeReference[] typeReference)
	{
		super(start, end, ast, Comment.TYPE_MULTILINE);
		this.variableReference = variableReference;
		this.typeReference = typeReference;
	}

	public VariableReference getVariableReference()
	{
		return variableReference;
	}

	public TypeReference[] getTypeReferences()
	{
		return typeReference;
	}

	/**
	 * Returns the type references as a String array.<br>
	 * [Appcelerator Mod]
	 * 
	 * @return A string array with the references names.
	 */
	public Set<Object> getTypeReferencesNames()
	{
		if (typeReference == null)
		{
			return Collections.emptySet();
		}
		Set<Object> names = new HashSet<Object>();
		for (TypeReference ref : typeReference)
		{
			names.add(ref.getName());
		}
		return names;
	}
}
