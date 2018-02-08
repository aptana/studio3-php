/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
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
