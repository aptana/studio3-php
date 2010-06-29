/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.indexer;

import org.eclipse.php.internal.core.ast.nodes.Program;

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
