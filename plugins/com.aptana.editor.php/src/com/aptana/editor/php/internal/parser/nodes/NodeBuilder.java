/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org2.eclipse.php.core.compiler.PHPFlags;
import org2.eclipse.php.internal.core.ast.nodes.Identifier;
import org2.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocBlock;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.html.IHTMLConstants;
import com.aptana.editor.html.parsing.HTMLParseState;
import com.aptana.editor.html.parsing.ast.HTMLElementNode;
import com.aptana.editor.html.parsing.ast.HTMLTextNode;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.parsing.ParseState;
import com.aptana.parsing.ParserPoolFactory;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.IParseRootNode;
import com.aptana.parsing.ast.ParseNode;

/**
 * PHP Node Builder.<br>
 * This builder is used by the {@link NodeBuildingVisitor} when a PHP node is to be created.<br>
 * The builder also parses the HTML parts in the source, and creates a nodes tree that is a composition of the PHP nodes
 * and the HTML nodes.<br>
 * Once the node building is done, calling the {@link #populateNodes()} will return a root parse node that holds the
 * nodes tree.
 * 
 * @author Pavel Petrochenko, Shalom Gibly
 */
public class NodeBuilder
{
	private IPHPParseNode current;
	private IPHPParseNode root;
	private Stack<Object> stack = new Stack<Object>();
	private List<Object> phpStarts = new ArrayList<Object>();
	private List<Object> phpEnds = new ArrayList<Object>();
	private List<Parameter> parameters = new ArrayList<Parameter>();

	/**
	 * Whether to collect variables.
	 */
	private boolean collectVariables = false;
	private boolean hasSyntaxErrors;
	private String source;
	private boolean parseHTML;

	public boolean hasSyntaxErrors()
	{
		return hasSyntaxErrors;
	}

	/**
	 * Constructs a new NodeBuilder
	 * 
	 * @param source
	 * @param root
	 */
	public NodeBuilder(String source, IPHPParseNode root)
	{
		this.source = source;
		this.current = root;
		this.root = root;
		this.parseHTML = true;
	}

	/**
	 * Constructs a new NodeBuilder
	 * 
	 * @param source
	 */
	public NodeBuilder(String source)
	{
		this(source, new PHPBaseParseNode((short) 0, 0, 0, 0, StringUtil.EMPTY));
	}

	/**
	 * Constructs a new NodeBuilder
	 * 
	 * @param source
	 * @param collectVariables
	 *            - whether to collect variables.
	 * @param parseHTML
	 *            - whether
	 */
	public NodeBuilder(String source, boolean collectVariables, boolean parseHTML)
	{
		this(source);
		this.collectVariables = collectVariables;
		this.parseHTML = parseHTML;
	}

	public void handleClassConstDeclaration(String constName, IPHPDocBlock docInfo, int startPosition, int endPosition,
			int stopPosition)
	{
		PHPVariableParseNode pn = new PHPVariableParseNode(0, startPosition, endPosition, constName);
		pn.setField(true);
		current.addChild(pn);
	}

	public void handleUse(String useName, String useAs, int startPosition, int stopPosition)
	{
		PHPUseNode un = new PHPUseNode(startPosition, stopPosition, useName, "use"); //$NON-NLS-1$
		un.setNameNode(useName, startPosition, stopPosition);
		current.addChild(un);
	}

	public void handleNamespaceDeclaration(String namespaceName, int startPosition, int endPosition, int stopPosition)
	{
		PHPNamespaceNode un = new PHPNamespaceNode(startPosition, endPosition, namespaceName, StringUtil.EMPTY);
		un.setNameNode(namespaceName, startPosition, stopPosition);
		pushNode(un);
	}

	/**
	 * Handle class declaration.
	 * 
	 * @param className
	 * @param modifier
	 * @param docInfo
	 * @param startPosition
	 * @param endPosition
	 * @param lineNumber
	 */
	public void handleClassDeclaration(String className, int modifier, IPHPDocBlock docInfo, int startPosition,
			int endPosition, int lineNumber)
	{
		PHPClassParseNode pn = new PHPClassParseNode(modifier, startPosition, endPosition, className);
		if (docInfo != null)
		{
			pn.setDocumentation(docInfo);
		}
		pushNode(pn);
	}

