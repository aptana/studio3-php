/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.nodes;

/**
 * A class that holds definitions for arbitrary node types, such as punctuation and operators types.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class NodeTypes
{
	/**
	 * Supported node types for punctuation.
	 */
	public enum TypePunctuation
	{
		COLON(":"), //$NON-NLS-1$
		CASE_COLON(":"), //$NON-NLS-1$
		SEMICOLON(";"), //$NON-NLS-1$
		COMMA(","), //$NON-NLS-1$
		NAMESPACE_SEPARATOR("\\"); //$NON-NLS-1$

		String name;

		TypePunctuation(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}
	};

	/**
	 * Supported node types for operators.
	 */
	public enum TypeOperator
	{
		ASSIGNMENT("="), //$NON-NLS-1$
		DOT("."), //$NON-NLS-1$
		DOT_EQUAL(".="), //$NON-NLS-1$
		PLUS_EQUAL("+="), //$NON-NLS-1$
		MINUS_EQUAL("-="), //$NON-NLS-1$
		MULTIPLY_EQUAL("*="), //$NON-NLS-1$
		DIVIDE_EQUAL("/="), //$NON-NLS-1$
		MULTIPLY("*"), //$NON-NLS-1$
		PLUS("+"), //$NON-NLS-1$
		MINUS("+"), //$NON-NLS-1$
		DIVIDE("/"), //$NON-NLS-1$
		INCREMENT("++"), //$NON-NLS-1$
		DECREMENT("--"), //$NON-NLS-1$
		OR("||"), //$NON-NLS-1$
		AND("&&"), //$NON-NLS-1$
		XOR("^"), //$NON-NLS-1$
		BINARY_OR("|"), //$NON-NLS-1$
		BINARY_AND("&"), //$NON-NLS-1$
		BINARY_OR_EQUAL("|="), //$NON-NLS-1$
		BINARY_AND_EQUAL("&="), //$NON-NLS-1$
		EQUALITY("=="), //$NON-NLS-1$
		TYPE_EQUAL("==="), //$NON-NLS-1$
		TILDA("~"), //$NON-NLS-1$
		NOT("!"), //$NON-NLS-1$
		NOT_EQUAL("!="), //$NON-NLS-1$
		NOT_TYPE_EQUAL("!=="), //$NON-NLS-1$
		ARROW("->"), //$NON-NLS-1$
		STATIC_INVOCATION("::"); //$NON-NLS-1$

		String name;

		TypeOperator(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}

		/**
		 * Returns a {@link TypeOperator} by a string.
		 * 
		 * @param operationString
		 * @return The matching {@link TypeOperator}; Null, if no match was found.
		 */
		public static TypeOperator getTypeOperator(String operationString)
		{
			TypeOperator[] values = values();
			for (TypeOperator operator : values)
			{
				if (operator.toString().equals(operationString))
				{
					return operator;
				}
			}
			return null;
		}
	};
}
