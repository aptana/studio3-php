/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.indexer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;

public final class ASTVisitorRegistry
{
	private static final String EXTENSION_POINT_NAME = "com.aptana.editor.php.astVisitor"; //$NON-NLS-1$
	private static ASTVisitorRegistry instance;
	private List<IIndexingASTVisitor> visitors;

	private ASTVisitorRegistry()
	{
		IConfigurationElement[] configurationElementsFor = Platform.getExtensionRegistry().getConfigurationElementsFor(
				EXTENSION_POINT_NAME);
		visitors = new ArrayList<IIndexingASTVisitor>();
		for (IConfigurationElement e : configurationElementsFor)
		{
			try
			{
				IIndexingASTVisitor createExecutableExtension = (IIndexingASTVisitor) e
						.createExecutableExtension("class"); //$NON-NLS-1$
				visitors.add(createExecutableExtension);

			}
			catch (CoreException e1)
			{
				IdeLog.logError(PHPEditorPlugin.getDefault(), "Error loading a PHP indexing AST visitor extension", e1); //$NON-NLS-1$
			}
		}
	}

	public static ASTVisitorRegistry getInstance()
	{
		if (instance == null)
		{
			instance = new ASTVisitorRegistry();
		}
		return instance;
	}

	public IIndexingASTVisitor[] getVisitors()
	{
		return visitors.toArray(new IIndexingASTVisitor[visitors.size()]);
	}
}