	/**
	 * Handle a Trait declaration.
	 * 
	 * @param traitName
	 * @param modifier
	 * @param docInfo
	 * @param startPosition
	 * @param endPosition
	 * @param lineNumber
	 */
	public void handleTraitDeclaration(String traitName, int modifier, IPHPDocBlock docInfo, int startPosition,
			int endPosition, int lineNumber)
	{
		PHPTraitParseNode pn = new PHPTraitParseNode(modifier, startPosition, endPosition, traitName);
		if (docInfo != null)
		{
			pn.setDocumentation(docInfo);
		}
		pushNode(pn);
	}

	/**
	 * Handle the 'extends' section in the class declaration part.
	 * 
	 * @param superClassName
	 * @param startPosition
	 * @param endPosition
	 */
	public void handleSuperclass(String superClassName, int startPosition, int endPosition)
	{
		if (superClassName != null)
		{
			String decodeClassName = decodeClassName(superClassName);
			PHPClassParseNode classNode = (PHPClassParseNode) current;
			classNode.setSuperClassName(decodeClassName);
			PHPExtendsNode superClass = new PHPExtendsNode(0, startPosition, endPosition, decodeClassName);
			superClass.setNameNode(decodeClassName, startPosition, endPosition);
			classNode.addChild(superClass);
		}
	}

	/**
	 * Handle the 'extends' section in a Trait declaration part.
	 * 
	 * @param superClassName
	 * @param startPosition
	 * @param endPosition
	 */
	public void handleTraitSuperclass(String superClassName, int startPosition, int endPosition)
	{
		if (superClassName != null)
		{
			String decodeClassName = decodeClassName(superClassName);
			PHPTraitParseNode traitNode = (PHPTraitParseNode) current;
			traitNode.setSuperClassName(decodeClassName);
			PHPExtendsNode superClass = new PHPExtendsNode(0, startPosition, endPosition, decodeClassName);
			superClass.setNameNode(decodeClassName, startPosition, endPosition);
			traitNode.addChild(superClass);
		}
	}

	/**
	 * Handle an 'implements' section in the class declaration part.
	 * 
	 * @param interfacesNames
	 *            An array of interfaces names.
	 * @param startEndPositions
	 *            The start and the end of each interface in the interfaces names.
	 */
	public void handleImplements(String[] interfacesNames, int[][] startEndPositions)
	{
		if (interfacesNames != null && interfacesNames.length > 0 && startEndPositions != null)
		{
			PHPClassParseNode classNode = (PHPClassParseNode) current;
			List<String> interfaces = new ArrayList<String>(interfacesNames.length);
			for (int i = 0; i < interfacesNames.length; i++)
			{
				String interfaceName = decodeClassName(interfacesNames[i]);
				interfaces.add(interfaceName);
				classNode.addChild(new PHPExtendsNode(PHPFlags.AccInterface, startEndPositions[i][0],
						startEndPositions[i][1], interfaceName));
			}
			classNode.setInterfaces(interfaces);
		}
	}

	private void pushNode(PHPBaseParseNode pn)
	{
		current.addChild(pn);
		stack.push(current);
		current = pn;
	}

	public void handleClassVariablesDeclaration(String variables, int modifier, IPHPDocBlock docInfo,
			int startPosition, int endPosition, int stopPosition)
	{
		PHPVariableParseNode pn = new PHPVariableParseNode(modifier, startPosition, endPosition, variables);
		pn.setField(true);
		current.addChild(pn);
		pn.setNameNode(variables, startPosition, endPosition);
	}

