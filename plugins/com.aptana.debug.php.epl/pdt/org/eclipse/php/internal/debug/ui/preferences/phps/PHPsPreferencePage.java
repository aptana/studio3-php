/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org.eclipse.php.internal.debug.ui.preferences.phps;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.php.internal.debug.core.interpreter.preferences.PHPexeItem;
import org.eclipse.php.internal.debug.core.preferences.PHPDebugCorePreferenceNames;
import org.eclipse.php.internal.debug.core.preferences.PHPProjectPreferences;
import org.eclipse.php.internal.debug.core.preferences.PHPexes;
import org.eclipse.php.internal.debug.ui.PHPDebugUIMessages;
import org.eclipse.php.internal.debug.ui.wizard.exe.PHPExeEditDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.aptana.debug.php.epl.PHPDebugEPLPlugin;

/**
 * The Installed PHPs preference page.
 *
 * @since 3.0
 */
public class PHPsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static String ID = "org.eclipse.php.debug.ui.preferencesphps.PHPsPreferencePage";
	// PHP Block
	private InstalledPHPsBlock fPHPBlock;
	private Object data;

	public PHPsPreferencePage() {
		super();

		// only used when page is shown programatically
		setTitle(PHPDebugUIMessages.PHPsPreferencePage_1); 

		setDescription(PHPDebugUIMessages.PHPsPreferencePage_2); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * Apply the data to the receiver. We override this method to open the item editing dialog with the page.
	 * 
	 * @param data
	 * @since 3.1
	 */
	public void applyData(Object data) {
		this.data = data;
	}

	protected Preferences getModelPreferences() {
		return PHPProjectPreferences.getModelPreferences();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite ancestor) {
		initializeDialogUnits(ancestor);

		noDefaultAndApplyButton();

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		ancestor.setLayout(layout);

		fPHPBlock = new InstalledPHPsBlock();
		fPHPBlock.createControl(ancestor);
		Control control = fPHPBlock.getControl();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		control.setLayoutData(data);

		fPHPBlock.restoreColumnSettings(PHPDebugEPLPlugin.getDefault().getDialogSettings(), PHPDebugCorePreferenceNames.DIALOG_COLUMN_WIDTH);

		initDefaultPHP();
		//		fPHPBlock.addSelectionChangedListener(new ISelectionChangedListener() {
		//			public void selectionChanged(SelectionChangedEvent event) {
		//				PHPexeItem phpexe = getCurrentDefaultPHP();
		//				if (phpexe == null) {
		//					setValid(false);
		//					setErrorMessage(PHPDebugUIMessages.PHPsPreferencePage_13); //$NON-NLS-1$
		//				} else {
		//					setValid(true);
		//					setErrorMessage(null);
		//				}
		//			}
		//		});
		applyDialogFont(ancestor);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(ancestor, IPHPHelpContextIds.PHP_EXECUTABLES_PREFERENCES);
		return ancestor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {

		// save column widths
		IDialogSettings settings = PHPDebugEPLPlugin.getDefault().getDialogSettings();
		fPHPBlock.saveColumnSettings(settings, PHPDebugCorePreferenceNames.DIALOG_COLUMN_WIDTH);

		fPHPBlock.commitChanges();
		return super.performOk();
	}

	private void verifyDefaultPHP(PHPexeItem php) {
		if (php != null) {
			boolean exist = php.getExecutable().exists();
			// If all library locations exist, check the corresponding entry in the list,
			// otherwise remove the PHP setting
			if (!exist) {
				fPHPBlock.removePHPs(new PHPexeItem[] { php });
				ErrorDialog.openError(getControl().getShell(), PHPDebugUIMessages.PHPsPreferencePage_1, PHPDebugUIMessages.PHPsPreferencePage_10, new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IStatus.ERROR, PHPDebugUIMessages.PHPsPreferencePage_11, null)); 
				return;
			}
		}
	}

	private void initDefaultPHP() {
		PHPexeItem realDefault = PHPexes.getInstance().getDefaultItem(PHPDebugEPLPlugin.getCurrentDebuggerId());
		if (realDefault != null) {
			PHPexeItem[] phps = fPHPBlock.getPHPs();
			for (PHPexeItem fakePHP : phps) {
				if (fakePHP.equals(realDefault)) {
					verifyDefaultPHP(fakePHP);
					break;
				}
			}
		}
	}

	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		if (visible)
		{
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					if (PHPsPreferencePage.this.data instanceof PHPexeItem)
					{
						// open the item for editing
						PHPexes exes = PHPexes.getInstance();
						PHPexeItem pexeItem = (PHPexeItem) PHPsPreferencePage.this.data;
						PHPexeItem phpExeToEdit = new PHPexeItem(pexeItem.getName(), pexeItem.getExecutable(), pexeItem
								.getINILocation(), pexeItem.getDebuggerID(), pexeItem.isEditable());
						PHPExeEditDialog dialog = new PHPExeEditDialog(getShell(), phpExeToEdit, exes.getAllItems(),
								false);
						dialog.setTitle(PHPDebugUIMessages.InstalledPHPsBlock_8);
						if (dialog.open() != Window.OK)
						{
							performOk();
						}
						pexeItem.setName(phpExeToEdit.getName());
						pexeItem.setExecutable(phpExeToEdit.getExecutable());
						pexeItem.setINILocation(phpExeToEdit.getINILocation());
						String debuggerID = phpExeToEdit.getDebuggerID();
						pexeItem.setDebuggerID(debuggerID);
						performOk();
					}
				}
			});
		}
	}
}
