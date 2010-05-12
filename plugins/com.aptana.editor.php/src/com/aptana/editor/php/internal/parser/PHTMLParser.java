package com.aptana.editor.php.internal.parser;

import java.io.StringReader;

import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.php.internal.core.ast.nodes.Program;

import com.aptana.editor.common.parsing.CompositeParser;
import com.aptana.editor.html.parsing.HTMLParser;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.preferences.PHPVersionProvider;
import com.aptana.editor.php.internal.parser.nodes.NodeBuilderClient;
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
public class PHTMLParser extends CompositeParser
{
	/**
	 * Constructs a new PHTMLParser
	 */
	public PHTMLParser()
	{
		super(new PHTMLParserScanner(), new HTMLParser());
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

	/*
	 * Process the AST and update the given IParseNode
	 */
	private void processChildren(Program ast, IParseNode root)
	{
		/* kept here for Debug purposes
		ApplyAll astPrinter = new ApplyAll()
		{
			@Override
			public boolean apply(ASTNode node)
			{
				System.out.println(node.toString());
				return true;
			}
		};
		ast.accept(astPrinter);
		*/
		NodeBuilderClient builderClient = new NodeBuilderClient();
		ast.accept(new NodeBuildingVisitor(builderClient));
		PHPBlockNode nodes = builderClient.populateNodes();
	    for (IParseNode child : nodes.getChildren())
	    {
	    	root.addChild(child);
	    }
	}
}