	/**
	 * {@inheritDoc}
	 */
	public void handleVariableName(String variableName, int line)
	{
		if (collectVariables && current == root)
		{
			PHPVariableParseNode pn = new PHPVariableParseNode(0, -1, -1, variableName);
			pn.setField(false);
			pn.setLocalVariable(false);
			pn.setParameter(false);
			current.addChild(pn);
		}
	}

	public void handleDefine(String name, String value, IPHPDocBlock docInfo, int startPosition, int endPosition,
			int stopPosition)
	{
		PHPConstantNode pn = new PHPConstantNode(startPosition, endPosition, name);
		pn.setDocumentation(docInfo);
		pn.setField(true);
		pn.setNameNode(name, startPosition, endPosition);
		current.addChild(pn);
	}

	public void handleError(String description, int startPosition, int endPosition, int lineNumber)
	{
		// TODO: Shalom - See what needs to be done to handle those errors.
		IdeLog.logInfo(PHPEditorPlugin.getDefault(), "NodeBuilderClient.handleError() --> " + description, null, //$NON-NLS-1$
				PHPEditorPlugin.DEBUG_SCOPE);
	}

	public void handleFunctionDeclaration(String functionName, boolean isClassFunction, int modifier,
			IPHPDocBlock docInfo, int startPosition, int stopPosition, int lineNumber)
	{
		PHPFunctionParseNode pn = new PHPFunctionParseNode(modifier, startPosition, stopPosition, functionName);
		pn.setMethod(isClassFunction);
		pn.setParameters(parameters);
		if (docInfo != null)
		{
			pn.setDocumentation(docInfo);
		}
		parameters = new ArrayList<Parameter>();
		pushNode(pn);
	}

	public void handleFunctionParameter(String classType, String variableName, boolean isReference, boolean isConst,
			String defaultValue, int startPosition, int endPosition, int stopPosition, int lineNumber)
	{
		Parameter pr = new Parameter(classType, variableName, defaultValue, isReference, isConst);
		parameters.add(pr);
	}

	public void handleGlobalVar(String variableName)
	{
		PHPVariableParseNode pn = new PHPVariableParseNode(0, -1, -1, variableName);
		current.addChild(pn);
	}

	public void handlePHPStart(int startOffset, int endOffset)
	{
		phpStarts.add(new Integer(startOffset));
	}

	public void handlePHPEnd(int startOffset, int endOffset)
	{
		phpEnds.add(new Integer(startOffset));
	}

	public void handleStaticVar(String variableName)
	{
		PHPVariableParseNode pn = new PHPVariableParseNode(1, -1, -1, variableName);
		current.addChild(pn);
	}

	/**
	 * @return all nodes
	 */
	public PHPBlockNode populateNodes()
	{
		PHPBlockNode bn = new PHPBlockNode(0, 0, "php"); //$NON-NLS-1$
		for (int a = 0; a < current.getChildCount(); a++)
		{
			bn.addChild(current.getChild(a));
		}
		if (parseHTML)
		{
			replaceHtmlNodes(bn);
		}
		return bn;
	}

	/**
	 * Recursively go deeper into the nodes hierarchy and replace the PHP-HTML nodes with nodes that are generated using
	 * the HTML parser
	 * 
	 * @param pn
	 */
	private void replaceHtmlNodes(IParseNode pn)
	{

		IParseRootNode htmlParseResult = null;
		try
		{
			ParseState parseState = new HTMLParseState(source);
			htmlParseResult = ParserPoolFactory.parse(IHTMLConstants.CONTENT_TYPE_HTML, parseState).getRootNode();
		}
		catch (Exception e)
		{
			IdeLog.logWarning(PHPEditorPlugin.getDefault(),
					"A problem while integrating the HTML parse result nodes into the PHP parse result nodes", e); //$NON-NLS-1$
		}
		integrateNodesRecursively(pn, mapHTMLElementNodes(htmlParseResult.getChildren()));
	}

