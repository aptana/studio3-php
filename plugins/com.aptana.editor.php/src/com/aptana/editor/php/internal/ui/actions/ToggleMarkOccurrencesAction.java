package com.aptana.editor.php.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.php.internal.ui.preferences.PreferenceConstants;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;

/**
 * Mark Occurrences toggle action the Aptana PHP editor.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class ToggleMarkOccurrencesAction extends TextEditorAction implements IPropertyChangeListener
{

	private IPreferenceStore fStore;

	/**
	 * Constructs and updates the action.
	 */
	public ToggleMarkOccurrencesAction(ResourceBundle resourceBundle)
	{
		super(resourceBundle, "ToggleMarkOccurrencesAction.", null, IAction.AS_CHECK_BOX); //$NON-NLS-1$
		update();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run()
	{
		fStore.setValue(PreferenceConstants.EDITOR_MARK_OCCURRENCES, isChecked());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.TextEditorAction#update()
	 */
	public void update()
	{
		ITextEditor editor = getTextEditor();
		boolean shouldMarkOccurrences = false;
		if (editor instanceof PHPSourceEditor)
			shouldMarkOccurrences = ((PHPSourceEditor) editor).isMarkingOccurrences();

		setChecked(shouldMarkOccurrences);
		setEnabled(editor != null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.TextEditorAction#setEditor(org.eclipse.ui.texteditor.ITextEditor)
	 */
	public void setEditor(ITextEditor editor)
	{
		super.setEditor(editor);
		if (editor != null)
		{
			if (fStore == null)
			{
				fStore = PHPEditorPlugin.getDefault().getPreferenceStore();
				fStore.addPropertyChangeListener(this);
			}
		}
		else if (fStore != null)
		{
			fStore.removePropertyChangeListener(this);
			fStore = null;
		}
		update();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getProperty().equals(PreferenceConstants.EDITOR_MARK_OCCURRENCES))
			setChecked(Boolean.valueOf(event.getNewValue().toString()).booleanValue());
	}
}
