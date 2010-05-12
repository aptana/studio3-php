package com.aptana.editor.php.internal.editor.outline;

import org.eclipse.php.core.compiler.PHPFlags;
import org.eclipse.swt.graphics.Image;

import com.aptana.editor.html.outline.HTMLOutlineLabelProvider;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.parser.PHPMimeType;
import com.aptana.editor.php.internal.parser.nodes.IPHPParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPBaseParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPFunctionParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPVariableParseNode;
import com.aptana.parsing.IParseState;

public class PHTMLOutlineLabelProvider extends HTMLOutlineLabelProvider
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

	protected IParseState parseState;

	/**
	 * Constructs a new PHP - HTML outline label provider
	 */
	public PHTMLOutlineLabelProvider()
	{
		addSubLanguage(PHPMimeType.MimeType, new PHPOutlineLabelProvider());
	}

	/**
	 * Constructs a new PHP - HTML outline label provider with a given parse state.<br>
	 * @param parseState
	 */
	public PHTMLOutlineLabelProvider(IParseState parseState)
	{
		this();
		this.parseState = parseState;
	}

	@Override
	protected Image getDefaultImage(Object element)
	{
		Image result;
		if (element instanceof PHPBaseParseNode)
		{
			PHPBaseParseNode parseNode = (PHPBaseParseNode) element;
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
					result = super.getDefaultImage(element);
					break;
			}
		}
		else
		{
			result = super.getDefaultImage(element);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.html.outline.HTMLOutlineLabelProvider#getDefaultText(java.lang.Object)
	 */
	@Override
	protected String getDefaultText(Object element)
	{
		if (element instanceof PHPBaseParseNode)
		{
			return getDisplayText((PHPBaseParseNode) element);
		}
		return super.getDefaultText(element);
	}

	private String getDisplayText(PHPBaseParseNode element)
	{
		return element.getNodeName();
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
	// private String getDisplayText(ERBScript script)
	// {
	// StringBuilder text = new StringBuilder();
	//		text.append(script.getStartTag()).append(" "); //$NON-NLS-1$
	// String source = new String(fParseState.getSource());
	// // locates the ruby source
	// IRubyScript ruby = script.getScript();
	// source = source.substring(ruby.getStartingOffset(), ruby.getEndingOffset());
	// // gets the first line of the ruby source
	//		StringTokenizer st = new StringTokenizer(source, "\n\r\f"); //$NON-NLS-1$
	// source = st.nextToken();
	// if (source.length() <= TRIM_TO_LENGTH)
	// {
	// text.append(source);
	// }
	// else
	// {
	//			text.append(source.substring(0, TRIM_TO_LENGTH - 1)).append("..."); //$NON-NLS-1$
	// }
	//		text.append(" ").append(script.getEndTag()); //$NON-NLS-1$
	// return text.toString();
	// }
}