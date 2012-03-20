/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.launching;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org2.eclipse.php.internal.ui.wizard.field.DialogField;
import org2.eclipse.php.internal.ui.wizard.field.IDialogFieldListener;
import org2.eclipse.php.internal.ui.wizard.field.LayoutUtil;
import org2.eclipse.php.internal.ui.wizard.field.StringDialogField;
import org2.eclipse.php.util.StatusInfo;

import com.aptana.core.util.IOUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.php.debug.core.util.NameValuePair;

/**
 * A dialog input for name-value pairs.
 * 
 * @author Shalom
 */
public class NameValuePairInputDialog extends StatusDialog
{

	private class CompilerTodoTaskInputAdapter implements IDialogFieldListener
	{
		public void dialogFieldChanged(DialogField field)
		{
			doValidation();
		}
	}

	private StringDialogField fNameDialogField;
	private StringDialogField fValueDialogField;

	private List<String> fExistingNames;

	public NameValuePairInputDialog(Shell parent, NameValuePair pair, List<NameValuePair> existingEntries)
	{
		super(parent);
		fExistingNames = new ArrayList<String>(existingEntries.size());
		for (NameValuePair curr : existingEntries)
		{
			if (!curr.equals(pair))
			{
				fExistingNames.add(curr.name);
			}
		}

		if (pair == null)
		{
			setTitle(Messages.NameValuePairInputDialog_addTitle);
		}
		else
		{
			setTitle(Messages.NameValuePairInputDialog_editTitle);
		}

		CompilerTodoTaskInputAdapter adapter = new CompilerTodoTaskInputAdapter();

		fNameDialogField = new StringDialogField();
		fNameDialogField.setLabelText(Messages.NameValuePairInputDialog_nameLabel);
		fNameDialogField.setDialogFieldListener(adapter);

		fValueDialogField = new StringDialogField();
		fValueDialogField.setLabelText(Messages.NameValuePairInputDialog_valueLabel);
		fValueDialogField.setDialogFieldListener(adapter);

		fNameDialogField.setText((pair != null) ? pair.name : StringUtil.EMPTY);
		fValueDialogField.setText((pair != null && pair.value != null) ? pair.value : StringUtil.EMPTY);
	}

	public NameValuePair getResult()
	{
		NameValuePair pair = new NameValuePair();
		pair.name = fNameDialogField.getText();
		pair.value = fValueDialogField.getText();
		return pair;
	}

	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);

		Composite inner = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		inner.setLayout(layout);

		fNameDialogField.doFillIntoGrid(inner, 2);
		fValueDialogField.doFillIntoGrid(inner, 2);

		LayoutUtil.setHorizontalGrabbing(fNameDialogField.getTextControl(null));
		LayoutUtil.setWidthHint(fNameDialogField.getTextControl(null), convertWidthInCharsToPixels(45));
		fNameDialogField.postSetFocusOnDialogField(parent.getDisplay());

		applyDialogFont(composite);
		return composite;
	}

	private void doValidation()
	{
		StatusInfo status = new StatusInfo();
		String newText = fNameDialogField.getText();
		boolean hasErrorOrWarning = false;
		if (newText.length() == 0)
		{
			status.setError(Messages.NameValuePairInputDialog_enterFiledNameStatus);
		}
		else
		{
			if (fExistingNames.contains(newText))
			{
				status.setWarning(Messages.NameValuePairInputDialog_nameInUseWarning);
				hasErrorOrWarning = true;
			}
			else
			{
				try
				{
					String encoded = URLEncoder.encode(newText, IOUtil.UTF_8);
					if (!newText.equals(encoded))
					{
						status.setWarning(MessageFormat.format(Messages.NameValuePairInputDialog_encodingStatusWarning,
								newText, encoded));
						hasErrorOrWarning = true;
					}
				}
				catch (UnsupportedEncodingException e) // $codepro.audit.disable emptyCatchClause
				{
					// ignore
				}
			}
		}
		if (hasErrorOrWarning)
		{
			updateStatus(status);
			return;
		}
		// Check the value field
		newText = fValueDialogField.getText();
		try
		{
			String encoded = URLEncoder.encode(newText, IOUtil.UTF_8);
			if (!newText.equals(encoded))
			{
				status.setWarning(MessageFormat.format(Messages.NameValuePairInputDialog_decodingStatusWarning,
						newText, encoded));
			}
		}
		catch (UnsupportedEncodingException e) // $codepro.audit.disable emptyCatchClause
		{
			// ignore
		}
		updateStatus(status);
	}

	protected void setShellStyle(int newShellStyle)
	{
		super.setShellStyle(newShellStyle | SWT.RESIZE);
	}
}
