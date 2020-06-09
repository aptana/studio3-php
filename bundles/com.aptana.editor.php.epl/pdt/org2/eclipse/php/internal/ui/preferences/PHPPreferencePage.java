/*******************************************************************************
 * Copyright (c) 2009-2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *     Appcelerator Inc.
 *******************************************************************************/
package org2.eclipse.php.internal.ui.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import org2.eclipse.php.internal.core.IPHPEplCoreConstants;
import org2.eclipse.php.internal.ui.PHPUIMessages;

import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.preferences.CommonEditorPreferencePage;
import com.aptana.editor.common.preferences.IPreferenceConstants;
import com.aptana.editor.common.preferences.Messages;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.ui.preferences.AptanaPreferencePage;

/**
 * The page for setting the editor options for occurrences marking.
 */
@SuppressWarnings("restriction")
public final class PHPPreferencePage extends CommonEditorPreferencePage
{

	private Composite advancedOptions;
	private BooleanFieldEditor markOccurences;
	private Composite appearanceComposite;
	private Composite foldingGroup;
	private BooleanFieldEditor foldTypes;
	private BooleanFieldEditor foldFunctions;
	private BooleanFieldEditor foldPHPDoc;
	private BooleanFieldEditor foldComments;

	/**
	 * PHPPreferencePage
	 */
	public PHPPreferencePage()
	{
		super();
		setDescription(PHPUIMessages.getString("PHPPreferencePage.Title")); //$NON-NLS-1$
		setPreferenceStore(PHPEplPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Listens for changes in showing/hiding advanced options
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getSource() == markOccurences && advancedOptions != null)
		{
			if (!(Boolean) event.getNewValue())
			{
				toggleAdvancedOccurrenceSection(false);
			}
			else
			{
				toggleAdvancedOccurrenceSection(true);
			}
		}
		else if (event.getSource() == enableFolding)
		{
			Object newValue = event.getNewValue();
			boolean enableFoldingOptions = false;
			if (Boolean.TRUE.equals(newValue))
			{
				enableFoldingOptions = true;
			}
			foldTypes.setEnabled(enableFoldingOptions, foldingGroup);
			foldFunctions.setEnabled(enableFoldingOptions, foldingGroup);
			foldComments.setEnabled(enableFoldingOptions, foldingGroup);
			foldPHPDoc.setEnabled(enableFoldingOptions, foldingGroup);
		}
		super.propertyChange(event);
	}

	protected void initialize()
	{
		super.initialize();

		if (advancedOptions != null)
		{
			boolean markOccurrences = getPreferenceStore().getBoolean(IPreferenceConstants.EDITOR_MARK_OCCURRENCES);
			toggleAdvancedOccurrenceSection(markOccurrences);
		}
	}

	private void toggleAdvancedOccurrenceSection(boolean show)
	{
		advancedOptions.setVisible(show);
		if (advancedOptions.getLayoutData() != null)
		{
			((GridData) advancedOptions.getLayoutData()).exclude = !show;
		}
		appearanceComposite.layout(true, true);
	}

