package com.aptana.editor.php.internal.parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.php.internal.core.ast.nodes.Program;

import com.aptana.editor.common.parsing.CompositeParser;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.preferences.PHPVersionProvider;
import com.aptana.editor.php.internal.parser.nodes.NodeBuilder;
import com.aptana.editor.php.internal.parser.nodes.NodeBuildingVisitor;
import com.aptana.editor.php.internal.parser.nodes.PHPBlockNode;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.ParseBaseNode;
import com.aptana.parsing.ast.ParseRootNode;

/**
 * PHP and HTML composite parser.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPParser extends CompositeParser
{
	/**
	 * Constructs a new PHTMLParser
	 */
	public PHPParser()
	{
		// We create this PHTML parser with the PHTMLParserScanner, which uses the AST to parse by AST tokens, and with
		// a primary FilterHTMLParser, which parse all the non-PHP areas by filtering out any PHP blocks.
		super(new PHTMLParserScanner(), new FilterHTMLParser());
	}

	@Override
	protected IParseNode processEmbeddedlanguage(IParseState parseState) throws Exception
	{
		String source = new String(parseState.getSource());
		int startingOffset = parseState.getStartingOffset();
		IParseNode root = new ParseRootNode(PHPMimeType.MimeType, new ParseBaseNode[0], startingOffset, startingOffset
				+ source.length());
		Program ast = null;
		try
		{
			// TODO: Shalom - Get the active project and pass it to the version provider.
			ASTParser parser = ASTParser.newParser(new StringReader(source), PHPVersionProvider.getPHPVersion(null));
			ast = parser.createAST(null);
		}
		catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
			PHPEditorPlugin.logError(e);
		}
		if (ast != null)
		{
			processChildren(ast, root);
		}
		return root;
	}

	/**
	 * Override the default implementation to provide support for PHP nodes inside JavaScript.
	 */
	@Override
	public IParseNode parse(IParseState parseState) throws java.lang.Exception
	{
		String source = new String(parseState.getSource());
		fScanner.setSource(source);
		fCurrentSymbol = null;

		// first processes the embedded language
		fEmbeddedlanguageRoot = processEmbeddedlanguage(parseState);
		// then processes the source as normal
		IParseNode result = fParser.parse(parseState);

		if (fEmbeddedlanguageRoot != null)
		{
			// merges the tree for the embedded language into the result
			List<IParseNode> list = new LinkedList<IParseNode>();
			getAllNodes(result, list);

			IParseNode[] embeddedNodes = fEmbeddedlanguageRoot.getChildren();
			IParseNode parent;
			for (IParseNode node : embeddedNodes)
			{
				parent = findNode(node, list);
				if (parent == null)
				{
					// the node is at the end of the source
					result.addChild(node);
				}
				else
				{
					// Here is the change from the superclass implementation.
					// We check for JS node that is overlapping with the PHP node. If so, we replace it with the PHP
					// node instead of adding the PHP node as a new one.
					if (parent.getStartingOffset() == node.getStartingOffset()
							&& parent.getEndingOffset() == node.getEndingOffset())
					{
						int index = getIndexInParent(parent);
						result.setChildAt(index, node);
					}
					else
					{
						// In any other case, we deal with this node as in the superclass:
						// inserts the node into the right position
						List<IParseNode> newList = new ArrayList<IParseNode>();
						IParseNode[] children = parent.getChildren();
						boolean found = false;
						for (IParseNode child : children)
						{
							if (!found && child.getStartingOffset() > node.getStartingOffset())
							{
								found = true;
								newList.add(node);
							}
							newList.add(child);
						}
						if (!found)
						{
							// the node locates at the end of the parent node
							newList.add(node);
						}
						((ParseBaseNode) parent).setChildren(newList.toArray(new IParseNode[newList.size()]));
					}
				}
			}
		}

		return result;
	}

	/**
	 * Returns the index that the given node is located at his parent node.
	 * 
	 * @param node
	 * @return The index, or -1 if this node does not have any parent.
	 */
	protected int getIndexInParent(IParseNode node)
	{
		IParseNode parent = node.getParent();
		if (parent != null)
			return parent.getIndex(node);
		return -1;
	}

	/*
	 * Process the AST and update the given IParseNode
	 */
	private void processChildren(Program ast, IParseNode root)
	{
		/*
		 * kept here for Debug purposes ApplyAll astPrinter = new ApplyAll() {
		 * @Override public boolean apply(ASTNode node) { System.out.println(node.toString()); return true; } };
		 * ast.accept(astPrinter);
		 */
		NodeBuilder builderClient = new NodeBuilder();
		ast.accept(new NodeBuildingVisitor(builderClient));
		PHPBlockNode nodes = builderClient.populateNodes();
		for (IParseNode child : nodes.getChildren())
		{
			root.addChild(child);
		}
	}
}
