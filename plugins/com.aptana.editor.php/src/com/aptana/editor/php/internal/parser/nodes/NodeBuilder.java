/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.php.core.compiler.PHPFlags;
import org.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.ast.nodes.FunctionDeclaration;
import org.eclipse.php.internal.core.ast.nodes.Identifier;
import org.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration;
import org.eclipse.php.internal.core.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.editor.preferences.IPHPEditorPreferencesConstants;
import com.aptana.parsing.ast.IParseNode;

/**
 * @author Pavel Petrochenko
 */
public class NodeBuilder
{

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final int MAX_CHAR_COUNT = 25;
	private static final int SPACE_COUNT = 3;
	private IPHPParseNode current;
	private IPHPParseNode root;
	private Stack<Object> stack = new Stack<Object>();
	private ArrayList<Object> phpStarts = new ArrayList<Object>();
	private ArrayList<Object> phpEnds = new ArrayList<Object>();
	private ArrayList<Object> parameters = new ArrayList<Object>();

	/**
	 * Whether to collect variables.
	 */
	private boolean collectVariables = false;
	private boolean showShortContentInOutline = PHPEditorPlugin.getDefault().getPreferenceStore().getBoolean(
			IPHPEditorPreferencesConstants.PHPEDITOR_EMPTY_NODE_CONTENT_IN_OUTLINE);
	private boolean hasSyntaxErrors;

	public boolean hasSyntaxErrors()
	{
		return hasSyntaxErrors;
	}

	/**
	 * @param root
	 */
	public NodeBuilder(IPHPParseNode root)
	{
		this.current = root;
		this.root = root;
	}

	/**
	 * 
	 */
	public NodeBuilder()
	{
		current = new PHPBaseParseNode((short) 0, 0, 0, 0, EMPTY_STRING);
		this.root = current;
	}

	/**
	 * NodeBuilderClient constructor.
	 * 
	 * @param collectVariables
	 *            - whether to collect variables.
	 */
	public NodeBuilder(boolean collectVariables)
	{
		this();
		this.collectVariables = collectVariables;
	}

	public void handleClassConstDeclaration(String constName, PHPDocBlock docInfo, int startPosition, int endPosition,
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
	public void handleClassDeclaration(String className, int modifier, PHPDocBlock docInfo, int startPosition,
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

	public void handleClassVariablesDeclaration(String variables, int modifier, PHPDocBlock docInfo, int startPosition,
			int endPosition, int stopPosition)
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

	public void handleDefine(String name, String value, PHPDocBlock docInfo, int startPosition, int endPosition,
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
		// UserData userData = PHPCodeDataFactory.createUserData(workingFileName, startPosition, endPosition,
		// startPosition, lineNumber);
		// markers.add(new PHPTask(taskName, description, userData));
		// TODO
	}

	public void handleFunctionDeclaration(String functionName, boolean isClassFunction, int modifier,
			PHPDocBlock docInfo, int startPosition, int stopPosition, int lineNumber)
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

	public void handleStaticVar(String variableName)
	{
		PHPVariableParseNode pn = new PHPVariableParseNode(1, -1, -1, variableName);
		current.addChild(pn);
	}

	/**
	 * populates all nodes related to given position
	 * 
	 * @param parentNode
	 * @param position
	 * @param sourceUnsafe
	 */
	public void populateNodes(IParseNode parentNode, int position, char[] sourceUnsafe)
	{
		if (parentNode == null)
		{
			return;
		}
		int startingOffset = position;
		int from = -1;
		int to = -1;
		boolean ps = false;
		int phpEnd = -1;
		int phpStart = 0;
		for (int a = 0; a < phpStarts.size(); a++)
		{
			Integer pos = (Integer) phpStarts.get(a);

			if (startingOffset >= pos.intValue())
			{
				from = pos.intValue();
				phpStart = pos.intValue();
				continue;

			}
			if (!ps)
			{
				if (a < phpEnds.size() && a > 0)
				{
					phpEnd = ((Integer) phpEnds.get(a - 1)).intValue();
				}
				to = pos.intValue();
				ps = true;
			}
		}
		if (phpEnd == -1)
		{
			if (phpEnds.size() > 0)
			{
				phpEnd = ((Integer) phpEnds.get(phpEnds.size() - 1)).intValue();
			}
		}
		if (to == -1)
		{
			phpEnd = position;
			to = position;
		}
		if (from == -1 || true)
		{
			if (parentNode.getChildrenCount() > 0)
			{
				IParseNode child = parentNode.getChild(parentNode.getChildrenCount() - 1);
				from = child.getEndingOffset();
			}
		}
		PHPBlockNode bn = new PHPBlockNode(phpStart, phpEnd + 2, "php"); //$NON-NLS-1$		
		for (int a = 0; a < current.getChildrenCount(); a++)
		{
			IParseNode pn = current.getChild(a);
			if (pn.getStartingOffset() >= from)
			{
				if (to == -1 || pn.getStartingOffset() <= to)
				{
					bn.addChild(pn);
				}
			}
		}
		int childCount = bn.getChildrenCount();
		if (childCount == 0)
		{
			StringBuffer buf = new StringBuffer();
			int pos = phpStart + 2;
			if (from > -1)
			{
				pos = from;
			}
			int count = 0;
			int i = 0;
			boolean lastSpace = false;
			while (pos < sourceUnsafe.length)
			{
				char c = sourceUnsafe[pos];
				if (pos >= position)
				{
					break;
				}
				if (Character.isWhitespace(c))
				{
					pos++;
					if (!lastSpace)
					{
						buf.append(' ');
						count++;
					}
					lastSpace = true;
					continue;
				}
				lastSpace = false;
				if (count == SPACE_COUNT)
				{
					break;
				}
				if (i++ > MAX_CHAR_COUNT)
				{
					buf.append("..."); //$NON-NLS-1$
					break;
				}
				buf.append(c);
				pos++;
			}
			String string = buf.toString();
			if (string.startsWith("<?")) //$NON-NLS-1$
			{
				string = string.substring(2);
			}
			if (string.toLowerCase().startsWith("php")) //$NON-NLS-1$
			{
				string = string.substring(3);
			}
			if (!showShortContentInOutline)
			{
				string = EMPTY_STRING;
			}
			PHPBlockNode bn1 = new PHPBlockNode(phpStart, phpEnd + 2, string.trim());
			parentNode.addChild(bn1);
		}
		else
		{
			parentNode.addChild(bn);
		}
	}

	/**
	 * @return all nodes
	 */
	public PHPBlockNode populateNodes()
	{
		PHPBlockNode bn = new PHPBlockNode(0, 0, "php"); //$NON-NLS-1$
		for (int a = 0; a < current.getChildrenCount(); a++)
		{
			IParseNode pn = current.getChild(a);
			bn.addChild(pn);
		}
		return bn;
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
	public void handleIncludedFile(String includingType, String includeFileName, PHPDocBlock docInfo,
			int startPosition, int endPosition, int stopPosition, int lineNumber)
	{
		PHPIncludeNode node = new PHPIncludeNode(startPosition, endPosition, includeFileName, includingType);
		node.setNameNode(includeFileName, startPosition, endPosition);
		current.addChild(node);
	}

	public void handlePHPEnd(int startOffset, int endOffset)
	{
		phpEnds.add(new Integer(startOffset));
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
				PHPEditorPlugin.logWarning("PHP NodeBuilder.setNodeName got a null identifier."); //$NON-NLS-1$
			else
				PHPEditorPlugin
						.logWarning("PHP NodeBuilder.setNodeName didn't hold any current node to set a name on."); //$NON-NLS-1$
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
}