	@Override
	protected void createMarkOccurrenceOptions(Composite parent)
	{
		appearanceComposite = parent;

		Composite group = AptanaPreferencePage.createGroup(parent,
				PHPUIMessages.getString("PHPPreferencePage.Mark_Occurrences_Heading")); //$NON-NLS-1$

		markOccurences = new BooleanFieldEditor(IPreferenceConstants.EDITOR_MARK_OCCURRENCES,
				Messages.EditorsPreferencePage_MarkOccurrences, group);
		addField(markOccurences);

		advancedOptions = new Composite(group, SWT.NONE);
		advancedOptions.setLayout(GridLayoutFactory.fillDefaults().create());
		advancedOptions.setLayoutData(GridDataFactory.fillDefaults().indent(18, 0).create());

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_TYPE_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markTypeOccurrences"), advancedOptions)); //$NON-NLS-1$

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_METHOD_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markMethodOccurrences"), advancedOptions)); //$NON-NLS-1$

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_FUNCTION_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markFunctionOccurrences"), advancedOptions)); //$NON-NLS-1$

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_CONSTANT_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markConstantOccurrences"), advancedOptions)); //$NON-NLS-1$

		addField(new BooleanFieldEditor(
				PreferenceConstants.EDITOR_MARK_GLOBAL_VARIABLE_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markGlobalVariableOccurrences"), advancedOptions)); //$NON-NLS-1$

		addField(new BooleanFieldEditor(
				PreferenceConstants.EDITOR_MARK_LOCAL_VARIABLE_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markLocalVariableOccurrences"), advancedOptions)); //$NON-NLS-1$

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_METHOD_EXIT_POINTS,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markMethodExitPoints"), advancedOptions)); //$NON-NLS-1$

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_IMPLEMENTORS,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markImplementors"), advancedOptions)); //$NON-NLS-1$

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MARK_BREAK_CONTINUE_TARGETS,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_markBreakContinueTargets"), advancedOptions)); //$NON-NLS-1$

		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_STICKY_OCCURRENCES,
				PHPUIMessages.getString("MarkOccurrencesConfigurationBlock_stickyOccurrences"), advancedOptions)); //$NON-NLS-1$

		// Link to general text annotation prefs from Eclipse
		Link link = new Link(group, SWT.NONE);
		link.setText(Messages.CommonEditorPreferencePage_Default_Editor_Preference_Link);
		link.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(), e.text, null, null);
			}
		});
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
		return InstanceScope.INSTANCE.getNode(PHPEplPlugin.PLUGIN_ID);
	}

	@Override
	protected Composite createFoldingOptions(Composite parent)
	{
		this.foldingGroup = super.createFoldingOptions(parent);

		// Initially fold these elements:
		Label initialFoldLabel = new Label(foldingGroup, SWT.WRAP);
		initialFoldLabel.setText(PHPUIMessages.getString("PHPPreferencePage_initial_fold_options_label")); //$NON-NLS-1$

		// Types
		foldTypes = new BooleanFieldEditor(PreferenceConstants.EDITOR_FOLDING_CLASSES,
				PHPUIMessages.getString("PHPPreferencePage_fold_types_label"), foldingGroup); //$NON-NLS-1$
		addField(foldTypes);

		// Functions
		foldFunctions = new BooleanFieldEditor(PreferenceConstants.EDITOR_FOLDING_FUNCTIONS,
				PHPUIMessages.getString("PHPPreferencePage_fold_functions_label"), foldingGroup); //$NON-NLS-1$
		addField(foldFunctions);

		// PHPDoc
		foldPHPDoc = new BooleanFieldEditor(PreferenceConstants.EDITOR_FOLDING_PHPDOC,
				PHPUIMessages.getString("PHPPreferencePage_fold_phpdoc_label"), foldingGroup); //$NON-NLS-1$
		addField(foldPHPDoc);

		// Comments
		foldComments = new BooleanFieldEditor(PreferenceConstants.EDITOR_FOLDING_COMMENTS,
				PHPUIMessages.getString("PHPPreferencePage_fold_comments_label"), foldingGroup); //$NON-NLS-1$
		addField(foldComments);

		return foldingGroup;
	}

	@Override
	protected Composite createContentAssistOptions(Composite parent)
	{
		IPreferenceStore s = getChainedEditorPreferenceStore();
		if (s.contains(IPHPEplCoreConstants.STRICT_NS_CODE_ASSIST))
		{
			Label stringNsLabel = new Label(parent, SWT.WRAP);
			stringNsLabel.setText(PHPUIMessages.getString("PHPPreferencePage.strictNsInfo")); //$NON-NLS-1$
			addField(new BooleanFieldEditor(IPHPEplCoreConstants.STRICT_NS_CODE_ASSIST,
					PHPUIMessages.getString("PHPPreferencePage.strictNSContentAssist"), parent)); //$NON-NLS-1$
		}
		return parent;
	}
}
