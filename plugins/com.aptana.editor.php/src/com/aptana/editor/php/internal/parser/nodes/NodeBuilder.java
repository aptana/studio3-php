/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org2.eclipse.php.core.compiler.PHPFlags;
import org2.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.FunctionDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.Identifier;
import org2.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.NamespaceDeclaration;
import org2.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocBlock;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.html.IHTMLConstants;
import com.aptana.editor.html.parsing.HTMLParseState;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.IParser;
import com.aptana.parsing.IParserPool;
import com.aptana.parsing.ParserPoolFactory;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.ParseNode;

/**
 * @author Pavel Petrochenko
 */
public class NodeBuilder
{

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private IPHPParseNode current;
	private IPHPParseNode root;
	private Stack<Object> stack = new Stack<Object>();
	private ArrayList<Object> phpStarts = new ArrayList<Object>();
	private ArrayList<Object> phpEnds = new ArrayList<Object>();
	private ArrayList<Object> parameters = new ArrayList<Object>();
	private IParser htmlParser;

	/**
	 * Whether to collect variables.
	 */
	private boolean collectVariables = false;
	private boolean hasSyntaxErrors;
	private String source;

	public boolean hasSyntaxErrors()
	{
		return hasSyntaxErrors;
	}

	/**
	 * @param root
	 */
	public NodeBuilder(String source, IPHPParseNode root)
	{
		this.source = source;
		this.current = root;
		this.root = root;
	}

	/**
	 * 
	 */
	public NodeBuilder(String source)
	{
		this.source = source;
		current = new PHPBaseParseNode((short) 0, 0, 0, 0, EMPTY_STRING);
		this.root = current;
	}

	/**
	 * NodeBuilderClient constructor.
	 * 
	 * @param collectVariables
	 *            - whether to collect variables.
	 */
	public NodeBuilder(String source, boolean collectVariables)
	{
		this(source);
		this.collectVariables = collectVariables;
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
		PHPNamespaceNode un = new PHPNamespaceNode(startPosition, endPosition, namespaceName, EMPTY_STRING);
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
		System.out.println("NodeBuilderClient.handleError() --> " + description); //$NON-NLS-1$
	}

