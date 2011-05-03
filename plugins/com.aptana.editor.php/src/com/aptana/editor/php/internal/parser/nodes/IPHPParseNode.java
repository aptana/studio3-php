package com.aptana.editor.php.internal.parser.nodes;

import org2.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocBlock;

import com.aptana.parsing.ast.IParseNode;

public interface IPHPParseNode extends IParseNode
{
	/**
	 * CLASS_NODE
	 */
	public static final short CLASS_NODE = 4500;

	/**
	 * FUNCTION_NODE
	 */
	public static final short FUNCTION_NODE = 4501;

	/**
	 * VAR_NODE
	 */
	public static final short VAR_NODE = 4502;

	/**
	 * CONST_NODE
	 */
	public static final short CONST_NODE = 4503;

	/**
	 * BLOCK_NODE
	 */
	public static final short BLOCK_NODE = 4504;

	/**
	 * KEYWORD_NODE
	 */
	public static final short KEYWORD_NODE = 4505;

	/**
	 * IMPORT_NODE
	 */
	public static final short IMPORT_NODE = 4506;

	/**
	 * USE_NODE
	 */
	public static final short USE_NODE = 4507;

	/**
	 * NAMESPACE_NODE
	 */
	public static final short NAMESPACE_NODE = 4508;

	/**
	 * HTML_NODE
	 */
	public static final short HTML_NODE = 4509;

	/**
	 * COMMENT_NODE
	 */
	public static final short COMMENT_NODE = 4510;

	public void setDocumentation(IPHPDocBlock docInfo);

	public IPHPDocBlock getDocumentation();

	public String getNodeName();

	public boolean isEmpty();

	public void setEndOffset(int endOffset);

	public void setStartOffset(int startOffset);

	public boolean containsOffset(int offset);

	void setNameNode(String name, int startOffset, int endOffset);
}
