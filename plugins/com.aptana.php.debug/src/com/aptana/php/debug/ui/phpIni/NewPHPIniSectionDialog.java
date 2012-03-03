/** 
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain Eclipse Public Licensed code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.phpIni;

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

/**
 * New PHP ini file entry dialog.
 * 
 * @author Denis Denisenko
 */
public class NewPHPIniSectionDialog extends TitleAreaDialog
{
	/**
	 * Name edit.
	 */
	private Text nameEdit;

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Forbidden names.
	 */
	private List<String> forbiddenNames = null;

	/**
	 * NewsectionDialog constructor.
	 * 
	 * @param parentShell
	 *            - parent shell.
	 */
	public NewPHPIniSectionDialog(Shell parentShell)
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

		this.getShell().setText(Messages.NewPHPIniSectionDialog_0);
		this.setTitle(Messages.NewPHPIniSectionDialog_1);
		this.setMessage(Messages.NewPHPIniSectionDialog_2);

		par.setLayout(new GridLayout(4, false));

		createNameEdit(par);

		setTooltipText();

		return par;
	}

	/**
	 * Create section name edit.
	 * 
	 * @param par
	 *            - parent.
	 */
	private void createNameEdit(Composite par)
	{
		Label nameLabel = new Label(par, SWT.NONE);
		nameLabel.setText(Messages.NewPHPIniSectionDialog_3 + ":"); //$NON-NLS-2$
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
		nameEdit.setToolTipText(Messages.NewPHPIniSectionDialog_5);
	}

	/**
	 * Validates values.
	 */
	private void validate()
	{
		String nameText = nameEdit.getText();
		if (nameText.length() == 0)
		{
			disableOKButton();
			setErrorMessage(Messages.NewPHPIniSectionDialog_6);
			return;
		}
		String trimmedName = nameText.trim();
		if (forbiddenNames != null)

		{
			for (String forbiddenName : forbiddenNames)
			{
				if (forbiddenName.equalsIgnoreCase(trimmedName))
				{
					disableOKButton();
					setErrorMessage(Messages.NewPHPIniSectionDialog_7);
					return;
				}
			}
		}

		setErrorMessage(null);
		Button button = getButton(IDialogConstants.OK_ID);
		if (button != null)
		{
			button.setEnabled(true);
		}
	}

	/**
	 * Disables OK button.
	 */
	private void disableOKButton()
	{
		Button button = getButton(IDialogConstants.OK_ID);
		if (button != null)
		{
			button.setEnabled(false);
		}
	}
}