	public void handleTask(String taskName, String description, int startPosition, int endPosition, int lineNumber)
	{
		// IUserData userData = PHPCodeDataFactory.createUserData(workingFileName, startPosition, endPosition,
		// startPosition, lineNumber);
		// markers.add(new PHPTask(taskName, description, userData));
		// TODO
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
		parameters = new ArrayList<Object>();
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
		IParserPool pool = ParserPoolFactory.getInstance().getParserPool(IHTMLConstants.CONTENT_TYPE_HTML);
		if (pool != null)
		{
			try
			{
				htmlParser = pool.checkOut();
				if (htmlParser != null)
				{
					replaceHtmlNodes(bn);
				}
			}
			finally
			{
				if (htmlParser != null)
				{
					pool.checkIn(htmlParser);
					htmlParser = null;
				}
			}
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
		for (int i = 0; i < pn.getChildCount(); i++)
		{
			IParseNode child = pn.getChild(i);
			if (child.getNodeType() == PHPBaseParseNode.HTML_NODE)
			{
				// Replace that node with nodes that we grab through an HTML parser.
				insertHtmlNodes(pn, child, i);
			}
			else
			{
				replaceHtmlNodes(child);
			}
		}
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
				IdeLog.logWarning(PHPEditorPlugin.getDefault(),
						"PHP NodeBuilder.setNodeName got a null identifier.", PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
			else
				IdeLog.logWarning(
						PHPEditorPlugin.getDefault(),
						"PHP NodeBuilder.setNodeName didn't hold any current node to set a name on.", PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
		}
	}

	/**
	 * Handle a class declaration end
	 * 
	 * @param classDeclaration
	 */
	public void handleClassDeclarationEnd(ClassDeclaration classDeclaration)
	{
		current = (IPHPParseNode) stack.pop();
	}

	/**
	 * Handle an interface declaration end. We treat the interface as a class (pure abstract class).
	 * 
	 * @param interfaceDeclaration
	 */
	public void handleClassDeclarationEnd(InterfaceDeclaration interfaceDeclaration)
	{
		current = (IPHPParseNode) stack.pop();
	}

	/**
	 * Handle a function declaration end
	 * 
	 * @param functionDeclaration
	 */
	public void handleFunctionDeclarationEnd(FunctionDeclaration functionDeclaration)
	{
		current = (IPHPParseNode) stack.pop();
	}

	/**
	 * Handle a namespace declaration end
	 * 
	 * @param functionDeclaration
	 */
	public void handleNamespaceDeclarationEnd(NamespaceDeclaration namespaceDeclaration)
	{
		current = (IPHPParseNode) stack.pop();
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
	 * Append the HTML nodes children into the PHP block node.
	 * 
	 * @param parent
	 * @param htmlNode
	 */
	private void insertHtmlNodes(IParseNode parent, IParseNode htmlNode, int htmlNodeIndex)
	{
		IParseNode nodes = getHtmlNodes(htmlNode);
		// Skip the root node and grab the children directly.
		IParseNode[] htmlChildren = nodes.getChildren();
		if (htmlChildren.length > 0)
		{
			IParseNode[] siblings = parent.getChildren();
			IParseNode[] newChildren = new IParseNode[siblings.length + htmlChildren.length - 1];
			// Copy up to the HTML node index (not including the node)
			System.arraycopy(siblings, 0, newChildren, 0, htmlNodeIndex);
			// Copy the new HTML children
			System.arraycopy(htmlChildren, 0, newChildren, htmlNodeIndex, htmlChildren.length);
			// Finally, copy the rest of the nodes that exist after the original HTML index
			System.arraycopy(siblings, htmlNodeIndex + 1, newChildren, htmlNodeIndex + htmlChildren.length,
					siblings.length - htmlNodeIndex - 1);
			((ParseNode) parent).setChildren(newChildren);
		}
	}

	/**
	 * This method is called when the PHP nodes are populated. The method will replace any existing PHPHTMLNode that we
	 * have in the stack with 'real' HTML nodes that we grab from the HTML parser.
	 * 
	 * @param htmlNode
	 *            an {@link IParseNode} representing the HTML content.
	 * @return The parse node for the HTML, as generated by the HTML parser.
	 */
	private IParseNode getHtmlNodes(IParseNode htmlNode)
	{

		try
		{
			IParseState parseState = new HTMLParseState();
			String input = source.substring(htmlNode.getStartingOffset(), htmlNode.getEndingOffset());
			parseState.setEditState(input, null, 0, 0);
			IParseNode parseResult = htmlParser.parse(parseState);
			if (parseResult != null)
			{
				// We need to shift the offsets of all the returned nodes to fit out source.
				updateOffsets(htmlNode.getStartingOffset(), ((ParseNode) parseResult));
				return parseResult;
			}
		}
		catch (Exception e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(),
					"Error getting the HTML nodes for the HTML part in the PHP source", e); //$NON-NLS-1$
		}
		// If we got null from the HTML parser as parse-result, we just return the original PHP-HTML node.
		return htmlNode;
	}

	/**
	 * @param offsetToAdd
	 * @param parseNode
	 */
	private void updateOffsets(int offsetToAdd, ParseNode parseNode)
	{
		Queue<IParseNode> queue = new LinkedList<IParseNode>();
		queue.offer(parseNode);
		// Walk the parse nodes tree and process each child.
		while (queue.isEmpty() == false)
		{
			IParseNode node = queue.poll();
			((ParseNode) node).addOffset(offsetToAdd);
			for (IParseNode child : node)
			{
				queue.offer(child);
			}
		}
	}
}
