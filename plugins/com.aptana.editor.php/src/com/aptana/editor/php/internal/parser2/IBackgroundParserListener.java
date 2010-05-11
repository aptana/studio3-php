/**
 * Copyright (c) 2005-2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.parser2;

import org.eclipse.php.internal.core.ast.nodes.Program;

import com.aptana.editor.php.internal.builder.IModule;

/**
 * @author Pavel Petrochenko
 */
public interface IBackgroundParserListener
{

	/**
	 * @param program
	 *            reconciled
	 */
	public void reconciled(Program program, IModule module);
}
