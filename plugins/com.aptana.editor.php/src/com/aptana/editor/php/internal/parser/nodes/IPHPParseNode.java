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

	/**
	 * TRY_NODE
	 */
	public static final short TRY_NODE = 4511;

	/**
	 * CATCH_NODE
	 */
	public static final short CATCH_NODE = 4512;

	/**
	 * DO_NODE
	 */
	public static final short DO_NODE = 4513;

	/**
	 * FOR_NODE
	 */
	public static final short FOR_NODE = 4514;

	/**
	 * SWITCH_NODE
	 */
	public static final short SWITCH_NODE = 4515;

	/**
	 * SWITCH_CASE_NODE
	 */
	public static final short SWITCH_CASE_NODE = 4516;

	/**
	 * WHILE_NODE
	 */
	public static final short WHILE_NODE = 4517;

	/**
	 * IF_ELSE_NODE
	 */
	public static final short IF_ELSE_NODE = 4518;
	
	/**
	 * TRAIT_NODE
	 */
	public static final short TRAIT_NODE = 4519;

	public void setDocumentation(IPHPDocBlock docInfo);

	public IPHPDocBlock getDocumentation();

	public String getNodeName();

	public boolean isEmpty();

	public void setEndOffset(int endOffset);

	public void setStartOffset(int startOffset);

	public boolean containsOffset(int offset);

	void setNameNode(String name, int startOffset, int endOffset);
}
