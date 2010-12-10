/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.php.internal.core.project.options.PHPProjectOptions;

import com.aptana.core.build.UnifiedBuilder;
import com.aptana.core.util.ResourceUtil;
import com.aptana.editor.php.PHPEditorPlugin;

/**
 * Aptana PHP Nature definition.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public final class PHPNature implements IPHPNature
{

	private IProject fProject;

	private PHPProjectOptions options;

	/**
	 * Constructs a new PHPNature class.
	 */
	public PHPNature()
	{
	}

	/**
	 * Constructs a new PHPNature class.
	 * 
	 * @param fProject
	 */
	public PHPNature(IProject project)
	{
		this.fProject = project;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException
	{
		ResourceUtil.addBuilder(getProject(), PHPEditorPlugin.BUILDER_ID);
		ResourceUtil.addBuilder(getProject(), UnifiedBuilder.ID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException
	{
		ResourceUtil.removeBuilder(getProject(), PHPEditorPlugin.BUILDER_ID);
		ResourceUtil.removeBuilderIfOrphaned(getProject(), UnifiedBuilder.ID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject()
	{
		return fProject;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project)
	{
		this.fProject = project;

	}

	public PHPProjectOptions getOptions()
	{
		if (options == null)
		{
			options = new PHPProjectOptions(fProject);
			// boolean useProjectSpecificSettings = new
			// ProjectScope(project).getNode(PhpOptionsPreferenceKeys.PHP_OPTION_NODE).getBoolean(PhpOptionsPreferenceKeys.PHP_OPTIONS_PER_PROJECT,
			// false);
			// if (!useProjectSpecificSettings) {
			// PhpVerionsProjectOptionAdapter.setVersion(options,
			// CorePreferenceConstants.getPreferenceStore().getString(CorePreferenceConstants.Keys.PHP_VERSION));
			// }
			// IEclipsePreferences projectProperties = new
			// ProjectScope(project).getNode(PhpOptionsPreferenceKeys.PHP_OPTION_NODE);
			// projectProperties.put
			// projectProperties
		}
		return options;
	}
}
