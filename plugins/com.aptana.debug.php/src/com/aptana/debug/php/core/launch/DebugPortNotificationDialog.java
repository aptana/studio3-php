/**
 * 
 */
package com.aptana.debug.php.core.launch;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.aptana.debug.php.PHPDebugPlugin;
import com.aptana.debug.php.core.IPHPDebugCorePreferenceKeys;

/**
 * The port notification dialog is designed to appear in case the debug session is initialized and the debug-client port
 * is not set to its default value. The dialog provides a notification for that state and allows a continue or stop
 * actions, as well as remembering the decision.
 * 
 * @author Shalom Gibly
 * @since Aptana PHP 1.1
 */
public class DebugPortNotificationDialog extends MessageDialogWithToggle
{

	private final IStatus detailedInformation;

	/**
	 * Creates a message dialog with a toggle. See the superclass constructor for info on the other parameters.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param dialogTitle
	 *            the dialog title, or <code>null</code> if none
	 * @param image
	 *            the dialog title image, or <code>null</code> if none
	 * @param message
	 *            the dialog message
	 * @param detailedInformation
	 *            the details for this message (as an IStatus instance)
	 * @param dialogImageType
	 *            one of the following values:
	 *            <ul>
	 *            <li><code>MessageDialog.NONE</code> for a dialog with no image</li>
	 *            <li><code>MessageDialog.ERROR</code> for a dialog with an error image</li>
	 *            <li><code>MessageDialog.INFORMATION</code> for a dialog with an information image</li>
	 *            <li><code>MessageDialog.QUESTION </code> for a dialog with a question image</li>
	 *            <li><code>MessageDialog.WARNING</code> for a dialog with a warning image</li>
	 *            </ul>
	 * @param toggleMessage
	 *            the message for the toggle control, or <code>null</code> for the default message
	 * @param toggleState
	 *            the initial state for the toggle
	 */
	public DebugPortNotificationDialog(Shell parentShell, String dialogTitle, Image image, String message,
			IStatus detailedInformation, int dialogImageType, String toggleMessage, boolean toggleState)
	{
		super(parentShell, dialogTitle, image, message, dialogImageType, new String[] { IDialogConstants.PROCEED_LABEL,
				IDialogConstants.ABORT_LABEL }, 0, toggleMessage, toggleState);
		this.detailedInformation = detailedInformation;
		setPrefStore(PHPDebugPlugin.getDefault().getPreferenceStore());
		setPrefKey(IPHPDebugCorePreferenceKeys.NOTIFY_NON_STANDARD_PORT);
	}

	/**
	 * Override the toggle creation to create the information area that will hold the status messages.
	 * 
	 * @see org.eclipse.jface.dialogs.MessageDialogWithToggle#createToggleButton(org.eclipse.swt.widgets.Composite)
	 */
	protected Button createToggleButton(Composite parent)
	{
		createInformationArea(parent);
		Label buffer = new Label(parent, SWT.NONE);
		GridData data = new GridData(SWT.NONE);
		data.horizontalSpan = 2;
		buffer.setLayoutData(data);
		return super.createToggleButton(parent);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId)
	{
		super.buttonPressed(buttonId);
		if (buttonId != IDialogConstants.CANCEL_ID && getToggleState() && getPrefStore() != null
				&& getPrefKey() != null)
		{

			getPrefStore().setValue(getPrefKey(), NEVER);
		}
	}

	/**
	 * Create the area for extra error support information.
	 * 
	 * @param parent
	 */
	protected void createInformationArea(Composite parent)
	{
		if (detailedInformation != null)
		{
			Group supportArea = new Group(parent, SWT.NONE);
			supportArea.setText("Details");
			GridData supportData = new GridData(SWT.FILL, SWT.FILL, true, true);
			supportArea.setLayoutData(supportData);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			supportArea.setLayout(layout);
			IStatus[] status = null;
			if (detailedInformation.isMultiStatus())
			{
				status = detailedInformation.getChildren();
			}
			else
			{
				status = new IStatus[] { detailedInformation };
			}
			for (IStatus s : status)
			{
				Image image = null;
				switch (s.getSeverity())
				{
					case IStatus.ERROR:
						image = UnifiedEditorsPlugin.getImage("icons/error.png"); //$NON-NLS-1$
						break;
					case IStatus.INFO:
						image = UnifiedEditorsPlugin.getImage("icons/information.png"); //$NON-NLS-1$
						break;
					case IStatus.WARNING:
						image = UnifiedEditorsPlugin.getImage("icons/warning.png"); //$NON-NLS-1$
						break;
				}
				if (image != null) // we have a valid status
				{
					CLabel label = new CLabel(supportArea, SWT.WRAP);
					label.setText(s.getMessage());
					label.setImage(image);
				}
			}
		}
	}
}
