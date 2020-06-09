/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *******************************************************************************/
package org2.eclipse.php.internal.core.project.options;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple XML writer.
 */
public class XMLWriter extends PrintWriter {
	protected int tab;

	/* constants */
	protected static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; //$NON-NLS-1$

	public XMLWriter(OutputStream output) throws UnsupportedEncodingException {
		super(new OutputStreamWriter(output, "UTF8")); //$NON-NLS-1$
		tab = 0;
		println(XML_VERSION);
	}

	public void endTag(String name) {
		tab--;
		printTag('/' + name, null);
	}

	public void printSimpleTag(String name, Object value) {
		if (value != null) {
			printTag(name, null, true, false);
			print(getEscaped(String.valueOf(value)));
			printTag('/' + name, null, false, true);
		}
	}

	public void printTabulation() {
		for (int i = 0; i < tab; i++)
			super.print('\t');
	}

	public void printTag(String name, HashMap<String, Object> parameters) {
		printTag(name, parameters, true, true);
	}

	public void printTag(String name, HashMap<String, Object> parameters, boolean shouldTab, boolean newLine) {
		StringBuffer sb = new StringBuffer();
		sb.append("<"); //$NON-NLS-1$
		sb.append(name);
		if (parameters != null)
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				sb.append(" "); //$NON-NLS-1$
				String key = entry.getKey();
				sb.append(key);
				sb.append("=\""); //$NON-NLS-1$
				sb.append(getEscaped(String.valueOf(entry.getValue())));
				sb.append("\""); //$NON-NLS-1$
			}
		sb.append(">"); //$NON-NLS-1$
		if (shouldTab)
			printTabulation();
		if (newLine)
			println(sb.toString());
		else
			print(sb.toString());
	}

	public void startTag(String name, HashMap<String, Object> parameters) {
		startTag(name, parameters, true);
	}

	public void startTag(String name, HashMap<String, Object> parameters, boolean newLine) {
		printTag(name, parameters, true, newLine);
		tab++;
	}

	private static void appendEscapedChar(StringBuffer buffer, char c) {
		String replacement = getReplacement(c);
		if (replacement != null) {
			buffer.append('&');
			buffer.append(replacement);
			buffer.append(';');
		} else {
			buffer.append(c);
		}
	}

	public static String getEscaped(String s) {
		StringBuffer result = new StringBuffer(s.length() + 10);
		for (int i = 0; i < s.length(); ++i)
			appendEscapedChar(result, s.charAt(i));
		return result.toString();
	}

	private static String getReplacement(char c) {
		// Encode special XML characters into the equivalent character references.
		// These five are defined by default for all XML documents.
		switch (c) {
			case '<' :
				return "lt"; //$NON-NLS-1$
			case '>' :
				return "gt"; //$NON-NLS-1$
			case '"' :
				return "quot"; //$NON-NLS-1$
			case '\'' :
				return "apos"; //$NON-NLS-1$
			case '&' :
				return "amp"; //$NON-NLS-1$
		}
		return null;
	}
}
