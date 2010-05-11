/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.aptana.editor.php.internal.parser.nodes.PHPClassParseNode;

/**
 * @author Pavel Petrochenko
 *
 */
public interface ITypeNode extends IElementNode
{


	/**
	 * @param project
	 * @return true if node is on build path of the given project
	 */
	boolean isOnBuildPath(IProject project);
	
	
	/**
	 * @return related parse node
	 */
	PHPClassParseNode toParseNode();
	
	/**
	 * @param from
	 * @return include string
	 */
	String getIncludePath(IFile from);
}
