/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.indexer;

import org2.eclipse.php.internal.core.ast.nodes.Program;

import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * 
 * @author Pavel Petrochenko
 *
 */
public interface IIndexingASTVisitor
{
	/**
	 * process Program
	 * @param program
	 * @param reporter
	 * @param module
	 */
	public void process(Program program,IIndexReporter reporter,IModule module);
	
}
