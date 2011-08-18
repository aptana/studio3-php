/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.aptana.editor.php.internal.builder.preferences.DependenciesManager;
import com.aptana.editor.php.internal.builder.preferences.ProjectDependencies;

/**
 * @author Pavel Petrochenko
 * @author Denis Denisenko
 */
public class PHPBuildpathPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{

	private BuildPathEditingComposite bps;
	private IAdaptable project;

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		bps = new BuildPathEditingComposite(this, parent, SWT.NONE);
		initValues();
		return bps;
	}

	private void initValues()
	{
		ProjectDependencies buildPath = DependenciesManager.getDependencies((IProject) project);
		bps.init(buildPath);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performOk()
	{
		ProjectDependencies bp = new ProjectDependencies();
		bps.fillResult(bp);
		DependenciesManager.setDependencies((IProject) project, bp);
		return true;
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults()
	{
		List<IResource> ps = new ArrayList<IResource>();
		List<File> fs = new ArrayList<File>();
		bps.init(ps, fs);
		super.performDefaults();
	}

	/**
	 * @see org.eclipse.ui.dialogs.PropertyPage#setElement(org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	public void setElement(IAdaptable element)
	{
		this.project = element;
		super.setElement(element);
	}

}
