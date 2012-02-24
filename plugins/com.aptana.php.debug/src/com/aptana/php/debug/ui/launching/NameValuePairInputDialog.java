/**
 * 
 */
package com.aptana.php.debug.ui.launching;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.php.internal.ui.wizard.field.StringDialogField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org2.eclipse.php.internal.ui.wizard.field.DialogField;
import org2.eclipse.php.internal.ui.wizard.field.IDialogFieldListener;
import org2.eclipse.php.internal.ui.wizard.field.LayoutUtil;
import org2.eclipse.php.util.StatusInfo;

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

	public NameValuePairInputDialog(Shell parent, NameValuePair pair, List existingEntries)
	{
		super(parent);
		fExistingNames = new ArrayList<String>(existingEntries.size());
		for (int i = 0; i < existingEntries.size(); i++)
		{
			NameValuePair curr = (NameValuePair) existingEntries.get(i);
			if (!curr.equals(pair))
			{
				fExistingNames.add(curr.name);
			}
		}

		if (pair == null)
		{
			setTitle("Add");
		}
		else
		{
			setTitle("Edit");
		}

		CompilerTodoTaskInputAdapter adapter = new CompilerTodoTaskInputAdapter();

		fNameDialogField = new StringDialogField();
		fNameDialogField.setLabelText("Name");
		fNameDialogField.setDialogFieldListener(adapter);

		fValueDialogField = new StringDialogField();
		fValueDialogField.setLabelText("Value");
		fValueDialogField.setDialogFieldListener(adapter);

		fNameDialogField.setText((pair != null) ? pair.name : ""); //$NON-NLS-1$
		fValueDialogField.setText((pair != null && pair.value != null) ? pair.value : ""); //$NON-NLS-1$
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
			status.setError("Please enter the field name");
		}
		else
		{
			if (fExistingNames.contains(newText))
			{
				status.setWarning("The given name already exists in the parameters list");
				hasErrorOrWarning = true;
			}
			else
			{
				try
				{
					String encoded = URLEncoder.encode(newText, "UTF-8");
					if (!newText.equals(encoded))
					{
						status.setWarning("The given string '" + newText + "' will be encoded to '" + encoded
								+ "' during the session");
						hasErrorOrWarning = true;
					}
				}
				catch (UnsupportedEncodingException e)
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
			String encoded = URLEncoder.encode(newText, "UTF-8");
			if (!newText.equals(encoded))
			{
				status.setWarning("The string '" + newText + "' will be encoded to '" + encoded
						+ "' during the session");
			}
		}
		catch (UnsupportedEncodingException e)
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
