/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;

/**
 * Mark Occurrences toggle action the Aptana PHP editor.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class ToggleMarkOccurrencesAction extends TextEditorAction implements IPropertyChangeListener
{

	private IPreferenceStore prefStore;

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
		prefStore.setValue(com.aptana.editor.common.preferences.IPreferenceConstants.EDITOR_MARK_OCCURRENCES,
				isChecked());
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
		{
			shouldMarkOccurrences = ((PHPSourceEditor) editor).isMarkingOccurrences();
		}

		setEnabled(editor != null);
		setChecked(shouldMarkOccurrences);
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
			if (prefStore == null)
			{
				prefStore = PHPEplPlugin.getDefault().getPreferenceStore();
				prefStore.addPropertyChangeListener(this);
			}
		}
		else if (prefStore != null)
		{
			prefStore.removePropertyChangeListener(this);
			prefStore = null;
		}
		update();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getProperty().equals(
				com.aptana.editor.common.preferences.IPreferenceConstants.EDITOR_MARK_OCCURRENCES))
		{
			setChecked(Boolean.valueOf(event.getNewValue().toString()).booleanValue());
		}
	}
}
