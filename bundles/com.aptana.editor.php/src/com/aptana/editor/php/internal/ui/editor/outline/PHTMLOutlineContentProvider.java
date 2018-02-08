package com.aptana.editor.php.internal.ui.editor.outline;

import java.util.ArrayList;
import java.util.List;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.outline.CommonOutlineItem;
import com.aptana.editor.html.outline.HTMLOutlineContentProvider;
import com.aptana.editor.html.parsing.ast.HTMLTextNode;
import com.aptana.editor.php.internal.parser.nodes.IPHPParseNode;
import com.aptana.parsing.ast.IParseNode;

/**
 * An outline content provider for PHTML (PHP & HTML) content.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHTMLOutlineContentProvider extends HTMLOutlineContentProvider
{

	public PHTMLOutlineContentProvider(AbstractThemeableEditor editor)
	{
		super(editor);
	}

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
		filterRecursively(nodes, list);
		return list.toArray(new CommonOutlineItem[list.size()]);
	}

	private void filterRecursively(IParseNode[] nodes, List<CommonOutlineItem> list)
	{
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
				else
				{
					// the node may have children that we don't want to filter (like a function within an 'if'
					// statement).
					filterRecursively(node.getChildren(), list);
				}
			}
			else if (!(node instanceof HTMLTextNode))
			{
				list.add(getOutlineItem(node));
			}
		}
	}
}
