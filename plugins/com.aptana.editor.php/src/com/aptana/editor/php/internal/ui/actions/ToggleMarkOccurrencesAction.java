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
 * with certain other free and open source software ("FOSS") code and certain additional terms
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
package com.aptana.editor.php.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.php.internal.ui.preferences.PreferenceConstants;
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
		prefStore.setValue(com.aptana.editor.common.preferences.IPreferenceConstants.EDITOR_MARK_OCCURRENCES, isChecked());
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
		if (event.getProperty().equals(com.aptana.editor.common.preferences.IPreferenceConstants.EDITOR_MARK_OCCURRENCES))
		{
			setChecked(Boolean.valueOf(event.getNewValue().toString()).booleanValue());
		}
	}
}
