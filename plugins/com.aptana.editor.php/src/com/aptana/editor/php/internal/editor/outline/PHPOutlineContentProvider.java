package com.aptana.editor.php.internal.editor.outline;

import java.util.ArrayList;
import java.util.List;

import com.aptana.editor.common.outline.CommonOutlineContentProvider;
import com.aptana.editor.common.outline.CommonOutlineItem;
import com.aptana.editor.php.internal.parser.nodes.IPHPParseNode;
import com.aptana.parsing.ast.IParseNode;

/**
 * PHP outline content provider.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPOutlineContentProvider extends CommonOutlineContentProvider
{
	
	/* (non-Javadoc)
	 * @see com.aptana.editor.common.outline.CommonOutlineContentProvider#getOutlineItem(com.aptana.parsing.ast.IParseNode)
	 */
	@Override
	public CommonOutlineItem getOutlineItem(IParseNode node)
	{
		if (node == null)
		{
			return null;
		}
		return new PHPOutlineItem(node.getNameNode().getNameRange(), node);
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
				// filters out block elements
				if (element.getType() != IPHPParseNode.BLOCK_NODE)
				{
					list.add(getOutlineItem(element));
				}
			}
		}
		return list.toArray(new CommonOutlineItem[list.size()]);
	}
}
