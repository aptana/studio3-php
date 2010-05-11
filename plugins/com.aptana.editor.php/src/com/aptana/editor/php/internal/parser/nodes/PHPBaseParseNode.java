/**
 * Copyright (c) 2005-2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

import org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock;

import com.aptana.editor.php.internal.parser.PHPMimeType;
import com.aptana.parsing.ast.ParseBaseNode;

/**
 * PHP base ParseNode
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPBaseParseNode extends ParseBaseNode implements IPHPParseNode
{
	private static final String EMPTY = " "; //$NON-NLS-1$
	protected String name;
	private int modifiers;
	private PHPDocBlock documentation;

	/**
	 * Constructs a new PHPBaseParseNode
	 */
	public PHPBaseParseNode()
	{
		super(PHPMimeType.MimeType);
		name = EMPTY;
	}

	/**
	 * Constructs a new PHPBaseParseNode
	 * 
	 * @param typeIndex
	 * @param modifiers
	 * @param startOffset
	 * @param endOffset
	 * @param name
	 */
	public PHPBaseParseNode(int typeIndex, int modifiers, int startOffset, int endOffset, String name)
	{
		super(PHPMimeType.MimeType);
		this.name = name.length() != 0 ? name : " "; //$NON-NLS-1$
		// this.startOffset = startOffset;
		// this.endOffset = endOffset >= startOffset ? endOffset : startOffset;
		this.start = startOffset;
		this.end = endOffset >= startOffset ? endOffset : startOffset;
		this.modifiers = modifiers;
	}

	/**
	 * @param docInfo
	 */
	public void setDocumentation(PHPDocBlock docInfo)
	{
		this.documentation = docInfo;
	}

	/**
	 * @return documentation block or null
	 */

	public PHPDocBlock getDocumentation()
	{
		return documentation;
	}

	/**
	 * @return node name
	 */
	public String getNodeName()
	{
		return name;
	}

	/**
	 * Set the node name.
	 * 
	 * @param name
	 */
	protected void setNodeName(String name)
	{
		this.name = name;
	}

	/**
	 * Determines if this is an empty node
	 * 
	 * @return Returns true if this is an empty node
	 */
	public boolean isEmpty()
	{
		return getChildrenCount() == 0;
	}

	/**
	 * @return modifiers
	 */
	public int getModifiers()
	{
		return modifiers;
	}

	/**
	 * @param endOffset
	 */
	public void setEndOffset(int endOffset)
	{
		this.end = endOffset;
	}

	public boolean containsOffset(int offset)
	{
		if (getStart() <= offset && getEnd() >= offset)
		{
			return true;
		}
		return false;
	}
	
	public String toString() {
		return getNodeName();
	}
}
