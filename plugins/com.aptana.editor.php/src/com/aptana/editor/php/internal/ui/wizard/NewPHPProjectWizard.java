/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.IDialogSettings;

import com.aptana.core.build.UnifiedBuilder;
import com.aptana.core.logging.IdeLog;
import com.aptana.core.projects.templates.TemplateType;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.CorePreferenceConstants;
import com.aptana.editor.php.core.PHPNature;
import com.aptana.projects.WebProjectNature;
import com.aptana.projects.wizards.AbstractNewProjectWizard;
import com.aptana.projects.wizards.IWizardProjectCreationPage;

/**
 * A new PHP Project Wizard class.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class NewPHPProjectWizard extends AbstractNewProjectWizard implements IExecutableExtension
{

	public static final String PHP_WIZARD_ID = "com.aptana.editor.php.NewPHPProjectWizard"; //$NON-NLS-1$
	private static final String PHP_PROJ_IMAGE_PATH = "/icons/full/wizban/new_project.png"; //$NON-NLS-1$
	private String selectedVersion;

	/*
	 * (non-Javadoc)
	 * @see com.aptana.projects.internal.wizards.NewProjectWizard#initWizard()
	 */
	@Override
	protected void initDialogSettings()
	{
		IDialogSettings workbenchSettings = PHPEditorPlugin.getDefault().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("BasicNewProjectResourceWizard");//$NON-NLS-1$
		if (section == null)
		{
			section = workbenchSettings.addNewSection("BasicNewProjectResourceWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
	}

	@Override
	protected IWizardProjectCreationPage createMainPage()
	{
		PHPWizardNewProjectCreationPage mainPage = new PHPWizardNewProjectCreationPage(
				"phpProjectPage", selectedTemplate); //$NON-NLS-1$
		mainPage.setTitle(Messages.NewPHPProjectWizard_projectWizardTitle);
		mainPage.setDescription(Messages.NewPHPProjectWizard_projectWizardDescription);
		mainPage.setWizard(this);
		mainPage.setPageComplete(false);
		return mainPage;
	}

	@Override
	protected TemplateType[] getProjectTemplateTypes()
		{
		return new TemplateType[] { TemplateType.PHP };
		}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.projects.internal.wizards.NewProjectWizard#initializeDefaultPageImageDescriptor()
	 */
	protected void initializeDefaultPageImageDescriptor()
	{
		setDefaultPageImageDescriptor(PHPEditorPlugin.getImageDescriptor(PHP_PROJ_IMAGE_PATH));
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.projects.internal.wizards.NewProjectWizard#getProjectCreationDescription()
	 */
	protected String getProjectCreationDescription()
	{
		return Messages.NewPHPProjectWizard_projectWizardTitle;
	}

	/**
	 * Returns the wizard's ID (com.aptana.editor.php.wizards.PHPNewProjectWizard)
	 * 
	 * @return com.aptana.editor.php.wizards.PHPNewProjectWizard
	 */
	public String getID()
	{
		return PHP_WIZARD_ID;
	}

	/**
	 * Returns the project nature-id's.
	 * 
	 * @return The natures to be set to the project.
	 */
	protected String[] getProjectNatures()
	{
		return new String[] { PHPNature.NATURE_ID, WebProjectNature.ID };
	}

	/**
	 * Returns the project builder-id's.
	 * 
	 * @return The builders to be set to the project.
	 */
	protected String[] getProjectBuilders()
	{
		return new String[] { PHPNature.BUILDER_ID, UnifiedBuilder.ID };
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.projects.internal.wizards.NewProjectWizard#performFinish()
	 */
	public boolean performFinish()
	{
		PHPWizardNewProjectCreationPage pcp = (PHPWizardNewProjectCreationPage) mainPage;
		selectedVersion = pcp.getSelectedVersion();
		return super.performFinish();
	}

	@Override
	protected IProject createNewProject(IProgressMonitor monitor) throws InvocationTargetException
		{
		SubMonitor sub = SubMonitor.convert(monitor, 100);
		IProject project = super.createNewProject(sub.newChild(90));
		setPhpLangOptions(project, sub.newChild(10));
		sub.done();
		return project;
		}

	/**
	 * Apply the selected PHP version into the project's preferences.<br>
	 * This operation is done in the Job to avoid any delays when a user click the 'finish' button.
	 * 
	 * @param project
	 */
	protected void setPhpLangOptions(final IProject project, IProgressMonitor monitor)
	{
		IEclipsePreferences preferences = new ProjectScope(project).getNode(PHPEditorPlugin.PLUGIN_ID);
		preferences.put(CorePreferenceConstants.Keys.PHP_VERSION, selectedVersion);
		try
		{
			preferences.flush();
		}
		catch (Exception e)
		{
			// TODO Throw the exception up as an InvocationTargetException?
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error saving the project's PHP Version settings", e); //$NON-NLS-1$
		}
	}

	@Override
	protected String getProjectCreateEventName()
	{
		return "project.create.php"; //$NON-NLS-1$
	}
}
