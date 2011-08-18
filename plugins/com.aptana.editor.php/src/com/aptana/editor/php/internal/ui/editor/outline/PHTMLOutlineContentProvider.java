package com.aptana.editor.php.internal.ui.editor.outline;

import java.util.ArrayList;
import java.util.List;

import com.aptana.editor.common.outline.CommonOutlineItem;
import com.aptana.editor.html.outline.HTMLOutlineContentProvider;
import com.aptana.editor.php.internal.parser.nodes.IPHPParseNode;
import com.aptana.parsing.ast.IParseNode;

/**
 * An outline content provider for PHTML (PHP & HTML) content.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHTMLOutlineContentProvider extends HTMLOutlineContentProvider
{

	@Override
	public CommonOutlineItem getOutlineItem(IParseNode node)
	{
		if (node == null)
		{
			return null;
		}
		if (node instanceof IPHPParseNode)
		{
			return new PHPOutlineItem(node.getNameNode().getNameRange(), node);
		}
		else
		{
			return super.getOutlineItem(node);
		}
	}

	@Override
	protected Object[] filter(IParseNode[] nodes)
	{
		List<CommonOutlineItem> list = new ArrayList<CommonOutlineItem>();
		IPHPParseNode element;
		for (IParseNode node : nodes)
		{
			if (node instanceof IPHPParseNode)
			{
				element = (IPHPParseNode) node;
				// filters out any item that should not be in the outline
				if (!element.isFilteredFromOutline())
				{
					list.add(getOutlineItem(element));
				}
			}
			else
			{
				list.add(getOutlineItem(node));
			}
		}
		return list.toArray(new CommonOutlineItem[list.size()]);
	}
}
