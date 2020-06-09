/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
/**
 *
 */
package org2.eclipse.php.internal.debug.core.zend.debugger;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.BackingStoreException;
import org2.eclipse.php.internal.debug.core.PHPDebugCoreMessages;
import org2.eclipse.php.internal.debug.core.daemon.AbstractDebuggerCommunicationDaemon;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebugCorePreferenceNames;
import org2.eclipse.php.internal.debug.ui.preferences.AbstractDebuggerConfigurationDialog;

import com.aptana.php.debug.core.preferences.PHPDebugPreferencesUtil;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * Zend debugger configuration class.
 *
 * @author Shalom Gibly
 * @since PDT 1.0
 */
public class ZendDebuggerConfigurationDialog extends AbstractDebuggerConfigurationDialog {

	private Text fDebugTextBox;
	private Button fRunWithDebugInfo;
	private Text fClientIP;
	private ZendDebuggerConfiguration zendDebuggerConfiguration;
	private int originalPort;

	/**
	 * Constructs a new Zend debugger configuration dialog.
	 * @param zendDebuggerConfiguration
	 * @param parentShell
	 */
	public ZendDebuggerConfigurationDialog(ZendDebuggerConfiguration zendDebuggerConfiguration, Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.zendDebuggerConfiguration = zendDebuggerConfiguration;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#getInitialSize()
	 */
	//	protected Point getInitialSize() {
	//		Point p = super.getInitialSize();
	//		p.y -= 50;
	//		return p;
	//	}

	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);

		parent = (Composite) super.createDialogArea(parent);
		setTitle(PHPDebugCoreMessages.ZendDebuggerConfigurationDialog_zendDebuggerSettings);

		Composite composite = createSubsection(parent, PHPDebugCoreMessages.ZendDebuggerConfigurationDialog_zendDebugger);

		addLabelControl(composite, PHPDebugCoreMessages.DebuggerConfigurationDialog_debugPort, PHPDebugCorePreferenceNames.ZEND_DEBUG_PORT);
		fDebugTextBox = addTextField(composite, PHPDebugCorePreferenceNames.ZEND_DEBUG_PORT, 6, 2);
		GridData gridData = (GridData)fDebugTextBox.getLayoutData();
		gridData.widthHint = convertWidthInCharsToPixels(100);

		fDebugTextBox.addModifyListener(new DebugPortValidateListener());

		fRunWithDebugInfo = addCheckBox(composite, PHPDebugCoreMessages.ZendDebuggerConfigurationDialog_runWithDebugInfo, PHPDebugCorePreferenceNames.RUN_WITH_DEBUG_INFO, 0);

		addLabelControl(composite, "Client Host/IP:", PHPDebugCorePreferenceNames.CLIENT_IP);
		fClientIP = addTextField(composite, PHPDebugCorePreferenceNames.CLIENT_IP, 0, 2);
		gridData = (GridData)fClientIP.getLayoutData();
		gridData.widthHint = convertWidthInCharsToPixels(100);

		internalInitializeValues(); // Initialize the dialog's values.

		return composite;
	}

	private void internalInitializeValues() {
		fRunWithDebugInfo.setSelection(PHPDebugPreferencesUtil.getBoolean(PHPDebugCorePreferenceNames.RUN_WITH_DEBUG_INFO, true));
		originalPort = PHPDebugPreferencesUtil.getInt(PHPDebugCorePreferenceNames.ZEND_DEBUG_PORT, 10000);
		fDebugTextBox.setText(Integer.toString(originalPort));
		fClientIP.setText(PHPDebugPreferencesUtil.getString(PHPDebugCorePreferenceNames.CLIENT_IP, "127.0.0.1")); //$NON-NLS-1$
	}

	protected void okPressed() {
		IEclipsePreferences preferences = PHPDebugEPLPlugin.getInstancePreferences();
		preferences.putBoolean(PHPDebugCorePreferenceNames.RUN_WITH_DEBUG_INFO, fRunWithDebugInfo.getSelection());
		preferences.put(PHPDebugCorePreferenceNames.ZEND_DEBUG_PORT, fDebugTextBox.getText());
		preferences.put(PHPDebugCorePreferenceNames.CLIENT_IP, fClientIP.getText());
		try
		{
			preferences.flush();
		}
		catch (BackingStoreException e)
		{
			PHPDebugEPLPlugin.logError(e);
		}
		super.okPressed();
	}
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	@Override
	public void create()
	{
		super.create();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(), "com.aptana.php.debug.epl.zend_conf"); //$NON-NLS-1$
	}

	class DebugPortValidateListener implements ModifyListener {

		public void modifyText(ModifyEvent e) {
			String errorMessage = null;
			boolean valid = true;
			String value = ((Text) e.widget).getText();
			try {
				Integer iValue = new Integer(value);
				int i = iValue.intValue();
				if (i < 0 || i > 65535) {
					valid = false;
					errorMessage = PHPDebugCoreMessages.DebuggerConfigurationDialog_invalidPort;
				}
			} catch (NumberFormatException e1) {
				valid = false;
				errorMessage = PHPDebugCoreMessages.DebuggerConfigurationDialog_invalidPort;
			} catch (Exception e2) {
				valid = false;
				errorMessage = PHPDebugCoreMessages.DebuggerConfigurationDialog_invalidPort;
			}
			if (valid)
			{
				Integer iValue = new Integer(value);
				if (iValue != originalPort)
				{
					if (!AbstractDebuggerCommunicationDaemon.isPortAvailable(iValue))
					{
						valid = false;
						errorMessage = PHPDebugCoreMessages.DebuggerConfigurationDialog_portInUse;
					}
				}
			}
			setErrorMessage(errorMessage);
			Button bt = getButton(IDialogConstants.OK_ID);
			if (bt != null) {
				bt.setEnabled(valid);
			}
		}
	}
}
