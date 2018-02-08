/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

/**
 * PHP 'for' parse node.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class PHPForNode extends PHPNonOutlineParseNode
{
	public enum FOR_TYPE
	{
		FOR("for"), FOREACH("foreach"); //$NON-NLS-1$ //$NON-NLS-2$

		private String typeName;

		FOR_TYPE(String typeName)
		{
			this.typeName = typeName;
		}

		public String toString()
		{
			return typeName;
		}
	};

	private FOR_TYPE type;

	/**
	 * Constructs a new PHPForNode.
	 * 
	 * @param start
	 * @param end
	 */
	public PHPForNode(int start, int end, FOR_TYPE type)
	{
		super(IPHPParseNode.FOR_NODE, 0, start, end, type.toString());
		this.type = type;
	}

	/**
	 * Returns the type of the 'for' loop.
	 * 
	 * @return A {@link FOR_TYPE}
	 */
	public FOR_TYPE getType()
	{
		return type;
	}
}
