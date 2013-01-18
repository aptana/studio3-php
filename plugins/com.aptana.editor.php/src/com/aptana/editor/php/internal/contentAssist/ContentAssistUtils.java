/**
 * Aptana Studio
 * Copyright (c) 2005-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.contentAssist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org2.eclipse.php.internal.core.IPHPEplCoreConstants;
import org2.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocBlock;
import org2.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocTag;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.internal.indexer.language.PHPBuiltins;
import com.aptana.editor.php.internal.parser.nodes.IPHPParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPFunctionParseNode;

public class ContentAssistUtils
{
	/**
	 * Maximum symbols in documentation line.
	 */
	private static final int MAX_SYMBOLS_IN_DOC_LINE = 80;

	/**
	 * Minimum delta between the truncation position and previous &lt;br&gt; tag when we suggest truncating before the
	 * &lt;br&gt;
	 */
	private static final int BR_DELTA = 2;

	/**
	 * Built-in info
	 * 
	 * @author Denis Denisenko
	 */
	public static class BuiltinInfo
	{
		public Object builtIn;

		public String lowerCaseName;
	}

	/**
	 * Index of built-ins. Key is the first name character.
	 */
	private static Map<Character, List<BuiltinInfo>> index = null;

	private static Boolean filterByNamespace;

	/**
	 * Clean the PHP built-ins content assist index.
	 */
	public static void cleanIndex()
	{
		index = null;
	}

	// Patterns used for stripping HTML in case there is no support for a CustomBrowserInformationControl on the system
	private static final Map<Pattern, String> htmlStrippingMap;
	static
	{
		htmlStrippingMap = new HashMap<Pattern, String>();
		htmlStrippingMap.put(Pattern.compile("<b>|</b>"), StringUtil.EMPTY); //$NON-NLS-1$
		htmlStrippingMap.put(Pattern.compile("<p>|</p>"), "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		htmlStrippingMap.put(Pattern.compile("<br>|</br>"), "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		htmlStrippingMap.put(Pattern.compile("&lt"), "<"); //$NON-NLS-1$ //$NON-NLS-2$
		htmlStrippingMap.put(Pattern.compile("&gt"), ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param name
	 * @param eq
	 * @return list of model element that matches to given name
	 */
	public synchronized static List<Object> selectModelElements(String name, boolean eq)
	{
		if (index == null || index.isEmpty())
		{
			Collection<Object> builtins = PHPBuiltins.getInstance().getBuiltins();
			if (builtins == null || PHPBuiltins.getInstance().isInitializing())
			{
				// The built-ins are loading now
				return null;
			}
			initializeBuiltinsIndex(builtins);
		}

		if (name == null)
		{
			return null;
		}

		List<Object> toReturn = new ArrayList<Object>();

		if (name.length() == 0)
		{
			toReturn = new ArrayList<Object>();
			toReturn.addAll(PHPBuiltins.getInstance().getBuiltins());
			return toReturn;
		}

		String lowerCaseName = name.toLowerCase();
		Character firstCharacter = lowerCaseName.charAt(0);
		if (eq)
		{
			List<BuiltinInfo> lst = index.get(firstCharacter);
			if (lst != null)
			{
				for (BuiltinInfo info : lst)
				{
					if (acceptsNodeEquals(info, lowerCaseName))
					{
						toReturn.add(info.builtIn);
					}
				}
			}
		}
		else
		{
			List<BuiltinInfo> lst = index.get(firstCharacter);
			if (lst != null)
			{
				for (BuiltinInfo info : lst)
				{
					if (acceptsNodeStartingWith(info, lowerCaseName))
					{
						toReturn.add(info.builtIn);
					}
				}
			}
		}

		return toReturn;
	}

	/**
	 * Gets whether node is acceptable cause starting with a name.
	 * 
	 * @param info
	 *            - info.
	 * @param name
	 *            - lower case name.
	 * @return true if acceptable, false otherwise
	 */
	public static boolean acceptsNodeStartingWith(BuiltinInfo info, String name)
	{
		return info.lowerCaseName.startsWith(name);
	}

	/**
	 * Gets whether node is acceptable cause equal to a name.
	 * 
	 * @param info
	 *            - info.
	 * @param name
	 *            - lower case name.
	 * @return true if acceptable, false otherwise
	 */
	public static boolean acceptsNodeEquals(BuiltinInfo info, String name)
	{
		return info.lowerCaseName.equals(name);
	}

	/**
	 * Truncates documentation line putting "..." in the end.
	 * 
	 * @param line
	 *            - documentation line.
	 * @return truncated line
	 */
	public static String truncateLineIfNeeded(String line)
	{
		if (line.length() > MAX_SYMBOLS_IN_DOC_LINE)
		{
			// ending pos is max symbols minus 3 ("...").
			int endPos = MAX_SYMBOLS_IN_DOC_LINE - 3;

			// checking if we have <br> nearby the ending position and modifying endPos accordinly
			Pattern pattern = Pattern.compile("<br>"); //$NON-NLS-1$
			Matcher matcher = pattern.matcher(line);
			while (matcher.find())
			{
				int start = matcher.start();
				int end = matcher.end();

				if (start <= endPos && end >= endPos)
				{
					// if we have ending position inside the <br>, making ending position to point to the previous
					// symbol before the start of <br>
					endPos = start - 1;
					break;
				}
				else if (start > endPos)
				{
					// this <br> is already after the ending position, breaking the search
					break;
				}
				else if (end >= endPos - BR_DELTA)
				{
					endPos = start - 1;
					break;
				}
			}

			if (endPos < 0)
			{
				endPos = 0;
			}

			String subString = line.substring(0, endPos);
			return subString + "..."; //$NON-NLS-1$
		}

		return line;
	}

	/**
	 * @param node
	 * @param name
	 * @return documentation for given AST node
	 */
	public static String getDocumentation(IPHPParseNode node, String name)
	{
		String additionalInfo = Messages.ContentAssistUtils_noAvailableDocumentation;
		StringBuffer buf = new StringBuffer();
		IPHPDocBlock documentation = node.getDocumentation();
		if (!(node instanceof PHPFunctionParseNode))
		{
			buf.append("<b>" + name + "</b><br>"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			buf.append("<b>" + ((PHPFunctionParseNode) node).getSignature() + "</b><br>"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (documentation != null)
		{

			String longDescription = documentation.getLongDescription();
			if (longDescription.length() > 0)
			{
				buf.append(longDescription);
			}
			else
			{
				buf.append(documentation.getShortDescription());
			}
			IPHPDocTag[] tagsAsArray = documentation.getTagsAsArray();
			buf.append("<br>"); //$NON-NLS-1$
			for (int a = 0; a < tagsAsArray.length; a++)
			{
				buf.append("<br>"); //$NON-NLS-1$
				buf.append(tagsAsArray[a].toString());
			}
		}
		else
		{
			buf.append(additionalInfo);
		}
		additionalInfo = buf.toString();
		return additionalInfo;
	}

	/**
	 * Strip out, or replace with regular ascii characters, any 'b', 'br', 'lt' or 'gt' tags.
	 * 
	 * @param content
	 * @return
	 */
	public static String stripBasicHTML(String content)
	{
		for (Pattern strippingPattern : htmlStrippingMap.keySet())
		{
			content = strippingPattern.matcher(content).replaceAll(htmlStrippingMap.get(strippingPattern));
		}
		return content;
	}

	/**
	 * Returns <code>true</code> in case the preferences are set to filter the content assist by looking at the call
	 * namespace; <code>false</code> otherwise.
	 */
	public static boolean isFilterByNamespace()
	{
		// This method is called very frequently, so we add a preferences listener that will exist through the entire
		// Studio session and will monitor any change in the STRICT_NS_ASSIST property.
		if (filterByNamespace == null)
		{
			filterByNamespace = PHPEplPlugin.getDefault().getPreferenceStore()
					.getBoolean(IPHPEplCoreConstants.STRICT_NS_CODE_ASSIST);
			// add a listener for the STRICT_NS_ASSIST property
			PHPEplPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent event)
				{
					if (IPHPEplCoreConstants.STRICT_NS_CODE_ASSIST.equals(event.getProperty()))
					{
						filterByNamespace = (Boolean) event.getNewValue();
					}

				}
			});
		}
		return filterByNamespace;
	}

	/**
	 * Initializes built-in index.
	 * 
	 * @param builtins
	 *            - built-ins.
	 */
	private static void initializeBuiltinsIndex(Collection<Object> builtins)
	{
		index = new HashMap<Character, List<BuiltinInfo>>();
		for (Object builtin : builtins)
		{
			if (builtin instanceof IPHPParseNode)
			{
				String lowercaseName = ((IPHPParseNode) builtin).getNodeName().toLowerCase();
				if (lowercaseName.length() != 0)
				{
					Character firstCharacter = lowercaseName.charAt(0);
					List<BuiltinInfo> charList = index.get(firstCharacter);
					if (charList == null)
					{
						charList = new ArrayList<BuiltinInfo>();
						index.put(firstCharacter, charList);
					}

					BuiltinInfo info = new BuiltinInfo();
					info.builtIn = builtin;
					info.lowerCaseName = lowercaseName;
					charList.add(info);
				}
			}
		}
	}
}
