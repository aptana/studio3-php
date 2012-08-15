/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.ui.wizard.exe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org2.eclipse.php.internal.debug.core.interpreter.preferences.PHPexeItem;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebuggersRegistry;
import org2.eclipse.php.internal.debug.ui.PHPDebugUIImages;
import org2.eclipse.php.internal.debug.ui.PHPDebugUIMessages;
import org2.eclipse.php.internal.debug.ui.wizard.CompositeFragment;
import org2.eclipse.php.internal.debug.ui.wizard.IControlHandler;
import org2.eclipse.php.internal.ui.util.PixelConverter;
import org2.eclipse.php.internal.ui.wizard.field.DialogField;
import org2.eclipse.php.internal.ui.wizard.field.IDialogFieldListener;
import org2.eclipse.php.internal.ui.wizard.field.IStringButtonAdapter;
import org2.eclipse.php.internal.ui.wizard.field.StringButtonDialogField;
import org2.eclipse.php.internal.ui.wizard.field.StringDialogField;

import com.aptana.core.util.StringUtil;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;
import com.aptana.php.debug.ui.phpini.PHPIniEditor;

public class PHPExeCompositeFragment extends CompositeFragment implements IPHPExeCompositeFragment
{

	private static final String[] EXTENSIONS_FILTERS = new String[] { "*.ini", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String PHP_INI = "php.ini"; //$NON-NLS-1$
	private PHPexeItem[] existingItems;
	private StringDialogField fPHPexeName;
	private StringButtonDialogField fPHPExePath;
	private StringButtonDialogField fPHPIni;
	private List<String> debuggersIds;
	private Label fDebuggersLabel;
	private Combo fDebuggers;
	private String initialName;
	private PHPIniEditor iniEditor;
	private boolean needValidation;

	public PHPExeCompositeFragment(Composite parent, IControlHandler handler, boolean isForEditing)
	{
		super(parent, handler, isForEditing);
		this.setDescription(PHPDebugUIMessages.PHPExeCompositeFragment_description);
		this.setDisplayName(PHPDebugUIMessages.PHPExeCompositeFragment_displayName);
		this.controlHandler.setDescription(this.getDescription());
		this.controlHandler.setImageDescriptor(PHPDebugUIImages.getImageDescriptor(PHPDebugUIImages.IMG_WIZBAN_PHPEXE));

		this.debuggersIds = new LinkedList<String>(PHPDebuggersRegistry.getDebuggersIds());
		this.createControl();
		if (handler instanceof PHPExeEditDialog && iniEditor != null)
		{
			// Set the validation flag for cases where a validation at initialization is needed.
			needValidation = ((PHPExeEditDialog) handler).shouldValidate();
		}
	}

	public void setExistingItems(PHPexeItem[] existingItems)
	{
		this.existingItems = existingItems;
	}

	public void setData(Object data)
	{
		if ((data != null) && !(data instanceof PHPexeItem))
		{
			throw new IllegalArgumentException("Data must be instance of PHPExeItem"); //$NON-NLS-1$
		}
		super.setData(data);
		this.init();
	}

	public PHPexeItem getPHPExeItem()
	{
		return (PHPexeItem) super.getData();
	}

	protected String getPHPexeName()
	{
		return this.fPHPexeName.getText();
	}

	protected File getInstallLocation()
	{
		return new File(this.fPHPExePath.getText());
	}

	protected File getIniLocation()
	{
		return new File(this.fPHPIni.getText());
	}

	protected void createControl()
	{
		PixelConverter pixelConverter = new PixelConverter(this);

		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		this.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite parent = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 3;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		this.fPHPexeName = new StringDialogField();
		this.fPHPexeName.setLabelText(PHPDebugUIMessages.addPHPexeDialog_phpName);

		this.fPHPExePath = new StringButtonDialogField(new IStringButtonAdapter()
		{
			public void changeControlPressed(DialogField field)
			{
				FileDialog dialog = new FileDialog(PHPExeCompositeFragment.this.getShell());
				dialog.setFilterPath(PHPExeCompositeFragment.this.fPHPExePath.getText());
				dialog.setText(PHPDebugUIMessages.addPHPexeDialog_pickPHPRootDialog_message);
				String newPath = dialog.open();
				if (newPath != null)
				{
					PHPExeCompositeFragment.this.fPHPExePath.setText(newPath);
					iniEditor.setPHPExe(newPath);
				}
			}
		});
		this.fPHPExePath.setLabelText(PHPDebugUIMessages.addPHPexeDialog_phpHome);
		this.fPHPExePath.setButtonLabel(PHPDebugUIMessages.addPHPexeDialog_browse1);

		this.fPHPIni = new StringButtonDialogField(new IStringButtonAdapter()
		{
			public void changeControlPressed(DialogField field)
			{
				FileDialog dialog = new FileDialog(PHPExeCompositeFragment.this.getShell());
				dialog.setFilterPath(PHPExeCompositeFragment.this.fPHPIni.getText());
				dialog.setFilterExtensions(EXTENSIONS_FILTERS);
				dialog.setText(PHPDebugUIMessages.addPHPexeDialog_pickPHPIniDialog_message);
				String newPath = dialog.open();
				if (newPath != null)
				{
					PHPExeCompositeFragment.this.fPHPIni.setText(newPath);
					if (newPath != null && !StringUtil.EMPTY.equals(newPath))
					{
						try
						{
							iniEditor.openFile(newPath);
						}
						catch (IOException e)
						{
							String message = MessageFormat.format(
									PHPDebugUIMessages.PHPExeCompositeFragment_errorOpening, iniEditor.getFileName());
							if (isPermissionProblem(e))
							{
								message += PHPDebugUIMessages.PHPExeCompositeFragment_openErrorPermissionMessage;
							}
							openError(PHPDebugUIMessages.PHPExeCompositeFragment_errorTitle, message, e);
						}
					}
				}
			}
		});
		this.fPHPIni.setLabelText(PHPDebugUIMessages.addPHPexeDialog_phpIni
				+ PHPDebugUIMessages.addPHPexeDialog_optional);
		this.fPHPIni.setButtonLabel(PHPDebugUIMessages.addPHPexeDialog_browse1);

		this.fPHPexeName.doFillIntoGrid(parent, 3);
		this.fPHPExePath.doFillIntoGrid(parent, 3);
		((GridData) this.fPHPExePath.getTextControl(parent).getLayoutData()).widthHint = pixelConverter
				.convertWidthInCharsToPixels(50);

		this.fPHPIni.doFillIntoGrid(parent, 3);
		((GridData) this.fPHPIni.getTextControl(parent).getLayoutData()).widthHint = pixelConverter
				.convertWidthInCharsToPixels(50);

		this.fDebuggersLabel = new Label(parent, SWT.LEFT | SWT.WRAP);
		this.fDebuggersLabel.setFont(parent.getFont());
		this.fDebuggersLabel.setText(PHPDebugUIMessages.addPHPexeDialog_phpDebugger);
		GridData data = new GridData();
		data.horizontalSpan = 1;
		this.fDebuggersLabel.setLayoutData(data);

		this.fDebuggers = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		data = new GridData();
		data.horizontalSpan = 1;
		data.grabExcessHorizontalSpace = true;
		this.fDebuggers.setLayoutData(data);

		for (int i = 0; i < this.debuggersIds.size(); ++i)
		{
			String id = this.debuggersIds.get(i);
			String debuggerName = PHPDebuggersRegistry.getDebuggerName(id);
			this.fDebuggers.add(debuggerName, i);
		}
		fDebuggers.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int selectionIndex = fDebuggers.getSelectionIndex();
				String debuggerID = debuggersIds.get(selectionIndex);
				getPHPExeItem().setDebuggerID(debuggerID);
				iniEditor.setDebuggerID(debuggerID);
			}
		});
		TabFolder settings = new TabFolder(parent, SWT.NONE);
		// creating ini editor group
		TabItem iniEditorTab = new TabItem(settings, SWT.NONE);
		iniEditorTab.setText(PHPDebugUIMessages.PHPExeCompositeFragment_iniEditorTabText);
		Composite iniEditorGroup = new Composite(settings, SWT.NONE);
		iniEditorTab.setControl(iniEditorGroup);
		GridData grdata = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		grdata.heightHint = 400;
		settings.setLayoutData(grdata);
		GridLayout iniGroupLayout = new GridLayout();
		iniGroupLayout.marginWidth = 5;
		iniEditorGroup.setLayout(iniGroupLayout);
		// creating ini editor
		iniEditor = new PHPIniEditor();
		Control iniEditorControl = iniEditor.createControl(iniEditorGroup);
		iniEditorControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		parent.layout();

		this.init();
		this.createFieldListeners();
		this.validate();

		Dialog.applyDialogFont(this);
	}

	/**
	 * 
	 */
	protected void createFieldListeners()
	{
		this.fPHPexeName.setDialogFieldListener(new IDialogFieldListener()
		{
			public void dialogFieldChanged(DialogField field)
			{
				PHPExeCompositeFragment.this.validate();
			}
		});

		this.fPHPExePath.setDialogFieldListener(new IDialogFieldListener()
		{
			public void dialogFieldChanged(DialogField field)
			{
				PHPExeCompositeFragment.this.validate();
			}
		});

		this.fPHPIni.setDialogFieldListener(new IDialogFieldListener()
		{
			public void dialogFieldChanged(DialogField field)
			{
				PHPExeCompositeFragment.this.validate();
			}
		});
	}

	protected void init()
	{
		PHPexeItem phpExeItem = getPHPExeItem();
		if ((phpExeItem == null) || (phpExeItem.getName() == null))
		{
			this.fPHPexeName.setText(StringUtil.EMPTY);
			this.fPHPExePath.setText(StringUtil.EMPTY);
			this.fPHPIni.setText(StringUtil.EMPTY);
			String defaultDebuggerId = PHPDebuggersRegistry.getDefaultDebuggerId();
			if (defaultDebuggerId != null)
			{
				int index = this.fDebuggers.indexOf(PHPDebuggersRegistry.getDebuggerName(defaultDebuggerId));
				this.fDebuggers.select(index);
			}
			else
			{
				if (this.fDebuggers.getItemCount() > 0)
				{
					this.fDebuggers.select(0);
				}
				else
				{
					this.hideDebuggersCombo();
				}
			}
			this.setTitle(PHPDebugUIMessages.PHPExeCompositeFragment_addInterpreterTitle);
		}
		else
		{
			this.initialName = phpExeItem.getName();
			this.fPHPexeName.setTextWithoutUpdate(phpExeItem.getName());
			this.fPHPexeName.setEnabled(phpExeItem.isEditable());
			this.fPHPExePath.setTextWithoutUpdate(phpExeItem.getExecutable().getAbsolutePath());
			this.fPHPExePath.setEnabled(phpExeItem.isEditable());
			this.iniEditor.setPHPExe(phpExeItem.getExecutable().getAbsolutePath());
			if (phpExeItem.getINILocation() != null)
			{
				this.fPHPIni.setTextWithoutUpdate(phpExeItem.getINILocation().toString());
			}
			this.fPHPIni.setEnabled(phpExeItem.isEditable());
			if (fPHPIni.getText() != null && !StringUtil.EMPTY.equals(fPHPIni.getText()))
			{
				try
				{
					iniEditor.openFile(fPHPIni.getText());
				}
				catch (IOException e)
				{
					String message = MessageFormat.format(PHPDebugUIMessages.PHPExeCompositeFragment_errorOpening,
							iniEditor.getFileName());
					if (isPermissionProblem(e))
					{
						message += PHPDebugUIMessages.PHPExeCompositeFragment_openErrorPermissionMessage;
					}
					openError(PHPDebugUIMessages.PHPExeCompositeFragment_errorTitle, message, e);
				}
			}

			String debuggerID = phpExeItem.getDebuggerID();
			this.fDebuggers.setEnabled(phpExeItem.isEditable());
			this.fDebuggersLabel.setEnabled(phpExeItem.isEditable());
			int index = this.fDebuggers.indexOf(PHPDebuggersRegistry.getDebuggerName(debuggerID));
			if (index > -1)
			{
				this.fDebuggers.select(index);
			}
			else
			{
				if (this.fDebuggers.getItemCount() > 0)
				{
					this.fDebuggers.select(0);
				}
				else
				{
					this.hideDebuggersCombo();
				}
			}
			iniEditor.setDebuggerID(debuggerID);
			this.setTitle(PHPDebugUIMessages.PHPExeCompositeFragment_editInterpreterTitle);
		}

		this.controlHandler.setTitle(this.getTitle());
		this.validate();
		if (needValidation)
		{
			needValidation = iniEditor.validateExtensions();
		}
	}

	private void hideDebuggersCombo()
	{
		this.fDebuggers.setVisible(false);
		this.fDebuggersLabel.setVisible(false);
	}

	protected void validate()
	{
		PHPexeItem phpExeItem = getPHPExeItem();

		// Check whether we can edit this item
		if (phpExeItem == null)
		{
			this.setMessage(PHPDebugUIMessages.addPHPexeDialog_readOnlyPHPExe, IMessageProvider.INFORMATION);
			return;
		}

		String name = this.fPHPexeName.getText();
		if ((name == null) || (name.trim().length() == 0))
		{
			this.setMessage(PHPDebugUIMessages.addPHPexeDialog_enterName, IMessageProvider.INFORMATION);
			return;
		}

		// Check whether the name already exists:
		if (existingItems != null)
		{
			for (PHPexeItem item : existingItems)
			{
				if (!item.getName().equals(this.initialName) && item.getName().equals(name))
				{
					this.setMessage(PHPDebugUIMessages.addPHPexeDialog_duplicateName, IMessageProvider.ERROR);
					return;
				}
			}
		}

		String locationName = this.fPHPExePath.getText();
		if (locationName.length() == 0)
		{
			this.setMessage(PHPDebugUIMessages.addPHPexeDialog_enterLocation, IMessageProvider.INFORMATION);
			return;
		}

		final File executableLocation = new File(locationName);
		if (!executableLocation.exists())
		{
			this.setMessage(PHPDebugUIMessages.addPHPexeDialog_locationNotExists, IMessageProvider.ERROR);
			return;
		}
		boolean iniOptional = new File(executableLocation.getParentFile(), PHP_INI).exists();
		if (iniOptional)
		{
			this.fPHPIni.setLabelText(PHPDebugUIMessages.addPHPexeDialog_phpIni
					+ PHPDebugUIMessages.addPHPexeDialog_optional); // set the 'optional' label
		}
		else
		{
			this.fPHPIni.setLabelText(PHPDebugUIMessages.addPHPexeDialog_phpIni); // not 'optional'
		}
		String iniLocationName = this.fPHPIni.getText();
		File iniFile = null;
		if (iniLocationName.trim().length() == 0)
		{
			// Check for a php.ini next to the executable
			if (executableLocation.exists())
			{
				iniFile = new File(executableLocation.getParentFile(), PHP_INI);
				iniLocationName = iniFile.getAbsolutePath();
			}
		}
		else
		{
			iniFile = new File(iniLocationName);
		}
		if (iniFile == null || !iniFile.exists())
		{
			this.setMessage(PHPDebugUIMessages.addPHPexeDialog_iniLocationNotExists, IMessageProvider.ERROR);
			if (iniEditor != null)
			{
				iniEditor.disable();
			}
			return;
		}

		phpExeItem.setName(name);
		phpExeItem.setExecutable(executableLocation);
		if (phpExeItem.getExecutable() == null)
		{
			// The executable could not be set.
			this.setMessage(PHPDebugUIMessages.PHPExeCompositeFragment_executableFatalError, IMessageProvider.ERROR);
			return;
		}
		phpExeItem.setDebuggerID(this.debuggersIds.get(this.fDebuggers.getSelectionIndex()));
		phpExeItem.setINILocation(iniFile);
		this.setMessage(this.getDescription(), IMessageProvider.NONE);

		if (iniEditor != null)
		{
			if (iniLocationName.trim() != null && !StringUtil.EMPTY.equals(iniLocationName.trim()))
			{
				if (!iniLocationName.equals(iniEditor.getFileName()))
				{
					// if editor was displaying other file, we should reopen the file
					try
					{
						iniEditor.openFile(iniLocationName);
					}
					catch (IOException e)
					{
						String message = MessageFormat.format(PHPDebugUIMessages.PHPExeCompositeFragment_errorOpening,
								iniLocationName);
						if (isPermissionProblem(e))
						{
							message += PHPDebugUIMessages.PHPExeCompositeFragment_openErrorPermissionMessage;
						}
						openError(PHPDebugUIMessages.PHPExeCompositeFragment_errorTitle, message, e);
					}
				}
			}
			else
			{
				iniEditor.disable();
			}
		}

		this.controlHandler.update();
	}

	protected void setMessage(String message, int type)
	{
		this.controlHandler.setMessage(message, type);
		this.setComplete(type == IMessageProvider.NONE);
		this.controlHandler.update();
	}

	public boolean performOk()
	{
		try
		{
			iniEditor.save();
		}
		catch (IOException e)
		{
			String message = MessageFormat.format(PHPDebugUIMessages.PHPExeCompositeFragment_errorSaving,
					iniEditor.getFileName());
			if (isPermissionProblem(e))
			{
				message += PHPDebugUIMessages.PHPExeCompositeFragment_saveErrorPermissionMessage;
			}
			openError(PHPDebugUIMessages.PHPExeCompositeFragment_saveErrorTitle, message, e);
		}
		return true;
	}

	private void openError(final String title, final String error, final Throwable t)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				PHPDebugEPLPlugin.logError(error, t);
				Display display = Display.getDefault();
				Shell activeShell = display.getActiveShell();
				MessageDialog.openError(activeShell, title, error);
			}
		});
	}

	private boolean isPermissionProblem(Throwable t)
	{
		if (t instanceof FileNotFoundException)
		{
			if (t.getMessage() != null && t.getMessage().toLowerCase().contains("permission")) //$NON-NLS-1$
			{
				return true;
			}
		}
		return false;
	}
}