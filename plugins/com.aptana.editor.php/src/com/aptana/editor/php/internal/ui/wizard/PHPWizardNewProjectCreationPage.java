/**
 * 
 */
package com.aptana.editor.php.internal.ui.wizard;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org2.eclipse.php.internal.core.PHPVersion;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.projects.templates.IProjectTemplate;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.ui.preferences.Messages;
import com.aptana.editor.php.internal.ui.preferences.PHPVersionConfigurationBlock;
import com.aptana.projects.wizards.CommonWizardNewProjectCreationPage;

/**
 * New PHP project main creation page.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPWizardNewProjectCreationPage extends CommonWizardNewProjectCreationPage
{

	private Combo fPHPVersions;
	private String selectedAlias;

	public PHPWizardNewProjectCreationPage(String pageName, IProjectTemplate projectTemplate)
	{
		super(pageName, projectTemplate);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent)
	{
		super.createControl(parent);
		Composite control = (Composite) getControl();

		Group group = new Group(control, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(Messages.PHPDevelopmentPage_compatibility);

		Label label = new Label(group, SWT.NONE);
		label.setText(Messages.PHPDevelopmentPage_phpVersion);
		fPHPVersions = new Combo(group, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
		fPHPVersions.setItems(PHPVersionConfigurationBlock.PHP_VERSION_NAMES
				.toArray(new String[PHPVersionConfigurationBlock.PHP_VERSION_NAMES.size()]));
		selectedAlias = PHPVersion.getLatest().getAlias();
		setSelectedVersion(selectedAlias);
		// Update the 'selectedAlias' on combo selection changes.
		// We do that to avoid a 'widget dispose' errors when accessing this field after the page was disposed.
		fPHPVersions.addSelectionListener(new SelectionListener()
		{
			public void widgetSelected(SelectionEvent e)
			{
				selectedAlias = PHPVersionConfigurationBlock.PHP_ALIASES.get(fPHPVersions.getSelectionIndex());
			}

			public void widgetDefaultSelected(SelectionEvent e)
			{
			}
		});
		Dialog.applyDialogFont(control);
		setControl(control);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.projects.internal.wizards.IWizardProjectCreationPage#isCloneFromGit()
	 */
	public boolean isCloneFromGit()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.projects.internal.wizards.IWizardProjectCreationPage#getCloneURI()
	 */
	public String getCloneURI()
	{
		return null;
	}

	/**
	 * Returns the selected PHP version.
	 * 
	 * @return The selected PHP version.
	 */
	public String getSelectedVersion()
	{
		return selectedAlias;
	}

	/**
	 * Select a PHP version.
	 * 
	 * @param phpAlias
	 *            The version alias.
	 */
	private void setSelectedVersion(String phpAlias)
	{
		int index = PHPVersionConfigurationBlock.PHP_ALIASES.indexOf(phpAlias);
		if (index < 0)
		{
			IdeLog.logWarning(
					PHPEditorPlugin.getDefault(),
					"Unresolved PHP version: " + phpAlias, new Exception("Unresolved PHP version"), PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$ //$NON-NLS-2$
			index = 0;
		}
		fPHPVersions.select(index);
	}

	@Override
	public String getStepName()
	{
		return com.aptana.editor.php.internal.ui.wizard.Messages.NewPHPProjectWizard_projectWizardStepLbl;
	}
}
