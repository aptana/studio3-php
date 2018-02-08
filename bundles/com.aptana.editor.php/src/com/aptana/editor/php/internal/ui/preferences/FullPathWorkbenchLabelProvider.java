/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.preferences;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Label provider that displays full path for directories.
 * 
 * @author Denis Denisenko
 */
public class FullPathWorkbenchLabelProvider extends WorkbenchLabelProvider
{
	private static final String DEFAULT_DECORATION = Messages.FullPathWorkbenchLabelProvider_default;
	private IProject defaultProject;

	public FullPathWorkbenchLabelProvider()
	{
	}

	/**
	 * Constructs this label provider with a default project that will be checked when the project name is decorated.
	 * 
	 * @param defaultProject
	 * @see #setDefaultProject(IProject)
	 */
	public FullPathWorkbenchLabelProvider(IProject defaultProject)
	{
		this.defaultProject = defaultProject;
	}

	/**
	 * Set the default project that will be checked when the project name is decorated.
	 * 
	 * @param defaultProject
	 */
	public void setDefaultProject(IProject defaultProject)
	{
		this.defaultProject = defaultProject;
	}

	/**
	 * Returns a label that is based on the given label, but decorated with additional information relating to the state
	 * of the provided object. Subclasses may implement this method to decorate an object's label.
	 * 
	 * @param input
	 *            The base text to decorate.
	 * @param element
	 *            The element used to look up decorations.
	 * @return the resulting text
	 */
	protected String decorateText(String input, Object element)
	{
		if (element == defaultProject)
		{
			return new StringBuilder(input).append(DEFAULT_DECORATION).toString();
		}
		else if (element instanceof IFolder)
		{
			return ((IFolder) element).getFullPath().toString();
		}
		return input;
	}

	@Override
	public void dispose()
	{
	}

	/**
	 * Disposes the provider.
	 */
	public void doDispose()
	{
		super.dispose();
	}
}
