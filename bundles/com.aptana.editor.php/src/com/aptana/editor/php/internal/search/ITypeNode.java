/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
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
