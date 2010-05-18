/**
 * 
 */
package com.aptana.editor.php.internal.ui.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.CorePreferenceConstants;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.internal.ui.preferences.Messages;
import com.aptana.editor.php.internal.ui.preferences.PhpDevelopmentPage;

/**
 * New PHP project main creation page.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPWizardNewProjectCreationPage extends WizardNewProjectCreationPage
{

	private Combo fPHPVersions;

	public PHPWizardNewProjectCreationPage(String pageName)
	{
		super(pageName);
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
		GridLayout layout = new GridLayout();
		control.setLayout(layout);

		Group group = new Group(control, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(Messages.PHPDevelopmentPage_compatibility);

		Label label = new Label(group, SWT.NONE);
		label.setText(Messages.PHPDevelopmentPage_phpVersion);
		fPHPVersions = new Combo(group, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
		fPHPVersions.setItems(PhpDevelopmentPage.PHP_VERSION_NAMES
				.toArray(new String[PhpDevelopmentPage.PHP_VERSION_NAMES.size()]));
		selectVersion(PHPVersion.PHP5_3.getAlias());
		Dialog.applyDialogFont(control);
		setControl(control);
	}

	protected Preferences getPreferences(IProject project)
	{
		return new ProjectScope(project).getNode(PHPEplPlugin.PLUGIN_ID);
	}

	/**
	 * Apply the selected PHP version into the project's preferences.
	 * 
	 * @param project
	 */
	protected void setPhpLangOptions(IProject project)
	{
		Preferences preferences = getPreferences(project);
		preferences.put(CorePreferenceConstants.Keys.PHP_VERSION, PhpDevelopmentPage.PHP_ALIASES.get(fPHPVersions
				.getSelectionIndex()));
		try
		{
			preferences.flush();
		}
		catch (BackingStoreException e)
		{
			PHPEditorPlugin.logError(e);
		}
	}

	/**
	 * @param phpVersion
	 */
	private void selectVersion(String phpAlias)
	{
		int index = PhpDevelopmentPage.PHP_ALIASES.indexOf(phpAlias);
		if (index < 0)
		{
			PHPEditorPlugin.logWarning("Unresolved PHP version: " + phpAlias); //$NON-NLS-1$
			index = 0;
		}
		fPHPVersions.select(index);
	}
}