	/**
	 * Integrate the PHP nodes and the HTML nodes.
	 * 
	 * @param phpParseNode
	 * @param htmlElementNodes
	 */
	private void integrateNodesRecursively(IParseNode phpParseNode, Map<Integer, HTMLElementNode> htmlElementNodes)
	{
		Queue<IParseNode> queue = new LinkedList<IParseNode>();
		HTMLElementNode htmlNode = null;
		for (IParseNode node : phpParseNode.getChildren())
		{
			if (htmlNode != null)
			{
				// check if the next child is under the HTML node
				if (htmlNode.contains(node.getStartingOffset()))
				{
					if (node.getNodeType() != PHPBaseParseNode.HTML_NODE)
					{
						repositionNode((ParseNode) node, htmlNode);
						// We still need to visit that PHP node, so adding it to the queue
						queue.offer(node);
					}
					continue;
				}
				else
				{
					// reset the HTML node
					htmlNode = null;
				}
			}
			if (node.getNodeType() == PHPBaseParseNode.HTML_NODE)
			{
				// find the first non-white character in the node
				int offset = node.getStartingOffset();
				for (; offset < node.getEndingOffset(); offset++)
				{
					if (!Character.isWhitespace(source.charAt(offset)))
					{
						break;
					}
				}
				htmlNode = htmlElementNodes.get(offset);
				if (htmlNode != null)
				{
					// Found a matching HTML element node at this offset.
					// Replace the PHP node that represents the HTML part with the real HTML node
					IParseNode parent = node.getParent();
					while (parent != null && parent.isFilteredFromOutline()
							&& parent.getNodeType() != PHPBaseParseNode.BLOCK_NODE)
					{
						node = parent;
						parent = node.getParent();
					}
					if (parent != null)
					{
						IParseNode nextSibling = htmlNode.getNextSibling();
						Set<IParseNode> toInsert = new HashSet<IParseNode>();
						toInsert.add(htmlNode);
						// check if we need to insert any siblings as well
						while (nextSibling != null && nextSibling.getStartingOffset() < node.getEndingOffset())
						{
							if (nextSibling instanceof HTMLElementNode)
							{
								if (!toInsert.add(nextSibling))
								{
									// #APSTUD-3662 ==> In case the next sibling is already there, break the loop.
									break;
								}

							}
							nextSibling = nextSibling.getNextSibling();
						}
						// avoid any index out of bound in some cases.
						int nodeIndex = node.getIndex();
						if (nodeIndex > -1 && parent.getChildCount() > nodeIndex)
						{
							// Insert the node and the siblings that are nested in the PHP HTML node.
							IParseNode[] children = parent.getChildren();
							for (IParseNode child : children)
							{
								if (toInsert.contains(child))
								{
									toInsert.remove(child);
								}
							}
							int siblingsCount = toInsert.size();
							IParseNode[] newChildren = new IParseNode[children.length + siblingsCount];
							System.arraycopy(children, 0, newChildren, 0, nodeIndex);
							System.arraycopy(toInsert.toArray(), 0, newChildren, nodeIndex, siblingsCount);
							System.arraycopy(children, nodeIndex, newChildren, siblingsCount + nodeIndex,
									newChildren.length - nodeIndex - siblingsCount);
							((ParseNode) parent).setChildren(newChildren);
						}

					}
				}
			}
			else
			{
				queue.offer(node);
			}
		}

		// Recursively call the nodes in the queue
		for (IParseNode node : queue)
		{
			integrateNodesRecursively(node, htmlElementNodes);
		}
	}

