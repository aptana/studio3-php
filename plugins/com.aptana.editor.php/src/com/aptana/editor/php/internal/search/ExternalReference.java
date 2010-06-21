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

import org.eclipse.ui.IEditorInput;

import com.aptana.parsing.lexer.IRange;

public class ExternalReference
{
	/**
	 * corresponding editor input
	 */
	public final IEditorInput editorInput;
	/**
	 * position in the code
	 */
	public final IRange position;
	public ExternalReference(IEditorInput editorInput, IRange position)
	{
		super();
		this.editorInput = editorInput;
		this.position = position;
	}
}
