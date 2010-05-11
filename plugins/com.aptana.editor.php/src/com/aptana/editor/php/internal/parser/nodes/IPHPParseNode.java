package com.aptana.editor.php.internal.parser.nodes;

import org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock;

import com.aptana.parsing.ast.IParseNode;

public interface IPHPParseNode extends IParseNode
{
	/**
	 * CLASS_NODE
	 */
	public static final int CLASS_NODE = 450000;

	/**
	 * FUNCTION_NODE
	 */
	public static final int FUNCTION_NODE = 450001;

	/**
	 * VAR_NODE
	 */
	public static final int VAR_NODE = 450002;

	/**
	 * CONST_NODE
	 */
	public static final int CONST_NODE = 450003;

	/**
	 * BLOCK_NODE
	 */
	public static final int BLOCK_NODE = 450004;

	/**
	 * KEYWORD_NODE
	 */
	public static final int KEYWORD_NODE = 450005;

	/**
	 * IMPORT_NODE
	 */
	public static final int IMPORT_NODE = 450006;

	/**
	 * USE_NODE
	 */
	public static final int USE_NODE = 450007;

	/**
	 * NAMESPACE_NODE
	 */
	public static final int NAMESPACE_NODE = 450008;

	public void setDocumentation(PHPDocBlock docInfo);

	public PHPDocBlock getDocumentation();

	public String getNodeName();

	public boolean isEmpty();

	public void setEndOffset(int endOffset);

	public boolean containsOffset(int offset);
}