	/**
	 * Reposition a node under a new parent
	 * 
	 * @param phpNode
	 * @param newHtmlParent
	 * @param phpNodeIndex
	 */
	private void repositionNode(ParseNode phpNode, HTMLElementNode newHtmlParent)
	{
		IParseNode phpNodeParent = phpNode.getParent();
		int phpNodeIndex = phpNode.getIndex();
		if (newHtmlParent.getParent() != phpNodeParent)
		{
			// position the HTML at the PHP nodes tree
			newHtmlParent.setParent(phpNodeParent);
			phpNodeParent.replaceChild(phpNodeIndex, newHtmlParent);
		}
		// At this point, we already injected the HTML into the PHP tree.
		// We set the HTML node as a parent of the given PHP node.
		IParseNode closesedHTMLNode = newHtmlParent.getNodeAtOffset(phpNode.getStartingOffset());
		if (closesedHTMLNode.getParent() instanceof HTMLElementNode)
		{
			newHtmlParent = (HTMLElementNode) closesedHTMLNode.getParent();
		}
		phpNode.setParent(newHtmlParent);
		// We inject the PHP node into the HTML node, replacing the text-node that represents it.
		IParseNode[] htmlChildren = newHtmlParent.getChildren();
		Set<IParseNode> newChildren = new LinkedHashSet<IParseNode>(htmlChildren.length);
		boolean phpChildInserted = false;
		for (IParseNode child : htmlChildren)
		{
			if (!phpChildInserted && child.contains(phpNode.getStartingOffset()) && child instanceof HTMLTextNode)
			{
				// found the PHP representation in the HTML nodes.
				newChildren.add(phpNode);
				phpChildInserted = true;
			}
			else if (phpChildInserted)
			{
				// consume any child that is nested in this php element
				if (!phpNode.contains(child.getStartingOffset()))
				{
					newChildren.add(child);
				}
			}
			else
			{
				newChildren.add(child);
			}
		}
		htmlChildren = newChildren.toArray(new IParseNode[newChildren.size()]);
		newHtmlParent.setChildren(htmlChildren);

		// Finally, we need to remove the PHP node from its original PHP parent. To do so, we manipulate the array of
		// children and re-insert it to the parent.
		IParseNode[] phpChildren = phpNodeParent.getChildren();
		IParseNode[] newPhpChildren = new IParseNode[phpChildren.length - 1];
		System.arraycopy(phpChildren, 0, newPhpChildren, 0, phpNodeIndex);
		System.arraycopy(phpChildren, phpNodeIndex + 1, newPhpChildren, phpNodeIndex, phpChildren.length - phpNodeIndex
				- 1);
		((ParseNode) phpNodeParent).setChildren(newPhpChildren);
	}

	/**
	 * Generate a {@link Map} that holds {@link HTMLElementNode}s offsets to node references in the given
	 * {@link IParseNode} tree.
	 * 
	 * @param parseNode
	 * @return A Map of nodes-offset to node.
	 */
	private Map<Integer, HTMLElementNode> mapHTMLElementNodes(IParseNode[] parseNodes)
	{
		// Recursively traverse the HTML tree to collect the HTMLElementNode
		Map<Integer, HTMLElementNode> offsetToNode = new HashMap<Integer, HTMLElementNode>();
		for (IParseNode child : parseNodes)
		{
			if (child instanceof HTMLElementNode)
			{
				HTMLElementNode previous = offsetToNode.put(child.getStartingOffset(), (HTMLElementNode) child);
				if (previous != null)
				{
					// Just in case, have this check in case something is wrong in the HTML result.
					// This will prevent any infinite recursion.
					IdeLog.logError(PHPEditorPlugin.getDefault(), "Invalid HTML parse result structure"); //$NON-NLS-1$
					return offsetToNode;
				}
				offsetToNode.putAll(mapHTMLElementNodes(child.getChildren()));
			}
		}
		return offsetToNode;
	}

	/**
	 * @param includingType
	 * @param includeFileName
	 * @param docInfo
	 * @param startPosition
	 * @param endPosition
	 * @param stopPosition
	 * @param lineNumber
	 */
	public void handleIncludedFile(String includingType, String includeFileName, IPHPDocBlock docInfo,
			int startPosition, int endPosition, int stopPosition, int lineNumber)
	{
		PHPIncludeNode node = new PHPIncludeNode(startPosition, endPosition, includeFileName, includingType);
		node.setNameNode(includeFileName, startPosition, endPosition);
		current.addChild(node);
	}

