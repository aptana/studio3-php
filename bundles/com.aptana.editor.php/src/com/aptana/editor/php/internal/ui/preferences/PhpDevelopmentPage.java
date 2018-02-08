package com.aptana.editor.php.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org2.eclipse.php.internal.core.PHPVersion;

import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.ui.preferences.PropertyAndPreferencePage;

/**
 * The primary PHP development property page.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PhpDevelopmentPage extends PropertyAndPreferencePage implements IWorkbenchPropertyPage
{
	private static final String PROPERTY_PAGE_ID = "com.aptana.editor.php.PHPDevelopmentPage"; //$NON-NLS-1$
	private PHPVersionConfigurationBlock fConfigurationBlock;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent)
	{
		IWorkbenchPreferenceContainer container = (IWorkbenchPreferenceContainer) getContainer();
		fConfigurationBlock = new PHPVersionConfigurationBlock(getNewStatusChangedListener(), getProject(), container);

		super.createControl(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	public void performApply()
	{
		if (fConfigurationBlock != null)
		{
			fConfigurationBlock.performApply();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk()
	{
		if (fConfigurationBlock != null)
		{
			if (!fConfigurationBlock.performOk())
			{
				return false;
			}
			PHPVersionProvider.getInstance().notifyChange((IProject) getElement().getAdapter(IProject.class),
					PHPVersion.byAlias(fConfigurationBlock.getPHPVersionValue()));
		}
		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.ui.preferences.PropertyAndPreferencePage#enableProjectSpecificSettings(boolean)
	 */
	@Override
	protected void enableProjectSpecificSettings(boolean useProjectSpecificSettings)
	{
		if (fConfigurationBlock != null)
		{
			fConfigurationBlock.useProjectSpecificSettings(useProjectSpecificSettings);
		}
		super.enableProjectSpecificSettings(useProjectSpecificSettings);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults()
	{
		super.performDefaults();
		if (fConfigurationBlock != null)
		{
			fConfigurationBlock.performDefaults();
		}
	}

	@Override
	protected Control createPreferenceContent(Composite composite)
	{
		return fConfigurationBlock.createContents(composite);
	}

	@Override
	public void doLinkActivated(Link link)
	{
		// TODO - For now, this one does nothing, since we don't display this page in the preferences.
	}

	@Override
	protected String getPreferencePageID()
	{
		// TODO - Hook this up once we have this page in the preferences too.
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.ui.preferences.PropertyAndPreferencePage#isProjectPreferencePage()
	 */
	@Override
	protected boolean isProjectPreferencePage()
	{
		// TODO - For now, treat this page as a property page only
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.ui.preferences.PropertyAndPreferencePage#offerLink()
	 */
	@Override
	protected boolean offerLink()
	{
		// TODO - For now, treat this page as a property page only
		return false;
	}

	@Override
	protected String getPropertyPageID()
	{
		return PROPERTY_PAGE_ID;
	}

	@Override
	protected boolean hasProjectSpecificOptions(IProject project)
	{
		return fConfigurationBlock.hasProjectSpecificOptions(project);
	}

}
