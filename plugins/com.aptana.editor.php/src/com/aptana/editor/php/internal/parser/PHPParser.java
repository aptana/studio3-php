package com.aptana.editor.php.internal.parser;

import java.io.StringReader;

import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.php.internal.core.ast.nodes.Program;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.IPHPVersionListener;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.internal.parser.nodes.NodeBuilder;
import com.aptana.editor.php.internal.parser.nodes.NodeBuildingVisitor;
import com.aptana.editor.php.internal.parser.nodes.PHPBlockNode;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.IParser;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.ParseBaseNode;
import com.aptana.parsing.ast.ParseRootNode;

/**
 * PHP parser.<br>
 * This parser will only deal with PHP elements. Any other HTML elements are ignored (for now).
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 * @since Aptana PHP 3.0
 */
public class PHPParser implements IParser, IPHPVersionListener
{

	private PHPVersion phpVersion;

	/**
	 * Constructs a new PHTMLParser
	 */
	public PHPParser()
	{
	}

	/**
	 * Override the default implementation to provide support for PHP nodes inside JavaScript.
	 */
	@Override
	public IParseNode parse(IParseState parseState) throws java.lang.Exception
	{
		String source = new String(parseState.getSource());
		int startingOffset = parseState.getStartingOffset();
		IParseNode root = new ParseRootNode(PHPMimeType.MimeType, new ParseBaseNode[0], startingOffset, startingOffset
				+ source.length());
		Program ast = null;
		try
		{
			PHPVersion version = (phpVersion == null) ? PHPVersionProvider.getPHPVersion(null) : phpVersion;
			ASTParser parser = ASTParser.newParser(new StringReader(source), version);
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
		parseState.setParseResult(root);
		return root;
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

	@Override
	public void phpVersionChanged(PHPVersion newVersion)
	{
		this.phpVersion = newVersion;
	}
}
