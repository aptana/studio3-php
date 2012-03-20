/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.core.util.preferences;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aptana.core.util.StringUtil;
import com.aptana.core.util.replace.SimpleTextPatternReplacer;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.util.Key;

/**
 * XML preferences reader for reading XML structures from the preferences store. This class works in combination with
 * IXMLPreferencesStorable.
 */
public class XMLPreferencesReader
{

	public static final char DELIMITER = (char) 5;
	private static final SimpleTextPatternReplacer TAG_REPLACER = new SimpleTextPatternReplacer();
	static
	{
		TAG_REPLACER.addPattern("&amp;", "&"); //$NON-NLS-1$ //$NON-NLS-2$
		TAG_REPLACER.addPattern("&apos;", "'"); //$NON-NLS-1$ //$NON-NLS-2$
		TAG_REPLACER.addPattern("&quot;", "\""); //$NON-NLS-1$ //$NON-NLS-2$
		TAG_REPLACER.addPattern("&lt;", "<"); //$NON-NLS-1$ //$NON-NLS-2$
		TAG_REPLACER.addPattern("&gt;", ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public static final String STRING_DEFAULT = StringUtil.EMPTY;

	public static String getUnEscaped(String s)
	{
		return TAG_REPLACER.searchAndReplace(s);
	}

	private static Map read(NodeList nl)
	{
		Map map = new HashMap(nl.getLength());
		for (int i = 0; i < nl.getLength(); ++i)
		{
			Node n = nl.item(i);
			if (n.hasChildNodes())
			{
				if (n.getFirstChild().getNodeType() == Node.TEXT_NODE)
				{
					map.put(n.getNodeName(), getUnEscaped(n.getFirstChild().getNodeValue()));
				}
				else
				{
					map.put(n.getNodeName(), read(n.getChildNodes()));
				}
			}
		}
		return map;
	}

	private static Map read(String str)
	{
		try
		{
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			// docBuilderFactory.setValidating(true);
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new ByteArrayInputStream(str.getBytes()));

			return read(doc.getChildNodes());

		}
		catch (Exception e)
		{
			PHPEplPlugin.logError("Unexpected exception", e);
		}
		return null;
	}

	/**
	 * Reads a map of elements from the IPreferenceStore by a given key.
	 * 
	 * @param store
	 * @param prefsKey
	 * @return
	 */
	public static Map[] read(IPreferenceStore store, String prefsKey)
	{
		List<Map<?, ?>> maps = new ArrayList<Map<?, ?>>();
		StringTokenizer st = new StringTokenizer(store.getString(prefsKey), new String(new char[] { DELIMITER }));
		while (st.hasMoreTokens())
		{
			maps.add(read(st.nextToken()));
		}
		return maps.toArray(new HashMap[maps.size()]);
	}

	/**
	 * Reads a map of elements from the Preferences by a given key.
	 * 
	 * @param store
	 * @param prefsKey
	 * @return
	 */
	public static Map[] read(Preferences store, String prefsKey)
	{
		String storedValue = store.getString(prefsKey);
		return getHashFromStoredValue(storedValue);
	}

	/**
	 * Reads a map of elements from the project Properties by a given key.
	 * 
	 * @param prefsKey
	 *            The key to store by.
	 * @param projectScope
	 *            The context for the project Scope
	 * @param workingCopyManager
	 * @return
	 */
	public static Map[] read(Key prefKey, ProjectScope projectScope, IWorkingCopyManager workingCopyManager)
	{

		String storedValue = prefKey.getStoredValue(projectScope, workingCopyManager);
		if (storedValue == null)
		{
			storedValue = STRING_DEFAULT;
		}
		return getHashFromStoredValue(storedValue);

	}

	public static Map[] getHashFromStoredValue(String storedValue)
	{

		List<Map> maps = new ArrayList<Map>();
		StringTokenizer st = new StringTokenizer(storedValue, new String(new char[] { DELIMITER }));
		while (st.hasMoreTokens())
		{
			maps.add(read(st.nextToken()));
		}
		return maps.toArray(new HashMap[maps.size()]);

	}
}
