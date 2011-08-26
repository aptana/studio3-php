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
package org2.eclipse.php.internal.core.ast.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org2.eclipse.dltk.ast.references.TypeReference;
import org2.eclipse.dltk.ast.references.VariableReference;
import org2.eclipse.php.internal.core.ast.nodes.AST;
import org2.eclipse.php.internal.core.compiler.ast.nodes.VarComment;

public class ASTUtils
{

	private static final Pattern VAR_COMMENT_PATTERN = Pattern.compile("(.*)(\\$[\\S]+)(\\s+)([\\S]+).*"); //$NON-NLS-1$

	/**
	 * Parses @@var comment using regular expressions
	 * 
	 * @param ast
	 *            An {@link AST} reference
	 * @param content
	 *            Content of the @@var comment token
	 * @param start
	 *            Token start position
	 * @param end
	 *            Token end position
	 * @return {@link VarComment}
	 */
	public static VarComment parseVarComment(AST ast, String content, int start, int end)
	{
		Matcher m = VAR_COMMENT_PATTERN.matcher(content);
		if (m.matches())
		{
			int varStart = start + m.group(1).length();
			String varName = m.group(2);
			int varEnd = varStart + varName.length();

			List<TypeReference> typeReferences = new ArrayList<TypeReference>();
			int typeStart = varEnd + m.group(3).length();
			String types = m.group(4);

			int pipeIdx = types.indexOf('|');
			while (pipeIdx >= 0)
			{
				String typeName = types.substring(0, pipeIdx);
				int typeEnd = typeStart + typeName.length();
				if (typeName.length() > 0)
				{
					typeReferences.add(new TypeReference(typeStart, typeEnd, typeName));
				}
				types = types.substring(pipeIdx + 1);
				typeStart += pipeIdx + 1;
				pipeIdx = types.indexOf('|');
			}
			String typeName = types;
			int typeEnd = typeStart + typeName.length();
			if (typeName.length() > 0)
			{
				typeReferences.add(new TypeReference(typeStart, typeEnd, typeName));
			}

			VariableReference varReference = new VariableReference(varStart, varEnd, varName);
			VarComment varComment = new VarComment(start, end, ast, varReference,
					(TypeReference[]) typeReferences.toArray(new TypeReference[typeReferences.size()]));
			return varComment;
		}
		return null;
	}
}
