/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.core.documentModel.phpElementData;

import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PHPDocBlockImp implements IPHPDocBlock
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_DESCRIPTION_TEXT = "Enter description here..."; //$NON-NLS-1$

	private String shortDescription;
	private String longDescription;
	private IPHPDocTag[] tags;
	private int type;
	private int startPosition;
	private int endPosition;

	private String content;

	public PHPDocBlockImp(String shortDescription, String longDescription, IPHPDocTag[] tags, int type)
	{
		this.shortDescription = shortDescription;
		this.longDescription = longDescription;
		this.tags = tags;
		this.type = type;
	}

	public String getShortDescription()
	{
		if (shortDescription == null)
		{
			return DEFAULT_DESCRIPTION_TEXT;
		}
		return shortDescription;
	}

	public String getLongDescription()
	{
		if (longDescription == null)
		{
			return ""; //$NON-NLS-1$
		}
		return longDescription;
	}

	public IPHPDocTag[] getTagsAsArray()
	{
		return tags;
	}

	public IPHPDocTag[] getTags()
	{
		return tags;
	}

	public Iterator getTags(int id)
	{
		if (tags == null)
		{
			return null;
		}
		ArrayList rv = new ArrayList(tags.length);
		for (int i = 0; i < tags.length; i++)
		{
			IPHPDocTag tag = tags[i];
			if (tag.getID() == id)
			{
				rv.add(tag);
			}
		}
		return rv.iterator();
	}

	public int getType()
	{
		return type;
	}

	public void setStartPosition(int value)
	{
		startPosition = value;
	}

	public void setEndPosition(int value)
	{
		endPosition = value;
	}

	public void setShortDescription(String shortDescription)
	{
		this.shortDescription = shortDescription;
	}

	public int getStartPosition()
	{
		return startPosition;
	}

	public int getEndPosition()
	{
		return endPosition;
	}

	public boolean containsPosition(int position)
	{
		return position > getStartPosition() && position <= getEndPosition();
	}

	public boolean hasTagOf(int id)
	{
		final Iterator tagsOf = getTags(id);
		return tagsOf != null && tagsOf.hasNext();
	}

	public void setContent(String comment)
	{
		this.content = comment;
	}

	public String getContent()
	{
		return this.content;
	}

}
