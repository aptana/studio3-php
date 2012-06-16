/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.indexer;

import org2.eclipse.php.internal.core.ast.nodes.Program;

import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * @author Pavel Petrochenko
 */
public interface IProgramIndexer
{

	/**
	 * Indexes module contents.
	 * 
	 * @param module
	 *            - module to build index for.
	 * @param reporter
	 *            - reporter to report the built index.
	 */
	void indexModule(Program program, IModule module, IIndexReporter reporter);

	/**
	 * @param program
	 * @param module
	 * @param source
	 * @param iIndexReporter
	 */
	void indexModule(Program program, IModule module, String source, IIndexReporter iIndexReporter);
}
