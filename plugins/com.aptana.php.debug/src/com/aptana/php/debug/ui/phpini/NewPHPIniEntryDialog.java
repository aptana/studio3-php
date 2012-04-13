/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.phpini;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.aptana.core.util.StringUtil;

/**
 * New PHP ini file entry dialog.
 * 
 * @author Denis Denisenko
 */
public class NewPHPIniEntryDialog extends TitleAreaDialog
{
	/**
	 * Name edit.
	 */
	private Text nameEdit;

	/**
	 * Content edit.
	 */
	private Text contentEdit;

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Content.
	 */
	private String value;

	/**
	 * Forbidden names.
	 */
	private List<String> forbiddenNames = null;

	/**
	 * NewentryDialog constructor.
	 * 
	 * @param parentShell
	 *            - parent shell.
	 */
	public NewPHPIniEntryDialog(Shell parentShell)
	{
		super(parentShell);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		super.createButtonsForButtonBar(parent);
		// validating
		validate();
	}

	/**
	 * Gets name.
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets value.
	 * 
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Sets forbidden names.
	 * 
	 * @param names
	 *            - forbidden names list.
	 */
	public void setForbiddenNames(List<String> names)
	{
		this.forbiddenNames = names;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void okPressed()
	{
		if (nameEdit != null)
		{
			name = nameEdit.getText();
		}
		if (contentEdit != null)
		{
			value = contentEdit.getText();
		}
		super.okPressed();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite returned = (Composite) super.createDialogArea(parent);

		Composite par = new Composite(returned, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 200;
		par.setLayoutData(data);

		// setting titles

		this.getShell().setText(Messages.NewPHPIniEntryDialog_createEntry);
		this.setTitle(Messages.NewPHPIniEntryDialog_createEntry);
		this.setMessage(Messages.NewPHPIniEntryDialog_inputNameValue);

		par.setLayout(new GridLayout(4, false));

		createNameEdit(par);

		createContentEdit(par);

		setTooltipText();

		return par;
	}

	/**
	 * Creates entry content edit.
	 * 
	 * @param par
	 *            - parent.
	 */
	private void createContentEdit(Composite par)
	{
		Label contentLabel = new Label(par, SWT.NONE);
		contentLabel.setText(StringUtil.makeFormLabel(Messages.NewPHPIniEntryDialog_value));
		contentLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		contentEdit = new Text(par, SWT.BORDER);
		contentEdit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (value != null)
		{
			contentEdit.setText(value);
		}
		contentEdit.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				validate();
			}
		});
	}

	/**
	 * Create entry name edit.
	 * 
	 * @param par
	 *            - parent.
	 */
	private void createNameEdit(Composite par)
	{
		Label nameLabel = new Label(par, SWT.NONE);
		nameLabel.setText(StringUtil.makeFormLabel(Messages.NewPHPIniEntryDialog_name));
		nameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		nameEdit = new Text(par, SWT.BORDER);
		nameEdit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		nameEdit.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				validate();
			}
		});
		if (name != null)
		{
			nameEdit.setText(name);
		}
	}

	/**
	 * Sets tooltip text.
	 */
	protected void setTooltipText()
	{
		contentEdit.setToolTipText(Messages.NewPHPIniEntryDialog_enterEntryValueTooltip);
	}

	/**
	 * Validates values.
	 */
	private void validate()
	{
		String contentText = contentEdit.getText();

		String nameText = nameEdit.getText();
		if (nameText.length() == 0)
		{
			setOKButtonEnabled(false);
			setErrorMessage(Messages.NewPHPIniEntryDialog_entryNameMissingError);
			return;
		}
		String trimmedName = nameText.trim();
		if (forbiddenNames != null && forbiddenNames.contains(trimmedName))
		{
			setOKButtonEnabled(false);
			setErrorMessage(Messages.NewPHPIniEntryDialog_entryNameDuplicateError);
			return;
		}

		if (contentText.length() == 0)
		{
			setErrorMessage(Messages.NewPHPIniEntryDialog_missingEntryValueError);
			setOKButtonEnabled(false);
			return;
		}

		setErrorMessage(null);
		setOKButtonEnabled(true);
	}

	/**
	 * Disables or enables the OK button.
	 */
	private void setOKButtonEnabled(boolean enabled)
	{
		Button button = getButton(IDialogConstants.OK_ID);
		if (button != null)
		{
			button.setEnabled(enabled);
		}
	}
}