	/**
	 * Decodes class or interface name.
	 * 
	 * @param encodedName
	 *            - encoded name.
	 * @return decoded name.
	 */
	private String decodeClassName(String encodedName)
	{
		int bracketIndex = encodedName.indexOf(']');
		if (bracketIndex == -1 || bracketIndex == encodedName.length() - 1)
		{
			return encodedName;
		}

		return encodedName.substring(bracketIndex + 1);
	}

	public void handleSyntaxError(int currToken, String currText, short[] rowOfProbe, int startPosition,
			int endPosition, int lineNumber)
	{
		hasSyntaxErrors = true;
	}

	public void setNodeName(Identifier nameIdentifier)
	{
		if (current != null && nameIdentifier != null)
		{
			current.setNameNode(nameIdentifier.getName(), nameIdentifier.getStart(), nameIdentifier.getEnd() - 1);
		}
		else
		{
			if (nameIdentifier == null)
			{
				IdeLog.logWarning(PHPEditorPlugin.getDefault(),
						"PHP NodeBuilder.setNodeName got a null identifier.", PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
			}
			else
			{
				IdeLog.logWarning(
						PHPEditorPlugin.getDefault(),
						"PHP NodeBuilder.setNodeName didn't hold any current node to set a name on.", PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Handle a common declaration end.
	 */
	public void handleCommonDeclarationEnd()
	{
		try
		{
			current = (IPHPParseNode) stack.pop();
		}
		catch (Exception e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error building the PHP nodes.", e); //$NON-NLS-1$
		}
	}

	/**
	 * Handles an inline HTML content.
	 * 
	 * @param start
	 * @param end
	 */
	public void handleInlineHtml(int start, int end)
	{
		handlePHPEnd(start, -1);
		handlePHPStart(end, -1);
		// Check if the last child of the current node is also a HTML node. If so, we should unify both to one node with
		// a larger offset.
		if (current.getChildCount() > 0 && current.getLastChild().getNodeType() == PHPBaseParseNode.HTML_NODE)
		{
			PHPBaseParseNode lastChild = (PHPBaseParseNode) current.getLastChild();
			lastChild.setLocation(lastChild.getStart(), end);
		}
		else
		{
			// We temporarily insert that html node into the stack. This node will be popped and replaced with the real
			// HTML nodes once we verify that we are no longer receiving new inline-html nodes from the PHP parser.
			current.addChild(new PHPHTMLNode(start, end));
		}
	}

	/**
	 * @param start
	 * @param end
	 */
	public void handleTryStatement(int start, int end)
	{
		pushNode(new PHPTryNode(start, end));
	}

	/**
	 * @param start
	 * @param end
	 */
	public void handleCatchStatement(int start, int end)
	{
		pushNode(new PHPCatchNode(start, end));
	}

	/**
	 * @param start
	 * @param end
	 */
	public void handleDoStatement(int start, int end)
	{
		pushNode(new PHPDoNode(start, end));
	}

	/**
	 * @param start
	 * @param end
	 */
	public void handleForStatement(int start, int end)
	{
		pushNode(new PHPForNode(start, end, PHPForNode.FOR_TYPE.FOR));
	}

	/**
	 * @param start
	 * @param end
	 */
	public void handleForEachStatement(int start, int end)
	{
		pushNode(new PHPForNode(start, end, PHPForNode.FOR_TYPE.FOREACH));
	}

	/**
	 * @param start
	 * @param end
	 */
	public void handleSwitchCaseStatement(int start, int end)
	{
		pushNode(new PHPSwitchCaseNode(start, end));
	}

	/**
	 * @param start
	 * @param end
	 */
	public void handleSwitchStatement(int start, int end)
	{
		pushNode(new PHPSwitchNode(start, end));
	}

	/**
	 * @param start
	 * @param end
	 */
	public void handleWhileStatement(int start, int end)
	{
		pushNode(new PHPWhileNode(start, end));
	}

	/**
	 * @param start
	 * @param end
	 * @param type
	 */
	public void handleIfElseStatement(int start, int end, String type)
	{
		pushNode(new PHPIfElseNode(start, end, type));
	}
}
