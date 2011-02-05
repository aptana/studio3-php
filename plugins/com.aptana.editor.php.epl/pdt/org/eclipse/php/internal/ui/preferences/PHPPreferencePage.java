/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org.eclipse.php.internal.ui.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.preferences.CommonEditorPreferencePage;
import com.aptana.editor.php.epl.PHPEplPlugin;

/**
 * The page for setting the editor options for occurrences marking.
 */
@SuppressWarnings("restriction")
public final class PHPPreferencePage extends CommonEditorPreferencePage
{

	/**
	 * PHPPreferencePage
	 */
	public PHPPreferencePage()
	{
		super();
		setDescription("Preferences for the Aptana PHP Editor");
		setPreferenceStore(PHPEplPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void createMarkOccurrenceOptions(Composite parent)
	{
		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_TYPE_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markTypeOccurrences"), parent));

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_METHOD_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markMethodOccurrences"), parent));

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_FUNCTION_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markFunctionOccurrences"), parent));

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_CONSTANT_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markConstantOccurrences"), parent));

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_GLOBAL_VARIABLE_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markGlobalVariableOccurrences"), parent));

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_LOCAL_VARIABLE_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markLocalVariableOccurrences"), parent));

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_METHOD_EXIT_POINTS,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markMethodExitPoints"), parent));

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_IMPLEMENTORS,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markImplementors"), parent));

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_BREAK_CONTINUE_TARGETS,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markBreakContinueTargets"), parent));

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_STICKY_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_stickyOccurrences"), parent));

	}

	@Override
	protected IPreferenceStore getChainedEditorPreferenceStore()
	{
		return new ChainedPreferenceStore(new IPreferenceStore[] { PHPEplPlugin.getDefault().getPreferenceStore(),
				CommonEditorPlugin.getDefault().getPreferenceStore(), EditorsPlugin.getDefault().getPreferenceStore() });
	}

	@Override
	protected IEclipsePreferences getPluginPreferenceStore()
	{
		return new InstanceScope().getNode(PHPEplPlugin.PLUGIN_ID);
	}

}
