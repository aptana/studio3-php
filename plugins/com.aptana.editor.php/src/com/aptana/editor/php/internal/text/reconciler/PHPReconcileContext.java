/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.text.reconciler;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import com.aptana.core.build.ReconcileContext;
import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.internal.parser.PHPParseState;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;
import com.aptana.parsing.ast.IParseRootNode;

/**
 * PHP reconciling parse state.<br>
 * The class is designed to provide a {@link PHPParseState} when computing the AST. This way, the PHP indexer is
 * triggered and maintain a valid status.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class PHPReconcileContext extends ReconcileContext
{

	private PHPSourceEditor editor;

	/**
	 * @param editor
	 * @param file
	 * @param contents
	 */
	public PHPReconcileContext(AbstractThemeableEditor editor, IFile file, String contents)
	{
		super(editor.getContentType(), file, contents);
		if (editor instanceof PHPSourceEditor)
		{
			this.editor = (PHPSourceEditor) editor;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.index.core.build.BuildContext#getAST()
	 */
	public IParseRootNode getAST() throws CoreException
	{
		PHPParseState parseState = new PHPParseState();
		if (editor != null)
		{
			parseState.setModule(editor.getModule());
			parseState.setSourceModule(editor.getSourceModule());
		}
		IFile file = getFile();
		if (file != null)
		{
			parseState.setPHPVersion(PHPVersionProvider.getPHPVersion(file.getProject()));
		}
		return getAST(parseState);
	}
}
