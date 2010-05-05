package com.aptana.editor.php.internal.parser;

import com.aptana.parsing.IParseState;
import com.aptana.parsing.IParser;
import com.aptana.parsing.ast.IParseNode;

public class PHPParser implements IParser
{

	private PHPSourceParser fParser;

	public PHPParser()
	{
		fParser = new PHPSourceParser();
	}

	@Override
	public IParseNode parse(IParseState parseState) throws Exception
	{
//		String source = new String(parseState.getSource());
//		PHPScript root = new PHPScript(parseState.getStartingOffset(), parseState.getStartingOffset()
//				+ source.length());
//		PHPStructureBuilder builder = new PHPStructureBuilder(root);
//		SourceElementVisitor visitor = new SourceElementVisitor(builder);
//		visitor.acceptNode(fParser.parse(source).getAST());
//		parseState.setParseResult(root);

//		return root;
		// TODO - Shalom: Convert the PDT ASTNode to IParseNode ?
		return null;
	}
}
