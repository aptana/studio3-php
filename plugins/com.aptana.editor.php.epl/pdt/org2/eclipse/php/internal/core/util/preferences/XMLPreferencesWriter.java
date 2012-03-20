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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.IWorkingCopyManager;

import com.aptana.editor.php.util.Key;

/**
 * XML preferences writer for writing XML structures into the prefernces store. This class works in combination with
 * IXMLPreferencesStorable.
 */
public class XMLPreferencesWriter
{

	public static final char DELIMITER = (char) 5;

	public static String getEscaped(String s)
	{
		StringBuilder result = new StringBuilder(s.length() + 10);
		for (int i = 0; i < s.length(); ++i)
		{
			appendEscapedChar(result, s.charAt(i));
		}
		return result.toString();
	}

	private static void appendEscapedChar(StringBuilder buffer, char c)
	{
		String replacement = getReplacement(c);
		if (replacement != null)
		{
			buffer.append('&');
			buffer.append(replacement);
			buffer.append(';');
		}
		else
		{
			buffer.append(c);
		}
	}

	private static String getReplacement(char c)
	{
		// Encode special XML characters into the equivalent character references.
		// These five are defined by default for all XML documents.
		switch (c)
		{
			case '<':
				return "lt"; //$NON-NLS-1$
			case '>':
				return "gt"; //$NON-NLS-1$
			case '"':
				return "quot"; //$NON-NLS-1$
			case '\'':
				return "apos"; //$NON-NLS-1$
			case '&':
				return "amp"; //$NON-NLS-1$
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private static void write(StringBuilder sb, Map map)
	{
		Set keys = map.keySet();
		for (Iterator i = keys.iterator(); i.hasNext();)
		{
			String key = (String) i.next();
			sb.append("<"); //$NON-NLS-1$
			sb.append(key);
			sb.append(">"); //$NON-NLS-1$
			Object object = map.get(key);
			if (object instanceof Map)
			{
				write(sb, (Map) object);
			}
			else
			{
				if (object != null)
				{
					sb.append(getEscaped(object.toString()));
				}
				else
				{
					sb.append(""); //$NON-NLS-1$
				}
			}
			sb.append("</"); //$NON-NLS-1$
			sb.append(key);
			sb.append(">"); //$NON-NLS-1$
		}
	}

	/**
	 * Writes a group of IXMLPreferencesStorables to the given the project properties.
	 * 
	 * @param prefsKey
	 *            The key to store by.
	 * @param objects
	 *            The IXMLPreferencesStorables to store.
	 * @param projectScope
	 *            The project Scope
	 * @param workingCopyManager
	 */
	public static void write(Key prefsKey, IXMLPreferencesStorable[] objects, ProjectScope projectScope,
			IWorkingCopyManager workingCopyManager)
	{
		StringBuilder sb = new StringBuilder();
		appendDelimitedString(sb, objects);
		prefsKey.setStoredValue(projectScope, sb.toString(), workingCopyManager);

	}

	/**
	 * Writes an IXMLPreferencesStorables to the given IPreferenceStore.
	 * 
	 * @param store
	 *            An IPreferenceStore instance
	 * @param prefsKey
	 *            The key to store by.
	 * @param object
	 *            The IXMLPreferencesStorables to store.
	 */
	public static void write(IPreferenceStore store, String prefsKey, IXMLPreferencesStorable object)
	{
		StringBuilder sb = new StringBuilder();
		write(sb, object.storeToMap());
		store.setValue(prefsKey, sb.toString());
	}

	/**
	 * Writes a group of IXMLPreferencesStorables to the given IPreferenceStore.
	 * 
	 * @param store
	 *            An IPreferenceStore instance
	 * @param prefsKey
	 *            The key to store by.
	 * @param objects
	 *            The IXMLPreferencesStorables to store.
	 */
	public static void write(IPreferenceStore store, String prefsKey, IXMLPreferencesStorable[] objects)
	{
		StringBuilder sb = new StringBuilder();
		appendDelimitedString(sb, objects);
		store.setValue(prefsKey, sb.toString());
	}

	/**
	 * Writes a group of IXMLPreferencesStorables to the given plugin preferences. The caller to this method should also
	 * make sure that {@link Plugin#savePluginPreferences()} is called in order to really store the changes.
	 * 
	 * @param pluginPreferences
	 *            A Preferences instance
	 * @param prefsKey
	 *            The key to store by.
	 * @param objects
	 *            The IXMLPreferencesStorables to store.
	 */
	public static void write(Preferences pluginPreferences, String prefsKey, IXMLPreferencesStorable[] objects)
	{
		StringBuilder sb = new StringBuilder();
		appendDelimitedString(sb, objects);
		pluginPreferences.setValue(prefsKey, sb.toString());
	}

	/**
	 * Writes an IXMLPreferencesStorable to the given plugin preferences. The caller to this method should also make
	 * sure that {@link Plugin#savePluginPreferences()} is called in order to really store the changes.
	 * 
	 * @param pluginPreferences
	 *            A Preferences instance
	 * @param prefsKey
	 *            The key to store by.
	 * @param object
	 *            The IXMLPreferencesStorable to store.
	 */
	public static void write(Preferences pluginPreferences, String prefsKey, IXMLPreferencesStorable object)
	{
		StringBuilder sb = new StringBuilder();
		write(sb, object.storeToMap());
		pluginPreferences.setValue(prefsKey, sb.toString());
	}

	// Append the elements one by one into the given StringBuffer.
	private static void appendDelimitedString(StringBuilder buffer, IXMLPreferencesStorable[] elements)
	{
		if (elements != null)
		{
			for (int i = 0; i < elements.length; ++i)
			{
				write(buffer, elements[i].storeToMap());
				if (i < elements.length - 1)
				{
					buffer.append(DELIMITER);
				}
			}
		}
	}

	public static String storableElementsToString(IXMLPreferencesStorable[] elements)
	{
		StringBuilder sb = new StringBuilder();
		appendDelimitedString(sb, elements);
		return sb.toString();
	}
}
