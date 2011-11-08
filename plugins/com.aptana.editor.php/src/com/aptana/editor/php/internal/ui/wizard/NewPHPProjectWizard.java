/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.wizard;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.aptana.core.build.UnifiedBuilder;
import com.aptana.core.logging.IdeLog;
import com.aptana.core.projects.templates.IProjectTemplate;
import com.aptana.core.projects.templates.TemplateType;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.CorePreferenceConstants;
import com.aptana.editor.php.core.PHPNature;
import com.aptana.projects.WebProjectNature;
import com.aptana.projects.internal.wizards.NewProjectWizard;
import com.aptana.projects.internal.wizards.ProjectTemplateSelectionPage;
import com.aptana.usage.FeatureEvent;
import com.aptana.usage.StudioAnalytics;

/**
 * A new PHP Project Wizard class.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class NewPHPProjectWizard extends NewProjectWizard implements IExecutableExtension
{

	public static final String PHP_WIZARD_ID = "com.aptana.editor.php.NewPHPProjectWizard"; //$NON-NLS-1$
	private static final String PHP_PROJ_IMAGE_PATH = "/icons/full/wizban/new_project.png"; //$NON-NLS-1$

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

	/*
	 * (non-Javadoc)
	 * @see com.aptana.projects.internal.wizards.NewProjectWizard#addPages()
	 */
	public void addPages()
	{
		validateProjectTemplate(new TemplateType[] { TemplateType.PHP });

		mainPage = new PHPWizardNewProjectCreationPage("phpProjectPage", selectedTemplate); //$NON-NLS-1$
		mainPage.setTitle(Messages.NewPHPProjectWizard_projectWizardTitle);
		mainPage.setDescription(Messages.NewPHPProjectWizard_projectWizardDescription);
		mainPage.setWizard(this);
		mainPage.setPageComplete(false);
		addPage(mainPage);

		List<IProjectTemplate> templates = getProjectTemplates(new TemplateType[] { TemplateType.PHP });
		if (templates.size() > 0 && selectedTemplate == null)
		{
			addPage(templatesPage = new ProjectTemplateSelectionPage("templateSelectionPage", templates)); //$NON-NLS-1$
		}

		// TODO - Hook the project reference page to the builder and enable it
		// referencePage = new WizardNewProjectReferencePage("phpReferencePage"); //$NON-NLS-1$
		// addPage(referencePage);
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
		boolean finish = super.performFinish();
		if (finish)
		{
			setPhpLangOptions(newProject);
		}
		return finish;
	}

	/**
	 * Apply the selected PHP version into the project's preferences.<br>
	 * This operation is done in the Job to avoid any delays when a user click the 'finish' button.
	 * 
	 * @param project
	 */
	protected void setPhpLangOptions(final IProject project)
	{
		Job job = new Job("Setting the PHP Version...") //$NON-NLS-1$
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				PHPWizardNewProjectCreationPage pcp = (PHPWizardNewProjectCreationPage) getPages()[0];
				Preferences preferences = new ProjectScope(project).getNode(PHPEditorPlugin.PLUGIN_ID);
				preferences.put(CorePreferenceConstants.Keys.PHP_VERSION, pcp.getSelectedVersion());
				try
				{
					preferences.flush();
				}
				catch (BackingStoreException e)
				{
					IdeLog.logError(PHPEditorPlugin.getDefault(), "Error saving the project's PHP Version settings", e); //$NON-NLS-1$
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(Job.SHORT);
		job.schedule();
	}

	@Override
	protected void sendProjectCreateEvent(Map<String, String> payload)
	{
		StudioAnalytics.getInstance().sendEvent(new FeatureEvent("project.create.php", payload)); //$NON-NLS-1$
	}
}
