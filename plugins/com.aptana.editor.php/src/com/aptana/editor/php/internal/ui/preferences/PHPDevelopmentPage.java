package com.aptana.editor.php.internal.ui.preferences;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.CorePreferenceConstants;
import com.aptana.editor.php.core.PHPVersionProvider;

/**
 * The primary PHP development property page.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PhpDevelopmentPage extends PropertyPage implements IWorkbenchPropertyPage
{
	public static final List<String> PHP_ALIASES = Arrays.asList(PHPVersion.PHP4.getAlias(),
			PHPVersion.PHP5.getAlias(), PHPVersion.PHP5_3.getAlias());
	public static final List<String> PHP_VERSION_NAMES = Arrays.asList(Messages.PHPDevelopmentPage_php4,
			Messages.PHPDevelopmentPage_php5, Messages.PHPDevelopmentPage_php53);
	private Combo fPHPVersions;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(Messages.PHPDevelopmentPage_compatibility);

		Label label = new Label(group, SWT.NONE);
		label.setText(Messages.PHPDevelopmentPage_phpVersion);
		fPHPVersions = new Combo(group, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);

		initialize();
		Dialog.applyDialogFont(composite);
		return composite;
	}

	// Initialize the php versions with the default preferences
	private void initialize()
	{
		Preferences pref = getPreferences((IProject) getElement().getAdapter(IProject.class));
		if (pref != null)
		{
			fPHPVersions.setItems(PHP_VERSION_NAMES.toArray(new String[PHP_VERSION_NAMES.size()]));
			String phpVersion = pref.get(CorePreferenceConstants.Keys.PHP_VERSION, PHPVersion.PHP5_3.getAlias());
			selectVersion(phpVersion);
		}
	}

	/**
	 * @param phpVersion
	 */
	private void selectVersion(String phpAlias)
	{
		int index = PHP_ALIASES.indexOf(phpAlias);
		if (index < 0)
		{
			PHPEditorPlugin.logWarning("Unresolved PHP version: " + phpAlias); //$NON-NLS-1$
			index = 0;
		}
		fPHPVersions.select(index);
	}

	private Preferences getPreferences(IProject project)
	{
		return new ProjectScope(project).getNode(PHPEditorPlugin.PLUGIN_ID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk()
	{
		Preferences pref = getPreferences((IProject) getElement().getAdapter(IProject.class));
		if (pref != null)
		{
			String selectedVersion = fPHPVersions.getText();
			int index = PHP_VERSION_NAMES.indexOf(selectedVersion);
			String selectedAlias = PHP_ALIASES.get(index);

			if (!selectedAlias.equals(pref.get(CorePreferenceConstants.Keys.PHP_VERSION, null)))
			{
				pref.put(CorePreferenceConstants.Keys.PHP_VERSION, selectedAlias);
				try
				{
					pref.flush();
				}
				catch (BackingStoreException e)
				{
					PHPEditorPlugin.logError(e);
				}
				PHPVersionProvider.getInstance().notifyChange((IProject) getElement().getAdapter(IProject.class), PHPVersion.byAlias(selectedAlias));
			}
		}
		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults()
	{
		IPreferenceStore store = PHPEditorPlugin.getDefault().getPreferenceStore();
		if (store != null)
		{
			String defaultAlias = store.getDefaultString(CorePreferenceConstants.Keys.PHP_VERSION);
			selectVersion(defaultAlias);
		}
		super.performDefaults();
	}

}
