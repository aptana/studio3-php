/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.pathmapper;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents path-style entities (file-system paths, URLs).
 * Paths are case insensitive.
 *
 * @author michael
 */
public class VirtualPath implements Cloneable {

	private static final Pattern VOLNAME = Pattern.compile("([A-Za-z]:)[/\\\\](.*)");
	private static final Pattern PROTOCOL = Pattern.compile("([A-Za-z]*://)(.*)");
	private LinkedList<String> segments;
	private String device;
	private char sepChar;
	private Boolean isLocal;

	/**
	 * Constructs new abstract path instance
	 * @param path Full path
	 */
	public VirtualPath(String path) {
		if (path == null) {
			throw new NullPointerException();
		}
		if (path.startsWith("\\\\")) { // Network path
			sepChar = '\\';
			device = "\\\\";
			path = path.substring(2);
		} else {
			Matcher m = VOLNAME.matcher(path);
			if (m.matches()) { // Windows path
				sepChar = '\\';
				device = m.group(1) + "\\"; // correct path from C:/ to C:\
				path = m.group(2);
			} else if (path.startsWith("/")) { // Unix path
				sepChar = '/';
				device = "/";
			} else {
				m = PROTOCOL.matcher(path);
				if (m.matches()) { // URL
					sepChar = '/';
					device = m.group(1);
					path = m.group(2);
				} else {
					throw new IllegalArgumentException("Illegal or not full path: " + path);
				}
			}
		}

		StringTokenizer st = new StringTokenizer(path, "/\\");
		segments = new LinkedList<String>();
		while (st.hasMoreTokens()) {
			String segment = st.nextToken();
			if (segment.length() > 0) {
				segments.add(segment);
			}
		}
	}

	/**
	 * Checks whether the given path is absolute
	 * @param path
	 * @return <code>true</code> if given path is the absolute one, otherwise <code>false</code>
	 */
	public static boolean isAbsolute(String path) {
		return (path.startsWith("\\\\") || VOLNAME.matcher(path).matches() || path.startsWith("/") || PROTOCOL.matcher(path).matches());
	}

	protected VirtualPath(String device, char sepChar, LinkedList<String> segments) {
		this.device = device;
		this.sepChar = sepChar;
		this.segments = segments;
	}

	public String getLastSegment() {
		return segments.getLast();
	}

	public int getSegmentsCount() {
		return segments.size();
	}

	public String removeFirstSegment() {
		isLocal = null;
		return segments.removeFirst();
	}

	public String removeLastSegment() {
		isLocal = null;
		return segments.removeLast();
	}

	public void addLastSegment(String segment) {
		isLocal = null;
		segments.addLast(segment);
	}

	public String[] getSegments() {
		return segments.toArray(new String[segments.size()]);
	}

	public char getSeparatorChar() {
		return sepChar;
	}

	public boolean isPrefixOf(VirtualPath path) {
		Iterator<String> i1 = segments.iterator();
		Iterator<String> i2 = path.segments.iterator();
		while (i1.hasNext() && i2.hasNext()) {
			if (!i1.next().equals(i2.next())) {
				return false;
			}
		}
		return !i1.hasNext();
	}

	// SG: Aptana mod - isLocal
	public boolean isLocal() {
		if (isLocal == null) {
			isLocal = !"\\\\".equals(device) && new File(toString()).exists();
		}
		return isLocal;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder(device);
		Iterator<String> i = segments.iterator();
		while (i.hasNext()) {
			buf.append(i.next());
			if (i.hasNext()) {
				buf.append(sepChar);
			}
		}
		return buf.toString();
	}

	public VirtualPath clone() {
		LinkedList<String> segments = new LinkedList<String>();
		Iterator<String> i = this.segments.iterator();
		while (i.hasNext()) {
			segments.add(i.next());
		}
		VirtualPath path = new VirtualPath(device, sepChar, segments);
		return path;
	}

	public int hashCode() {
		return device.hashCode() + 13 * segments.hashCode() + 31 * sepChar;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof VirtualPath)) {
			return false;
		}
		VirtualPath other = (VirtualPath) obj;
		boolean segmentsEqual = other.segments.size() == segments.size();
		if (segmentsEqual) {
			Iterator<String> i = segments.iterator();
			Iterator<String> j = other.segments.iterator();
			while (segmentsEqual && i.hasNext() && j.hasNext()) {
				segmentsEqual &= i.next().equalsIgnoreCase(j.next());
			}
		}
		return other.device.equalsIgnoreCase(device) && segmentsEqual && other.sepChar == sepChar;
	}
}