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

import org2.eclipse.php.debug.core.debugger.pathmapper.PathMapper;

/**
 * This class represents container for {@link PathMapper}
 * @author michael
 */
public class PathEntry {

	/**
	 * Type of the file that this entry contains
	 */
	public enum Type {
		WORKSPACE("Workspace File"),
		INCLUDE_VAR("Include Path Variable"),
		INCLUDE_FOLDER("Include Path Folder"),
		EXTERNAL("External File"),
		;

		private String name;
		private Type(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}

	private Object container;
	private VirtualPath abstractPath;
	private Type type;

	/**
	 * Constructs new path entry
	 * @param path Path string
	 * @param type Path entry type
	 * @param container This path container. It can be either workspace resource, include path, or folder on file system
	 */
	public PathEntry(String path, Type type, Object container) {
		this(new VirtualPath(path), type, container);
	}

	/**
	 * Constructs new path entry
	 * @param path Abstract path
	 * @param type Path entry type
	 * @param container This path container. It can be either workspace resource, include path, or folder on file system
	 */
	public PathEntry(VirtualPath path, Type type, Object container) {
		this.abstractPath = path;
		this.type = type;
		this.container = container;
	}

	/**
	 * Returns abstract path of this entry
	 * @return abstract path
	 */
	public VirtualPath getAbstractPath() {
		return abstractPath;
	}

	/**
	 * Returns type of file contained in this entry
	 * @return file type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns container of this file. It can be either workspace resource, include path, or folder on file system
	 * @return container
	 */
	public Object getContainer() {
		return container;
	}

	/**
	 * Returns path string of this entry. It can be either a path to existing file, or path
	 * that contains variable from Include Path.
	 * @return path string
	 */
	public String getPath() {
		return abstractPath.toString();
	}

	/**
	 * Returns path to the file that this entry contains. If original path contained variables
	 * from Include Path it will be resolved.
	 * @return resolved path
	 */
	public String getResolvedPath() {
//		if (type == Type.INCLUDE_VAR) {
//			IPath resolvedPath = IncludePathVariableManager.instance().resolveVariablePath(getPath());
//			return resolvedPath.toOSString();
//		}
		return getPath();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof PathEntry)) {
			return false;
		}
		PathEntry other = (PathEntry) obj;
		return other.abstractPath.equals(abstractPath) && other.type == type;
	}

	public int hashCode() {
		return abstractPath.hashCode() + 13 * type.ordinal();
	}

	public String toString() {
		StringBuilder buf = new StringBuilder("Path Entry: ");
		buf.append(abstractPath).append(" (").append(type).append(")");
		return buf.toString();
	}
}
