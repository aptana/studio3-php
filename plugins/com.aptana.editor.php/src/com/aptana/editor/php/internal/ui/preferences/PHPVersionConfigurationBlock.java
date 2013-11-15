package com.aptana.editor.php.internal.ui.preferences;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org2.eclipse.php.internal.core.PHPVersion;
import org2.eclipse.php.util.StatusInfo;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.CorePreferenceConstants.Keys;
import com.aptana.editor.php.ui.preferences.IStatusChangeListener;
import com.aptana.editor.php.util.Key;

/**
 * PHP version configuration block.
 */
public class PHPVersionConfigurationBlock extends PHPCoreOptionsConfigurationBlock
{

	public static final List<String> PHP_ALIASES = Arrays.asList(PHPVersion.PHP4.getAlias(),
			PHPVersion.PHP5.getAlias(), PHPVersion.PHP5_3.getAlias(), PHPVersion.PHP5_4.getAlias());
	public static final List<String> PHP_VERSION_NAMES = Arrays.asList(Messages.PHPDevelopmentPage_php4,
			Messages.PHPDevelopmentPage_php5, Messages.PHPDevelopmentPage_php53, Messages.PHPDevelopmentPage_php54);
	private static final Key PREF_PHP_VERSION = getPHPCoreKey(Keys.PHP_VERSION);

	private Combo fPHPVersions;

	// private boolean useProjectSpecificSettings;

	/**
	 * Constructs a new PHPVersionConfigurationBlock
	 * 
	 * @param context
	 * @param project
	 * @param container
	 */
	public PHPVersionConfigurationBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container)
	{
		super(context, project, getKeys(), container);
	}

	public void setEnabled(boolean isEnabled)
	{
		fPHPVersions.setEnabled(isEnabled);
	}

	private static Key[] getKeys()
	{
		return new Key[] { PREF_PHP_VERSION };
	}

	// Accessed from the PHP project Wizard
	public Control createContents(Composite parent)
	{

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		createVersionContent(composite);
		// createUseAspTagsContent(composite);
		unpackPHPVersion();
		// unpackUseAspTags();
		validateSettings(null, null, null);
		return composite;
	}

	private Composite createVersionContent(Composite parent)
	{
		// Composite composite = new Composite(parent, SWT.RESIZE);
		// GridLayout layout = new GridLayout(2, false);
		// layout.marginLeft = 0;
		// layout.marginRight = 0;
		// layout.horizontalSpacing = 5;
		// layout.verticalSpacing = 0;
		// layout.marginHeight = 0;
		// layout.marginWidth = 0;
		//
		// // layout.marginHeight = Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.VERTICAL_MARGIN);
		// // layout.marginWidth = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
		// IDialogConstants.HORIZONTAL_MARGIN);
		// // layout.verticalSpacing = Dialog.convertVerticalDLUsToPixels(fontMetrics,
		// IDialogConstants.VERTICAL_SPACING);
		// // layout.horizontalSpacing = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
		// // IDialogConstants.HORIZONTAL_SPACING);
		// composite.setLayout(layout);
		//
		// nameLabel = new Label(composite, SWT.NONE);
		// nameLabel.setText(PHPUIMessages.getString("PHPVersionComboName"));
		//
		// GC gc = new GC(nameLabel);
		// gc.setFont(nameLabel.getFont());
		// // FontMetrics fontMetrics = gc.getFontMetrics();
		// gc.dispose();
		//
		// List entryList = prepareVersionEntryList();
		// versionCombo = new ValuedCombo(composite, SWT.READ_ONLY, entryList);
		// versionCombo.addSelectionListener(new SelectionListener()
		// {
		// public void widgetSelected(SelectionEvent e)
		// {
		// String selectedValue = versionCombo.getSelectionValue();
		// setPhpVersionValue(selectedValue);
		// }
		//
		// public void widgetDefaultSelected(SelectionEvent e)
		// {
		// }
		//
		// });

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
		// Add a version listener
		fPHPVersions.addSelectionListener(new SelectionListener()
		{
			public void widgetSelected(SelectionEvent e)
			{
				setPhpVersion(getSelectedVersion());
			}

			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

		});
		Dialog.applyDialogFont(composite);

		return composite;
	}

	// Initialize the php versions with the default preferences
	private void initialize()
	{
		fPHPVersions.setItems(PHP_VERSION_NAMES.toArray(new String[PHP_VERSION_NAMES.size()]));

		// // TODO - integrate this?
		// Preferences pref = getPreferences((IProject) getElement().getAdapter(IProject.class));
		// if (pref != null)
		// {
		// String phpVersion = pref.get(CorePreferenceConstants.Keys.PHP_VERSION, PHPVersion.PHP5_3.getAlias());
		// selectVersion(phpVersion);
		// }
	}

	private String getSelectedVersion()
	{
		String selectedVersion = fPHPVersions.getText();
		int index = PHP_VERSION_NAMES.indexOf(selectedVersion);
		String selectedAlias = PHP_ALIASES.get(index);
		return selectedAlias;
	}

	/**
	 * @param phpVersion
	 */
	private void selectVersion(String phpAlias)
	{
		int index = PHP_ALIASES.indexOf(phpAlias);
		if (index < 0)
		{
			IdeLog.logWarning(
					PHPEditorPlugin.getDefault(),
					"Unresolved PHP version: " + phpAlias, new Exception("Unresolved PHP version"), PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$ //$NON-NLS-2$
			index = 0;
		}
		fPHPVersions.select(index);
	}

	protected void validateSettings(Key changedKey, String oldValue, String newValue)
	{
		fContext.statusChanged(new StatusInfo());
	}

	private void setPhpVersion(String value)
	{
		setValue(PREF_PHP_VERSION, value);
		validateSettings(PREF_PHP_VERSION, null, null);
	}

	protected String[] getFullBuildDialogStrings(boolean workspaceSettings)
	{
		String title = Messages.PHPVersionConfigurationBlock_needsbuild_title;
		String message;
		if (workspaceSettings)
		{
			message = Messages.PHPVersionConfigurationBlock_needsfullbuild_message;
		}
		else
		{
			message = Messages.PHPVersionConfigurationBlock_needsprojectbuild_message;
		}
		return new String[] { title, message };
		// return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock#updateControls()
	 */
	protected void updateControls()
	{
		unpackPHPVersion();
	}

	public void useProjectSpecificSettings(boolean enable)
	{
		// useProjectSpecificSettings = enable;
		super.useProjectSpecificSettings(enable);
		if (fProject != null && fPHPVersions != null)
		{
			unpackPHPVersion();
		}

	}

	private void unpackPHPVersion()
	{
		String version = getValue(PREF_PHP_VERSION);
		selectVersion(version);

	}

	// Accessed from the PHP project Wizard
	public String getPHPVersionValue()
	{
		return getValue(PREF_PHP_VERSION);
	}
}
