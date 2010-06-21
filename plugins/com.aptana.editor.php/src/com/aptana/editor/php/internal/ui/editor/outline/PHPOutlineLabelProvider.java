package com.aptana.editor.php.internal.ui.editor.outline;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.php.core.compiler.PHPFlags;
import org.eclipse.swt.graphics.Image;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.internal.indexer.AbstractPHPEntryValue;
import com.aptana.editor.php.internal.parser.nodes.IPHPParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPBaseParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPClassParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPConstantNode;
import com.aptana.editor.php.internal.parser.nodes.PHPFunctionParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPIncludeNode;
import com.aptana.editor.php.internal.parser.nodes.PHPNamespaceNode;
import com.aptana.editor.php.internal.parser.nodes.PHPVariableParseNode;
import com.aptana.parsing.ast.INameNode;
import com.aptana.parsing.ast.IParseNode;

public class PHPOutlineLabelProvider extends LabelProvider
{
	/**
	 * PROPERTY_ICON
	 */
	public static final Image PROPERTY_ICON = PHPEditorPlugin.getImage("icons/full/obj16/php_property.gif"); //$NON-NLS-1$

	/**
	 * FUNCTION_ICON
	 */
	public static final Image FUNCTION_ICON = PHPEditorPlugin.getImage("icons/full/obj16/php_function.gif"); //$NON-NLS-1$

	/**
	 * CLASS_ICON
	 */
	public static final Image CLASS_ICON = PHPEditorPlugin.getImage("icons/full/obj16/php_class.gif"); //$NON-NLS-1$

	/**
	 * INTERFACE_ICON
	 */
	public static final Image INTERFACE_ICON = PHPEditorPlugin.getImage("icons/full/obj16/php_interface.gif"); //$NON-NLS-1$

	/**
	 * PRIVATE_METHOD_ICON
	 */
	public static final Image PRIVATE_METHOD_ICON = PHPEditorPlugin.getImage("icons/full/obj16/methpri_obj.gif"); //$NON-NLS-1$

	/**
	 * PROTECTED_METHOD_ICON
	 */
	public static final Image PROTECTED_METHOD_ICON = PHPEditorPlugin.getImage("icons/full/obj16/methpro_obj.gif"); //$NON-NLS-1$

	/**
	 * PUBLIC_METHOD_ICON
	 */
	public static final Image PUBLIC_METHOD_ICON = PHPEditorPlugin.getImage("icons/full/obj16/methpub_obj.gif"); //$NON-NLS-1$

	/**
	 * PRIVATE_FIELD_ICON
	 */
	public static final Image PRIVATE_FIELD_ICON = PHPEditorPlugin.getImage("icons/full/obj16/field_private_obj.gif"); //$NON-NLS-1$

	/**
	 * PROTECTED_FIELD_ICON
	 */
	public static final Image PROTECTED_FIELD_ICON = PHPEditorPlugin
			.getImage("icons/full/obj16/field_protected_obj.gif"); //$NON-NLS-1$

	/**
	 * DEFAULT_FIELD_ICON
	 */
	public static final Image DEFAULT_FIELD_ICON = PHPEditorPlugin.getImage("icons/full/obj16/field_default_obj.gif"); //$NON-NLS-1$

	/**
	 * PUBLIC_FIELD_ICON
	 */
	public static final Image PUBLIC_FIELD_ICON = PHPEditorPlugin.getImage("icons/full/obj16/field_public_obj.gif"); //$NON-NLS-1$

	/**
	 * LOCALVARIABLE_ICON
	 */
	public static final Image LOCALVARIABLE_ICON = PHPEditorPlugin.getImage("icons/full/obj16/localvariable_obj.gif"); //$NON-NLS-1$

	/**
	 * LOCALVARIABLE_ICON
	 */
	public static final Image NAMESPACE_ICON = PHPEditorPlugin.getImage("icons/full/obj16/namespace_obj.gif"); //$NON-NLS-1$

	/**
	 * IMPORT_ICON
	 */
	public static final Image IMPORT_ICON = PHPEditorPlugin.getImage("icons/full/obj16/imp_obj.gif"); //$NON-NLS-1$

