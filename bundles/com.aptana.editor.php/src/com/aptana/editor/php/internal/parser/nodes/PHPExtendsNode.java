package com.aptana.editor.php.internal.parser.nodes;

import com.aptana.parsing.ast.IParseNode;

/**
 * node for extend elements in outline
 * 
 * @author Pavel Petrochenko
 */
public class PHPExtendsNode extends PHPBaseParseNode /* implements IResolvableItem */
{

	/**
	 * constructor for extends node
	 */
	public PHPExtendsNode(int modifiers, int startOffset, int endOffset, String name)
	{
		super(PHPBaseParseNode.CLASS_NODE, modifiers, startOffset, endOffset, name);
	}

	/**
	 * returns editor input that corresponds to this extends node
	 */
    /*
	public IEditorInput getEditorInput()
	{
		ExternalReference resolveType = PHPSearchEngine.getInstance().resolveClassToReference(getNodeName());
		if (resolveType != null)
		{
			return resolveType.editorInput;
		}

		return null;
	}
    */
	// /**
	// * returns editor input that corresponds to this extends node
	// */
	// public IResolvableItem getParentItem() {
	// return null;
	// }

	/**
	 * returns true
	 */
	public boolean isResolvable()
	{
		return true;
	}

	/**
	 * returns true
	 */
	public boolean stillHighlight()
	{
		return true;
	}

	/**
	 * returns children of the given extends node
	 */
	public IParseNode[] getExtendsChildren()
	{
		/*
		PHPBaseParseNode resolveType = PHPSearchEngine.getInstance().resolveClass(getNodeName());
		if (resolveType != null)
		{
			IParseNode[] children = resolveType.getChildren();
			return children;
		}
		*/
		return new PHPBaseParseNode[] { new PHPBaseParseNode((short)0, 0, 0, 0, Messages.PHPExtendsNode_NonOnBuildPath0) };
	}
}
