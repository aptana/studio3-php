/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import com.aptana.parsing.ast.IParseNode;

/**
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class ParseNodesIterator implements Iterator<IParseNode>
{
	private Stack<IParseNode> stack;
	private Set<IParseNode> visited;

	private IParseNode next;

	public ParseNodesIterator(IParseNode root)
	{
		stack = new Stack<IParseNode>();
		visited = new HashSet<IParseNode>();
		if (root != null)
		{
			stack.push(root);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		return !stack.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public IParseNode next()
	{
		iterateToNext();
		return next;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
		// Not implemented
	}

	/**
	 * Iterate to the next node.
	 */
	private void iterateToNext()
	{
		if (stack.isEmpty())
		{
			next = null;
			return;
		}
		next = stack.pop();

		while (next.hasChildren() && !visited.contains(next))
		{
			visited.add(next);
			// push it bask it and add its children to the stack
			stack.push(next);
			IParseNode[] children = next.getChildren();
			for (int i = children.length - 1; i >= 0; i--)
			{
				stack.push(children[i]);
			}
			next = stack.pop();
		}
		visited.remove(next);
	}
}