	private static final Image BLOCK_ICON = PHPEditorPlugin.getImage("icons/full/obj16/php.gif"); //$NON-NLS-1$

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	// Template PHPBaseParseNodes that we use to convert AbstractPHPEntryValue to PHPBaseParseNodes (see getParseNode).
	// The AbstractPHPEntryValue are usually arriving from the content assist system.
	private static final PHPBaseParseNode classParseNodeTemplate = new PHPClassParseNode(0, 0, 0, EMPTY_STRING);
	private static final PHPBaseParseNode constantParseNodeTemplate = new PHPConstantNode(0, 0, EMPTY_STRING);
	private static final PHPBaseParseNode functionParseNodeTemplate = new PHPFunctionParseNode(0, 0, 0, EMPTY_STRING);
	private static final PHPBaseParseNode namespaceParseNodeTemplate = new PHPNamespaceNode(0, 0, EMPTY_STRING,
			EMPTY_STRING);
	private static final PHPBaseParseNode includeParseNodeTemplate = new PHPIncludeNode(0, 0, EMPTY_STRING,
			EMPTY_STRING);
	private static final PHPBaseParseNode variableParseNodeTemplate = new PHPVariableParseNode(0, 0, 0, EMPTY_STRING,
			true);

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element)
	{
		PHPBaseParseNode parseNode = getParseNode(element);
		if (parseNode != null)
		{
			INameNode nameNode = parseNode.getNameNode();
			String name = nameNode.getName();
			if (name == null || name.isEmpty())
			{
				return parseNode.getNodeName();
			}
			return name;
		}
		return super.getText(element);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element)
	{
		Image result;
		PHPBaseParseNode parseNode = getParseNode(element);
		if (parseNode != null)
		{
			int modifiers = parseNode.getModifiers();
			switch (parseNode.getType())
			{
				case IPHPParseNode.BLOCK_NODE:
				case IPHPParseNode.KEYWORD_NODE:
					return BLOCK_ICON;
				case IPHPParseNode.IMPORT_NODE:
				case IPHPParseNode.USE_NODE:
					return IMPORT_ICON;
				case IPHPParseNode.VAR_NODE:
				{
					result = getVariableNodeImage(parseNode, modifiers);
					break;
				}
				case IPHPParseNode.FUNCTION_NODE:
				{
					result = getFunctionNodeImage(parseNode, modifiers);
					break;
				}

				case IPHPParseNode.NAMESPACE_NODE:
					result = NAMESPACE_ICON;
					break;
				case IPHPParseNode.CLASS_NODE:
					result = getClassNodeImage(modifiers);
					break;

				default:
					result = getDefaultImage(element);
					break;
			}
		}
		else
		{
			result = getDefaultImage(element);
		}

		return result;
	}

	private Image getDefaultImage(Object element)
	{
		return BLOCK_ICON;
	}

	private PHPBaseParseNode getParseNode(Object element)
	{
		if (element instanceof PHPOutlineItem)
		{
			IParseNode referenceNode = ((PHPOutlineItem) element).getReferenceNode();
			return (PHPBaseParseNode) referenceNode;
		}
		else if (element instanceof AbstractPHPEntryValue)
		{
			// Convert this content-assist AbstractPHPEntryValue item to a PHPBaseParseNode
			AbstractPHPEntryValue value = (AbstractPHPEntryValue) element;
			PHPBaseParseNode result = null;
			switch (value.getKind())
			{
				case IPHPIndexConstants.CLASS_CATEGORY:
					result = classParseNodeTemplate;
					break;
				case IPHPIndexConstants.CONST_CATEGORY:
					result = constantParseNodeTemplate;
					break;
				case IPHPIndexConstants.FUNCTION_CATEGORY:
					result = functionParseNodeTemplate;
					break;
				case IPHPIndexConstants.IMPORT_CATEGORY:
					result = includeParseNodeTemplate;
					break;
				case IPHPIndexConstants.NAMESPACE_CATEGORY:
					result = namespaceParseNodeTemplate;
					break;
				case IPHPIndexConstants.VAR_CATEGORY:
					result = variableParseNodeTemplate;
					break;
			}
			if (result != null)
			{
				result.setModifiers(value.getModifiers());
				return result;
			}
		}
		return null;
	}

	private Image getVariableNodeImage(PHPBaseParseNode parseNode, int modifiers)
	{
		Image result;
		result = PROPERTY_ICON;
		PHPVariableParseNode fn = (PHPVariableParseNode) parseNode;
		if (fn.isParameter() || fn.isLocalVariable())
		{
			return LOCALVARIABLE_ICON;
		}
		if (fn.isField())
		{
			if (PHPFlags.isPublic(modifiers))
			{
				result = PUBLIC_FIELD_ICON;
			}
			else if (PHPFlags.isProtected(modifiers))
			{
				result = PROTECTED_FIELD_ICON;
			}
			else if (PHPFlags.isPrivate(modifiers))
			{
				result = PRIVATE_FIELD_ICON;
			}
			else
			{
				result = DEFAULT_FIELD_ICON;
			}
		}
		return result;
	}

	private Image getClassNodeImage(int modifiers)
	{
		Image result;
		if (PHPFlags.isInterface(modifiers))
		{
			result = INTERFACE_ICON;
		}
		else
		{
			result = CLASS_ICON;
		}
		return result;
	}

	private Image getFunctionNodeImage(PHPBaseParseNode parseNode, int modifiers)
	{
		Image result;
		PHPFunctionParseNode fn = (PHPFunctionParseNode) parseNode;
		result = FUNCTION_ICON;
		if (fn.isMethod())
		{
			if (PHPFlags.isPublic(modifiers))
			{
				result = PUBLIC_METHOD_ICON;
			}
			else if (PHPFlags.isProtected(modifiers))
			{
				result = PROTECTED_METHOD_ICON;
			}
			else if (PHPFlags.isPrivate(modifiers))
			{
				result = PRIVATE_METHOD_ICON;
			}
			else
			{
				result = PUBLIC_METHOD_ICON;
			}
		}
		return result;
	}
}
