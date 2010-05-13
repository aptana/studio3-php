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

	/**
	 * @see com.aptana.editor.php.internal.parser.ide.editor.php.parsing.ParserClientAdapter#handleClassConstDeclaration(java.lang.String,
	 *      org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock, int, int, int)
	 */
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
		current.addChild(un);
	}

	public void handleNamespace(String namespaceName, int startPosition, int stopPosition)
	{
		PHPNamespaceNode un = new PHPNamespaceNode(startPosition, stopPosition, namespaceName, EMPTY_STRING);
		current.addChild(un);
	}

	/**
	 * @see com.aptana.editor.php.internal.parser.ide.editor.php.parsing.ParserClientAdapter#handleClassDeclaration(java.lang.String,
	 *      int, java.lang.String, java.lang.String, org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock,
	 *      int, int, int)
	 */
	public void handleClassDeclaration(String className, int modifier, String superClassName, String interfacesNames,
			PHPDocBlock docInfo, int startPosition, int stopPosition, int lineNumber)
	{
		PHPClassParseNode pn = new PHPClassParseNode(modifier, startPosition, stopPosition, className);
		if (docInfo != null)
		{
			pn.setDocumentation(docInfo);
		}

		if (superClassName != null)
		{
			String decodeClassName = decodeClassName(superClassName);
			pn.setSuperClassName(decodeClassName);
			pn.addChild(new PHPExtendsNode(0, startPosition, stopPosition, decodeClassName));
		}

		if (interfacesNames != null)
		{
			String[] encodedInterfaces = interfacesNames.split(","); //$NON-NLS-1$
			List<String> interfaces = new ArrayList<String>(encodedInterfaces.length);
			for (String encodedInterfaceName : encodedInterfaces)
			{
				interfaces.add(decodeClassName(encodedInterfaceName));
				pn.addChild(new PHPExtendsNode(PHPFlags.AccInterface, startPosition, stopPosition,
						decodeClassName(encodedInterfaceName)));
			}
			pn.setInterfaces(interfaces);
		}

		pushNode(pn);
	}

	private void pushNode(PHPBaseParseNode pn)
	{
		current.addChild(pn);
		stack.push(current);
		current = pn;
	}

	/**
	 * @see com.aptana.editor.php.internal.parser.ide.editor.php.parsing.ParserClientAdapter#handleClassVariablesDeclaration(java.lang.String,
	 *      int, org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock, int, int, int)
	 */
	public void handleClassVariablesDeclaration(String variables, int modifier, PHPDocBlock docInfo, int startPosition,
			int endPosition, int stopPosition)
	{
		PHPVariableParseNode pn = new PHPVariableParseNode(modifier, startPosition, endPosition, variables);
		pn.setField(true);
		current.addChild(pn);
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

	/**
	 * @see com.aptana.editor.php.internal.parser.ide.editor.php.parsing.ParserClientAdapter#handleDefine(java.lang.String,
	 *      java.lang.String, org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock, int, int, int)
	 */
	public void handleDefine(String name, String value, PHPDocBlock docInfo, int startPosition, int endPosition,
			int stopPosition)
	{
		try
		{
			name = name.trim().substring(1, name.length() - 1);
		}
		catch (StringIndexOutOfBoundsException e)
		{
			return;
		}
		PHPConstantNode pn = new PHPConstantNode(startPosition, stopPosition, name);
		pn.setDocumentation(docInfo);
		pn.setField(true);
		current.addChild(pn);
	}

	/**
	 * @see com.aptana.editor.php.internal.parser.ide.editor.php.parsing.ParserClientAdapter#handleError(java.lang.String,
	 *      int, int, int)
	 */
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

	/**
	 * @see com.aptana.editor.php.internal.parser.ide.editor.php.parsing.ParserClientAdapter#handleFunctionDeclaration(java.lang.String,
	 *      boolean, int, org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock, int, int, int)
	 */
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

	/**
	 * @see com.aptana.editor.php.internal.parser.ide.editor.php.parsing.ParserClientAdapter#handleFunctionParameter(java.lang.String,
	 *      java.lang.String, boolean, boolean, java.lang.String, int, int, int, int)
	 */
	public void handleFunctionParameter(String classType, String variableName, boolean isReference, boolean isConst,
			String defaultValue, int startPosition, int endPosition, int stopPosition, int lineNumber)
	{
		Parameter pr = new Parameter(classType, variableName, defaultValue, isReference, isConst);
		parameters.add(pr);
	}

	/**
	 * @see com.aptana.editor.php.internal.parser.ide.editor.php.parsing.ParserClientAdapter#handleGlobalVar(java.lang.String)
	 */
	public void handleGlobalVar(String variableName)
	{
		PHPVariableParseNode pn = new PHPVariableParseNode(0, -1, -1, variableName);
		current.addChild(pn);
	}

	/**
	 * @see com.aptana.editor.php.internal.parser.ide.editor.php.parsing.ParserClientAdapter#handlePHPStart(int, int)
	 */
	public void handlePHPStart(int startOffset, int endOffset)
	{
		phpStarts.add(new Integer(startOffset));
	}

	/**
	 * @see com.aptana.editor.php.internal.parser.ide.editor.php.parsing.ParserClientAdapter#handleStaticVar(java.lang.String)
	 */
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
		current.addChild(new PHPIncludeNode(startPosition, endPosition, includeFileName, includingType));
	}

	/**
	 * @see com.aptana.editor.php.internal.parser.ide.editor.php.parsing.ParserClientAdapter#handlePHPEnd(int, int)
	 */
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
}
